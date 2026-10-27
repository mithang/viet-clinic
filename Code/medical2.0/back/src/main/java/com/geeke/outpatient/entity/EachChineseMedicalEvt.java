package com.geeke.outpatient.entity;

import lombok.Data;

import java.io.Serializable;

/**
 * Chi tiết đơn thuốcEntity
 * @author txl
 * @version 2022-06-07
 */
@Data
public class EachChineseMedicalEvt implements Serializable {

	private static final long serialVersionUID = 1014474470772900087L;
	//Tên1
	private String nameOne;
	//Tên2
	private String nameTwo;
	//Tên3
	private String nameThree;
	//Tên4
	private String nameFour;
}