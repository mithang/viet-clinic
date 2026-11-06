package com.geeke.wx.service;

import com.alibaba.fastjson.JSONObject;
import com.geeke.admin.dao.UserDao;
import com.geeke.admin.entity.User;
import com.geeke.admin.service.UserService;
import com.geeke.sys.service.LoginService;
import com.geeke.utils.IdGen;
import com.geeke.utils.JwtUtils;
import com.geeke.wx.dao.WxUserDao;
import com.geeke.wx.entity.WxUser;
import com.geeke.common.service.CrudService;
import com.geeke.utils.WechatUtil;
import org.apache.shiro.crypto.hash.Md5Hash;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import java.util.Objects;

@Service
@Transactional(readOnly = true)
public class WxUserService extends CrudService<WxUserDao, WxUser> {

    @Autowired
    private WxUserDao wxUserDao;

    @Autowired
    private UserService userService;

    @Autowired
    private LoginService loginService;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private WechatUtil wechatUtil;

    @Transactional(readOnly = false)
    public WxUser getCode(WxUser wxUser) {
       // WxUser wxUser = new WxUser();
        // LấycodeDữ liệu
        JSONObject object = wechatUtil.getOpenId(wxUser.getCode());
        // DS84: getOpenId tra null khi goi WeChat loi, hoac JSON loi
        // khong co openid (code gia/het han, chua cau hinh app-id);
        // deref thang se NPE 500 -> tra null, controller chuyen
        // thanh 400. Khong cham DB khi chua co openid.
        if (object == null || object.get("openid") == null
                || object.get("session_key") == null) {
            return null;
        }
        // jsonDữ liệuChuyển đổi thành chuỗi
        String openid = object.get("openid").toString();
        String sessionkey = object.get("session_key").toString();
        // Khi tài khoản chính liên kết với mini-program thì có thể lấy được, Chưaliên kếtchỉ địnhkhôngphương phápLấy
        String unionId="";
        if(!ObjectUtils.isEmpty(object.get("unionid"))){
            unionId=object.get("unionid").toString();
        }
        wxUser.setOpenId(openid);
        wxUser.setSessionKey(sessionkey);
        wxUser.setUnionId(unionId);

        //LấyCó tồn tại khôngđóWeChatNgười dùng
        WxUser user = wxUserDao.getByOpenId(openid);
        if(!ObjectUtils.isEmpty(user)&&!ObjectUtils.isEmpty(user.getPhone())){

            return user;
        }

        //LưuNgười dùng của openId
        String uuid = IdGen.uuid();
        wxUser.setId(uuid);
        wxUserDao.save(wxUser);
        // Trả vềTham số
        return wxUser;
    }

    @Transactional(readOnly = false)
    public WxUser getPhone(WxUser wxUser) {
        WxUser dto = new WxUser();
        JSONObject token = wechatUtil.getToken();
        // DS84: getToken/getPhoneNumber tra null khi goi WeChat loi
        // (mat mang / cau hinh thieu); deref thang se NPE 500.
        // Token loi (khong co access_token) cung la that bai, khong
        // phai thanh cong voi data null mo ho -> tra null, controller
        // chuyen thanh 400.
        if (token == null || ObjectUtils.isEmpty(token.get("access_token"))) {
            return null;
        }
        JSONObject phone = wechatUtil.getPhoneNumber(wxUser.getCode(), token.get("access_token").toString());
        if (phone == null || phone.get("errcode") == null
                || !Objects.equals(phone.get("errcode").toString(), "0")
                || phone.get("phone_info") == null) {
            return null;
        }
        JSONObject jsonObject = JSONObject.parseObject(phone.get("phone_info").toString());
        if (jsonObject == null || jsonObject.get("phoneNumber") == null) {
            return null;
        }
        String phoneNumber = jsonObject.get("phoneNumber").toString();
        //QuaopenIdLấyCó hay khôngcóđóNgười dùngThông tin
        WxUser byOpenId = wxUserDao.getByOpenId(wxUser.getOpenId());
        String tokens = "";
        if (!ObjectUtils.isEmpty(byOpenId)) {
            logger.debug("WxUser found: {}", byOpenId);
            wxUserDao.update(phoneNumber, wxUser.getOpenId());

            //Lưuxongsautiến hành dòngsauđầuLưuWeChatBệnh nhân

            //TheoPhòng khámid và openidLấyCó hay khôngcóđóNgười dùng
            User user1 = userService.getCompanyIdAndOpenId(byOpenId.getCompany().getId(), wxUser.getOpenId());
            User user = new User();
            if (!ObjectUtils.isEmpty(user1)) {
                // Người dùngĐã tồn tại, Cập nhật
                user.setId(user1.getId());
                userService.updateWxUser(user);
            } else {
                // Người dùngKhông tồn tại, mớixây dựng
                user.setId(IdGen.uuid());
                user.setCompany(byOpenId.getCompany());
                user.setIsWxuser("1");
                user.setIsLocked(0);
                user.setLoginName(wxUser.getOpenId());
                int length = wxUser.getOpenId().length();
                String substring = wxUser.getOpenId().substring(length - 6);
                logger.debug("WxUser password suffix: {}", substring);
                Md5Hash md5 = new Md5Hash(substring, user.getId(), 10000);
                user.setLoginPassword(md5.toHex());
                user.setName(byOpenId.getOpenId());
                user.setPhone(phoneNumber);
                userService.saveWxUser(user);
            }
        }
        dto.setPhone(phoneNumber);
        dto.setToken(tokens);
        return dto;
    }

    @Transactional(readOnly = false)
    public WxUser initLogin(WxUser wxUser) {
        JSONObject object = wechatUtil.getOpenId(wxUser.getCode());
        // DS84: nhu getCode — tra loi WeChat loi / khong co openid
        // -> tra null thay vi NPE 500; controller chuyen thanh 400.
        if (object == null || object.get("openid") == null) {
            return null;
        }
        // jsonDữ liệuChuyển đổi thành chuỗi
        String openid = object.get("openid").toString();
        WxUser user = wxUserDao.getByOpenId(openid);
        if(ObjectUtils.isEmpty(user)){
            wxUser.setOpenId(openid);
            String uuid = IdGen.uuid();
            wxUser.setId(uuid);
            wxUserDao.save(wxUser);
            return wxUser;
        }

        return user;
    }
}
