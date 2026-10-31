package com.geeke.medicareutils.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.geeke.medicareutils.service.MdInventoryService;
import com.geeke.medicareutils.util.MdRequestUtil;
import com.geeke.outpatient.entity.RecipelInfoEvt;
import com.geeke.stock.entity.*;
import com.geeke.stock.service.DrugService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Arrays;

/**
 * BHYTtiến hànhtiêulưukiểm soátTriển khaiLớp
 */
@RequiredArgsConstructor
@Service
public class MdInventoryServiceImpl implements MdInventoryService {


    private final MdRequestUtil mdRequestUtil;

    private final DrugService drugService;
    /**
     * Tải lên kiểm kê hàng hóa  3501
     *
     * @return
     */
    @Override
    public JSONObject uploadInventory_3501() {
        JSONObject invinfo = new JSONObject();
        invinfo.put("med_list_codg", ""); // Mã danh mục y tế
        invinfo.put("fixmedins_hist_id", ""); // Mã danh mục của cơ sở y tế được chỉ định
        invinfo.put("fixmedins_hist_name", ""); // Tên danh mục của cơ sở y tế được chỉ định
        invinfo.put("rx_flag", ""); // Cờ thuốc kê đơn
        invinfo.put("invdate", ""); // Ngày kiểm kê
        invinfo.put("inv_cnt", ""); // Số lượng tồn kho
        invinfo.put("manu_lotnum", ""); // Số lô sản xuất
        invinfo.put("fixmedins_bchno", ""); // Số thứ tự lô của cơ sở y tế được chỉ định
        invinfo.put("manu_date", ""); // Ngày sản xuất
        invinfo.put("expy_end", ""); // Hết hạn đến
        invinfo.put("memo", ""); // Ghi chú
        invinfo.put("drugtracinfo", ""); // Thông tin nút mã truy xuất
        JSONObject drugtracinfo = new JSONObject();
        drugtracinfo.put("drug_trac_codg","");//Mã truy xuất thuốc
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("invinfo",jsonObject);
        jsonObject.put("drugtracinfo",drugtracinfo);

        return mdRequestUtil.getMedicareData("3501",jsonObject);
    }

    /**
     * Tải lên kiểm kê hàng hóa 3501A
     * Tham sốnhiều dòngxây dựng
     * @return
     */
    @Override
    public JSONObject uploadInventoryList_3501A() {
        JSONArray array = new  JSONArray();
        JSONObject invinfo = new JSONObject();
        invinfo.put("med_list_codg", ""); // Mã danh mục y tế
        invinfo.put("fixmedins_hist_id", ""); // Mã danh mục của cơ sở y tế được chỉ định
        invinfo.put("fixmedins_hist_name", ""); // Tên danh mục của cơ sở y tế được chỉ định
        invinfo.put("rx_flag", ""); // Cờ thuốc kê đơn
        invinfo.put("invdate", ""); // Ngày kiểm kê
        invinfo.put("inv_cnt", ""); // Số lượng tồn kho
        invinfo.put("manu_lotnum", ""); // Số lô sản xuất
        invinfo.put("fixmedins_bchno", ""); // Số thứ tự lô của cơ sở y tế được chỉ định
        invinfo.put("manu_date", ""); // Ngày sản xuất
        invinfo.put("expy_end", ""); // Hết hạn đến
        invinfo.put("memo", ""); // Ghi chú
        invinfo.put("drugtracinfo", ""); // Thông tin nút mã truy xuất
        //Chi tiếttruy xuấtmã
        JSONObject drugtracinfo = new JSONObject();
        drugtracinfo.put("drug_trac_codg","");//Mã truy xuất thuốc
        array.add(invinfo);
        JSONArray array1 = new JSONArray();
        array1.add(drugtracinfo);

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("invinfoDetail",array);
        jsonObject.put("drugtracinfo",array1);

        return mdRequestUtil.getMedicareData("3501A",jsonObject);
    }

    /**
     * hàng hóaTồn khothay đổi 3502
     *
     * @return
     */
    @Override
    public JSONObject updateInventory_3502(StorageEvt storageEvt, String invChgType) {
        JSONObject invinfo = new JSONObject();
        SupplierStock supplierStock = storageEvt.getSupplierStockList().get(0);
        //bằngdướiTrườngcầnTheothực tếnghiệp vụbổ sung
        invinfo.put("med_list_codg", ""); // Mã danh mục y tế
        invinfo.put("inv_chg_type", invChgType); // Loại thay đổi tồn kho
        invinfo.put("fixmedins_hist_id", ""); // Mã danh mục của cơ sở y tế được chỉ định
        invinfo.put("fixmedins_hist_name", ""); // Tên danh mục của cơ sở y tế được chỉ định
        invinfo.put("fixmedins_bchno", supplierStock.getSupplierStorage().getCode()); // Số thứ tự lô của cơ sở y tế được chỉ định
        invinfo.put("pric", supplierStock.getRetailPrice()); // Đơn giá
        invinfo.put("cnt", supplierStock.getCnt()); // Số lượng BHYT
        Drug drug = drugService.get(supplierStock.getDrug().getId());
        invinfo.put("rx_flag", drug.getRxFlag() == null ? "0" : drug.getRxFlag()); // Cờ thuốc kê đơn 0  không  1 có 
        invinfo.put("inv_chg_time", supplierStock.getCreateDate()); // Thời gian thay đổi tồn kho
        invinfo.put("inv_chg_opter_name", supplierStock.getCreateBy().substring(0, supplierStock.getCreateBy().indexOf("("))); // Tên người thực hiện thay đổi tồn kho
        invinfo.put("memo", ""); // Ghi chú
        invinfo.put("trdn_flag", ""); // Cờ bán lẻ
        // Lưu mảng mã truy xuất của thuốc này
        JSONArray drugTracInfoArray = new JSONArray();
        // Kiểm tra có nhiều mã truy xuất không, Phân tách bằng dấu phẩy
        if (supplierStock.getDrugTracCodg().contains(",")) {
            Arrays.asList(supplierStock.getDrugTracCodg().split(",")).forEach(
                    drugTracCodg -> {
                        JSONObject drugtracinfo = new JSONObject();
                        drugtracinfo.put("drug_trac_codg", drugTracCodg); // Mã truy xuất thuốc
                        drugTracInfoArray.add(drugtracinfo);
                    }
            );
        } else {
            JSONObject drugtracinfo = new JSONObject();
            drugtracinfo.put("drug_trac_codg", supplierStock.getDrugTracCodg()); // Mã truy xuất thuốc
            drugTracInfoArray.add(drugtracinfo);
        }
        // Thêm mảng mã truy xuất vào invinfo Trong đối tượng
        invinfo.put("drugtracinfo", drugTracInfoArray);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("invinfo", invinfo);
        return mdRequestUtil.getMedicareData("3502",jsonObject);
    }

    /**
     * 3502A  Phòng khámTồn khonhập kho
     *
     * @return resultNút: retRslt 1: Thành công, 0: Thất bại  msgRslt: Lý do thất bại
     */
    @Override
    public JSONObject updateInventoryList_3502A(StorageEvt storageEvt, String invChgType) {
        JSONArray array = new JSONArray();
        storageEvt.getSupplierStockList().forEach(supplierStock -> {
            Drug drug = drugService.get(supplierStock.getDrug().getId());
            JSONObject invinfo = new JSONObject();
            invinfo.put("med_list_codg", ""); // Mã danh mục y tế
            invinfo.put("inv_chg_type", invChgType); // Loại thay đổi tồn kho
            invinfo.put("fixmedins_hist_id", drug.getCode()); // Mã danh mục của cơ sở y tế được chỉ định
            invinfo.put("fixmedins_hist_name", drug.getName()); // Tên danh mục của cơ sở y tế được chỉ định
            invinfo.put("fixmedins_bchno", supplierStock.getSupplierStorage().getCode()); // Số thứ tự lô của cơ sở y tế được chỉ định
            invinfo.put("pric", supplierStock.getRetailPrice()); // Đơn giá
            invinfo.put("cnt", supplierStock.getCnt()); // Số lượng BHYT
            invinfo.put("rx_flag", drug.getRxFlag() == null ? "0" : drug.getRxFlag()); // Cờ thuốc kê đơn 0  không  1 có 
            invinfo.put("inv_chg_time", supplierStock.getCreateDate()); // Thời gian thay đổi tồn kho
            invinfo.put("inv_chg_opter_name", supplierStock.getCreateBy().substring(0, supplierStock.getCreateBy().indexOf("("))); // Tên người thực hiện thay đổi tồn kho
            invinfo.put("memo", ""); // Ghi chú
            invinfo.put("trdn_flag", ""); // Cờ bán lẻ
            // Lưu mảng mã truy xuất của thuốc này
            JSONArray drugTracInfoArray = new JSONArray();
            // Kiểm tra có nhiều mã truy xuất không, Phân tách bằng dấu phẩy
            if (supplierStock.getDrugTracCodg().contains(",")) {
                Arrays.asList(supplierStock.getDrugTracCodg().split(",")).forEach(
                        drugTracCodg -> {
                            JSONObject drugtracinfo = new JSONObject();
                            drugtracinfo.put("drug_trac_codg", drugTracCodg); // Mã truy xuất thuốc
                            drugTracInfoArray.add(drugtracinfo);
                        }
                );
            } else {
                JSONObject drugtracinfo = new JSONObject();
                drugtracinfo.put("drug_trac_codg", supplierStock.getDrugTracCodg()); // Mã truy xuất thuốc
                drugTracInfoArray.add(drugtracinfo);
            }
            // Thêm mảng mã truy xuất vào invinfo Trong đối tượng
            invinfo.put("drugtracinfo", drugTracInfoArray);
            // Sẽ  invinfo Thêm vào mảng chính
            array.add(invinfo);
        });
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("invinfoDetail", array);
        return mdRequestUtil.getMedicareData("3502A", jsonObject);
    }

    /**
     * phát thuốcTrả thuốc  3502A
     * @param dispensingEvt
     * @return
     */
    @Override
    public JSONObject updateInventoryList_3502A(DispensingEvt dispensingEvt) {
        JSONArray array = new JSONArray();
        dispensingEvt.getDispensingDetailEvtList().forEach(detailEvt -> {
            JSONObject invinfo = new JSONObject();
            Drug drug = drugService.get(detailEvt.getId());
            invinfo.put("med_list_codg", ""); // Mã danh mục y tế
            if("0".equals(dispensingEvt.getDispensingStatus())){
                //Phát thuốc  Xuất kho
                invinfo.put("inv_chg_type", "101");
            }else if("1".equals(dispensingEvt.getDispensingStatus())){
                //Trả thuốc  Nhập kho
                invinfo.put("inv_chg_type", "102");
            }
            invinfo.put("fixmedins_hist_id", drug.getCode()); // Mã danh mục của cơ sở y tế được chỉ định
            invinfo.put("fixmedins_hist_name", drug.getName()); // Tên danh mục của cơ sở y tế được chỉ định
            invinfo.put("fixmedins_bchno", dispensingEvt.getRegistrationId()); // Số thứ tự lô của cơ sở y tế được chỉ định
            invinfo.put("pric",drug.getPrice()); // Đơn giá
            invinfo.put("cnt",""); // Số lượng
            invinfo.put("rx_flag", ""); // Cờ thuốc kê đơn
            invinfo.put("inv_chg_time", ""); // Thời gian thay đổi tồn kho
            invinfo.put("inv_chg_opter_name", ""); // Tên người thực hiện thay đổi tồn kho
            invinfo.put("memo", ""); // Ghi chú
            invinfo.put("trdn_flag", ""); // Cờ bán lẻ
            invinfo.put("drugtracinfo", ""); // Thông tin nút mã truy xuất
            // Lưu mảng mã truy xuất của thuốc này
            JSONArray drugTracInfoArray = new JSONArray();
            // Kiểm tra có nhiều mã truy xuất không, Phân tách bằng dấu phẩy
            if (detailEvt.getDrugTracCodg().contains(",")) {
                Arrays.asList(detailEvt.getDrugTracCodg().split(",")).forEach(
                        drugTracCodg -> {
                            JSONObject drugtracinfo = new JSONObject();
                            drugtracinfo.put("drug_trac_codg", drugTracCodg); // Mã truy xuất thuốc
                            drugTracInfoArray.add(drugtracinfo);
                        }
                );
            } else {
                JSONObject drugtracinfo = new JSONObject();
                drugtracinfo.put("drug_trac_codg", detailEvt.getDrugTracCodg()); // Mã truy xuất thuốc
                drugTracInfoArray.add(drugtracinfo);
            }
            // Thêm mảng mã truy xuất vào invinfo Trong đối tượng
            invinfo.put("drugtracinfo", drugTracInfoArray);
            // Sẽ  invinfo Thêm vào mảng chính
            array.add(invinfo);
        });
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("invinfoDetail", array);
        return mdRequestUtil.getMedicareData("3502A", jsonObject);
    }

    /**
     * Xuất kho 3502A
     *
     * @param outboundEvt Phiếu xuất kho
     * @return
     */
    @Override
    public JSONObject updateInventoryList_3502A(OutboundEvt outboundEvt) {
        JSONArray array = new JSONArray();
        outboundEvt.getSupplierOutboundDetailList().forEach(supplierStock -> {
            Drug drug = drugService.get(supplierStock.getDrug().getId());
            JSONObject invinfo = new JSONObject();
            invinfo.put("med_list_codg", ""); // Mã danh mục y tế
            invinfo.put("inv_chg_type", "101"); // Loại thay đổi tồn kho
            invinfo.put("fixmedins_hist_id", drug.getCode()); // Mã danh mục của cơ sở y tế được chỉ định
            invinfo.put("fixmedins_hist_name", drug.getName()); // Tên danh mục của cơ sở y tế được chỉ định
            invinfo.put("fixmedins_bchno", supplierStock.getMedicinalStorage().getId()); // Số thứ tự lô của cơ sở y tế được chỉ định
            invinfo.put("pric", drug.getPrice()); // Đơn giá
            invinfo.put("cnt", supplierStock.getCnt()); // Số lượng BHYT
            invinfo.put("rx_flag", drug.getRxFlag() == null ? "0" : drug.getRxFlag()); // Cờ thuốc kê đơn 0  không  1 có 
            invinfo.put("inv_chg_time", supplierStock.getCreateDate()); // Thời gian thay đổi tồn kho
            invinfo.put("inv_chg_opter_name", supplierStock.getCreateBy().substring(0, supplierStock.getCreateBy().indexOf("("))); // Tên người thực hiện thay đổi tồn kho
            invinfo.put("memo", ""); // Ghi chú
            invinfo.put("trdn_flag", ""); // Cờ bán lẻ
            // Lưu mảng mã truy xuất của thuốc này
            JSONArray drugTracInfoArray = new JSONArray();
            // Kiểm tra có nhiều mã truy xuất không, Phân tách bằng dấu phẩy
            if (supplierStock.getDrugTracCodg().contains(",")) {
                Arrays.asList(supplierStock.getDrugTracCodg().split(",")).forEach(
                        drugTracCodg -> {
                            JSONObject drugtracinfo = new JSONObject();
                            drugtracinfo.put("drug_trac_codg", drugTracCodg); // Mã truy xuất thuốc
                            drugTracInfoArray.add(drugtracinfo);
                        }
                );
            } else {
                JSONObject drugtracinfo = new JSONObject();
                drugtracinfo.put("drug_trac_codg", supplierStock.getDrugTracCodg()); // Mã truy xuất thuốc
                drugTracInfoArray.add(drugtracinfo);
            }
            // Thêm mảng mã truy xuất vào invinfo Trong đối tượng
            invinfo.put("drugtracinfo", drugTracInfoArray);
            // Sẽ  invinfo Thêm vào mảng chính
            array.add(invinfo);
        });
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("invinfoDetail", array);
       return mdRequestUtil.getMedicareData("3502A", jsonObject);
    }

    /**
     * bán lẻXuất kho 3502A
     *
     * @param recipelInfoEvt@return
     */
    @Override
    public JSONObject updateInventoryList_3502A(RecipelInfoEvt recipelInfoEvt) {
        JSONArray array = new JSONArray();
        recipelInfoEvt.getRecipelDetailEvtList().forEach(supplierStock -> {
            Drug drug = drugService.get(supplierStock.getDrugStuffId().getDrug().getId());
            JSONObject invinfo = new JSONObject();
            invinfo.put("med_list_codg", ""); // Mã danh mục y tế
            invinfo.put("inv_chg_type", "101"); // Loại thay đổi tồn kho
            invinfo.put("fixmedins_hist_id", drug.getCode()); // Mã danh mục của cơ sở y tế được chỉ định
            invinfo.put("fixmedins_hist_name", drug.getName()); // Tên danh mục của cơ sở y tế được chỉ định
            invinfo.put("fixmedins_bchno", recipelInfoEvt.getRecipelInfo().getId()); // Số thứ tự lô của cơ sở y tế được chỉ định
            invinfo.put("pric", drug.getPrice()); // Đơn giá
            invinfo.put("cnt", supplierStock.getTotal()); // Số lượng BHYT
            invinfo.put("rx_flag", drug.getRxFlag() == null ? "0" : drug.getRxFlag()); // Cờ thuốc kê đơn 0  không  1 có 
            invinfo.put("inv_chg_time", supplierStock.getCreateDate()); // Thời gian thay đổi tồn kho
            invinfo.put("inv_chg_opter_name", supplierStock.getCreateBy().substring(0, supplierStock.getCreateBy().indexOf("("))); // Tên người thực hiện thay đổi tồn kho
            invinfo.put("memo", ""); // Ghi chú
            invinfo.put("trdn_flag", ""); // Cờ bán lẻ
            // Lưu mảng mã truy xuất của thuốc này
            JSONArray drugTracInfoArray = new JSONArray();
            // Kiểm tra có nhiều mã truy xuất không, Phân tách bằng dấu phẩy
            if (supplierStock.getDrugTracCodg().contains(",")) {
                Arrays.asList(supplierStock.getDrugTracCodg().split(",")).forEach(
                        drugTracCodg -> {
                            JSONObject drugtracinfo = new JSONObject();
                            drugtracinfo.put("drug_trac_codg", drugTracCodg); // Mã truy xuất thuốc
                            drugTracInfoArray.add(drugtracinfo);
                        }
                );
            } else {
                JSONObject drugtracinfo = new JSONObject();
                drugtracinfo.put("drug_trac_codg", supplierStock.getDrugTracCodg()); // Mã truy xuất thuốc
                drugTracInfoArray.add(drugtracinfo);
            }
            // Thêm mảng mã truy xuất vào invinfo Trong đối tượng
            invinfo.put("drugtracinfo", drugTracInfoArray);
            // Sẽ  invinfo Thêm vào mảng chính
            array.add(invinfo);
        });
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("invinfoDetail", array);
        return mdRequestUtil.getMedicareData("3502A", jsonObject);
    }

    /**
     * Mua hàng hóa 3503
     *
     * @return resultNút: retRslt 1: Thành công, 0: Thất bại  msgRslt: Lý do thất bại
     */
    @Override
    public JSONObject createPurchaseOrder_3503() {
        JSONObject purcinfo = new JSONObject();
        purcinfo.put("med_list_codg", ""); // Mã danh mục y tế
        purcinfo.put("fixmedins_hist_id", ""); // Mã danh mục của cơ sở y tế được chỉ định
        purcinfo.put("fixmedins_hist_name", ""); // Tên danh mục của cơ sở y tế được chỉ định
        purcinfo.put("dynt_no", ""); // Số phiếu kèm hàng
        purcinfo.put("fixmedins_bchno", ""); // Số thứ tự lô của cơ sở y tế được chỉ định
        purcinfo.put("spler_name", ""); // Tên nhà cung cấp
        purcinfo.put("spler_pmtno", ""); // Số giấy phép nhà cung cấp
        purcinfo.put("manu_lotnum", ""); // Số lô sản xuất
        purcinfo.put("prodentp_name", ""); // Tên nhà sản xuất
        purcinfo.put("aprvno", ""); // Số phê duyệt
        purcinfo.put("manu_date", ""); // Ngày sản xuất
        purcinfo.put("expy_end", ""); // Hết hạn đến
        purcinfo.put("finl_trns_pric", 0.0); // Đơn giá giao dịch cuối cùng
        purcinfo.put("purc_retn_cnt", 0); // Mua hàng/Số lượng trả hàng
        purcinfo.put("purc_invo_codg", ""); // Mã hóa đơn mua hàng
        purcinfo.put("purc_invo_no", ""); // Số hóa đơn mua hàng
        purcinfo.put("rx_flag", ""); // Cờ thuốc kê đơn
        purcinfo.put("purc_retn_stoin_time", ""); // Mua hàng/Thời gian trả hàng nhập kho
        purcinfo.put("purc_retn_opter_name", ""); // Mua hàng/Tên người thực hiện trả hàng
        purcinfo.put("prod_geay_flag", "0"); // Cờ tặng kèm hàng hóa
        purcinfo.put("memo", ""); // Ghi chú
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("purcinfo", purcinfo);
        return mdRequestUtil.getMedicareData("3503", jsonObject);
    }

    /**
     * Mua hàng hóa 3503A
     *
     * @return resultNút: retRslt 1: Thành công, 0: Thất bại  msgRslt: Lý do thất bại
     */
    @Override
    public JSONObject createPurchaseOrderList_3503A() {
        JSONObject purcinfo = new JSONObject();
        purcinfo.put("med_list_codg", ""); // Mã danh mục y tế
        purcinfo.put("fixmedins_hist_id", ""); // Mã danh mục của cơ sở y tế được chỉ định
        purcinfo.put("fixmedins_hist_name", ""); // Tên danh mục của cơ sở y tế được chỉ định
        purcinfo.put("dynt_no", ""); // Số phiếu kèm hàng
        purcinfo.put("fixmedins_bchno", ""); // Số thứ tự lô của cơ sở y tế được chỉ định
        purcinfo.put("spler_name", ""); // Tên nhà cung cấp
        purcinfo.put("spler_pmtno", ""); // Số giấy phép nhà cung cấp
        purcinfo.put("manu_lotnum", ""); // Số lô sản xuất
        purcinfo.put("prodentp_name", ""); // Tên nhà sản xuất
        purcinfo.put("aprvno", ""); // Số phê duyệt
        purcinfo.put("manu_date", ""); // Ngày sản xuất
        purcinfo.put("expy_end", ""); // Hết hạn đến
        purcinfo.put("finl_trns_pric", 0.0); // Đơn giá giao dịch cuối cùng
        purcinfo.put("purc_retn_cnt", 0); // Mua hàng/Số lượng trả hàng
        purcinfo.put("purc_invo_codg", ""); // Mã hóa đơn mua hàng
        purcinfo.put("purc_invo_no", ""); // Số hóa đơn mua hàng
        purcinfo.put("rx_flag", ""); // Cờ thuốc kê đơn
        purcinfo.put("purc_retn_stoin_time", ""); // Mua hàng/Thời gian trả hàng nhập kho
        purcinfo.put("purc_retn_opter_name", ""); // Mua hàng/Tên người thực hiện trả hàng
        purcinfo.put("prod_geay_flag", "0"); // Cờ tặng kèm hàng hóa
        purcinfo.put("memo", ""); // Ghi chú
        JSONArray array = new JSONArray();
        array.add(purcinfo);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("purcinfoDetail", array);
        return mdRequestUtil.getMedicareData("3503A", jsonObject);
    }

    /**
     * Trả hàng mua hàng hóa 3504
     *
     * @return resultNút: retRslt 1: Thành công, 0: Thất bại  msgRslt: Lý do thất bại
     */
    @Override
    public JSONObject createPurchaseReturnOrder_3504() {
        JSONObject purcinfo = new JSONObject();
        purcinfo.put("med_list_codg", ""); // Mã danh mục y tế
        purcinfo.put("fixmedins_hist_id", ""); // Mã danh mục của cơ sở y tế được chỉ định
        purcinfo.put("fixmedins_hist_name", ""); // Tên danh mục của cơ sở y tế được chỉ định
        purcinfo.put("fixmedins_bchno", ""); // Số thứ tự lô của cơ sở y tế được chỉ định
        purcinfo.put("spler_name", ""); // Tên nhà cung cấp
        purcinfo.put("spler_pmtno", ""); // Số giấy phép nhà cung cấp
        purcinfo.put("manu_date", ""); // Ngày sản xuất (Định dạng: yyyy-MM-dd)
        purcinfo.put("expy_end", ""); // Hết hạn đến (Định dạng: yyyy-MM-dd)
        purcinfo.put("finl_trns_pric", 0.0); // Đơn giá giao dịch cuối cùng
        purcinfo.put("purc_retn_cnt", 0); // Mua hàng/Số lượng trả hàng
        purcinfo.put("purc_invo_codg", ""); // Mã hóa đơn mua hàng
        purcinfo.put("purc_invo_no", ""); // Số hóa đơn mua hàng
        purcinfo.put("rx_flag", ""); // Cờ thuốc kê đơn
        purcinfo.put("purc_retn_stoin_time", ""); // Mua hàng/Thời gian trả hàng nhập kho (Định dạng: yyyy-MM-dd HH:mm:ss)
        purcinfo.put("purc_retn_opter_name", ""); // Mua hàng/Tên người thực hiện trả hàng
        purcinfo.put("memo", ""); // Ghi chú
        purcinfo.put("medins_prod_purc_no", ""); // Mua hàng hóasố thứ tự
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("purcinfo", purcinfo);
        return mdRequestUtil.getMedicareData("3504", jsonObject);
    }

    /**
     * Trả hàng mua hàng hóa 3504A
     *
     * @return XuấtNút purcinfoErrDetail
     */
    @Override
    public JSONObject createPurchaseReturnOrderList_3504A() {
        JSONObject purcinfo = new JSONObject();
        purcinfo.put("med_list_codg", ""); // Mã danh mục y tế
        purcinfo.put("fixmedins_hist_id", ""); // Mã danh mục của cơ sở y tế được chỉ định
        purcinfo.put("fixmedins_hist_name", ""); // Tên danh mục của cơ sở y tế được chỉ định
        purcinfo.put("fixmedins_bchno", ""); // Số thứ tự lô của cơ sở y tế được chỉ định
        purcinfo.put("spler_name", ""); // Tên nhà cung cấp
        purcinfo.put("spler_pmtno", ""); // Số giấy phép nhà cung cấp
        purcinfo.put("manu_date", ""); // Ngày sản xuất (Định dạng: yyyy-MM-dd)
        purcinfo.put("expy_end", ""); // Hết hạn đến (Định dạng: yyyy-MM-dd)
        purcinfo.put("finl_trns_pric", 0.0); // Đơn giá giao dịch cuối cùng
        purcinfo.put("purc_retn_cnt", 0); // Mua hàng/Số lượng trả hàng
        purcinfo.put("purc_invo_codg", ""); // Mã hóa đơn mua hàng
        purcinfo.put("purc_invo_no", ""); // Số hóa đơn mua hàng
        purcinfo.put("rx_flag", ""); // Cờ thuốc kê đơn
        purcinfo.put("purc_retn_stoin_time", ""); // Mua hàng/Thời gian trả hàng nhập kho (Định dạng: yyyy-MM-dd HH:mm:ss)
        purcinfo.put("purc_retn_opter_name", ""); // Mua hàng/Tên người thực hiện trả hàng
        purcinfo.put("memo", ""); // Ghi chú
        purcinfo.put("medins_prod_purc_no", ""); // Mua hàng hóasố thứ tự
        JSONArray array = new JSONArray();
        array.add(purcinfo);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("purcinfoDetail", array);
        return mdRequestUtil.getMedicareData("3504A", jsonObject);
    }

    /**
     * Bán hàng hóa 3505
     *
     * @return
     */
    @Override
    public JSONObject createSaleOrder_3505() {
        JSONObject selinfo = new JSONObject();

        selinfo.put("med_list_codg", "");          // Mã danh mục y tế
        selinfo.put("fixmedins_hilist_id", "");    // Mã danh mục của cơ sở y tế được chỉ định
        selinfo.put("fixmedins_hilist_name", "");  // Tên danh mục của cơ sở y tế được chỉ định
        selinfo.put("fixmedins_bchno", "");        // Số thứ tự lô của cơ sở y tế được chỉ định
        selinfo.put("prsc_dr_cert_type", "");      // Loại giấy tờ của bác sĩ kê đơn
        selinfo.put("prsc_dr_certno", "");         // Số giấy tờ của bác sĩ kê đơn
        selinfo.put("prsc_dr_name", "");            // Tên bác sĩ kê đơn
        selinfo.put("phar_cert_type", "");         // Loại giấy tờ của dược sĩ
        selinfo.put("phar_certno", "");            // Số giấy tờ của dược sĩ
        selinfo.put("phar_name", "");               // Tên dược sĩ
        selinfo.put("phar_prac_cert_no", "");      // Số chứng chỉ hành nghề của dược sĩ
        selinfo.put("hi_feesetl_type", "");        // Loại quyết toán chi phí BHYT
        selinfo.put("setl_id", "");                 // Quyết toán ID
        selinfo.put("mdtrt_sn", "");                // Số thứ tự khám bệnh
        selinfo.put("psn_no", "");                  // Mã nhân viên
        selinfo.put("psn_cert_type", "");          // Loại giấy tờ nhân viên
        selinfo.put("certno", "");                  // Số giấy tờ
        selinfo.put("psn_name", "");                // Tên nhân viên
        selinfo.put("manu_lotnum", "");            // Số lô sản xuất
        selinfo.put("manu_date", "");              // Ngày sản xuất
        selinfo.put("expy_end", "");                // Hết hạn đến
        selinfo.put("rx_flag", "");                 // Cờ thuốc kê đơn
        selinfo.put("trdn_flag", "");               // Cờ bán lẻ
        selinfo.put("finl_trns_pric", 0.0);        // Đơn giá giao dịch cuối cùng
        selinfo.put("rxno", "");                    // Số đơn thuốc
        selinfo.put("rx_circ_flag", "");           // ngoàimuaĐơn thuốccờ
        selinfo.put("rtal_docno", "");              // bán lẻđơndữ liệusố
        selinfo.put("stoout_no", "");               // bán hàngXuất khođơndữ liệusố
        selinfo.put("bchno", "");                   // Số lô
        selinfo.put("drug_trac_codg", "");         // Mã truy xuất thuốc
        selinfo.put("drug_prod_barc", "");         // Thuốcmã vạch
        selinfo.put("shelf_posi", "");              // hàngkệvị
        selinfo.put("sel_retn_cnt", 0);             // Bán hàng/Số lượng trả hàng
        selinfo.put("sel_retn_time", "");           // Bán hàng/Thời gian trả hàng
        selinfo.put("sel_retn_opter_name", "");     // Bán hàng/Tên người thực hiện trả hàng
        selinfo.put("memo", "");// Ghi chú
        selinfo.put("MDTRT_SETL_TYPE", "");        // khám bệnhQuyết toánLoại
        selinfo.put("drugtracinfo", "");            // Thông tin nút mã truy xuất

        JSONObject drugtracinfo = new JSONObject();
        drugtracinfo.put("drug_trac_codg","");//Mã truy xuất thuốc

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("selinfo", selinfo);
        jsonObject.put("drugtracinfo", drugtracinfo);
        return mdRequestUtil.getMedicareData("3505", jsonObject);
    }

    /**
     * Bán hàng hóa 3505A
     *
     * @return
     */
    @Override
    public JSONObject createSaleOrderList_3505A() {
        JSONObject selinfo = new JSONObject();
        JSONArray array = new JSONArray();
        selinfo.put("med_list_codg", "");          // Mã danh mục y tế
        selinfo.put("fixmedins_hilist_id", "");    // Mã danh mục của cơ sở y tế được chỉ định
        selinfo.put("fixmedins_hilist_name", "");  // Tên danh mục của cơ sở y tế được chỉ định
        selinfo.put("fixmedins_bchno", "");        // Số thứ tự lô của cơ sở y tế được chỉ định
        selinfo.put("prsc_dr_cert_type", "");      // Loại giấy tờ của bác sĩ kê đơn
        selinfo.put("prsc_dr_certno", "");         // Số giấy tờ của bác sĩ kê đơn
        selinfo.put("prsc_dr_name", "");            // Tên bác sĩ kê đơn
        selinfo.put("phar_cert_type", "");         // Loại giấy tờ của dược sĩ
        selinfo.put("phar_certno", "");            // Số giấy tờ của dược sĩ
        selinfo.put("phar_name", "");               // Tên dược sĩ
        selinfo.put("phar_prac_cert_no", "");      // Số chứng chỉ hành nghề của dược sĩ
        selinfo.put("hi_feesetl_type", "");        // Loại quyết toán chi phí BHYT
        selinfo.put("setl_id", "");                 // Quyết toán ID
        selinfo.put("mdtrt_sn", "");                // Số thứ tự khám bệnh
        selinfo.put("psn_no", "");                  // Mã nhân viên
        selinfo.put("psn_cert_type", "");          // Loại giấy tờ nhân viên
        selinfo.put("certno", "");                  // Số giấy tờ
        selinfo.put("psn_name", "");                // Tên nhân viên
        selinfo.put("manu_lotnum", "");            // Số lô sản xuất
        selinfo.put("manu_date", "");              // Ngày sản xuất
        selinfo.put("expy_end", "");                // Hết hạn đến
        selinfo.put("rx_flag", "");                 // Cờ thuốc kê đơn
        selinfo.put("trdn_flag", "");               // Cờ bán lẻ
        selinfo.put("finl_trns_pric", 0.0);        // Đơn giá giao dịch cuối cùng
        selinfo.put("rxno", "");                    // Số đơn thuốc
        selinfo.put("rx_circ_flag", "");           // ngoàimuaĐơn thuốccờ
        selinfo.put("rtal_docno", "");              // bán lẻđơndữ liệusố
        selinfo.put("stoout_no", "");               // bán hàngXuất khođơndữ liệusố
        selinfo.put("bchno", "");                   // Số lô
        selinfo.put("drug_trac_codg", "");         // Mã truy xuất thuốc
        selinfo.put("drug_prod_barc", "");         // Thuốcmã vạch
        selinfo.put("shelf_posi", "");              // hàngkệvị
        selinfo.put("sel_retn_cnt", 0);             // Bán hàng/Số lượng trả hàng
        selinfo.put("sel_retn_time", "");           // Bán hàng/Thời gian trả hàng
        selinfo.put("sel_retn_opter_name", "");     // Bán hàng/Tên người thực hiện trả hàng
        selinfo.put("memo", "");// Ghi chú
        selinfo.put("MDTRT_SETL_TYPE", "");        // khám bệnhQuyết toánLoại
        selinfo.put("drugtracinfo", "");            // Thông tin nút mã truy xuất
        array.add(selinfo);

        JSONObject drugtracinfo = new JSONObject();
        drugtracinfo.put("drug_trac_codg","");//Mã truy xuất thuốc
        JSONArray array1 = new JSONArray();
        array1.add(drugtracinfo);

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("selinfoDetail", array);
        jsonObject.put("drugtracinfo", array1);

        return mdRequestUtil.getMedicareData("3505A", jsonObject);
    }

    /**
     * Trả hàng bán 3506
     *
     * @return
     */
    @Override
    public JSONObject createSaleReturnOrder_3506() {
        JSONObject selinfo = new JSONObject();
        selinfo.put("med_list_codg", ""); // Mã danh mục y tế
        selinfo.put("fixmedins_hilist_id", ""); // Mã danh mục của cơ sở y tế được chỉ định
        selinfo.put("fixmedins_hilist_name", ""); // Tên danh mục của cơ sở y tế được chỉ định
        selinfo.put("fixmedins_bchno", ""); // Số thứ tự lô của cơ sở y tế được chỉ định
        selinfo.put("setl_id", ""); // Quyết toán ID
        selinfo.put("psn_no", ""); // Mã nhân viên
        selinfo.put("psn_cert_type", ""); // Loại giấy tờ nhân viên
        selinfo.put("certno", ""); // Số giấy tờ
        selinfo.put("psn_name", ""); // Tên nhân viên
        selinfo.put("manu_lotnum", ""); // Số lô sản xuất
        selinfo.put("manu_date", ""); // Ngày sản xuất
        selinfo.put("expy_end", ""); // Hết hạn đến
        selinfo.put("rx_flag", ""); // Cờ thuốc kê đơn
        selinfo.put("trdn_flag", ""); // Cờ bán lẻ
        selinfo.put("finl_trns_pric", ""); // Đơn giá giao dịch cuối cùng
        selinfo.put("sel_retn_cnt", ""); // Bán hàng/Số lượng trả hàng
        selinfo.put("sel_retn_time", ""); // Bán hàng/Thời gian trả hàng
        selinfo.put("sel_retn_opter_name", ""); // Bán hàng/Tên người thực hiện trả hàng
        selinfo.put("memo", ""); // Ghi chú
        selinfo.put("medins_prod_sel_no", ""); // Bán hàng hóasố thứ tự
        selinfo.put("mdtrt_sn", ""); // Số thứ tự khám bệnh
        selinfo.put("drugtracinfo", ""); // Thông tin nút mã truy xuất
        JSONObject drugtracinfo = new JSONObject();
        drugtracinfo.put("drug_trac_codg","");//Mã truy xuất thuốc
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("selinfo", selinfo);
        jsonObject.put("drugtracinfo",drugtracinfo);
        return mdRequestUtil.getMedicareData("3506", jsonObject);
    }

    /**
     * Trả hàng bán 3506A
     *
     * @return
     */
    @Override
    public JSONObject createSaleReturnOrderList_3506A() {
        JSONObject selinfo = new JSONObject();
        JSONArray array  = new JSONArray();
        selinfo.put("med_list_codg", ""); // Mã danh mục y tế
        selinfo.put("fixmedins_hilist_id", ""); // Mã danh mục của cơ sở y tế được chỉ định
        selinfo.put("fixmedins_hilist_name", ""); // Tên danh mục của cơ sở y tế được chỉ định
        selinfo.put("fixmedins_bchno", ""); // Số thứ tự lô của cơ sở y tế được chỉ định
        selinfo.put("setl_id", ""); // Quyết toán ID
        selinfo.put("psn_no", ""); // Mã nhân viên
        selinfo.put("psn_cert_type", ""); // Loại giấy tờ nhân viên
        selinfo.put("certno", ""); // Số giấy tờ
        selinfo.put("psn_name", ""); // Tên nhân viên
        selinfo.put("manu_lotnum", ""); // Số lô sản xuất
        selinfo.put("manu_date", ""); // Ngày sản xuất
        selinfo.put("expy_end", ""); // Hết hạn đến
        selinfo.put("rx_flag", ""); // Cờ thuốc kê đơn
        selinfo.put("trdn_flag", ""); // Cờ bán lẻ
        selinfo.put("finl_trns_pric", ""); // Đơn giá giao dịch cuối cùng
        selinfo.put("sel_retn_cnt", ""); // Bán hàng/Số lượng trả hàng
        selinfo.put("sel_retn_time", ""); // Bán hàng/Thời gian trả hàng
        selinfo.put("sel_retn_opter_name", ""); // Bán hàng/Tên người thực hiện trả hàng
        selinfo.put("memo", ""); // Ghi chú
        selinfo.put("medins_prod_sel_no", ""); // Bán hàng hóasố thứ tự
        selinfo.put("mdtrt_sn", ""); // Số thứ tự khám bệnh
        selinfo.put("drugtracinfo", ""); // Thông tin nút mã truy xuất
        array.add(selinfo);
        JSONObject drugtracinfo = new JSONObject();
        drugtracinfo.put("drug_trac_codg","");//Mã truy xuất thuốc
        JSONArray array1 = new JSONArray();
        array1.add(drugtracinfo);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("selinfoDetail", array);
        jsonObject.put("drugtracinfo",array1);
        return mdRequestUtil.getMedicareData("3506A", jsonObject);
    }

    /**
     * Xóa thông tin hàng hóa 3507
     * Xóa thông tin một lô hàng hóa nào đó qua giao dịch này
     *
     * @return
     */
    @Override
    public JSONObject removeProductRecord_3507() {
        JSONObject data =  new JSONObject();
        data.put("fixmedins_bchno","");
        data.put("inv_data_type","");
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("data", data);
        return mdRequestUtil.getMedicareData("3507", jsonObject);
    }

    /**
     * Xóa thông tin hàng hóa 3507A
     * Xóa thông tin một lô hàng hóa nào đó qua giao dịch này
     *
     * @return
     */
    @Override
    public JSONObject removeProductRecordList() {
        JSONObject data =  new JSONObject();
        data.put("fixmedins_bchno","");
        data.put("inv_data_type","");
        JSONArray array = new JSONArray();
        array.add(data);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("goodDetail", array);
        return mdRequestUtil.getMedicareData("3507", jsonObject);

    }

    /**
     * hàng hóaTồn khoThông tinTruy vấn 3508
     *
     * @return
     */
    @Override
    public JSONObject getInventoryInfo_3508() {
        JSONObject data = new JSONObject();
        data.put("fixmedins_code", ""); // Mã cơ sở y tế được chỉ định
        data.put("medins_list_codg", ""); // Mã danh mục của cơ sở y tế
        data.put("fixmedins_bchno", ""); // Số thứ tự lô của cơ sở y tế được chỉ định
        data.put("begndate", ""); // Ngày bắt đầu
        data.put("enddate", ""); // Ngày kết thúc
        data.put("medins_prod_inv_no", ""); // y dược được chỉ địnhCơ sởhàng hóaTồn khosố thứ tự
        data.put("med_list_codg", ""); // Mã danh mục y tế
        data.put("medins_list_name", ""); // Tên danh mục của cơ sở y tế
        data.put("rx_flag", ""); // Cờ thuốc kê đơn
        data.put("list_sp_item_flag", ""); // Cờ hạng mục đặc biệt trong danh mục
        data.put("trdn_flag", ""); // Cờ bán lẻ
        data.put("invdate", ""); // Tồn khoNgày
        data.put("manu_lotnum", ""); // Số lô sản xuất
        data.put("manu_date", ""); // Ngày sản xuất
        data.put("expy_end", ""); // Hết hạn đến
        data.put("memo", ""); // Ghi chú
        data.put("vali_flag", ""); // Cờ hiệu lực
        data.put("rid", ""); // Số bản ghi duy nhất của dữ liệu
        data.put("crter_id", ""); // Người tạo ID
        data.put("crter_name", ""); // Tên người tạo
        data.put("crte_optins_no", ""); // Mã cơ sở tạo
        data.put("opter_id", ""); // Người thực hiện ID
        data.put("opter_name", ""); // Tên người thực hiện
        data.put("optins_no", ""); // Mã cơ sở thực hiện
        data.put("poolarea_no", ""); // Mã vùng điều phối
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("data", data);
        return mdRequestUtil.getMedicareData("3508", jsonObject);
    }

    /**
     * Tồn khothay đổiBản ghiTruy vấn 3509
     *
     * @return
     */
    @Override
    public JSONObject getInventoryChangeRecords_3509() {
        JSONObject data = new JSONObject();
        data.put("fixmedins_code", ""); // Mã cơ sở y tế được chỉ định
        data.put("medins_list_codg", ""); // Mã danh mục của cơ sở y tế
        data.put("fixmedins_bchno", ""); // Số thứ tự lô của cơ sở y tế được chỉ định
        data.put("begndate", ""); // Ngày bắt đầu
        data.put("enddate", ""); // Ngày kết thúc
        data.put("medins_prod_inv_chg_no", ""); // y dược được chỉ địnhCơ sởhàng hóaTồn khothay đổisố thứ tự
        data.put("med_list_codg", ""); // Mã danh mục y tế
        data.put("inv_chg_type", ""); // Loại thay đổi tồn kho
        data.put("medins_list_name", ""); // Tên danh mục của cơ sở y tế
        data.put("rx_flag", ""); // Cờ thuốc kê đơn
        data.put("list_sp_item_flag", ""); // Cờ hạng mục đặc biệt trong danh mục
        data.put("trdn_flag", ""); // Cờ bán lẻ
        data.put("inv_chg_time", ""); // Thời gian thay đổi tồn kho
        data.put("inv_chg_opter_name", ""); // Tên người thực hiện thay đổi tồn kho
        data.put("memo", ""); // Ghi chú
        data.put("vali_flag", ""); // Cờ hiệu lực
        data.put("rid", ""); // Số bản ghi duy nhất của dữ liệu
        data.put("crter_id", ""); // Người tạo ID
        data.put("crter_name", ""); // Tên người tạo
        data.put("crte_optins_no", ""); // Mã cơ sở tạo
        data.put("opter_id", ""); // Người thực hiện ID
        data.put("opter_name", ""); // Tên người thực hiện
        data.put("optins_no", ""); // Mã cơ sở thực hiện
        data.put("poolarea_no", ""); // Mã vùng điều phối
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("data", data);
        return mdRequestUtil.getMedicareData("3509", jsonObject);
    }

    /**
     * Mua hàngBản ghiTruy vấn 3510
     *
     * @return
     */
    @Override
    public JSONObject getProcurementRecords_3510() {
        JSONObject data = new JSONObject();
        data.put("fixmedins_code", ""); // Mã cơ sở y tế được chỉ định
        data.put("medins_list_codg", ""); // Mã danh mục của cơ sở y tế
        data.put("fixmedins_bchno", ""); // Số thứ tự lô của cơ sở y tế được chỉ định
        data.put("begndate", ""); // Ngày bắt đầu
        data.put("enddate", ""); // Ngày kết thúc
        data.put("medins_prod_purc_no", ""); // y dược được chỉ địnhCơ sởMua hàng hóasố thứ tự
        data.put("med_list_codg", ""); // Mã danh mục y tế
        data.put("medins_list_name", ""); // Tên danh mục của cơ sở y tế
        data.put("dynt_no", ""); // Số phiếu kèm hàng
        data.put("spler_name", ""); // cung cấphàngnhà cung cấpTên
        data.put("spler_pmtno", ""); // Số giấy phép nhà cung cấp
        data.put("manu_lotnum", ""); // Số lô sản xuất
        data.put("prodentp_name", ""); // sinhsảndoanh nghiệpnghiệp vụTên
        data.put("aprvno", ""); // Số phê duyệt
        data.put("manu_date", ""); // Ngày sản xuất
        data.put("expy_end", ""); // Hết hạn đến
        data.put("purc_invo_codg", ""); // Mã hóa đơn mua hàng
        data.put("purc_invo_no", ""); // Số hóa đơn mua hàng
        data.put("rx_flag", ""); // Cờ thuốc kê đơn
        data.put("list_sp_item_flag", ""); // Cờ hạng mục đặc biệt trong danh mục
        data.put("purc_retn_stoin_time", ""); // Mua hàng/Thời gian trả hàng nhập kho
        data.put("purc_retn_opter_name", ""); // Mua hàng/Tên người thực hiện trả hàng
        data.put("prod_geay_flag", ""); // Cờ tặng kèm hàng hóa
        data.put("prod_retn_flag", ""); // hàng hóatrảhàngcờ
        data.put("memo", ""); // Ghi chú
        data.put("vali_flag", ""); // Cờ hiệu lực
        data.put("rid", ""); // Số bản ghi duy nhất của dữ liệu
        data.put("crter_id", ""); // Người tạo ID
        data.put("crter_name", ""); // Tên người tạo
        data.put("crte_optins_no", ""); // Mã cơ sở tạo
        data.put("opter_id", ""); // Người thực hiện ID
        data.put("opter_name", ""); // Tên người thực hiện
        data.put("optins_no", ""); // Mã cơ sở thực hiện
        data.put("poolarea_no", ""); // Mã vùng điều phối
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("data", data);
        return mdRequestUtil.getMedicareData("3510", jsonObject);
    }

    /**
     * bán hàngBản ghiTruy vấn 3511
     *
     * @return
     */
    @Override
    public JSONObject getSalesRecords_3511() {
        JSONObject data = new JSONObject();
        data.put("fixmedins_code", ""); // Mã cơ sở y tế được chỉ định
        data.put("medins_list_cod", ""); // Mã danh mục của cơ sở y tế
        data.put("fixmedins_bchno", ""); // Số thứ tự lô của cơ sở y tế được chỉ định
        data.put("begndate", ""); // Ngày bắt đầu
        data.put("enddate", ""); // Ngày kết thúc
        data.put("medins_prod_sel_no", ""); // y dược được chỉ địnhCơ sởBán hàng hóasố thứ tự
        data.put("med_list_codg", ""); // Mã danh mục y tế
        data.put("medins_list_name", ""); // Tên danh mục của cơ sở y tế
        data.put("prsc_dr_cert_type", ""); // lập đơnBác sĩLoại giấy tờ
        data.put("prsc_dr_certno", ""); // lập đơnBác sĩgiấy tờsố
        data.put("bilg_dr_name", ""); // lập đơnBác sĩTên
        data.put("phar_cert_type", ""); // Loại giấy tờ của dược sĩ
        data.put("phar_certno", ""); // Số giấy tờ của dược sĩ
        data.put("phar_name", ""); // Tên dược sĩ
        data.put("phar_prac_cert_no", ""); // Số chứng chỉ hành nghề của dược sĩ
        data.put("hi_feesetl_type", ""); // Loại quyết toán chi phí BHYT
        data.put("setl_id", ""); // Quyết toán ID
        data.put("psn_no", ""); // Mã nhân viên
        data.put("psn_cert_type", ""); // Loại giấy tờ nhân viên
        data.put("certno", ""); // Số giấy tờ
        data.put("psn_name", ""); // Tên nhân viên
        data.put("manu_lotnum", ""); // Số lô sản xuất
        data.put("manu_date", ""); // Ngày sản xuất
        data.put("expy_end", ""); // Hết hạn đến
        data.put("elec_supn_codg", ""); // điện tửgiám sátquản lýMã
        data.put("rx_flag", ""); // Cờ thuốc kê đơn
        data.put("list_sp_item_flag", ""); // Cờ hạng mục đặc biệt trong danh mục
        data.put("trdn_flag", ""); // Cờ bán lẻ
        data.put("sel_retn_time", ""); // Bán hàng/Thời gian trả hàng
        data.put("sel_retn_opter_name", ""); // Bán hàng/Tên người thực hiện trả hàng
        data.put("memo", ""); // Ghi chú
        data.put("vali_flag", ""); // Cờ hiệu lực
        data.put("rid", ""); // Số bản ghi duy nhất của dữ liệu
        data.put("crter_id", ""); // Người tạo ID
        data.put("crter_name", ""); // Tên người tạo
        data.put("crte_optins_no", ""); // Mã cơ sở tạo
        data.put("opter_id", ""); // Người thực hiện ID
        data.put("opter_name", ""); // Tên người thực hiện
        data.put("optins_no", ""); // Mã cơ sở thực hiện
        data.put("poolarea_no", ""); // Mã vùng điều phối
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("data", data);
        return mdRequestUtil.getMedicareData("3511", jsonObject);
    }

    /**
     * nhập khoThuốctruy xuấtThông tinTruy vấn   3512
     *
     * @return
     */
    @Override
    public JSONObject getInboundDetails_3512() {
        JSONObject data = new JSONObject();
        data.put("fixmedins_code", ""); // Mã cơ sở y tế được chỉ định
        data.put("medins_list_codg", ""); // Mã danh mục của cơ sở y tế
        data.put("fixmedins_bchno", ""); // Số thứ tự lô của cơ sở y tế được chỉ định
        data.put("begndate", ""); // Ngày bắt đầu, Định dạng: yyyy-MM-dd
        data.put("enddate", ""); // Ngày kết thúc, Định dạng: yyyy-MM-dd
        data.put("med_list_codg", ""); // Mã danh mục y tế
        data.put("drug_trac_codg", ""); // Mã truy xuất thuốc
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("data", data);
        return mdRequestUtil.getMedicareData("3512", jsonObject);
    }

    /**
     * bán hàngThuốctruy xuấtThông tinTruy vấn   3513
     *
     * @return
     */
    @Override
    public JSONObject getSalesDetails_3513() {
        JSONObject data = new JSONObject();
        data.put("fixmedins_code", "");  // Mã cơ sở y tế được chỉ định (Bắt buộc)
        data.put("medins_list_codg", ""); // Mã danh mục của cơ sở y tế (Tùy chọn điền)
        data.put("fixmedins_bchno", "");  // Số thứ tự lô của cơ sở y tế được chỉ định (Tùy chọn điền)
        data.put("begndate", "");          // Ngày bắt đầu (Tùy chọn điền)
        data.put("enddate", "");            // Ngày kết thúc (Tùy chọn điền)
        data.put("med_list_codg", "");     // Mã danh mục y tế (Tùy chọn điền)
        data.put("mdtrt_id", "");           // Khám bệnh ID (Tùy chọn điền)
        data.put("psn_no", "");             // Mã nhân viên (Tùy chọn điền)
        data.put("psn_cert_type", "");      // Loại giấy tờ nhân viên (Bắt buộc)
        data.put("certno", "");             // Số giấy tờ (Tùy chọn điền)
        data.put("psn_name", "");           // Tên nhân viên (Tùy chọn điền)
        data.put("drug_trac_codg", "");     // Mã truy xuất thuốc (Tùy chọn điền)
        data.put("sel_retn_opter_name", ""); // Bán hàng/Tên người thực hiện trả hàng (Tùy chọn điền)
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("data", data);
        return mdRequestUtil.getMedicareData("3513", jsonObject);
    }
}
