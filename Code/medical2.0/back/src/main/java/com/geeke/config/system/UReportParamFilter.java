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
 *
 * DS676: cac action con lai cua cung thu vien jar (UReportServlet
 * dispatch bang reflection: retriveMethod lay doan path sau action url,
 * invokeMethod goi method do; method khong ton tai -> NoSuchMethodException
 * -> UReportServlet tu set 500; tham so thieu -> NPE / ReportDesignException
 * / SQLException / JsonParseException / ClassCastException -> 500, vi
 * servlet tho khong qua Spring MVC handler nen IAE cung 500, khong 400):
 * - Preview sub-method /ureport/preview/loadData|loadPrintPages|loadPagePaper:
 *   decode(_u) truoc isBlank -> _u thieu/rong -> ReportComputeException
 *   "Report file can not be null." 500 (filter DS670 chi chan path
 *   /ureport/preview chinh xac nen 3 duong con nay lot).
 * - Datasource /ureport/datasource/*: buildClass clazz null -> NPE
 *   (Class.forName(null)); loadMethods beanId null -> IAE "'name' must
 *   not be null"; buildDatabaseTables/testConnection/buildFields/previewData
 *   -> buildConnection: type null -> nhanh buildin, name null -> NPE;
 *   type=jdbc + driver null -> NPE (Class.forName(null)), url null ->
 *   SQLException "The url cannot be null", username null -> driver dung
 *   user mac dinh 'root' khong password -> SQLException "Access denied";
 *   buildFields/previewData: sql null -> NPE (parseSql); parameters JSON
 *   hong -> JsonParseException, phan tu khong phai Map / thieu name/type /
 *   type khong co trong DataType enum / defaultValue khong phai String ->
 *   NPE / ClassCastException / "No enum constant ... DataType.*".
 * - Method khong ton tai tren ca 4 action dispatch (preview/designer/
 *   datasource/chart): /ureport/<action>/ds676bogus -> 500 NoSuchMethod.
 * Tat ca -> 400 fail-closed o filter. Chi chan tham so THIEU/RONG hoac
 * sai dang co the kiem tra khong tac dung phu (blank, JSON shape,
 * DataType enum); ten sai nhung dung dang (beanId la, buildin name la,
 * driver/clazz la, _u ten file la) GIU hanh vi cu, de DS677 (can kiem
 * tra ton tai: containsBean / Utils.getBuildinDatasources /
 * Class.forName / provider). testConnection, chart storeData, import,
 * loadBuildinDatasources, loadReportProviders da tra 200-JSON/200 nen
 * khong chan. Password jdbc khong bat buoc (password rong la hop le).
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

    // DS676: method hop le cua tung action dispatch (jar goi bang reflection;
    // method la TOAN BO doan path sau action prefix, trim — retriveMethod
    // khong cat o '/' thu hai, nen "loadData/extra" cung la method la).
    private static final Set<String> DESIGNER_METHODS = new HashSet<>(Arrays.asList(
            "savePreviewData", "saveReportFile", "loadReport", "deleteReportFile",
            "loadReportProviders", "parseDatasetName", "scriptValidation",
            "conditionScriptValidation"));

    private static final String PREVIEW_PREFIX = "/ureport/preview/";

    private static final Set<String> PREVIEW_METHODS = new HashSet<>(Arrays.asList(
            "loadData", "loadPrintPages", "loadPagePaper"));

    private static final String DATASOURCE_PREFIX = "/ureport/datasource/";

    private static final Set<String> DATASOURCE_METHODS = new HashSet<>(Arrays.asList(
            "loadBuildinDatasources", "loadMethods", "buildClass",
            "buildDatabaseTables", "buildFields", "previewData", "testConnection"));

    private static final String CHART_PREFIX = "/ureport/chart/";

    private static final Set<String> CHART_METHODS = new HashSet<>(Arrays.asList(
            "storeData"));

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
        // DS676: preview sub-method, datasource, chart (method la).
        String actionError = validateOtherAction(path, request);
        if (actionError != null) {
            logger.warn("UReport {} {}: {}", request.getMethod(), path, actionError);
            sendBadRequest(response, actionError);
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
    // DS676: method la toan bo doan sau prefix (retriveMethod cua jar lay
    // het, khong cat '/'), method khong co trong whitelist -> 400.
    private static String validateDesigner(String path, HttpServletRequest request) {
        if (!path.startsWith(DESIGNER_PREFIX)) {
            return null;
        }
        String method = extractMethod(path, DESIGNER_PREFIX);
        if (method == null) {
            return null;
        }
        if (!DESIGNER_METHODS.contains(method)) {
            return "Action không tồn tại: " + method;
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
            case "loadReportProviders":
                return null;
            default:
                return null;
        }
    }

    // DS676: method la toan bo doan path sau action prefix, trim; null khi
    // khong co method (mo trang action, giu hanh vi cu 200).
    private static String extractMethod(String path, String prefix) {
        String rest = path.substring(prefix.length()).trim();
        return rest.isEmpty() ? null : rest;
    }

    // DS676: preview sub-method, datasource, chart. Tra msg loi, null neu
    // hop le hoac khong thuoc 3 action nay.
    private static String validateOtherAction(String path, HttpServletRequest request) {
        if (path.startsWith(PREVIEW_PREFIX)) {
            String method = extractMethod(path, PREVIEW_PREFIX);
            if (method == null) {
                return null;
            }
            if (!PREVIEW_METHODS.contains(method)) {
                return "Action không tồn tại: " + method;
            }
            if (isBlank(request.getParameter("_u"))) {
                return "Thiếu tham số bắt buộc: _u";
            }
            return null;
        }
        if (path.startsWith(DATASOURCE_PREFIX)) {
            String method = extractMethod(path, DATASOURCE_PREFIX);
            if (method == null) {
                return null;
            }
            if (!DATASOURCE_METHODS.contains(method)) {
                return "Action không tồn tại: " + method;
            }
            return validateDatasource(method, request);
        }
        if (path.startsWith(CHART_PREFIX)) {
            String method = extractMethod(path, CHART_PREFIX);
            if (method == null) {
                return null;
            }
            if (!CHART_METHODS.contains(method)) {
                return "Action không tồn tại: " + method;
            }
            // storeData da tra 200 voi moi input (chartData null -> return
            // som, _width/_height chi doc khi co chartData) nen khong chan.
            return null;
        }
        return null;
    }

    // DS676: datasource theo method. loadBuildinDatasources khong can tham
    // so; testConnection tu bat Exception tra JSON 200 nen khong chan.
    private static String validateDatasource(String method, HttpServletRequest request) {
        switch (method) {
            case "loadBuildinDatasources":
            case "testConnection":
                return null;
            case "loadMethods": {
                if (isBlank(request.getParameter("beanId"))) {
                    return "Thiếu tham số bắt buộc: beanId";
                }
                return null;
            }
            case "buildClass": {
                if (isBlank(request.getParameter("clazz"))) {
                    return "Thiếu tham số bắt buộc: clazz";
                }
                return null;
            }
            case "buildDatabaseTables": {
                return validateDatasourceConnection(request);
            }
            case "buildFields":
            case "previewData": {
                String connError = validateDatasourceConnection(request);
                if (connError != null) {
                    return connError;
                }
                if (isBlank(request.getParameter("sql"))) {
                    return "Thiếu tham số bắt buộc: sql";
                }
                return validateDatasourceParameters(request.getParameter("parameters"));
            }
            default:
                return null;
        }
    }

    // DS676: buildConnection cua jar: type == "jdbc" thi doc driver/url/
    // username/password (driver null -> NPE, url null -> SQLException,
    // username null -> driver dung user mac dinh, sai password -> 500);
    // type khac "jdbc" thi nhanh buildin, doc name (name null -> NPE).
    // Password khong bat buoc (password rong la hop le).
    private static String validateDatasourceConnection(HttpServletRequest request) {
        String type = request.getParameter("type");
        if (isBlank(type)) {
            return "Thiếu tham số bắt buộc: type";
        }
        if ("jdbc".equals(type)) {
            if (isBlank(request.getParameter("driver"))) {
                return "Thiếu tham số bắt buộc: driver";
            }
            if (isBlank(request.getParameter("url"))) {
                return "Thiếu tham số bắt buộc: url";
            }
            if (isBlank(request.getParameter("username"))) {
                return "Thiếu tham số bắt buộc: username";
            }
            return null;
        }
        if (isBlank(request.getParameter("name"))) {
            return "Thiếu tham số bắt buộc: name";
        }
        return null;
    }

    // DS676: buildParameters cua jar parse parameters bang codehaus Jackson
    // thanh List<Map>, roi deref tung phan tu: name/type .toString() (thieu
    // -> NPE), DataType.valueOf(type) (type la -> IAE, servlet tho -> 500),
    // defaultValue cast (String) (khong phai chuoi -> ClassCastException),
    // phan tu khong phai Map -> ClassCastException, "null" -> List null
    // -> NPE. Filter kiem tra dung hinh dang do truoc khi jar doc.
    private static String validateDatasourceParameters(String parameters) {
        if (isBlank(parameters)) {
            return null;
        }
        try {
            org.codehaus.jackson.map.ObjectMapper mapper =
                    new org.codehaus.jackson.map.ObjectMapper();
            Object parsed = mapper.readValue(parameters, java.util.ArrayList.class);
            if (!(parsed instanceof java.util.List)) {
                return "Tham số parameters không hợp lệ";
            }
            for (Object element : (java.util.List<?>) parsed) {
                if (!(element instanceof java.util.Map)) {
                    return "Tham số parameters không hợp lệ";
                }
                java.util.Map<?, ?> item = (java.util.Map<?, ?>) element;
                if (item.get("name") == null) {
                    return "Tham số parameters không hợp lệ: thiếu name";
                }
                Object type = item.get("type");
                if (type == null) {
                    return "Tham số parameters không hợp lệ: thiếu type";
                }
                try {
                    com.bstek.ureport.definition.datasource.DataType.valueOf(
                            String.valueOf(type));
                } catch (IllegalArgumentException e) {
                    return "Tham số parameters không hợp lệ: type không tồn tại";
                }
                Object defaultValue = item.get("defaultValue");
                if (defaultValue != null && !(defaultValue instanceof String)) {
                    return "Tham số parameters không hợp lệ: defaultValue phải là chuỗi";
                }
            }
            return null;
        } catch (Exception e) {
            return "Tham số parameters không phải JSON hợp lệ";
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
