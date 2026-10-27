package com.geeke.medicareutils.domain.reqpo;

import lombok.Data;

/**
 * BHYTgiao diệnchínhVui lòngthân yêu cầu
 */
@Data
public class RequestData {
    // infno: giao dịchThông tinmã số, Dùng để định danh duy nhất một giao dịch
    private String infno;

    // msgid: thông báomã số, Dùng để định danh duy nhất mỗiCáithông báo(thông thườngbao gồmCơ sởmã số, Thời giandấu và ngẫu nhiênsố)
    private String msgid;

    // mdtrtarea_admvs: thìyđịa phươngBHYTQuận划, cho biếtthìyđịa phươngđiểm của BHYTQuậnvùngmã, 6ký tự
    private String mdtrtarea_admvs;

    // insuplc_admdvs: Vùng BHYT nơi tham gia bảo hiểm, cho biếttham gia bảo hiểmngười của BHYTQuậnvùngmã, 6ký tự
    private String insuplc_admdvs;

    // recer_sys_code: Mã hệ thống bên nhận, Dùng để định danh bên nhận của Hệ thống, thông thường là 10ký tự
    private String recer_sys_code;

    // dev_no: Mã thiết bị, dùng đểđịnh danhđặt备 của duy nhấtmã số, Tối đa100Ký tự
    private String dev_no;

    // dev_safe_info: Thông tin an toàn thiết bị, dùng đểlưu trữđặt备 của an toàntươngđóngThông tin, Tối đa2000Ký tự
    private String dev_safe_info;

    // cainfo: CAchứng书Thông tin, lưu trữ và an toànchứng书tươngđóng của chuỗi, NếuKhông cóthì là trốngchuỗi
    private String cainfo;

    // signtype: Loại chữ ký, chỉchỉ địnhnơisử dụng của chữ kýthuật toánLoại, thông thường là SM2, SM3 v.v. 
    private String signtype;

    // infver: Số phiên bản giao diện, cho biếtHiện tạigiao diệnphiên bản của chuỗi, thông thường có 6ký tự, Ví dụ "V1.0"
    private String infver;

    // opter_type: Người thực hiệnLớploại, Cho biết nhân viên thao tácLoại của định danh, Ví dụ"Y1"cho biếtNgười thực hiện, "Y2"cho biết终đầuthao tác v.v. 
    private String opter_type;

    // opter: Người thực hiện hoặc thao tácnhân viên, Xác định mã nhân viên hoặc mã thiết bị đầu cuối thực hiện giao dịch này, Tối đa30Ký tự
    private String opter;

    // opter_name: Tên người thực hiện, Cho biết nhân viên thao tác của tênchữ
    private String opter_name;

    // inf_time: giao dịchThời gian, cho biếtgiao dịchphátsinh của cụ thểThời gian, định dạng là  "yyyy-MM-dd HH:mm:ss"
    private String inf_time;

    // fixmedins_code: Mã cơ sở y tế được chỉ định, cho biếty tếCơ sở của duy nhấtmã số, Tối đa30Ký tự
    private String fixmedins_code;

    // fixmedins_name: y dược được chỉ địnhCơ sởTên, cho biếty tếCơ sở của Tên, Tối đa200Ký tự
    private String fixmedins_name;

    // sign_no: giao dịchkýĐếnsố thứ tự, duy nhấtđịnh danhmột lầnkýĐếngiao dịch, thông thường có QuakýĐếngiao dịchLấy được của , Tối đa30Ký tự
    private String sign_no;

}
