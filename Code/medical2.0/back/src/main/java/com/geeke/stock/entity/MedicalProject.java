package com.geeke.stock.entity;

import com.geeke.basicdata.entity.ManufactureFactory;
import com.geeke.sys.entity.DictItem;
import lombok.Data;

import java.math.BigDecimal;

/**
 * Thông tin vật tưEntity
 * @author txl
 * @version 2022-06-22
 */
@Data
public class MedicalProject
{
	private String id;
	private String name;
	private String pinyinCode;        // Mã pinyin
	private ManufactureFactory factory;      // Nhà sản xuất
	private String specifications;        // Quy cách
	private Integer packNumber;        // Số lượng đóng gói
	private DictItem minUnit;      // Đơn vị nhỏ nhất
	private DictItem packUnit;      // Đơn vị đóng gói
	private BigDecimal priceOutSell;        // Giá bán ra ngoài
	private String isUnpackSell;        // Cho phép bán lẻ
	private BigDecimal retailPrice;        // Giá bán lẻ sau khi tách
	private String stuffType;		// Loại vật liệu
	private int inventory;  //Tồn kho
	private DictItem dosisUnit; //Đơn vị
	private DictItem pack;//đóng góiLớp
}