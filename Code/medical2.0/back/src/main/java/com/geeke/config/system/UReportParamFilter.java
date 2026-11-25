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
 *
 * DS675: cac action designer cua cung thu vien jar doc thang tham so
 * ma khong kiem tra null / XML hong:
 * - savePreviewData: content null -> NPE (IOUtils.toInputStream(null));
 *   content khong phai XML hop le -> ReportParser.parse nem SAXParseException;
 *   content co root khac "ureport" -> "Unknow report file.".
 * - saveReportFile: file null -> NPE (file.startsWith(prefix)); content
 *   null -> "Column 'content' cannot be null" (DB); content XML hong
 *   -> parse loi sau khi save (ReportParser.parse + cacheReportDefinition).
 * - loadReport / deleteReportFile: file null/rong -> ReportDesignException
 *   "Report file can not be null." / "Report [] not support.".
 * - parseDatasetName: expr null/rong/khong co Identifier -> NPE o
 *   DatasetContext.Identifier().getText() (ANTLRInputStream(null) cung NPE).
 * - scriptValidation / conditionScriptValidation: content null -> NPE
 *   (content rong thi jar tra JSON loi validation 200 nen chi chan null).
 * Tat ca deu la loi dau vao cua client -> 400 fail-closed o filter,
 * theo mau DS670. Chi chan tham so THIEU/RONG hoac XML sai dang:
 * ten file sai nhung dung dang ten (loadReport file la) giu hanh vi cu.
 */
@Component
public class UReportParamFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(UReportParamFilter.class);

    private static final String PREVIEW_PATH = "/ureport/preview";

    private static final Set<String> EXPORT_PATHS = new HashSet<>(Arrays.asList(
            "/ureport/pdf", "/ureport/excel", "/ureport/excel97", "/ureport/word"));

    // DS675: designer method -> ten tham so file/content/expr jar doc.
    private static final String DESIGNER_PREFIX = "/ureport/designer/";

    private static final Set<String> DESIGNER_FILE_METHODS = new HashSet<>(Arrays.asList(
            "loadReport", "deleteReportFile"));

    private static final java.util.regex.Pattern DATASET_IDENT =
            java.util.regex.Pattern.compile("[A-Za-z][A-Za-z0-9_-]*");

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
            sendBadRequest(response, "Thiếu tham số bắt buộc: _u");
            return;
        }
        // DS675: designer actions (method la doan path sau /ureport/designer/).
        String designerError = validateDesigner(path, request);
        if (designerError != null) {
            logger.warn("UReport designer {} {}: {}", request.getMethod(), path, designerError);
            sendBadRequest(response, designerError);
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

    private static void sendBadRequest(HttpServletResponse response, String msg) throws IOException {
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(
                "{\"code\":\"400\",\"msg\":\"" + msg + "\","
                + "\"data\":{},\"type\":\"warning\"}");
    }

    // DS675: tra msg loi khi designer input thieu/sai dang, null neu hop le.
    // GET /ureport/designer (mo trang designer, khong co method) di qua.
    private static String validateDesigner(String path, HttpServletRequest request) {
        if (!path.startsWith(DESIGNER_PREFIX)) {
            return null;
        }
        String method = path.substring(DESIGNER_PREFIX.length());
        int slash = method.indexOf('/');
        if (slash >= 0) {
            method = method.substring(0, slash);
        }
        if (method.isEmpty()) {
            return null;
        }
        switch (method) {
            case "savePreviewData": {
                String content = request.getParameter("content");
                if (isBlank(content)) {
                    return "Thiếu tham số bắt buộc: content";
                }
                if (!isValidReportXml(content)) {
                    return "Nội dung báo cáo không phải XML ureport hợp lệ";
                }
                return null;
            }
            case "saveReportFile": {
                if (isBlank(request.getParameter("file"))) {
                    return "Thiếu tham số bắt buộc: file";
                }
                String content = request.getParameter("content");
                if (isBlank(content)) {
                    return "Thiếu tham số bắt buộc: content";
                }
                if (!isValidReportXml(content)) {
                    return "Nội dung báo cáo không phải XML ureport hợp lệ";
                }
                return null;
            }
            case "loadReport":
            case "deleteReportFile": {
                if (isBlank(request.getParameter("file"))) {
                    return "Thiếu tham số bắt buộc: file";
                }
                return null;
            }
            case "parseDatasetName": {
                String expr = request.getParameter("expr");
                if (isBlank(expr)) {
                    return "Thiếu tham số bắt buộc: expr";
                }
                if (!hasDatasetIdentifier(expr)) {
                    return "Biểu thức dataset không hợp lệ: expr";
                }
                return null;
            }
            case "scriptValidation":
            case "conditionScriptValidation": {
                // Chi chan null: content rong jar tra JSON loi validation 200.
                if (request.getParameter("content") == null) {
                    return "Thiếu tham số bắt buộc: content";
                }
                return null;
            }
            default:
                return null;
        }
    }

    // DS675: XML phai well-formed va co root <ureport>. Parse chi de
    // kiem tra, khong load DTD/entity ngoai (chong XXE); ca 7 bao cao
    // that trong DB deu khong co DOCTYPE nen chan DOCTYPE luon.
    private static boolean isValidReportXml(String xml) {
        try {
            javax.xml.parsers.DocumentBuilderFactory factory =
                    javax.xml.parsers.DocumentBuilderFactory.newInstance();
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            factory.setXIncludeAware(false);
            factory.setExpandEntityReferences(false);
            factory.setNamespaceAware(false);
            javax.xml.parsers.DocumentBuilder builder = factory.newDocumentBuilder();
            org.w3c.dom.Document doc = builder.parse(
                    new java.io.ByteArrayInputStream(
                            xml.getBytes(java.nio.charset.StandardCharsets.UTF_8)));
            return "ureport".equals(doc.getDocumentElement().getTagName());
        } catch (Exception e) {
            return false;
        }
    }

    // DS675: lexer DSL cua jar chi tra Identifier khi expr chua token
    // bat dau bang chu cai (tiep theo la chu/so/_/-); literal '...' /
    // "..." la STRING token nen loai truoc khi kiem tra. Da kiem chung
    // live: "123", "@@@", "(((", '"abc"', "é", "_1" deu NPE 500.
    private static boolean hasDatasetIdentifier(String expr) {
        String noStrings = expr.replaceAll("'[^']*'", "").replaceAll("\"[^\"]*\"", "");
        return DATASET_IDENT.matcher(noStrings).find();
    }
}
