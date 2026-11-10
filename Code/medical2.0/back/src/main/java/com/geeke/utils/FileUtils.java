package com.geeke.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

/**
 * Tệpthao tácLớp công cụ
 * Triển khaiTệp của tạo, Xóa, Sao chép, nén, Giải nén và thư mục của tạo, Xóa, Sao chép, néngiải nén v.v. chức năng
 * @author
 * @version 2015-3-16
 */
public class FileUtils extends org.apache.commons.io.FileUtils {
	
	private static Logger logger = LoggerFactory.getLogger(FileUtils.class);

	/**
	 * Sao chép một tệp, NếuđíchTệpTồn tại, thìkhôngghi đè
	 * @param srcFileName chờsao chép của Tên tệp
	 * @param descFileName đíchTên tệp
	 * @return Nếusao chépThành công, Thì trả vềtrue, Nếu không thì trả vềfalse
	 */
	public static boolean copyFile(String srcFileName, String descFileName) {
		return FileUtils.copyFileCover(srcFileName, descFileName, false);
	}

	/**
	 * Sao chép một tệp
	 * @param srcFileName chờsao chép của Tên tệp
	 * @param descFileName đíchTên tệp
	 * @param coverlay NếuđíchTệpĐã tồn tại, Có hay khôngghi đè
	 * @return Nếusao chépThành công, Thì trả vềtrue, Nếu không thì trả vềfalse
	 */
	public static boolean copyFileCover(String srcFileName,
			String descFileName, boolean coverlay) {
		File srcFile = new File(srcFileName);
		// Kiểm tranguồnTệpCó tồn tại không
		if (!srcFile.exists()) {
			logger.debug("Sao chép tệp thất bại, nguồn Tệp" + srcFileName + " Không tồn tại!");
			return false;
		}
		// Kiểm tranguồnTệpCó hay không có hợp lệ của Tệp
		else if (!srcFile.isFile()) {
			logger.debug("Sao chép tệp thất bại, " + srcFileName + "không có một Cái Tệp!");
			return false;
		}
		File descFile = new File(descFileName);
		// Kiểm trađíchTệpCó tồn tại không
		if (descFile.exists()) {
			// NếuđíchTệpTồn tại, vàvàcho phépghi đè
			if (coverlay) {
				logger.debug("đích Tệp Đã tồn tại, chuẩn bị Xóa!");
				if (!FileUtils.delFile(descFileName)) {
					logger.debug("Xóa đích Tệp" + descFileName + " Thất bại!");
					return false;
				}
			} else {
				logger.debug("Sao chép tệp thất bại, đích Tệp" + descFileName + " Đã tồn tại!");
				return false;
			}
		} else {
			if (!descFile.getParentFile().exists()) {
				// NếuđíchTệpnơi của danh mụcKhông tồn tại, Thì tạo thư mục
				logger.debug("đích Tệp nơi của danh mục Không tồn tại, Tạo thư mục!");
				// tạođíchTệpnơi của danh mục
				if (!descFile.getParentFile().mkdirs()) {
					logger.debug("tạo đích Tệp nơi của danh mục Thất bại!");
					return false;
				}
			}
		}

		// chuẩn bịsao chépTệp
		// đọc của vịsố
		int readByte = 0;
		InputStream ins = null;
		OutputStream outs = null;
		try {
			// mởnguồnTệp
			ins = new FileInputStream(srcFile);
			// mởđíchTệp của Luồng xuất
			outs = new FileOutputStream(descFile);
			byte[] buf = new byte[1024];
			// một lầnđọc1024byte, khireadByte là -1 giờcho biếtTệpĐãđọcxong
			while ((readByte = ins.read(buf)) != -1) {
				// Sẽ đọc của byteluồngghiĐếnLuồng xuất
				outs.write(buf, 0, readByte);
			}
			logger.debug("Sao chép một tệp " + srcFileName + " Đến" + descFileName
					+ "Thành công!");
			return true;
		} catch (Exception e) {
			logger.debug("Sao chép tệp thất bại: " + e.getMessage());
			return false;
		} finally {
			// đóngNhậpLuồng xuất, đầu tiêntrướcđóngLuồng xuất, nhưngsaulạiđóngNhậpluồng
			if (outs != null) {
				try {
					outs.close();
				} catch (IOException oute) {
					logger.error("đóng Luồng xuất Thất bại", oute);
				}
			}
			if (ins != null) {
				try {
					ins.close();
				} catch (IOException ine) {
					logger.error("đóng Nhập luồng Thất bại", ine);
				}
			}
		}
	}

	/**
	 * sao chépnguyênCáidanh mục của nội dung, Nếuthư mục đíchTồn tại, thìkhôngghi đè
	 * @param srcDirName nguồndanh mụctên
	 * @param descDirName thư mục đíchtên
	 * @return Nếusao chépThành côngTrả vềtrue, Nếu không thì trả vềfalse
	 */
	public static boolean copyDirectory(String srcDirName, String descDirName) {
		return FileUtils.copyDirectoryCover(srcDirName, descDirName,
				false);
	}

	/**
	 * sao chépnguyênCáidanh mục của nội dung 
	 * @param srcDirName nguồndanh mụctên
	 * @param descDirName thư mục đíchtên
	 * @param coverlay Nếuthư mục đíchTồn tại, Có hay khôngghi đè
	 * @return Nếusao chépThành côngTrả vềtrue, Nếu không thì trả vềfalse
	 */
	public static boolean copyDirectoryCover(String srcDirName,
			String descDirName, boolean coverlay) {
		File srcDir = new File(srcDirName);
		// Kiểm tranguồndanh mụcCó tồn tại không
		if (!srcDir.exists()) {
			logger.debug("sao chép thư mục Thất bại, nguồn danh mục" + srcDirName + " Không tồn tại!");
			return false;
		}
		// Kiểm tranguồndanh mụcCó hay không có danh mục
		else if (!srcDir.isDirectory()) {
			logger.debug("sao chép thư mục Thất bại," + srcDirName + "không có một Cá idanh mục!");
			return false;
		}
		// Nếu tên thư mục đích không kết thúc bằng dấu phân cách tệp, tự độngThêmTệp phútcáchký tự
		String descDirNames = descDirName;
		if (!descDirNames.endsWith(File.separator)) {
			descDirNames = descDirNames + File.separator;
		}
		File descDir = new File(descDirNames);
		// NếuđíchTệpkẹpTồn tại
		if (descDir.exists()) {
			if (coverlay) {
				// Cho phép ghi đè thư mục đích
				logger.debug("thư mục đích Đã tồn tại, chuẩn bị Xóa!");
				if (!FileUtils.delFile(descDirNames)) {
					logger.debug("Xóa thư mục " + descDirNames + " Thất bại!");
					return false;
				}
			} else {
				logger.debug("thư mục đích sao chép Thất bại, thư mục đích" + descDirNames + " Đã tồn tại!");
				return false;
			}
		} else {
			// Tạo thư mục đích
			logger.debug("thư mục đích Không tồn tại, chuẩn bị tạo!");
			if (!descDir.mkdirs()) {
				logger.debug("Tạo thư mục đích Thất bại!");
				return false;
			}

		}

		boolean flag = true;
		// Liệt kê tất cả tên tệp và tên thư mục con trong thư mục nguồn
		File[] files = srcDir.listFiles();
		for (int i = 0; i < files.length; i++) {
			// Nếu có mộtCáiđơnCáiTệp, thìtrực tiếpsao chép
			if (files[i].isFile()) {
				flag = FileUtils.copyFile(files[i].getAbsolutePath(),
						descDirName + files[i].getName());
				// Nếusao chépTệpThất bại, Thì thoát vòng lặp
				if (!flag) {
					break;
				}
			}
			// Nếu có condanh mục, Thì tiếp tục sao chép thư mục
			if (files[i].isDirectory()) {
				flag = FileUtils.copyDirectory(files[i]
						.getAbsolutePath(), descDirName + files[i].getName());
				// Nếusao chépdanh mụcThất bại, Thì thoát vòng lặp
				if (!flag) {
					break;
				}
			}
		}

		if (!flag) {
			logger.debug("sao chép thư mục " + srcDirName + " Đến " + descDirName + " Thất bại!");
			return false;
		}
		logger.debug("sao chép thư mục " + srcDirName + " Đến " + descDirName + " Thành công!");
		return true;

	}

	/**
	 * 
	 * Xóa tệp, Có thểXóađơnCáiTệp hoặc Tệpkẹp
	 * 
	 * @param fileName bịXóa của Tên tệp
	 * @return NếuXóaThành công, Thì trả vềtrue,  không  có Trả vềfalse
	 */
	public static boolean delFile(String fileName) {
 		File file = new File(fileName);
		if (!file.exists()) {
			logger.debug(fileName + "Tệp Không tồn tại!");
			return true;
		} else {
			if (file.isFile()) {
				return FileUtils.deleteFile(fileName);
			} else {
				return FileUtils.deleteDirectory(fileName);
			}
		}
	}

	/**
	 * 
	 * XóađơnCáiTệp
	 * 
	 * @param fileName bịXóa của Tên tệp
	 * @return NếuXóaThành công, Thì trả vềtrue, Nếu không thì trả vềfalse
	 */
	public static boolean deleteFile(String fileName) {
		File file = new File(fileName);
		if (file.exists() && file.isFile()) {
			if (file.delete()) {
				logger.debug("Xóa tệp " + fileName + " Thành công!");
				return true;
			} else {
				logger.debug("Xóa tệp " + fileName + " Thất bại!");
				return false;
			}
		} else {
			logger.debug(fileName + "Tệp Không tồn tại!");
			return true;
		}
	}

	/**
	 * 
	 * Xóa thư mụcvàdanh mụcdưới của Tệp
	 * 
	 * @param dirName bịXóa của danh mụcnơi của Tệpđường dẫn
	 * @return Nếudanh mụcXóaThành công, Thì trả vềtrue, Nếu không thì trả vềfalse
	 */
	public static boolean deleteDirectory(String dirName) {
		String dirNames = dirName;
		if (!dirNames.endsWith(File.separator)) {
			dirNames = dirNames + File.separator;
		}
		File dirFile = new File(dirNames);
		if (!dirFile.exists() || !dirFile.isDirectory()) {
			logger.debug(dirNames + "danh mục Không tồn tại!");
			return true;
		}
		boolean flag = true;
		// CộtraTất cảTệpvàcondanh mục
		File[] files = dirFile.listFiles();
		for (int i = 0; i < files.length; i++) {
			// XóaconTệp
			if (files[i].isFile()) {
				flag = FileUtils.deleteFile(files[i].getAbsolutePath());
				// NếuXóa tệpThất bại, Thì thoát vòng lặp
				if (!flag) {
					break;
				}
			}
			// Xóacondanh mục
			else if (files[i].isDirectory()) {
				flag = FileUtils.deleteDirectory(files[i]
						.getAbsolutePath());
				// NếuXóacondanh mụcThất bại, Thì thoát vòng lặp
				if (!flag) {
					break;
				}
			}
		}

		if (!flag) {
			logger.debug("Xóa thư mục Thất bại!");
			return false;
		}
		// XóaHiện tạidanh mục
		if (dirFile.delete()) {
			logger.debug("Xóa thư mục " + dirName + " Thành công!");
			return true;
		} else {
			logger.debug("Xóa thư mục " + dirName + " Thất bại!");
			return false;
		}

	}

	/**
	 * tạođơnCáiTệp
	 * @param descFileName Tên tệp, bao gồmđường dẫn
	 * @return NếutạoThành công, Thì trả vềtrue, Nếu không thì trả vềfalse
	 */
	public static boolean createFile(String descFileName) {
		File file = new File(descFileName);
		if (file.exists()) {
			logger.debug("Tệp " + descFileName + " Đã tồn tại!");
			return false;
		}
		if (descFileName.endsWith(File.separator)) {
			logger.debug(descFileName + "là danh mục, Không thể Tạo thư mục!");
			return false;
		}
		if (!file.getParentFile().exists()) {
			// NếuTệpnơi của danh mụcKhông tồn tại, Thì tạo thư mục
			if (!file.getParentFile().mkdirs()) {
				logger.debug("tạo Tệp nơi của danh mục Thất bại!");
				return false;
			}
		}

		// tạoTệp
		try {
			if (file.createNewFile()) {
				logger.debug(descFileName + "Tệp tạo Thành công!");
				return true;
			} else {
				logger.debug(descFileName + "Tệp tạo Thất bại!");
				return false;
			}
		} catch (Exception e) {
			logger.error(descFileName + "Tệp tạo Thất bại!", e);
			return false;
		}

	}

	/**
	 * Tạo thư mục
	 * @param descDirName danh mụctên,bao gồmđường dẫn
	 * @return NếutạoThành công, Thì trả vềtrue, Nếu không thì trả vềfalse
	 */
	public static boolean createDirectory(String descDirName) {
		String descDirNames = descDirName;
		if (!descDirNames.endsWith(File.separator)) {
			descDirNames = descDirNames + File.separator;
		}
		File descDir = new File(descDirNames);
		if (descDir.exists()) {
			logger.debug("Danh mục " + descDirNames + " Đã tồn tại!");
			return false;
		}
		// Tạo thư mục
		if (descDir.mkdirs()) {
			logger.debug("Danh mục " + descDirNames + "tạo Thành công!");
			return true;
		} else {
			logger.debug("Danh mục " + descDirNames + "tạo Thất bại!");
			return false;
		}

	}

	/**
	 * ghiTệp
	 * @param file cầnghi của Tệp
	 */
	public static void writeToFile(String fileName, String content, boolean append) {
		try {
			FileUtils.write(new File(fileName), content, "utf-8", append);
			logger.debug("Tệp " + fileName + "ghi Thành công!");
		} catch (IOException e) {
			logger.debug("Tệp " + fileName + "ghi Thất bại!" + e.getMessage());
		}
	}

	/**
	 * ghiTệp
	 * @param file cầnghi của Tệp
	 */
	public static void writeToFile(String fileName, String content, String encoding, boolean append) {
		try {
			FileUtils.write(new File(fileName), content, encoding, append);
			logger.debug("Tệp " + fileName + "ghi Thành công!");
		} catch (IOException e) {
			logger.debug("Tệp " + fileName + "ghi Thất bại!" + e.getMessage());
		}
	}
	
	/**
	 * nénTệp hoặc danh mục
	 * @param srcDirName nén của gốcdanh mục
	 * @param fileName Tên tệp hoặc thư mục chờ nén trong thư mục gốc, của nó trong * hoặc ""cho biếttheodanh mụcdưới của Tất cảTệp
	 * @param descFileName đíchzipTệp
	 */
/*	public static void zipFiles(String srcDirName, String fileName,
			String descFileName) {
		// Kiểm tradanh mụcCó tồn tại không
		if (srcDirName == null) {
			logger.debug("Tệp nén Thất bại, Danh mục" + srcDirName + " Không tồn tại!");
			return;
		}
		File fileDir = new File(srcDirName);
		if (!fileDir.exists() || !fileDir.isDirectory()) {
			logger.debug("Tệp nén Thất bại, Danh mục" + srcDirName + " Không tồn tại!");
			return;
		}
		String dirPath = fileDir.getAbsolutePath();
		File descFile = new File(descFileName);
		try {
			ZipOutputStream zouts = new ZipOutputStream(new FileOutputStream(
					descFile));
			if ("*".equals(fileName) || "".equals(fileName)) {
				FileUtils.zipDirectoryToZipFile(dirPath, fileDir, zouts);
			} else {
				File file = new File(fileDir, fileName);
				if (file.isFile()) {
					FileUtils.zipFilesToZipFile(dirPath, file, zouts);
				} else {
					FileUtils
							.zipDirectoryToZipFile(dirPath, file, zouts);
				}
			}
			zouts.close();
			logger.debug(descFileName + "Tệp nén Thành công!");
		} catch (Exception e) {
			logger.error("nén Tệp Thất bại", e);
		}

	}*/

	/**
	 * giảinénZIPTệp, Sẽ ZIPTệptrong của nội dunggiải nénĐếndescFileNamedanh mụcdưới
	 * @param zipFileName Cầngiải nén của ZIPTệp
	 * @param descFileName đíchTệp
	 */
/*	public static boolean unZipFiles(String zipFileName, String descFileName) {
		String descFileNames = descFileName;
		if (!descFileNames.endsWith(File.separator)) {
			descFileNames = descFileNames + File.separator;
		}		
        try {
			// TheoZIPTệptạoZipFileĐối tượng
			ZipFile zipFile = new ZipFile(zipFileName);
			ZipEntry entry = null;
			String entryName = null;
			String descFileDir = null;
			byte[] buf = new byte[4096];
			int readByte = 0;
			// LấyZIPTệptrongtất cả của entry
			@SuppressWarnings("rawtypes")
			Enumeration enums = zipFile.getEntries();
			// duyệttất cảentry
			while (enums.hasMoreElements()) {
				entry = (ZipEntry) enums.nextElement();
				// Lấy đượcentry của tênchữ
				entryName = entry.getName();
				descFileDir = descFileNames + entryName;
				if (entry.isDirectory()) {
					// Nếuentry có mộtCáidanh mục, Thì tạo thư mục
					new File(descFileDir).mkdirs();
					continue;
				} else {
					// Nếuentry có mộtCáiTệp, thìtạochadanh mục
					new File(descFileDir).getParentFile().mkdirs();
				}
				File file = new File(descFileDir);
				// mởTệpLuồng xuất
				OutputStream os = new FileOutputStream(file);
				// Từ ZipFileTrong đối tượngmởentry của Nhậpluồng
		        InputStream is = zipFile.getInputStream(entry);
				while ((readByte = is.read(buf)) != -1) {
					os.write(buf, 0, readByte);
				}
				os.close();
				is.close();
			}
			zipFile.close();
			logger.debug("Tệp giải nén Thành công!");
			return true;
		} catch (Exception e) {
			logger.debug("Tệp giải nén Thất bại:" + e.getMessage());
			return false;
		}
	}*/

	/**
	 * Sẽ danh mụcnénĐếnZIPLuồng xuất
	 * @param dirPath danh mụcđường dẫn
	 * @param fileDir TệpThông tin
	 * @param zouts Luồng xuất
	 */
/*	public static void zipDirectoryToZipFile(String dirPath, File fileDir, ZipOutputStream zouts) {
		if (fileDir.isDirectory()) {
			File[] files = fileDir.listFiles();
			// trống của Tệpkẹp
			if (files.length == 0) {
				// danh mụcThông tin
				ZipEntry entry = new ZipEntry(getEntryName(dirPath, fileDir));
				try {
					zouts.putNextEntry(entry);
					zouts.closeEntry();
				} catch (Exception e) {
					logger.error("đóng zip mục mục Thất bại", e);
				}
				return;
			}

			for (int i = 0; i < files.length; i++) {
				if (files[i].isFile()) {
					// Nếu có Tệp, thìgọiTệpnénPhương thức
					FileUtils
							.zipFilesToZipFile(dirPath, files[i], zouts);
				} else {
					// Nếu có danh mục, thìchuyểnquygọi
					FileUtils.zipDirectoryToZipFile(dirPath, files[i],
							zouts);
				}
			}
		}
	}*/

	/**
	 * Sẽ TệpnénĐếnZIPLuồng xuất(Đãloại bỏChưasử dụng của chếtmã)
	 */

	/**
	 * Theo“Tên tệp của sauhậu tố”LấyTệpnội dungLoại(màkhông phảiTheoFile.getContentType()đọc của Loại tệp)
	 * @param returnFileName kèmXác thực của Tên tệp
	 * @return Trả vềLoại tệp
	 */
	public static String getContentType(String returnFileName) {
		String contentType = "application/octet-stream";
		if (returnFileName.lastIndexOf(".") < 0)
			return contentType;
		returnFileName = returnFileName.toLowerCase();
		returnFileName = returnFileName.substring(returnFileName.lastIndexOf(".") + 1);
		if (returnFileName.equals("html") || returnFileName.equals("htm") || returnFileName.equals("shtml")) {
			contentType = "text/html";
		} else if (returnFileName.equals("apk")) {
			contentType = "application/vnd.android.package-archive";
		} else if (returnFileName.equals("sis")) {
			contentType = "application/vnd.symbian.install";
		} else if (returnFileName.equals("sisx")) {
			contentType = "application/vnd.symbian.install";
		} else if (returnFileName.equals("exe")) {
			contentType = "application/x-msdownload";
		} else if (returnFileName.equals("msi")) {
			contentType = "application/x-msdownload";
		} else if (returnFileName.equals("css")) {
			contentType = "text/css";
		} else if (returnFileName.equals("xml")) {
			contentType = "text/xml";
		} else if (returnFileName.equals("gif")) {
			contentType = "image/gif";
		} else if (returnFileName.equals("jpeg") || returnFileName.equals("jpg")) {
			contentType = "image/jpeg";
		} else if (returnFileName.equals("js")) {
			contentType = "application/x-javascript";
		} else if (returnFileName.equals("atom")) {
			contentType = "application/atom+xml";
		} else if (returnFileName.equals("rss")) {
			contentType = "application/rss+xml";
		} else if (returnFileName.equals("mml")) {
			contentType = "text/mathml";
		} else if (returnFileName.equals("txt")) {
			contentType = "text/plain";
		} else if (returnFileName.equals("jad")) {
			contentType = "text/vnd.sun.j2me.app-descriptor";
		} else if (returnFileName.equals("wml")) {
			contentType = "text/vnd.wap.wml";
		} else if (returnFileName.equals("htc")) {
			contentType = "text/x-component";
		} else if (returnFileName.equals("png")) {
			contentType = "image/png";
		} else if (returnFileName.equals("tif") || returnFileName.equals("tiff")) {
			contentType = "image/tiff";
		} else if (returnFileName.equals("wbmp")) {
			contentType = "image/vnd.wap.wbmp";
		} else if (returnFileName.equals("ico")) {
			contentType = "image/x-icon";
		} else if (returnFileName.equals("jng")) {
			contentType = "image/x-jng";
		} else if (returnFileName.equals("bmp")) {
			contentType = "image/x-ms-bmp";
		} else if (returnFileName.equals("svg")) {
			contentType = "image/svg+xml";
		} else if (returnFileName.equals("jar") || returnFileName.equals("var") 
				|| returnFileName.equals("ear")) {
			contentType = "application/java-archive";
		} else if (returnFileName.equals("doc")) {
			contentType = "application/msword";
		} else if (returnFileName.equals("pdf")) {
			contentType = "application/pdf";
		} else if (returnFileName.equals("rtf")) {
			contentType = "application/rtf";
		} else if (returnFileName.equals("xls")) {
			contentType = "application/vnd.ms-excel";
		} else if (returnFileName.equals("ppt")) {
			contentType = "application/vnd.ms-powerpoint";
		} else if (returnFileName.equals("7z")) {
			contentType = "application/x-7z-compressed";
		} else if (returnFileName.equals("rar")) {
			contentType = "application/x-rar-compressed";
		} else if (returnFileName.equals("swf")) {
			contentType = "application/x-shockwave-flash";
		} else if (returnFileName.equals("rpm")) {
			contentType = "application/x-redhat-package-manager";
		} else if (returnFileName.equals("der") || returnFileName.equals("pem")
				|| returnFileName.equals("crt")) {
			contentType = "application/x-x509-ca-cert";
		} else if (returnFileName.equals("xhtml")) {
			contentType = "application/xhtml+xml";
		} else if (returnFileName.equals("zip")) {
			contentType = "application/zip";
		} else if (returnFileName.equals("mid") || returnFileName.equals("midi") 
				|| returnFileName.equals("kar")) {
			contentType = "audio/midi";
		} else if (returnFileName.equals("mp3")) {
			contentType = "audio/mpeg";
		} else if (returnFileName.equals("ogg")) {
			contentType = "audio/ogg";
		} else if (returnFileName.equals("m4a")) {
			contentType = "audio/x-m4a";
		} else if (returnFileName.equals("ra")) {
			contentType = "audio/x-realaudio";
		} else if (returnFileName.equals("3gpp")
				|| returnFileName.equals("3gp")) {
			contentType = "video/3gpp";
		} else if (returnFileName.equals("mp4")) {
			contentType = "video/mp4";
		} else if (returnFileName.equals("mpeg")
				|| returnFileName.equals("mpg")) {
			contentType = "video/mpeg";
		} else if (returnFileName.equals("mov")) {
			contentType = "video/quicktime";
		} else if (returnFileName.equals("flv")) {
			contentType = "video/x-flv";
		} else if (returnFileName.equals("m4v")) {
			contentType = "video/x-m4v";
		} else if (returnFileName.equals("mng")) {
			contentType = "video/x-mng";
		} else if (returnFileName.equals("asx") || returnFileName.equals("asf")) {
			contentType = "video/x-ms-asf";
		} else if (returnFileName.equals("wmv")) {
			contentType = "video/x-ms-wmv";
		} else if (returnFileName.equals("avi")) {
			contentType = "video/x-msvideo";
		}
		return contentType;
	}
	
	/**
	 * sửađường dẫn, Sẽ  \\  hoặc  /  v.v. thay thế là  File.separator
	 * @param path chờsửa của đường dẫn
	 * @return sửasau của đường dẫn
	 */
	public static String path(String path){
		String p = StringUtils.replace(path, "\\", "/");
		p = StringUtils.join(StringUtils.split(p, "/"), "/");
		if (!StringUtils.startsWithAny(p, "/") && StringUtils.startsWithAny(path, "\\", "/")){
			p += "/";
		}
		if (!StringUtils.endsWithAny(p, "/") && StringUtils.endsWithAny(path, "\\", "/")){
			p = p + "/";
		}
		if (path != null && path.startsWith("/")){
			p = "/" + p; // linuxdướiđường dẫn
		}
		return p;
	}
	
	/**
	 * đượcdanh mụcdưới của TệpDanh sách
	 * @param dir tìm kiếmdanh mục
	 * @param searchDirs Có hay không có tìm kiếmdanh mục
	 * @return TệpDanh sách
	 */
	public static List<String> findChildrenList(File dir, boolean searchDirs) {
		List<String> files = Lists.newArrayList();
		for (String subFiles : dir.list()) {
			File file = new File(dir + "/" + subFiles);
			if (((searchDirs) && (file.isDirectory())) || ((!searchDirs) && (!file.isDirectory()))) {
				files.add(file.getName());
			}
		}
		return files;
	}

	/**
	 * LấyTệpmở rộngtên(Trả vềnhỏviết)
	 * @param fileName Tên tệp
	 * @return Ví dụ: test.jpg  Quay lại:   jpg
	 */
	public static String getFileExtension(String fileName) {
		if ((fileName == null) || (fileName.lastIndexOf(".") == -1) || (fileName.lastIndexOf(".") == fileName.length() - 1)) {
			return null;
		}
		return StringUtils.lowerCase(fileName.substring(fileName.lastIndexOf(".") + 1));
	}

	/**
	 * LấyTên tệp, khôngbao gồmmở rộngtên
	 * @param fileName Tên tệp
	 * @return Ví dụ: d:\files\test.jpg  Quay lại: d:\files\test
	 */
	public static String getFileNameWithoutExtension(String fileName) {
		if ((fileName == null) || (fileName.lastIndexOf(".") == -1)) {
			return null;
		}
		return fileName.substring(0, fileName.lastIndexOf("."));
	}
}
