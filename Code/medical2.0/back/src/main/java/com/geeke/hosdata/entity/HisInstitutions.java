package com.geeke.hosdata.entity;

import com.geeke.common.persistence.DataEntity;
import lombok.Data;

import java.io.Serializable;

@Data
public class HisInstitutions  extends DataEntity<HisInstitutions> implements Serializable {

    private  String companyId;
    private  String sfxmmc; //Chi phíTên
    private  String py; //Pinyin
    private  String dw; // Đơn vị
    private  String sfxmCode;//duy nhấthạng mụcMã
    private  String dj;//bán hàngđơn giá
    private  String dllx;//Hạng mục thu phíLoại


}
