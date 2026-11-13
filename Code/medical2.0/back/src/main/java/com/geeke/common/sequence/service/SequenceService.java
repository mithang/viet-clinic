package com.geeke.common.sequence.service;

import java.io.IOException;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.geeke.common.sequence.dao.SequenceDao;
import com.geeke.common.sequence.entity.Sequence;
import com.geeke.sys.dao.CodeRuleDao;
import com.geeke.sys.dao.CompanyCodeRuleDao;
import com.geeke.utils.FreeMarkers;
import com.google.common.collect.Maps;

/**
 * nghiệp vụmã sốService
 * @author lys
 * @version 2021-04-30
 */
 
@Service("sequenceService")
// @Transactional(readOnly = true)
public class SequenceService {

	@Autowired
	private SequenceDao sequenceDao;

	@Autowired
	private CompanyCodeRuleDao companyCodeRuleDao;
	
	@Autowired CodeRuleDao codeRuleDao;
	
	
	/**
	 * Lấymã số
	 * @param ruleCode    Mã quy tắc
	 * @param bizObject	     Đối tượng nghiệp vụ
	 * @return
	 */
	@Transactional(readOnly = false)
	public String generate( String ruleCode, Object bizObject) {
		return generate(null, ruleCode, bizObject);
	}
	
	/**
	 * Lấymã số
	 * @param companyId	       Công tyId  Công tyId là trống giờ, sử dụngthống nhất của số thứ tự
	 * @param ruleCode    Mã quy tắc
	 * @param bizObject	     Đối tượng nghiệp vụ
	 * @return
	 */
	// @Transactional(/*readOnly = false*/)
	public String generate(String companyId, String ruleCode, Object bizObject) {
		String ruleDef = null;
		// Công tyidkhông là trống, sử dụngLấycông ty của Biểu thức quy tắc đánh số
		if(StringUtils.isNotBlank(companyId)) {
			companyCodeRuleDao.getRuleDefByCode(companyId, ruleCode);
		}
		// Công tyId là trống,  hoặc công tymã sốquy tắcKhông tồn tại, LấyHệ thốngthống nhất của quy tắcbiểu thức
		if(ruleDef == null) {
			ruleDef = codeRuleDao.getRuleDefByCode(ruleCode);
		}
		
		if(ruleDef == null) {
			return null;
		}
		
		return doGenerate(companyId, ruleCode, ruleDef, bizObject);
	}
	
	/**
	 * Tạomã số
	 * @param companyId			 Công tyId
	 * @param ruleCode    Mã quy tắc
	 * @param templateString	quy tắcbiểu thức
	 * @param bizObject			Đối tượng nghiệp vụ
	 * @return
	 */
	private String doGenerate(String companyId, String ruleCode, String templateString, Object bizObject) {
		// Công tyId là trống giờ, sử dụngthống nhất của số thứ tự(companyId là 0)
		String theCompanyId = StringUtils.isBlank(companyId) ? "0" : companyId;
		
		Map<String, Object> model = Maps.newHashMap();
		model.put("biz", bizObject);
		model.put("seq", 0);
		// DS257: ruleDef hong (dong cu / import tay, luu truoc khi
		// CodeRuleService chan cu phap) truoc day nem RuntimeException
		// o FreeMarkers.renderString -> 500 moi lan sinh ma. Fail-closed
		// 400 ro nghia thay vi 500; chi boc render (loi DB o duoi giu
		// nguyen de khong doi hanh vi loi he thong).
		String prefix;
		try {
			prefix = FreeMarkers.renderString(templateString, model);
		} catch (RuntimeException e) {
			throw new com.geeke.common.service.ServiceException(
					"Quy tắc mã " + ruleCode + " không hợp lệ, không thể sinh mã mới");
		}
		Sequence sequence = sequenceDao.getByPrefix(theCompanyId, ruleCode, prefix);
		if(sequence == null) {
			sequence = insertSeq(theCompanyId, ruleCode, prefix);
		}
		
		model.put("seq", sequence.getVal());
		String seqNo;
		try {
			seqNo = FreeMarkers.renderString(templateString, model);
		} catch (RuntimeException e) {
			throw new com.geeke.common.service.ServiceException(
					"Quy tắc mã " + ruleCode + " không hợp lệ, không thể sinh mã mới");
		}
		
		sequenceDao.nextSeqById(sequence);
		
		return seqNo;
	}
	
	
	private synchronized Sequence insertSeq(String companyId, String ruleCode, String prefix) {
		Sequence sequence = sequenceDao.getByPrefix(companyId, ruleCode, prefix);
		if(sequence == null) {
			sequence = new Sequence();
			sequence.setCompanyId(companyId);
			sequence.setCode(ruleCode);
			sequence.setPrefix(prefix);
			sequence.preInsert();
			sequenceDao.insert(sequence);
			
			sequence = sequenceDao.getByPrefix(companyId, ruleCode, prefix);	// tăngDữ liệukhokhóa
		}
		return sequence;
	}
}