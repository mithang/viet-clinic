package com.geeke.gen.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * mãTạocấu hìnhLớp
 * 
 * @author lys
 * @date 2019/09/1
 */
@Component
public class GenConfigure {

	/* mãTạo của đường dẫncấu hình */
	public static String rootPath;
	
	/* Dữ liệukhoLoạicấu hìnhcấu hình */
	public static String dbName;
	
	@Value(value = "${genConfigure.rootPath:c:\\}")
	public void setRootPath(String path) {
		GenConfigure.rootPath = path;
	}
	
	@Value(value = "${genConfigure.dbName:mysql}")
	public void setDbName(String name) {
		GenConfigure.dbName = name;
	}
}
