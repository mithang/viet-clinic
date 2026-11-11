package com.geeke.outpatient.service;


import com.geeke.common.service.CrudService;
import com.geeke.common.service.ServiceException;
import com.geeke.outpatient.dao.SchedulingDao;
import com.geeke.outpatient.entity.*;
import com.geeke.utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;

import java.util.*;

@Service("schedulingService")
@Transactional(readOnly = true)
public class SchedulingService extends CrudService<SchedulingDao, Scheduling>{

    @Autowired
    private SchedulingDao schedulingDao;

    // DS106 B1/B2/B3/B5: SchedulingDao.xml truoc day KHONG CO get/update/
    // bulk statement nao, nen moi save/delete mang id deu 500
    // BindingException qua verifyTenantOwnershipAgainstStoredRow (dao.get)
    // cua CrudService, va save-update dong da ton tai khong bao gio lam
    // duoc; dong thoi controller save khong @Valid nen save-create thieu
    // userid/schedulingtime (NOT NULL) 500. Fix: them get/update vao
    // mapper (mau DS102 them mapper thieu), kiem tra ton tai + truong
    // bat buoc o day truoc moi ghi. @Transactional (readOnly = false)
    // tuong minh vi class-level la readOnly = true.
    @Override
    @Transactional(readOnly = false)
    public Scheduling save(Scheduling entity) {
        if (entity != null && StringUtils.isNotBlank(entity.getId())
                && schedulingDao.get(entity.getId()) == null) {
            throw new ServiceException("Lịch trực không tồn tại: "
                    + entity.getId());
        }
        if (entity != null && StringUtils.isBlank(entity.getUserid())) {
            throw new ServiceException("Người dùng xếp lịch không được để trống");
        }
        if (entity != null && entity.getSchedulingtime() == null) {
            throw new ServiceException("Ngày xếp lịch không được để trống");
        }
        return super.save(entity);
    }

    // DS136 B: single delete fail-closed 400. SchedulingService truoc
    // day KHONG CO delete() override: mapper <update id="delete"> xoa
    // theo KHOA COMPOSITE (userid, schedulingtime, company_id) -- id
    // cua entity khong bao gio duoc dung -- voi loc del_flag='0' AND
    // is_locked='0'. CrudService.delete chi kiem tra dong da luu qua
    // dao.get(entity.getId()) nen ghost id -> stored null -> bo qua
    // (lop DS123). He qua: khoa la/rong -> 200 data 0 im lang; xoa lan
    // 2 -> 200 data 0; dong khoa (is_locked='1') -> 200 data 0; va
    // XOA NHAM DONG SONG: payload mang id la nhung khoa (userid, ngay,
    // companyid) that se xoa dong song do (probe B6 pre-fix: 200 rows 1).
    // UI that (scheduling/index.vue delrowdata) gui model khong co id
    // nen id rong la duong hop le -- chi kiem tra id khi payload co gui:
    // id non-blank ma khong khop dong song nao cua khoa -> 400.
    // checkExists (DS103) KHONG tai dung: cho qua blank id, khong check
    // delFlag. bulkDelete khong co route, khong caller, mapper thieu
    // statement -- khong them (dead code, ngoai pham vi).
    @Override
    @Transactional(readOnly = false)
    public int delete(Scheduling entity) {
        if (entity == null || StringUtils.isBlank(entity.getUserid())
                || entity.getSchedulingtime() == null
                || StringUtils.isBlank(entity.getCompanyid())) {
            throw new ServiceException("Không xác định được lịch trực cần xóa");
        }
        List<Scheduling> live = schedulingDao.listcount(entity);
        if (live == null || live.isEmpty()) {
            throw new ServiceException("Lịch trực không tồn tại");
        }
        for (Scheduling row : live) {
            if ("1".equals(row.getIsLocked())) {
                throw new ServiceException("Lịch trực đã khóa không thể xóa");
            }
        }
        if (StringUtils.isNotBlank(entity.getId())) {
            boolean idMatches = false;
            for (Scheduling row : live) {
                if (entity.getId().equals(row.getId())) {
                    idMatches = true;
                    break;
                }
            }
            if (!idMatches) {
                throw new ServiceException("Lịch trực không tồn tại: " + entity.getId());
            }
        }
        return super.delete(entity);
    }

    public List<SchedulingDTO> getHisuserpblist(SchedulingDTO pageRequest) {
        List<SchedulingDTO> buserlist= schedulingDao.getpbuserlist(pageRequest);
        if (Objects.nonNull(buserlist)){
            for (SchedulingDTO buser : buserlist ){
                //String[] weekDays = {"Chủ nhật", "Thứ hai", "Thứ ba", "Thứ tư", "Thứ năm", "Thứ sáu", "Thứ bảy"};

                    List<WeekdayDTO> weeks = new ArrayList<>();
                    if (buser.getPbsl() > 0) {
                        buser.setCompanyid(pageRequest.getCompanyid());
                        buser.setStratime(pageRequest.getStratime());
                        buser.setEndtime(pageRequest.getEndtime());
                        List<Scheduling> schedulingList=schedulingDao.listscheduling(buser);
                        WeekdayDTO week=new WeekdayDTO();
                        for (Scheduling list:schedulingList)
                        {
                            if(list.getDelFlag().equals("1")){
                                continue;
                            }

                            Calendar cal = Calendar.getInstance();
                            cal.setTime(list.getSchedulingtime());

                            int w = cal.get(Calendar.DAY_OF_WEEK) - 1;
                            Date schedulingtime = list.getSchedulingtime();
                            List<Scheduling> list1 =  schedulingDao.getList(schedulingtime);
                            switch (w) {
                                case 0:
                                    for (Scheduling ls : list1){
                                        week.setSunday(ls.getTimeid());
                                    }
                                    break;
                                case 1:
                                    for (Scheduling ls : list1){
                                        week.setMonday(ls.getTimeid());
                                    }
                                    break;
                                case 2:
                                    for (Scheduling ls : list1){
                                        week.setTuesday(ls.getTimeid());
                                    }
                                    break;
                                case 3:
                                    for (Scheduling ls : list1){
                                        week.setWednesday(ls.getTimeid());
                                    }
                                    break;
                                case 4:
                                    for (Scheduling ls : list1){
                                        week.setThursday(ls.getTimeid());
                                    }
                                    break;
                                case 5:
                                    for (Scheduling ls : list1){
                                        week.setFriday(ls.getTimeid());
                                    }
                                    break;
                                case 6:
                                    for (Scheduling ls : list1){
                                        week.setSaturday(ls.getTimeid());
                                    }
                                    break;
                            }
                        }
                        weeks.add(week);
                    }else {
                        WeekdayDTO week=new WeekdayDTO();
                        weeks.add(week);
                    }
                    buser.setDates(weeks);

            }

        }
        return buserlist;
    }
    public List<SchedulingDTO> getlistzl(SchedulingDTO pageRequest) {
        List<SchedulingDTO> buserlist= schedulingDao.getpbuserlist(pageRequest);
        if (Objects.nonNull(buserlist)){
            for (SchedulingDTO buser : buserlist ){
                //String[] weekDays = {"Chủ nhật", "Thứ hai", "Thứ ba", "Thứ tư", "Thứ năm", "Thứ sáu", "Thứ bảy"};

                WeekdayDTO week=new WeekdayDTO();
                List<String> dates=new ArrayList<>();
                if (buser.getPbsl() > 0) {
                    buser.setCompanyid(pageRequest.getCompanyid());
                    buser.setStratime(pageRequest.getStratime());
                    buser.setEndtime(pageRequest.getEndtime());
                    List<Scheduling> schedulingList=schedulingDao.listscheduling(buser);


                    for (Scheduling list:schedulingList)
                    {
                        if(list.getDelFlag().equals("1")){
                            continue;
                        }

                        Calendar cal = Calendar.getInstance();
                        cal.setTime(list.getSchedulingtime());
                        //nàyCái có bạncầnchuyểnthànhsau của Thời gian của định dạng
                        SimpleDateFormat sdff=new SimpleDateFormat("yyyy-MM-dd");
                        // Thời giandấuchuyển đổithànhThời gian
                        String datetieme = sdff.format(list.getSchedulingtime());
                        int w = cal.get(Calendar.DAY_OF_WEEK) - 1;
                        switch (w) {
                            case 0:
                                week.setSunday(datetieme);
                                break;
                            case 1:
                                week.setMonday(datetieme);
                                break;
                            case 2:
                                week.setTuesday(datetieme);
                                break;
                            case 3:
                                week.setWednesday(datetieme);
                                break;
                            case 4:
                                week.setThursday(datetieme);
                                break;
                            case 5:
                                week.setFriday(datetieme);
                                break;
                            case 6:
                                week.setSaturday(datetieme);
                                break;
                        }
                    }

                    if (week.getMonday()!=null){dates.add(week.getMonday());}else dates.add(null);
                    if (week.getTuesday()!=null){dates.add(week.getTuesday());}else dates.add(null);
                    if (week.getWednesday()!=null){dates.add(week.getWednesday());}else dates.add(null);
                    if (week.getThursday()!=null){dates.add(week.getThursday());}else dates.add(null);
                    if (week.getFriday()!=null){dates.add(week.getFriday());}else dates.add(null);
                    if (week.getSaturday()!=null){dates.add(week.getSaturday());}else dates.add(null);
                    if (week.getSunday()!=null){dates.add(week.getSunday());}else dates.add(null);
                    buser.setDatelist(dates);
                }

                buser.setDatelist(dates);

            }

        }
        return buserlist;
    }

    public List<SchedulingDTO> listzlr(SchedulingDTO pageRequest) {
        List<SchedulingDTO> buserlist= schedulingDao.getpbuserlist(pageRequest);
        if (Objects.nonNull(buserlist)){
            for (SchedulingDTO buser : buserlist ){
                //String[] weekDays = {"Chủ nhật", "Thứ hai", "Thứ ba", "Thứ tư", "Thứ năm", "Thứ sáu", "Thứ bảy"};

                List<Integer> house=new ArrayList<>();
                if (buser.getPbsl() > 0) {
                    buser.setCompanyid(pageRequest.getCompanyid());
                    buser.setDatestra(pageRequest.getDatestra());
                    buser.setDateend(pageRequest.getDateend());
                    List<SchedulingmxDTO> schedulingList=schedulingDao.listschedulingmx(buser);

                    for (SchedulingmxDTO list:schedulingList)
                    {
                        if (list.getHouser()!=null){
                            house.add(list.getHouser());
                        }
                    }

                    //buser.setDatehouse(house);
                }

                buser.setDatehouse(house);

            }

        }
        return buserlist;
    }
    public List<SchedulingmxDTO> getlistmx(SchedulingDTO pageRequest)
    {
        // DS201: escape LIKE wildcards in patientname (mirror DS200
        // patientName) — "%" must match literally, not every patient.
        pageRequest.setPatientname(
                StringUtils.escapeLike(pageRequest.getPatientname()));
        return schedulingDao.listschedulingmx(pageRequest);
    }


    public int listcount(Scheduling scheduling){
        List<Scheduling> schedulingList=schedulingDao.listcount(scheduling);
        int count=0;
        if(schedulingList == null){
            return count;
        }else{
            count=schedulingList.size();
        }
        return count;
    }


}
