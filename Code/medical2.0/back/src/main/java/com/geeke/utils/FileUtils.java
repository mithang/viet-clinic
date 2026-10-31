package com.geeke.utils;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
			logger.debug("Sao chép tệp thất bại, nguồnTệp " + srcFileName + " Không tồn tại!");
			return false;
		}
		// Kiểm tranguồnTệpCó hay không có hợp lệ của Tệp
		else if (!srcFile.isFile()) {
			logger.debug("Sao chép tệp thất bại, " + srcFileName + " không có mộtCáiTệp!");
			return false;
		}
		File descFile = new File(descFileName);
		// Kiểm trađíchTệpCó tồn tại không
		if (descFile.exists()) {
			// NếuđíchTệpTồn tại, vàvàcho phépghi đè
			if (coverlay) {
				logger.debug("đíchTệpĐã tồn tại, chuẩn bịXóa!");
				if (!FileUtils.delFile(descFileName)) {
					logger.debug("XóađíchTệp " + descFileName + " Thất bại!");
					return false;
				}
			} else {
				logger.debug("Sao chép tệp thất bại, đíchTệp " + descFileName + " Đã tồn tại!");
				return false;
			}
		} else {
			if (!descFile.getParentFile().exists()) {
				// NếuđíchTệpnơi của danh mụcKhông tồn tại, Thì tạo thư mục
				logger.debug("đíchTệpnơi của danh mụcKhông tồn tại, Tạo thư mục!");
				// tạođíchTệpnơi của danh mục
				if (!descFile.getParentFile().mkdirs()) {
					logger.debug("tạođíchTệpnơi của danh mụcThất bại!");
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
					logger.error("đóngLuồng xuấtThất bại", oute);
				}
			}
			if (ins != null) {
				try {
					ins.close();
				} catch (IOException ine) {
					logger.error("đóngNhậpluồngThất bại", ine);
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
			logger.debug("sao chép thư mụcThất bại, nguồndanh mục " + srcDirName + " Không tồn tại!");
			return false;
		}
		// Kiểm tranguồndanh mụcCó hay không có danh mục
		else if (!srcDir.isDirectory()) {
			logger.debug("sao chép thư mụcThất bại, " + srcDirName + " không có mộtCáidanh mục!");
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
				logger.debug("thư mục đíchĐã tồn tại, chuẩn bịXóa!");
				if (!FileUtils.delFile(descDirNames)) {
					logger.debug("Xóa thư mục " + descDirNames + " Thất bại!");
					return false;
				}
			} else {
				logger.debug("thư mục đíchsao chépThất bại, thư mục đích " + descDirNames + " Đã tồn tại!");
				return false;
			}
		} else {
			// Tạo thư mục đích
			logger.debug("thư mục đíchKhông tồn tại, chuẩn bịtạo!");
			if (!descDir.mkdirs()) {
				logger.debug("Tạo thư mục đíchThất bại!");
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
			logger.debug(fileName + " TệpKhông tồn tại!");
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
			logger.debug(fileName + " TệpKhông tồn tại!");
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
			logger.debug(dirNames + " danh mụcKhông tồn tại!");
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
			logger.debug("Xóa thư mụcThất bại!");
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
			logger.debug(descFileName + "  là danh mục, Không thểTạo thư mục!");
			return false;
		}
		if (!file.getParentFile().exists()) {
			// NếuTệpnơi của danh mụcKhông tồn tại, Thì tạo thư mục
			if (!file.getParentFile().mkdirs()) {
				logger.debug("tạoTệpnơi của danh mụcThất bại!");
				return false;
			}
		}

		// tạoTệp
		try {
			if (file.createNewFile()) {
				logger.debug(descFileName + " TệptạoThành công!");
				return true;
			} else {
				logger.debug(descFileName + " TệptạoThất bại!");
				return false;
			}
		} catch (Exception e) {
			logger.error(descFileName + " TệptạoThất bại!", e);
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
			logger.debug("Danh mục " + descDirNames + " tạoThành công!");
			return true;
		} else {
			logger.debug("Danh mục " + descDirNames + " tạoThất bại!");
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
			logger.debug("Tệp " + fileName + " ghiThành công!");
		} catch (IOException e) {
			logger.debug("Tệp " + fileName + " ghiThất bại! " + e.getMessage());
		}
	}

	/**
	 * ghiTệp
	 * @param file cầnghi của Tệp
	 */
	public static void writeToFile(String fileName, String content, String encoding, boolean append) {
		try {
			FileUtils.write(new File(fileName), content, encoding, append);
			logger.debug("Tệp " + fileName + " ghiThành công!");
		} catch (IOException e) {
			logger.debug("Tệp " + fileName + " ghiThất bại! " + e.getMessage());
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
			logger.debug("TệpnénThất bại, Danh mục " + srcDirName + " Không tồn tại!");
			return;
		}
		File fileDir = new File(srcDirName);
		if (!fileDir.exists() || !fileDir.isDirectory()) {
			logger.debug("TệpnénThất bại, Danh mục " + srcDirName + " Không tồn tại!");
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
			logger.debug(descFileName + " TệpnénThành công!");
		} catch (Exception e) {
			logger.error("nénTệpThất bại", e);
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
			logger.debug("Tệpgiải nénThành công!");
			return true;
		} catch (Exception e) {
			logger.debug("Tệpgiải nénThất bại: " + e.getMessage());
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
					logger.error("đóngzipmụcmụcThất bại", e);
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
	 * Gửi tới trình duyệtTệpTải xuống, Hỗ trợ tải tiếp khi đứt đoạn
	 * @param file cầnTải xuống của Tệp
	 * @param request Vui lòngyêu cầuĐối tượng
	 * @param response phản hồiĐối tượng
	 * @return Trả về thông tin lỗi, khôngThông tin lỗiTrả vềnull
	 */
	public static String downFile(File file, HttpServletRequest request, HttpServletResponse response){
		 return downFile(file, request, response, null);
	}
	
	/**
	 * Gửi tới trình duyệtTệpTải xuống, Hỗ trợ tải tiếp khi đứt đoạn
	 * @param file cầnTải xuống của Tệp
	 * @param request Vui lòngyêu cầuĐối tượng
	 * @param response phản hồiĐối tượng
	 * @param fileName chỉchỉ địnhTải xuống của Tên tệp
	 * @return Trả về thông tin lỗi, khôngThông tin lỗiTrả vềnull
	 */
	public static String downFile(File file, HttpServletRequest request, HttpServletResponse response, String fileName){
		String error  = null;
		if (file != null && file.exists()) {
			if (file.isFile()) {
				if (file.length() <= 0) {
					error = "đóTệp có mộtCáitrốngTệp. ";
				}
				if (!file.canRead()) {
					error = "đóTệpKhông cóđọcQuyền. ";
				}
			} else {
				error = "đóTệp có mộtCáiTệpkẹp. ";
			}
		} else {
			error = "Tệp đã mất hoặc không tồn tại!";
		}
		if (error != null){
			logger.debug("---------------" + file + " " + error);
			return error;
		}

		long fileLength = file.length(); // Bản ghiTệpkích thước
		long pastLength = 0; 	// Bản ghiĐãTải xuốngTệpkích thước
		int rangeSwitch = 0; 	// 0: Từ tiêu đềBắt đầu của toàn vănTải xuống; 1: Từ mộtbyteBắt đầu của Tải xuống(bytes=27000-); 2: Từ mộtbyteBắt đầuĐếnmộtbyteKết thúc của Tải xuống(bytes=27000-39000)
		long toLength = 0; 		// Ghi lại độ lệch byte cuối cùng của đoạn byte mà client cần tải xuống(so vớiNhư bytes=27000-39000, thìnàyCáiGiá trị có  là 39000)
		long contentLength = 0; // Khách hàngđầuVui lòngyêu cầu của bytetổng lượng
		String rangeBytes = ""; // Bản ghiKhách hàngđầutruyềnđến của hìnhNhư “bytes=27000-” hoặc người“bytes=27000-39000” của nội dung
		RandomAccessFile raf = null; // âmtráchđọcDữ liệu
		OutputStream os = null; 	// viếtraDữ liệu
		OutputStream out = null; 	// đệmxung
		byte b[] = new byte[1024]; 	// tạmlưucontainer

		if (request.getHeader("Range") != null) { // Byte bắt đầu của khối tệp mà client yêu cầu tải xuống
			response.setStatus(javax.servlet.http.HttpServletResponse.SC_PARTIAL_CONTENT);
			logger.debug("request.getHeader(\"Range\") = " + request.getHeader("Range"));
			rangeBytes = request.getHeader("Range").replaceAll("bytes=", "");
			if (rangeBytes.indexOf('-') == rangeBytes.length() - 1) {// bytes=969998336-
				rangeSwitch = 1;
				rangeBytes = rangeBytes.substring(0, rangeBytes.indexOf('-'));
				pastLength = Long.parseLong(rangeBytes.trim());
				contentLength = fileLength - pastLength; // Khách hàngđầuVui lòngyêu cầu của  có  969998336  củasau của byte
			} else { // bytes=1275856879-1275877358
				rangeSwitch = 2;
				String temp0 = rangeBytes.substring(0, rangeBytes.indexOf('-'));
				String temp2 = rangeBytes.substring(rangeBytes.indexOf('-') + 1, rangeBytes.length());
				pastLength = Long.parseLong(temp0.trim()); // bytes=1275856879-1275877358, Từ Dòng  1275856879 byteBắt đầuTải xuống
				toLength = Long.parseLong(temp2); // bytes=1275856879-1275877358, ĐếnDòng  1275877358 byteKết thúc
				contentLength = toLength - pastLength; // Khách hàngđầuVui lòngyêu cầu của  có  1275856879-1275877358 giữa của byte
			}
		} else { // Từ Bắt đầutiến hành dòngTải xuống
			contentLength = fileLength; // Khách hàngđầucầnyêu cầutoàn vănTải xuống
		}

		// NếuđặtĐặtrồiContent-Length, thìKhách hàngđầusẽtự độngtiến hành dòngnhiềuluồngTải xuống. NếuKhông muốn hỗ trợ đa luồng, thìkhôngcầnĐặtnàyCáiTham số.  Định dạng của phản hồi là:
		// Content-Length: [Tổng kích thước tệp] - [Byte bắt đầu của khối tệp mà client yêu cầu tải xuống]
		// ServletActionContext.getResponse().setHeader("Content- Length", new Long(file.length() - p).toString());
		response.reset(); // Thông báo cho client được phép tải xuống nối tiếp đa luồng với điểm ngắt,Định dạng của phản hồi là:Accept-Ranges: bytes
		if (pastLength != 0) {
			response.setHeader("Accept-Ranges", "bytes");// Nếu có Dòng một lầndưới,cònKhông cótải tiếp khi đứt đoạn,Trạng thái có Mặc định của  200,khôngcầnhiển thịkiểuĐặt;Định dạng của phản hồi là:HTTP/1.1 200 OK
			// không có Từ nhấtBắt đầuTải xuống, Định dạng của phản hồi là: Content-Range: bytes [Tệpkhối của Bắt đầubyte]-[Tổng kích thước tệp - 1]/[Tổng kích thước tệp]
			logger.debug("---------------không có Từ Bắt đầutiến hành dòngTải xuống!dịch vụthiết bịtức làSẽ Bắt đầutải tiếp khi đứt đoạn...");
			switch (rangeSwitch) {
				case 1: { // Đối với bytes=27000- Yêu cầu
					String contentRange = new StringBuffer("bytes ").append(new Long(pastLength).toString()).append("-")
							.append(new Long(fileLength - 1).toString()).append("/").append(new Long(fileLength).toString()).toString();
					response.setHeader("Content-Range", contentRange);
					break;
				}
				case 2: { // Đối với bytes=27000-39000 Yêu cầu
					String contentRange = rangeBytes + "/" + new Long(fileLength).toString();
					response.setHeader("Content-Range", contentRange);
					break;
				}
				default: {
					break;
				}
			}
		} else {
			//  có Từ Bắt đầuTải xuống
			logger.debug("--------------- có Từ Bắt đầutiến hành dòngTải xuống!");
		}

		try {
			response.addHeader("Content-Disposition", "attachment; filename=\"" + 
					Encodes.urlEncode(StringUtils.isBlank(fileName) ? file.getName() : fileName) + "\"");
			response.setContentType(getContentType(file.getName())); // set the MIME type.
			response.addHeader("Content-Length", String.valueOf(contentLength));
			os = response.getOutputStream();
			out = new BufferedOutputStream(os);
			raf = new RandomAccessFile(file, "r");
			try {
				switch (rangeSwitch) {
					case 0: { // thông thườngTải xuống,  hoặc ngườiTừ tiêu đềBắt đầu của Tải xuống cùng1
					}
					case 1: { // Đối với bytes=27000- Yêu cầu
						raf.seek(pastLength); // hìnhNhư  bytes=969998336-  của Khách hàngđầuVui lòngyêu cầu, Bỏ qua 969998336 byte
						int n = 0;
						while ((n = raf.read(b, 0, 1024)) != -1) {
							out.write(b, 0, n);
						}
						break;
					}
					case 2: { // Đối với bytes=27000-39000 Yêu cầu
						raf.seek(pastLength); // hìnhNhư  bytes=1275856879-1275877358  của Khách hàngđầuVui lòngyêu cầu, tìmĐếnDòng  1275856879 byte
						int n = 0;
						long readLength = 0; // Bản ghiĐãđọcbytesố
						while (readLength <= contentLength - 1024) {// lớnMột phầnbytetạiỞ đâyđọc
							n = raf.read(b, 0, 1024);
							readLength += 1024;
							out.write(b, 0, n);
						}
						if (readLength <= contentLength) { // còn lạidưới của không đủ 1024 bytetạiỞ đâyđọc
							n = raf.read(b, 0, (int) (contentLength - readLength));
							out.write(b, 0, n);
						}
						break;
					}
					default: {
						break;
					}
				}
				out.flush();
				logger.debug("---------------Tải xuốnghoàn thành!");
			} catch (IOException ie) {
				/**
				 * tạiviếtDữ liệu của  giờchờ,  đối vớitại ClientAbortException củaLớp của ngoại lệ, 
				 *  có do là Khách hàngđầulấyhủyrồiTải xuống, Và khi phía server tiếp tục ghi dữ liệu cho trình duyệt,  ném ranàyCáingoại lệ, nàyCái có bình thường của . 
				 * Đặc biệt đối với phần mềm client hút băng thông như Xunlei,  rõrõĐãcómộtCáiluồngtạiđọc bytes=1275856879-1275877358, 
				 * NếungắnThời giantrongKhông cóđọcxong, Xunlei sẽ khởi động lạiDòng haiCái, Dòng baCái. . . Luồng để đọc cùng một thứ của byteđoạn,  trực tiếpĐếncómộtCáiluồngđọcxong, nhanhsấmsẽ KILL
				 * rơikhácđúngtạiTải xuốngcùngmộtbyteđoạn của luồng,  mạnh dòng trong dừngbyteđọcra, gây radịch vụthiết bịném ClientAbortException. 
				 * vì vậy, Chúng ta bỏ qua ngoại lệ này
				 */
				logger.debug("Nhắc nhở: khi truyền tới máy khách xuất hiện ngoại lệ IO, nhưng ngoại lệ này là cho phép, có thể máy khách đã hủy tải xuống, dẫn đến ngoại lệ này, không cần lo lắng!");
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		} finally {
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
					logger.error(e.getMessage(), e);
				}
			}
			if (raf != null) {
				try {
					raf.close();
				} catch (IOException e) {
					logger.error(e.getMessage(), e);
				}
			}
		}
		return null;
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
