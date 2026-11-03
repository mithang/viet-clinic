package com.geeke.admin.dao;

import com.geeke.admin.entity.User;
import com.geeke.common.persistence.CrudDao;
import com.geeke.toll.entity.OutpatientLog;
import com.geeke.utils.PageRequestClinic;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * Quản lý người dùngDAOGiao diện
 * @author lys
 * @version 2021-08-25
 */
@Mapper
public interface UserDao extends CrudDao<User> {
    /**
     * Sửa Mật khẩu
     */
    public int updateLoginPassword(@Param("id")String id, @Param("pass")String pass);

    /** Lấy người dùng cần giám sát trạng thái đăng nhập */
    List<User> loginUser();

    List<User> getListByLoginName(@Param("loginName") String loginName);

    void updateDelFlag(@Param("id") String id, @Param("delFalg") String delFalg);

    User getAdmin(String companyId);

    User getByPhone(String phone);

    List<User> listPageForNoAdmin(PageRequestClinic var1);

    List<User> getUserByCompanyIdAndJob(@Param("companyId")String companyId,@Param("jobType") String jobType);

    List<User> getUserByCompanyIdAndJobV2(@Param("companyId")String companyId,@Param("jobType") String jobType);

    int countForNoAdmin(PageRequestClinic var1);

    @Select("select tenant_id from user_tenant where user_id = #{userId} and del_flag != '1' ")
    List<String> getClinicId(@Param("userId") String userId);

    @Select("select tenant_id from user_tenant where login_name = #{phone} and del_flag != '1' ")
    List<String> getClinicIdByPhone(@Param("phone") String phone);

    @Select("select count(1) from user_tenant where login_name = #{phone} and del_flag != '1' ")
    int countClinicIdByPhone(@Param("phone") String phone);

    // H2 (iteration 28): dem theo user_id, mien nhiem voi
    // user_tenant.login_name cu sau doi SDT (xem H3) — countClinicIdByPhone
    // dem theo login_name nen sai khi ten do da loi thoi.
    @Select("select count(1) from user_tenant where user_id = #{userId} and del_flag != '1' ")
    int countActiveTenantsByUserId(@Param("userId") String userId);

    @Select("select user_id from user_tenant where tenant_id = #{tenantId}")
    List<String> getUserIdByClinicId(@Param("tenantId") String tenantId);

    User getByLoginName(@Param("loginName") String loginName);

    void insertUserTenant(@Param("id") String id, @Param("userId") String userId,
                          @Param("tenantId") String tenantId,@Param("loginName") String loginName);

    // H2 (iteration 28): chi danh co tenant chi dinh. tenantId rong/null
    // thi giu hanh vi cu (danh co moi tenant) cho session khong co tenant.
    void updateUserTenantDelFlag(@Param("userId") String userId, @Param("delFlag") String delFlag,
                                 @Param("tenantId") String tenantId);

    void updateAdminPhoneAndPass(@Param("loginName") String loginName, @Param("pass") String pass, @Param("id") String id);

    // H3 (iteration 28): dong bo user_tenant.login_name khi sys_user
    // doi SDT/loginName. Chi dong active (del_flag != '1'); dong tenant
    // da xoa mem giu ten cu nhu lich su, khi gan lai se insert dong moi.
    void updateUserTenantLoginName(@Param("userId") String userId, @Param("loginName") String loginName);

    // Iteration 29: giai quyet id cho delete khong id — chi user active
    // o tenant cua session, khop theo sys_user.phone HOAC
    // user_tenant.login_name (phong ten doi SDT chua dong bo het).
    // Khong dung countClinicIdByPhone o day vi no khong loc tenant.
    @Select("select u.id from sys_user u join user_tenant t on t.user_id = u.id "
            + "where u.del_flag != '1' and t.del_flag != '1' and t.tenant_id = #{tenantId} "
            + "and (u.phone = #{phone} or t.login_name = #{phone}) order by u.id limit 1")
    String findActiveUserIdByPhoneInTenant(@Param("phone") String phone, @Param("tenantId") String tenantId);

    User getCompanyIdAndOpenId(String companyId, String openId);

    void updateWxUser(User user);

    List<User> getUserByWxCompanyIdAndJob(@Param("companyId") String companyId,@Param("office") String office,@Param("job") String job);

}