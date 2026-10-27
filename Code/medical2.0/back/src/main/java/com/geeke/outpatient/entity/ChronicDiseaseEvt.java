package com.geeke.outpatient.entity;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.geeke.utils.SessionUtils;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Đơn thuốc bệnh mạn tínhChi tiếtEntity
 * @author txl
 * @version 2022-06-07
 */
@Data
public class ChronicDiseaseEvt implements Serializable {

	/**
	 * Loại phí
	 */
	private String febbe = "Loại phí: thông thườngbệnhngười";
	/**
	 * Tên bệnh nhân
	 */
	private String name;
	/**
	 * Giới tính
	 */
	private String gender;
	/**
	 * Tuổi
	 */
	private String age;
	/**
	 * Nhiệt độ cơ thể
	 */
	private String temperature;
	/**
	 * ngoại trú/Nhập việnbệnh ánsố, lấyĐăng ký khámđơnsố
	 */
	private String registrationId;
	/**
	 * Khoa/bệnhQuận/giườngvịsố, lấyKhoa
	 */
	private String clinicOffice;
	/**
	 * Chẩn đoán lâm sàng, lấyTâythuốcChẩn đoán
	 */
	private String westernDiagnose;
	/**
	 * khácChẩn đoán, lấy trong thuốcChẩn đoán
	 */
	private String chinaDiagnose;
	/**
	 * vàphátBệnh, Dữ liệutrước là trống
	 */
	private String concurrentDisease="vàphátBệnh: ";
	/**
	 * Ghi chú, lấyY lệnhviệchạng mục
	 */
	private String doctorAdvice;
	/**
	 * kêNgày, lấymedicalRecord của tạoThời gian
	 */
	private String createDate;
	/**
	 * Bác sĩ
	 */
	private String doctor;
	/**
	 * Số tiền thuốc
	 */
	private String amount;
	/**
	 * Nhân viên thu phí
	 */
	private String tollCollector = "Nhân viên thu phí: ";
	/**
	 * 注射phí
	 */
	private String injectionFee;
	/**
	 * Duyệt/Pha chế
	 */
	private String deploy = "Duyệt/Pha chế: ";
	/**
	 * Đối chiếuphát thuốc
	 */
	private String dispensing = "Đối chiếu/Phát thuốc: ";
	/**
	 * lớnsố tiềnĐơn thuốcBệnh nhâný kiến
	 */
	private String patientOpinion = "lớnsố tiềnĐơn thuốcBệnh nhâný kiến: cùngý khác nhauý";
	/**
	 * Bệnh nhânchữ ký
	 */
	private String signature = "Bệnh nhânchữ ký: ";
	/**
	 * liên hệĐịa chỉ và Số điện thoại
	 */
	private String address;
	/**
	 * Bệnh việnTên
	 */
	private String clinicName;
	/**
	 * Tiểu loại đơn thuốc, chậmbệnhraphát, thông thườngĐơn thuốc
	 */
	private String smallType ="Đơn thuốc bệnh mạn tính";

	private String tips = "(Thu phíphiếudữ liệuVui lòng贴附Đơn thuốc背mặt)";
	public ChronicDiseaseEvt(Patient patient, Registration registration,
							 MedicalRecord medicalRecord,RecipelInfo recipelInfo,
							 List<RecipelDetail> detailList) {
		this.buildPatient(patient);
		this.buildRegistration(registration);
		this.buildMedicalRecord(medicalRecord);
		this.buildRecipelInfo(recipelInfo);
		this.buildRecipelDetail(detailList);
	}

	private void buildRecipelDetail(List<RecipelDetail> detailList) {
		if (CollUtil.isNotEmpty(detailList)) {
			BigDecimal injectionFeeSum = detailList.stream()
					.filter(detail -> detail.getIsExtra() == 1)
					.map(RecipelDetail::getAllFee)
					.reduce(BigDecimal.ZERO, BigDecimal::add);
			this.injectionFee = "Phí phụ thêm: ￥" + injectionFeeSum;
			BigDecimal amountSum = detailList.stream()
					.filter(detail -> detail.getIsExtra() == 0)
					.map(RecipelDetail::getAllFee)
					.reduce(BigDecimal.ZERO, BigDecimal::add);
			this.amount = "Số tiền thuốc: ￥" + amountSum;
		}else {
			this.injectionFee = "Phí phụ thêm: ￥ 0";
			this.amount = "Số tiền thuốc: ￥ 0";
		}
	}

	private void buildRecipelInfo(RecipelInfo recipelInfo) {
		this.smallType = recipelInfo.getChronicDisease() ? "Đơn thuốc bệnh mạn tính" : "thông thườngĐơn thuốc";
		this.doctorAdvice = "Ghi chú: " + (StrUtil.isBlank(recipelInfo.getEntrust()) ? "" : recipelInfo.getEntrust());
	}

	private void buildPatient(Patient patient){
		this.name = "Tên bệnh nhân: " + patient.getName();
		this.gender = "Giới tính: " + patient.getGender().getName();
		this.age = "Tuổi: " + (StrUtil.isBlank(patient.getAge()) ? "" : patient.getAge());
		this.address = "Số điện thoại liên hệ hoặc Địa chỉ(Bệnh nhântự愿điền): " + patient.getAddress();
		this.clinicName = patient.getCompany().getName()+"Đơn thuốcgiấy";
	}
	private void buildRegistration(Registration registration){
		this.registrationId ="ngoại trú/Nhập việnbệnh ánsố: "+ registration.getId();
//		this.clinicOffice = "Khoa/bệnhQuận/giườngvịsố: "+registration.getClinicOffice().getName();
		this.clinicOffice = "Khoa: "+registration.getClinicOffice().getName();
		this.temperature = "Nhiệt độ cơ thể: " + (StrUtil.isBlank(registration.getTemperature()) ? "" : registration.getTemperature())+"℃";
	}

	private void buildMedicalRecord(MedicalRecord medicalRecord){
		this.westernDiagnose = "Chẩn đoán tây y: " + medicalRecord.getWesternDiagnose();
		this.chinaDiagnose = "Chẩn đoán đông y: " + medicalRecord.getChinaDiagnose();
		this.createDate = "kêNgày: " + DateUtil.formatDateTime(medicalRecord.getCreateDate());
		this.doctor = "Bác sĩ: " + medicalRecord.getDoctor().getName();
	}
	public Map<String, Object> toMap() {
		Map<String, Object> map = new HashMap<>();
		map.put("febbe", febbe);
		map.put("name", name);
		map.put("gender", gender);
		map.put("age", age);
		map.put("temperature", temperature);
		map.put("registrationId", registrationId);
		map.put("clinicOffice", clinicOffice);
		map.put("westernDiagnose", westernDiagnose);
		map.put("chinaDiagnose", chinaDiagnose);
		map.put("concurrentDisease", concurrentDisease);
		map.put("doctorAdvice", doctorAdvice);
		map.put("createDate", createDate);
		map.put("doctor", doctor);
		map.put("amount", amount);
		map.put("tollCollector", tollCollector);
		map.put("injectionFee", injectionFee);
		map.put("deploy", deploy);
		map.put("dispensing", dispensing);
		map.put("patientOpinion", patientOpinion);
		map.put("signature", signature);
		map.put("address", address);
		map.put("clinicName", clinicName);
		map.put("smallType", smallType);
		map.put("tips", tips);
		return map;
	}

}