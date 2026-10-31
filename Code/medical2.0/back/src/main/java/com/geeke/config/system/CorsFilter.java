package com.geeke.config.system;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component  // vượtvùngfilterXử lý
public class CorsFilter implements Filter {

    final static org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(CorsFilter.class);

    /**
     * cho phép của vượtvùngđếnnguồn, nhiềuCáidùngPhân tách bằng dấu phẩy. Mặc địnhcho phépnàyđịa phươngmởphát. 
     * Môi trường production nênQuamôi trườngthay đổilượng CORS_ALLOWED_ORIGINS cấu hìnhcụ thểvùngtên. 
     */
    @Value("${cors.allowed-origins:http://localhost:7020,http://127.0.0.1:7020}")
    private String allowedOrigins;

    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        String origin = request.getHeader("Origin");

        // cho phép của đếnnguồnDanh sách
        List<String> allowedOriginList = Arrays.asList(allowedOrigins.split(","));

        // Nếucấu hình là  * Thì cho phép mọi nguồn(khôngđẩykhuyến nghị và  Allow-Credentials mộtbắt đầusử dụng)
        if ("*".equals(allowedOrigins.trim())) {
            response.setHeader("Access-Control-Allow-Origin", "*");
        } else if (origin != null && allowedOriginList.contains(origin.trim())) {
            // reflectionVui lòngyêu cầu của  Origin, hỗ trợkèmchứng từ của vượtvùngVui lòngyêu cầu
            response.setHeader("Access-Control-Allow-Origin", origin);
            response.setHeader("Access-Control-Allow-Credentials", "true");
        }

        response.setHeader("Access-Control-Allow-Methods", "POST, PUT, GET, OPTIONS, DELETE");
        response.setHeader("Access-Control-Max-Age", "3600");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type, X-Token, Authorization");

        // trướckiểmVui lòngyêu cầutrực tiếpTrả về
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            response.setStatus(HttpServletResponse.SC_OK);
            return;
        }

        chain.doFilter(req, res);
    }
    public void init(FilterConfig filterConfig) {}
    public void destroy() {}
}