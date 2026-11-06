package com.geeke.common.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import com.geeke.common.service.ServiceException;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * ServiceLớp cơ sở
 * @author lys
 * @version 2014-05-16
 */
@Transactional(readOnly = true)
public abstract class BaseService {

	/**
	 * Ngàynhật kýĐối tượng
	 */
	protected static final Logger logger = LoggerFactory.getLogger(BaseService.class);

	/**
	 * TạokèmNgàytiền tố của tựtăngmã số
	 * Định dạng: yyyyMMdd + 7vịtựtăngsố thứ tự(Như  202606170000001)
	 *
	 * @param lastCode Dữ liệukho trong cuối cùng mộtmụcBản ghi của mã số, có thể là null hoặc trống
	 * @return mớimã số
	 */
	protected static String generateDatePrefixCode(String lastCode) {
		String datePrefix = new SimpleDateFormat("yyyyMMdd").format(new Date());
		if (lastCode == null || lastCode.length() < 9) {
			return datePrefix + "0000001";
		}
		// DS98: a latest code whose 7-char suffix is not numeric (bad
		// legacy/manual row) NFE'd -> 500 on every new slip. Fail closed
		// 400 so the bad code row is fixed instead of failing silently.
		int increment;
		try {
			increment = Integer.parseInt(lastCode.substring(8)) + 1;
		} catch (NumberFormatException e) {
			throw new ServiceException("Mã phiếu gần nhất không hợp lệ: " + lastCode);
		}
		String incrementStr = String.valueOf(increment);
		StringBuilder prefix = new StringBuilder(datePrefix);
		for (int i = 0; i < 7 - incrementStr.length(); i++) {
			prefix.append("0");
		}
		return prefix.append(incrementStr).toString();
	}

}
