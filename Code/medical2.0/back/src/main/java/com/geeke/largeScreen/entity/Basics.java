package com.geeke.largeScreen.entity;

import lombok.Data;


/**
 * lớnmàn hìnhcơ bảnDữ liệu
 * @author zhanghan
 * @version 2023-09-05
 */
@Data
public class Basics {
	/** Thêm tác vụ mớisố */
	private String quest;

	/** Số người đăng ký khám hôm nay */
	private String register;

	/** Số lượt tiếp nhận hôm nay */
	private String receivePatients;

	/** Thu nhập hôm nay */
	private String income;

	/** Số lượt ký mới hôm nay */
	private String signing;

}
