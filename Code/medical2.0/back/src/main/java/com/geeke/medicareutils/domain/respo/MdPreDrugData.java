package com.geeke.medicareutils.domain.respo;

import lombok.Data;

import java.util.List;

/**
 * @Description điện tửĐơn thuốclấy thuốcKết quảTrả về
 * @Author Hzx
 * @Date 2024/11/13
 */
@Data
public class MdPreDrugData {
    private String hiRxno;
    private String setlTime;
    private List<MdMedicineInfo> eltdelts;

    @Data
    public static class MdMedicineInfo {
        // y dượcCơ sởThuốcmã số
        private String medinsListCodg; // Kiểu ký tự 20
        // Tên chung
        private String drugGenname; // Kiểu ký tự 50
        // Tên thương mại của thuốc
        private String drugProdname; // Kiểu ký tự 50
        // Dạng bào chế của thuốc
        private String drugDosform; // Kiểu ký tự 20
        // Quy cách thuốc
        private String drugSpec; // Kiểu ký tự 20
        // Số lượng
        private Integer ent; // Kiểu số 16,3
        // Số phê duyệt
        private String aprvno; // Kiểu ký tự 20
        // Số lô
        private String bchno; // Kiểu ký tự 20
        // Số lô sản xuất
        private String manuLotnum; // Kiểu ký tự 20
        // Nhà sản xuất
        private String prdrName; // Kiểu ký tự 50

    }

}
