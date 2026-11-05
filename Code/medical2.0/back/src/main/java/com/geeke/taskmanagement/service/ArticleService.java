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
