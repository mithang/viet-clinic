package com.geeke.hosdata.entity;

import com.geeke.common.persistence.DataEntity;
import lombok.Data;

import java.io.Serializable;

@Data
public class HisMaterials extends DataEntity<HisMaterials> implements Serializable {
    private String companyId;      // Phòng khámid
    private String  sfxmmc ; //Tên vật tư
    private String  py ;//Mã pinyin
    private String  sfxmCode; //duy nhấtVật tưMã
    private String  status; //Trạng thái
    private String  dwjls ;//Đơn vịtínhlượngsố
    private String  dw; //Đơn vị nhỏ nhất
    private String  gg; //Quy cách đóng gói
    private String  dj; //Giá bán ra ngoài
}
