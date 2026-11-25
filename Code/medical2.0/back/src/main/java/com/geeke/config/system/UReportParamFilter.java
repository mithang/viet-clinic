package com.geeke.config.system;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * DS670: UReport bao cao thieu tham so _u -> 500, chan som ve 400.
 * Thu vien UReport 2.2.9 (com.bstek.ureport, jar ngoai, khong sua duoc
 * source) doc thang tham so _u ma khong kiem tra null:
 * - HtmlPreviewServletAction.buildTitle(:161) goi decode(_u) khi _u
 *   null -> NullPointerException qua GET /ureport/preview (chi khi
 *   _title cung trong, vi co _title thi buildTitle khong doc _u);
 * - ExportPdf/ExportExcel/ExportExcel97/ExportWordServletAction nem
 *   "Report file can not be null." qua GET /ureport/pdf|excel|
 *   excel97|word khi thieu _u (ke ca khi co _title).
 * Day la loi dau vao cua client (thieu tham so bat buoc) nen tra 400
 * fail-closed theo mau DS94/DS160/DS635, thay vi de roi xuong
 * RuntimeException handler -> 500. Filter dung truoc UReportServlet
 * (dang ky o MyApplication.buildUReportServlet, /ureport/*) nen chan
 * duoc ca 5 duong. Chi chan khi _u == null (tham so vang mat hoan
 * toan); _u="" hoac _u sai ten file van giu hanh vi cu (200, thu vien
 * tu xu ly), /ureport/designer|image|chart|import|datasource|res
 * khong can _u nen khong chan.
 */
@Component
public class UReportParamFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(UReportParamFilter.class);

    private static final String PREVIEW_PATH = "/ureport/preview";

    private static final Set<String> EXPORT_PATHS = new HashSet<>(Arrays.asList(
            "/ureport/pdf", "/ureport/excel", "/ureport/excel97", "/ureport/word"));

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;
        String uri = request.getRequestURI();
        String ctx = request.getContextPath();
        String path = (ctx != null && !ctx.isEmpty() && uri.startsWith(ctx))
                ? uri.substring(ctx.length()) : uri;
        boolean isPreview = PREVIEW_PATH.equals(path);
        boolean needsReportFile = EXPORT_PATHS.contains(path)
                || (isPreview && isBlank(request.getParameter("_title")));
        if (needsReportFile && request.getParameter("_u") == null) {
            logger.warn("UReport thieu tham so _u: {} {}", request.getMethod(), path);
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write(
                    "{\"code\":\"400\",\"msg\":\"Thiếu tham số bắt buộc: _u\","
                    + "\"data\":{},\"type\":\"warning\"}");
            return;
        }
        chain.doFilter(req, res);
    }

    @Override
    public void destroy() {
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}
