package com.geeke.outpatient.service;

import com.geeke.common.data.Parameter;
import com.geeke.common.service.CrudService;
import com.geeke.outpatient.dao.RecipelInfoReviewDao;
import com.geeke.outpatient.vo.StatementVO;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Minimal unit tests for RecipelInfoReviewService.listPageStatement.
 * Covers the gate fix: a request without "params" (null parameter list)
 * used to throw NPE and surface as HTTP 500; it must now return an
 * empty page. No Spring context, no database: the DAO is mocked.
 */
public class RecipelInfoReviewServiceTest {

    private RecipelInfoReviewService service;
    private RecipelInfoReviewDao dao;

    @Before
    public void setUp() throws Exception {
        service = new RecipelInfoReviewService();
        dao = mock(RecipelInfoReviewDao.class);
        Field daoField = CrudService.class.getDeclaredField("dao");
        daoField.setAccessible(true);
        daoField.set(service, dao);
    }

    @Test
    public void nullParametersReturnsEmptyPageInsteadOfNpe() {
        com.geeke.common.data.Page<StatementVO> page = service.listPageStatement(null, "");
        Assert.assertNotNull(page);
        Assert.assertEquals(0L, page.getTotal());
        Assert.assertNotNull(page.getRows());
        Assert.assertTrue(page.getRows().isEmpty());
    }

    @Test
    public void emptyParametersReturnsEmptyPageFromDao() {
        when(dao.listPageStatement(any(), any())).thenReturn(Collections.emptyList());
        com.geeke.common.data.Page<StatementVO> page =
                service.listPageStatement(new ArrayList<Parameter>(), "");
        Assert.assertEquals(0L, page.getTotal());
        Assert.assertTrue(page.getRows().isEmpty());
    }

    @Test
    public void groupByParameterIsExtractedAndDataReturned() {
        List<StatementVO> rows = Arrays.asList(new StatementVO(), new StatementVO());
        when(dao.listPageStatement(any(), any())).thenReturn(rows);

        List<Parameter> params = new ArrayList<Parameter>();
        params.add(new Parameter("company_id", "=", "1"));
        params.add(new Parameter("`groupBy`", "=", "company_id"));
        com.geeke.common.data.Page<StatementVO> page = service.listPageStatement(params, "");

        Assert.assertEquals(2L, page.getTotal());
        Assert.assertEquals(2, page.getRows().size());

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<Parameter>> paramsCaptor = ArgumentCaptor.forClass(List.class);
        verify(dao).listPageStatement(paramsCaptor.capture(), eq(Arrays.asList("company_id")));
        for (Parameter p : paramsCaptor.getValue()) {
            Assert.assertNotEquals("`groupBy`", p.getColumnName());
        }
    }
}
