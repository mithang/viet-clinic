package com.geeke.medicareutils.service;

import com.alibaba.fastjson.JSONObject;
import com.geeke.outpatient.entity.RecipelInfoEvt;
import com.geeke.stock.entity.DispensingEvt;
import com.geeke.stock.entity.OutboundEvt;
import com.geeke.stock.entity.StorageEvt;

/**
 * BHYTtiến hành销lưuQuản lý
 */
public interface MdInventoryService {

    /**
     * Tải lên kiểm kê hàng hóa  3501
     * @return
     */
    JSONObject uploadInventory_3501();
    /**
     * Tải lên kiểm kê hàng hóa 3501A
     * @return
     */
    JSONObject uploadInventoryList_3501A();

    /**
     * hàng hóaTồn khothay đổi 3502
     * @return
     */
    JSONObject updateInventory_3502(StorageEvt storageEvt, String invChgType);

    /**
     * Nhập kho  3502A
     * @return
     */
    JSONObject updateInventoryList_3502A(StorageEvt storageEvt, String invChgType);

    /**
     * phát thuốcTrả thuốc 3502A
     * @param dispensingEvt
     * @return
     */
    JSONObject updateInventoryList_3502A(DispensingEvt dispensingEvt);


    /**
     * Xuất kho 3502A
     * @param
     * @return
     */
    JSONObject updateInventoryList_3502A(OutboundEvt outboundEvt);

    /**
     * bán lẻXuất kho 3502A
     * @param
     * @return
     */
    JSONObject updateInventoryList_3502A(RecipelInfoEvt recipelInfoEvt);




    /**
     * Mua hàng hóa 3503
     * @return
     */
    JSONObject createPurchaseOrder_3503();

    /**
     * Mua hàng hóa 3503A
     * @return
     */
    JSONObject createPurchaseOrderList_3503A();

    /**
     * Trả hàng mua hàng hóa 3504
     * @return
     */
    JSONObject createPurchaseReturnOrder_3504();

    /**
     * Trả hàng mua hàng hóa 3504A
     * @return
     */
    JSONObject createPurchaseReturnOrderList_3504A();

    /**
     * Bán hàng hóa 3505
     * @return
     */
    JSONObject createSaleOrder_3505();

    /**
     * Bán hàng hóa 3505A
     * @return
     */
    JSONObject createSaleOrderList_3505A();

    /**
     * Trả hàng bán 3506
     * @return
     */
    JSONObject createSaleReturnOrder_3506();

    /**
     * Trả hàng bán 3506A
     * @return
     */
    JSONObject createSaleReturnOrderList_3506A();

    /**
     * Xóa thông tin hàng hóa 3507
     * Xóa thông tin một lô hàng hóa nào đó qua giao dịch này
     * @return
     */
    JSONObject removeProductRecord_3507();
    /**
     * Xóa thông tin hàng hóa 3507A
     * Xóa thông tin một lô hàng hóa nào đó qua giao dịch này
     * @return
     */
    JSONObject removeProductRecordList();

    /**
     *hàng hóaTồn khoThông tinTruy vấn 3508
     * @return
     */
     JSONObject getInventoryInfo_3508();

    /**
     * Tồn khothay đổiBản ghiTruy vấn 3509
     * @return
     */
     JSONObject getInventoryChangeRecords_3509();

    /**
     * Mua hàngBản ghiTruy vấn 3510
     * @return
     */
     JSONObject getProcurementRecords_3510();

    /**
     * bán hàngBản ghiTruy vấn 3511
     * @return
     */
     JSONObject getSalesRecords_3511();

    /**
     * nhập khoThuốctruy xuấtThông tinTruy vấn   3512
     * @return
     */
     JSONObject getInboundDetails_3512();

    /**
     * bán hàngThuốctruy xuấtThông tinTruy vấn   3513
     * @return
     */
     JSONObject getSalesDetails_3513();




}
