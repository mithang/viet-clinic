package com.geeke.taskmanagement.service;

import com.geeke.common.data.Page;
import com.geeke.common.data.PageRequest;
import com.geeke.common.data.Parameter;
import com.geeke.common.service.CrudService;
import com.geeke.common.service.ServiceException;
import com.geeke.utils.SessionUtils;
import com.geeke.utils.StringUtils;
import com.geeke.outpatient.dao.SchedulingDao;
import com.geeke.outpatient.entity.Scheduling;
import com.geeke.taskmanagement.dao.ArticleDao;
import com.geeke.taskmanagement.entity.article;
import com.geeke.taskmanagement.entity.taskmanagement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;

import java.util.*;

@Service("ArticleService")
@Transactional(readOnly = true)
public class ArticleService extends CrudService<ArticleDao, article>{

    @Autowired
    private ArticleDao articleDao;

    public article listgetid(article article){
        article articelgetid=articleDao.listgetid(article);
        return articelgetid;
    }
    /**
     * ITER63 item 3: audit/update bundled guard + write enablement — NEVER
     * commit readOnly=false alone. The class is @Transactional(readOnly =
     * true), so without the method override this write 500s globally
     * (N5-P5: own and foreign alike — a dead function, not a live hole).
     * The mapper UPDATE is bare `WHERE id`, so enabling the write alone
     * would CREATE a cross-tenant write hole. Guard FIRST, then write:
     * resolve the STORED company via a direct single-column lookup
     * (X3/N1 style — trusts neither payload company/companyid nor the
     * JOINed mapper get, whose LEFT JOIN yields '' when the org_company
     * row is missing while company_id itself is NOT NULL truth).
     * Stored company != session tenant -> ServiceException 400,
     * fail-closed, row untouched. Nonexistent id (lookup null) and
     * no-tenant/'null' sessions pass as before (N3c parity: article has
     * no parent entity, so nonexistent id IS the orphan case here; a
     * row whose company_id names an unregistered company is
     * determinably not-own and is correctly blocked). Both controller
     * endpoints `update` and `audit` call this single method, so one
     * guard covers both paths.
     */
    private void checkArticleTenant(String articleId) {
        if (StringUtils.isBlank(articleId)) {
            return;
        }
        String storedCompanyId = articleDao.getArticleCompanyId(articleId);
        if (StringUtils.isBlank(storedCompanyId)) {
            return;
        }
        String tenantId = SessionUtils.getLoginTenantId();
        if (StringUtils.isBlank(tenantId) || "null".equals(tenantId)) {
            return;
        }
        if (!tenantId.equals(storedCompanyId)) {
            throw new ServiceException("Không có quyền truy cập tenant khác");
        }
    }

    /**
     * DS22: nhanh id rong cua listgetid — ep companyid ve session tenant
     * (cung nguon getLoginTenantId nhu isArticleForeignOwned; controller
     * dung com.geeke.sys.utils.SessionUtils nen khong co ham nay).
     * Session rong/"null" giu hanh vi cu.
     */
    @Transactional(readOnly = true)
    public void forceListgetidCompany(article entity) {
        if (entity == null) {
            return;
        }
        String tenantId = SessionUtils.getLoginTenantId();
        if (StringUtils.isNotBlank(tenantId) && !"null".equals(tenantId)) {
            entity.setCompanyid(tenantId);
        }
    }

    /**
     * DS22: read-side ownership for article/listgetid (same stored-row
     * lookup as checkArticleTenant, but read-only so the controller can
     * answer 403 instead of the write guard's 400). list/listAll/lists
     * are already tenant-scoped (ensureCompanyFilter in listPages, and
     * the generic list/listAll floor), but listgetid calls the DAO
     * directly and filters only by a.id — any permissioned session
     * could read another tenant's article (title/abstracts/content)
     * by id, and the id-blank branch trusted the caller-supplied
     * companyid the same way. Nonexistent id returns null (caller
     * keeps the 200-null contract); blank/'null' session keeps the
     * old behaviour, parity with checkArticleTenant.
     */
    @Transactional(readOnly = true)
    public boolean isArticleForeignOwned(String articleId) {
        if (StringUtils.isBlank(articleId)) {
            return false;
        }
        String storedCompanyId = articleDao.getArticleCompanyId(articleId);
        if (StringUtils.isBlank(storedCompanyId)) {
            return false;
        }
        String tenantId = SessionUtils.getLoginTenantId();
        if (StringUtils.isBlank(tenantId) || "null".equals(tenantId)) {
            return false;
        }
        return !tenantId.equals(storedCompanyId);
    }

    @Transactional(readOnly = false)
    public int update(article article){
        checkArticleTenant(article.getId());
        int updatecount=articleDao.update(article);
        return updatecount;
    }

    /**
     * DS121 A: save fail-closed 400. Ba lo probe-proven:
     * (1) create thieu truong NOT NULL (companyid/userid/title/abstracts/
     * content/username) -> 500 NOT NULL (entity khong co @Valid, controller
     * khong @Valid); bat buoc day du o day, theo dung cac cot NOT NULL.
     * (2) companyid la STRING, con guard tenant generic
     * (CrudService.verifyTenantOwnership) chi doc object `company` ma UI
     * (articleForm.vue) khong bao gio gui, nen save voi companyid cua
     * tenant khac ghi thang dong vao tenant do; doi chieu companyid voi
     * session tenant o nhanh create (nhanh update: mapper UPDATE khong bao
     * gio ghi company_id, stored-row check generic bao ve dong da luu).
     * (3) save voi id khong ton tai -> 400 (pre: 500 BindingException vi
     * Article.xml khong co statement `get`, dong thoi lam chet MOI
     * save-update; them `get` vao Article.xml de stored-row check generic
     * chay duoc). auditstatus null -> mac dinh "0" theo DB default (mapper
     * INSERT truyen NULL tuong minh nen default DB khong bao gio ap dung;
     * UI cung gui "0").
     */
    @Override
    @Transactional(readOnly = false)
    public article save(article entity) {
        if (entity == null) {
            throw new ServiceException("Dữ liệu bài viết không được để trống");
        }
        if (StringUtils.isBlank(entity.getId())) {
            if (StringUtils.isBlank(entity.getCompanyid())) {
                throw new ServiceException("Định danh phòng khám không được để trống");
            }
            if (StringUtils.isBlank(entity.getUserid())) {
                throw new ServiceException("Định danh người dùng không được để trống");
            }
            if (StringUtils.isBlank(entity.getTitle())) {
                throw new ServiceException("Tiêu đề không được để trống");
            }
            if (StringUtils.isBlank(entity.getAbstracts())) {
                throw new ServiceException("Tóm tắt không được để trống");
            }
            if (StringUtils.isBlank(entity.getContent())) {
                throw new ServiceException("Nội dung chính không được để trống");
            }
            if (StringUtils.isBlank(entity.getUsername())) {
                throw new ServiceException("Tên người phát hành không được để trống");
            }
            String tenantId = SessionUtils.getLoginTenantId();
            if (StringUtils.isNotBlank(tenantId) && !"null".equals(tenantId)
                    && !tenantId.equals(entity.getCompanyid())) {
                throw new ServiceException("Không có quyền truy cập tenant khác");
            }
            if (StringUtils.isBlank(entity.getAuditstatus())) {
                entity.setAuditstatus("0");
            }
        } else {
            if (articleDao.getArticleCompanyId(entity.getId()) == null) {
                throw new ServiceException("Bài viết không tồn tại");
            }
        }
        return super.save(entity);
    }

    public Page<article> listPages(List<Parameter> params, int offset, int limit, String orderby) {
        ensureCompanyFilter(params);
        PageRequest pageRequest = new PageRequest(offset, limit, params, orderby);
        int total = this.dao.counts(pageRequest);
        List<article> list = null;
        if (total > 0) {
            list = this.dao.listPages(pageRequest);
        }

        return new Page<>((long)total, list);
    }
}
