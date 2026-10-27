package com.geeke.hosdata.entity;

import com.geeke.stock.entity.Drug;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class HisDrugsStorage {
    private String pc;//Số lô sản xuất
    private Date scrq;//Ngày sản xuất
    private Date Yxq; //Thời hạn hiệu lực
    private Integer Sl; //Số lượng
    private String ckdw;//Xuất khoĐơn vị
    private BigDecimal Pfj; //Đơn vị nhỏ nhấtlôphátgiá
    private BigDecimal Ykpfj; //Giá nhập
    private String Ypdm;//Thuốcmã
    private String FPh;//Số hóa đơn
    private String Ypmc;//Tên thuốc
    private String zstbzt; //đồng bộTrạng thái
    private String Ckzhyz;//Xuất khochuyển đổidocon >1thì là tiêu chuẩnĐơn vị
    private String crkmxId;//Tồn khoThông tinduy nhấtđịnh danh
    private String Cksj; //phát thuốcThời gian
}
