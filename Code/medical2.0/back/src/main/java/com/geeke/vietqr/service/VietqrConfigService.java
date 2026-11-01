package com.geeke.vietqr.service;

import com.geeke.common.persistence.BaseEntity;
import com.geeke.common.service.CrudService;
import com.geeke.org.entity.Company;
import com.geeke.utils.StringUtils;
import com.geeke.vietqr.dao.VietqrConfigDao;
import com.geeke.vietqr.entity.VietqrConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

/**
 * Cấu hình VietQR Service (G4 research loop).
 *
 * Ranh giới an toàn:
 * - Chỉ đọc/ghi bảng vietqr_config, và luôn giới hạn đúng tenant đang
 *   đăng nhập (company_id = tenant phiên). Không đọc/ghi dữ liệu khám
 *   chữa, không gọi API ngân hàng, không lưu credential nào.
 * - Validate ở service (không chỉ annotation): BIN đúng 6 chữ số, số tài
 *   khoản chỉ chữ số 6–20 ký tự, tên chủ tài khoản 1–50 ký tự, tên ngân
 *   hàng tối đa 50 ký tự. Dữ liệu sai trả 400, không ghi DB.
 * - Một tenant chỉ có một dòng hiệu lực: đã có thì UPDATE tại chỗ, chưa
 *   có thì INSERT. Không bao giờ tạo dòng thứ hai cho cùng tenant.
 */
@Service("vietqrConfigService")
@Transactional(readOnly = true)
@Slf4j
public class VietqrConfigService extends CrudService<VietqrConfigDao, VietqrConfig> {

    public VietqrConfig getActive(String companyId) {
        if (StringUtils.isBlank(companyId)) {
            return null;
        }
        Map<String, Object> params = new HashMap<>();
        params.put("companyId", companyId);
        params.put("DEL_FLAG_NORMAL", BaseEntity.DEL_FLAG_NORMAL);
        return dao.findActive(params);
    }

    @Transactional(readOnly = false)
    public VietqrConfig saveConfig(String companyId, VietqrConfig input) {
        if (StringUtils.isBlank(companyId)) {
            throw new IllegalArgumentException("Chưa xác định được phòng khám.");
        }
        if (input == null) {
            throw new IllegalArgumentException("Cấu hình VietQR không được để trống.");
        }
        String bankBin = input.getBankBin() == null ? null : input.getBankBin().trim();
        String accountNo = input.getAccountNo() == null ? null : input.getAccountNo().trim();
        String accountName = input.getAccountName() == null ? null : input.getAccountName().trim();
        String bankName = input.getBankName() == null ? null : input.getBankName().trim();
        if (bankBin == null || !bankBin.matches("\\d{6}")) {
            throw new IllegalArgumentException("Mã BIN ngân hàng phải đúng 6 chữ số.");
        }
        if (accountNo == null || !accountNo.matches("\\d{6,20}")) {
            throw new IllegalArgumentException("Số tài khoản phải là 6–20 chữ số.");
        }
        if (StringUtils.isBlank(accountName) || accountName.length() > 50) {
            throw new IllegalArgumentException("Tên chủ tài khoản không được để trống (tối đa 50 ký tự).");
        }
        if (bankName != null && bankName.length() > 50) {
            throw new IllegalArgumentException("Tên ngân hàng tối đa 50 ký tự.");
        }
        VietqrConfig existing = getActive(companyId);
        if (existing != null) {
            existing.setBankBin(bankBin);
            existing.setBankName(StringUtils.isBlank(bankName) ? null : bankName);
            existing.setAccountNo(accountNo);
            existing.setAccountName(accountName);
            existing.preUpdate();
            dao.update(existing);
            return existing;
        }
        Company company = new Company();
        company.setId(companyId);
        input.setCompany(company);
        input.setBankBin(bankBin);
        input.setBankName(StringUtils.isBlank(bankName) ? null : bankName);
        input.setAccountNo(accountNo);
        input.setAccountName(accountName);
        input.preInsert();
        dao.insert(input);
        return input;
    }
}
