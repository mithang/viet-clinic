package com.geeke.common.service;

/**
 * Servicetầngchungdùng của Exception, Từ doSpringKhi ném ra trong hàm quản lý giao dịch sẽ kích hoạt rollback giao dịch.
 * @author lys
 */
public class ServiceException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public ServiceException() {
		super();
	}

	public ServiceException(String message) {
		super(message);
	}

	public ServiceException(Throwable cause) {
		super(cause);
	}

	public ServiceException(String message, Throwable cause) {
		super(message, cause);
	}
}
