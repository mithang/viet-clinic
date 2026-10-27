package com.geeke.hosdata.constant;


/**
 * bản việntruy cậpđường dẫn
 */
public class ApiUrl {
    // phúttrangThuốcđường dẫn
    public static final String HOS_GET_MEDICINE_PAGE = "/api/BaseData/GetMedicinePage";
    //Tất cảThuốcđường dẫn
    public static final String HOS_GET_MEDICINE_LIST = "/api/BaseData/GetMedicineList";
    //Thu phíhạng mục khám chữa bệnh
    public static final String HOS_GET_CHARGE_ITEM_PAGE = "/api/BaseData/GetChargeItemPage";
    //Thu phílớnLớp
    public static final String HOS_GET_CHARGE_ITEM = "/api/BaseData/GetChargeCategory";
    // phúttrangVật tưđường dẫn
    public static final String HOS_GET_MATERIAL_ITEM_PAGE = "/api/BaseData/GetMaterialItemPage";
    public static  final String HOS_GET_TOKEN ="/api/Auth/GetAppFrienAuthToken" ;
    //thuốckhophát thuốc
    public   static final String HOS_GET_DRUGS_STORAGE_PAGE = "/api/DrugStorage/GetDepartmentDrugsPage";
    //nhập khosaucallbackTrạng thái xét duyệt
    public static final String HOS_UPDATE_DRUGS_STORAGE_STATUS = "/api/DrugStorage/UpdateMedicationStatus";


}
