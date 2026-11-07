package com.geeke.outpatient.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.geeke.common.data.Page;
import com.geeke.common.data.PageRequest;
import com.geeke.common.data.Parameter;
import com.geeke.common.data.SearchParamsBuilder;
import com.geeke.common.service.CrudService;
import com.geeke.common.service.ServiceException;
import com.geeke.outpatient.dao.RecipelInfoReviewDao;
import com.geeke.outpatient.entity.*;
import com.geeke.outpatient.vo.PrescriptionStatisticsVO;
import com.geeke.outpatient.vo.ReviewVO;
import com.geeke.outpatient.vo.StatementVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Iterator;
import java.util.List;

/**
 * Hệ thốngTham sốcấu hìnhService
 * @author lys
 * @version 2021-07-16
 */
 
@Service("recipellnfoReviewService")
public class RecipelInfoReviewService extends CrudService<RecipelInfoReviewDao, RecipelInfoReview>{
    /** Các cột duy nhất được phép xuất hiện trong GROUP BY của listPageStatement
     * (đúng 6 cột frontend gửi; xem getGroupBy() ở recipelInfoReviewStatement).
     * Mọi token khác đều bị từ chối trước khi tới mapper. */
    private static final java.util.Set<String> ALLOWED_GROUP_BY_COLUMNS =
            new java.util.HashSet<String>(java.util.Arrays.asList(
                    "recipelInfo.company_id",
                    "company.name",
                    "registration.department_id",
                    "clinicOffice.name",
                    "registration.doctor_id",
                    "doctor.name"));
    @Resource
    private RecipelInfoReviewDao recipelInfoReviewDao;
    @Resource
    private RecipelInfoService recipelInfoService;
    @Resource
    private RegistrationService registrationService;
    @Resource
    private MedicalRecordService medicalRecordService;
    @Resource
    private RecipelDetailService recipelDetailService;
    public RecipelInfoReview getByRecipelInfoId(String recipelInfoId) {
        return this.recipelInfoReviewDao.getByRecipelInfoId(recipelInfoId);
    }

    /** Y1: ủy thác lấy parent_id của company cho controller ép phạm vi thống kê. */
    public String getCompanyParentId(String companyId) {
        return this.recipelInfoReviewDao.getCompanyParentId(companyId);
    }

    /** N1: recipel_info_id của stored review row (lookup trực tiếp một cột). */
    public String getRecipelInfoIdByReviewId(String reviewId) {
        return this.recipelInfoReviewDao.getRecipelInfoIdByReviewId(reviewId);
    }

    /**
     * DS127 B: save/bulkInsert/bulkUpdate fail-closed 400 (DS94/DS120
     * class). Guard tenant hien co (N1) khong chan duoc id la: lookup
     * tra null thi cho qua, nen save voi id la roi thang vao
     * CrudService.save -> doUpdate 0 dong -> 200 silent no-op
     * (probe B1); saveList goi thang service.save tung dong nen cung
     * lo (B11). Chi chap nhan update khi dong da ton tai, kiem tra
     * bang getRecipelInfoIdByReviewId truc tiep (cot recipel_info_id
     * NOT NULL nen lookup null dong nghia dong khong ton tai); insert
     * (id rong) giu nguyen. bulkInsert: null/[]/[null] -> 400 (mau
     * DS120; truoc day [null] NPE 500 o vong lap controller va o
     * CrudService.bulkInsert), thieu recipelInfo -> 400 thay vi 500
     * NOT NULL o mapper (B5; bulk bypass @Valid, chi FK don moi co
     * the 500 vi entity defaults cover reviewResult/reviewerName/
     * reviewStatus). bulkUpdate: null/[]/[null]/id rong/id la -> 400,
     * tien kiem TOAN BO list truoc khi ghi dong nao (B6/B7: truoc day
     * ghost 200 silent, mixed ghi mot phan). Delete: DS128 B doi
     * single delete ghost/rong sang 400 (xem delete() duoi); bulkDelete
     * ghost giu 200 idempotent (DS126 B).
     */
    @Override
    @Transactional(readOnly = false)
    public RecipelInfoReview save(RecipelInfoReview entity) {
        if (ObjectUtil.isNull(entity)) {
            throw new ServiceException("Rà soát đơn thuốc không được để trống");
        }
        if (StrUtil.isNotBlank(entity.getId())
                && StrUtil.isBlank(getRecipelInfoIdByReviewId(entity.getId()))) {
            throw new ServiceException("Rà soát đơn thuốc không tồn tại: " + entity.getId());
        }
        return super.save(entity);
    }

    @Override
    @Transactional(readOnly = false)
    public List<String> bulkInsert(List<RecipelInfoReview> entitys) {
        if (CollUtil.isEmpty(entitys)) {
            throw new ServiceException("Danh sách rà soát đơn thuốc thêm mới không được để trống");
        }
        for (RecipelInfoReview entity : entitys) {
            if (ObjectUtil.isNull(entity)) {
                throw new ServiceException("Rà soát đơn thuốc không được để trống");
            }
            if (entity.getRecipelInfo() == null
                    || StrUtil.isBlank(entity.getRecipelInfo().getId())) {
                throw new ServiceException("Rà soát đơn thuốc phải gắn với một đơn thuốc");
            }
        }
        return super.bulkInsert(entitys);
    }

    @Override
    @Transactional(readOnly = false)
    public List<String> bulkUpdate(List<RecipelInfoReview> entitys) {
        if (CollUtil.isEmpty(entitys)) {
            throw new ServiceException("Danh sách rà soát đơn thuốc cập nhật không được để trống");
        }
        for (RecipelInfoReview entity : entitys) {
            if (ObjectUtil.isNull(entity)) {
                throw new ServiceException("Rà soát đơn thuốc không được để trống");
            }
            if (StrUtil.isBlank(entity.getId())) {
                throw new ServiceException("Cập nhật hàng loạt yêu cầu id của rà soát đơn thuốc đã tồn tại");
            }
            if (StrUtil.isBlank(getRecipelInfoIdByReviewId(entity.getId()))) {
                throw new ServiceException("Rà soát đơn thuốc không tồn tại: " + entity.getId());
            }
        }
        return super.bulkUpdate(entitys);
    }

    /**
     * DS128 B: single delete fail-closed 400 -- id rong / id ma / dong
     * da xoa cung (mapper DELETE WHERE id, khong loc del_flag nen xoa
     * lan 2 cung la id ma). Deep search: DS123/DS124/DS125 da doi
     * single delete ghost sang 400 o ~25 service anh em; DS127 giu 200
     * o day theo "parity DS123" la nguoc -- chinh DS123 la vong pha
     * parity 200 do. Chi bulkDelete ghost moi giu 200 idempotent
     * (DS99/DS106/DS126 B), khong doi o day. Kiem tra ton tai bang
     * getRecipelInfoIdByReviewId (DS127 B: recipel_info_id NOT NULL
     * nen lookup rong dong nghia dong khong ton tai). Controller delete
     * chay checkReviewWriteTenant truoc (id ma cho qua, lookup rong);
     * guard nay chay sau do, truoc super.delete().
     */
    @Override
    @Transactional(readOnly = false)
    public int delete(RecipelInfoReview entity) {
        if (ObjectUtil.isNull(entity) || StrUtil.isBlank(entity.getId())) {
            throw new ServiceException("Không xác định được rà soát đơn thuốc cần xóa.");
        }
        if (StrUtil.isBlank(getRecipelInfoIdByReviewId(entity.getId()))) {
            throw new ServiceException("Rà soát đơn thuốc không tồn tại: " + entity.getId());
        }
        return super.delete(entity);
    }

    public ReviewVO reviewForm(String recipelInfoId) {
        RecipelInfoReview recipelInfoReview = this.getByRecipelInfoId(recipelInfoId);
        if (ObjectUtil.isNull(recipelInfoReview)) {
            return null;
        }
        ReviewVO vo = new ReviewVO();
        vo.setRecipelInfoReview(recipelInfoReview);
        // trong thuốcTâythuốc
        List<Parameter> parameters = SearchParamsBuilder.create()
                .eq("registration_id", recipelInfoReview.getRecipelInfo().getRegistration().getId())
                .build();
        List<MedicalRecord> medicalRecordList = medicalRecordService.listAll(parameters, StrUtil.EMPTY);
        if (CollUtil.isNotEmpty(medicalRecordList)) {
            MedicalRecord medicalRecord = medicalRecordList.get(0);
            vo.setWesternDiagnose(medicalRecord.getWesternDiagnose());
            vo.setChinaDiagnose(medicalRecord.getChinaDiagnose());
        }
        //Đơn thuốcChi tiết
        List<Parameter> recipelParams = SearchParamsBuilder.create()
                .eq("company_id", recipelInfoReview.getRecipelInfo().getCompany().getId())
                .eq("recipel_info_id", recipelInfoId)
                .eq("is_extra", 0)
                .build();
        List<RecipelDetail> recipelDetailList = this.recipelDetailService.listAll(recipelParams, org.apache.commons.lang3.StringUtils.EMPTY);
        if (CollUtil.isNotEmpty(recipelDetailList)) {
            recipelDetailList.forEach(recipelDetail -> {

                recipelDetail.setRecipelInfo(recipelInfoReview.getRecipelInfo());
                recipelDetail.setDrugStuffId(medicalRecordService.getDrugStuffEvt(recipelDetail));
            });
        }
        vo.setRecipelDetails(recipelDetailList);
        //Phí phụ thêm
        List<Parameter> additionalParams = SearchParamsBuilder.create()
                .eq("company_id", recipelInfoReview.getRecipelInfo().getCompany().getId())
                .eq("recipel_info_id", recipelInfoId)
                .eq("is_extra", 1)
                .build();
        List<RecipelDetail> additionalCharges = this.recipelDetailService.listAll(additionalParams, org.apache.commons.lang3.StringUtils.EMPTY);
        if (CollUtil.isNotEmpty(additionalCharges)) {
            additionalCharges.forEach(recipelDetail -> {
                recipelDetail.setDrugStuffId(medicalRecordService.getDrugStuffEvt(recipelDetail));
            });
        }
        vo.setAdditionalCharges(additionalCharges);
        return vo;
    }

    public Page<StatementVO> listPageStatement(List<Parameter> parameters, String orderby) {
        // Gate fix: request không có "params" trước đây gây NPE (HTTP 500) vì
        // parameters == null. Trả trang rỗng thay vì lỗi 500; frontend luôn
        // gửi params nên hành vi UI không đổi.
        if (parameters == null) {
            return new Page<StatementVO>(0L, CollUtil.newArrayList());
        }
        String groupBy = null;
        for (int i = parameters.size() - 1; i >= 0; i--) {
            Parameter parameter = parameters.get(i);
            if (StrUtil.equals(parameter.getColumnName(), "`groupBy`")) {
                Object value = parameter.getValue();
                groupBy = value == null ? null : value.toString();
                parameters.remove(i);
            }
        }
        // groupBy thiếu/rỗng: không gọi DAO với GROUP BY rỗng (trước đây
        // sinh SQL lỗi → HTTP 500). Trả trang rỗng, khớp hợp đồng hiện tại
        // với params null/rỗng; frontend luôn gửi groupBy nên UI không đổi.
        if (groupBy == null || groupBy.trim().isEmpty()) {
            return new Page<StatementVO>(0L, CollUtil.newArrayList());
        }
        // Allowlist bắt buộc: chỉ đúng các cột mà frontend thực dùng (xem
        // getGroupBy() ở recipelInfoReviewStatement/index.vue). Mapper đưa
        // groupBy thẳng vào GROUP BY ${column} nên mọi token ngoài allowlist
        // — kể cả payload injection — đều bị từ chối bằng HTTP 400
        // (ServiceException → GlobalExceptionHandler), không bao giờ tới SQL.
        List<String> groupByColumns = new java.util.ArrayList<String>();
        for (String token : StrUtil.split(groupBy, ",")) {
            String column = token == null ? "" : token.trim();
            if (!ALLOWED_GROUP_BY_COLUMNS.contains(column)) {
                throw new com.geeke.common.service.ServiceException(
                        "groupBy không hợp lệ: chỉ chấp nhận các cột tổng hợp của trang rà soát đơn thuốc");
            }
            groupByColumns.add(column);
        }
        List<StatementVO> list = dao.listPageStatement(parameters,groupByColumns);
        return new Page<StatementVO>(Long.valueOf((list.size()+"")), list);
    }

    /**
     * Đơn thuốcthống kê
     * @param id Cơ sởID, dùng đểqualọcHiện tạiCơ sở của Dữ liệu
     * @return
     */
    public PrescriptionStatisticsVO prescriptionStatistics(String id,String startTime,String endTime) {
        return this.dao.prescriptionStatistics(id,startTime,endTime);
    }
}