package edu.fafu.controller.errorcontroller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Controller;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.RequestMapping;

import java.nio.charset.StandardCharsets;

@Controller
public class ErrorController implements org.springframework.boot.webmvc.error.ErrorController {

    private static final Logger log = LoggerFactory.getLogger(ErrorController.class);

    @RequestMapping("/error")
    public void handleError(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Integer status = (Integer) request.getAttribute("jakarta.servlet.error.status_code");
        String uri = (String) request.getAttribute("jakarta.servlet.error.request_uri");
        Throwable throwable = (Throwable) request.getAttribute("jakarta.servlet.error.exception");

        if (uri != null && (uri.startsWith("/page/") || uri.startsWith("/user/") || uri.startsWith("/merchant/") || uri.startsWith("/manager/") || uri.startsWith("/postman/") || uri.startsWith("/vip/") || uri.startsWith("/ai/") || uri.startsWith("/config/") || uri.startsWith("/tool/") || uri.startsWith("/file/"))) {
            response.setStatus(status != null ? status : HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.setContentType("application/json;charset=UTF-8");
            String message = throwable != null ? throwable.getMessage() : "服务器内部错误";
            if (message == null) {
                switch (status != null ? status : 500) {
                    case 403 -> message = "访问被拒绝";
                    case 404 -> message = "资源不存在";
                    default -> message = "服务器内部错误";
                }
            }
            log.warn("错误页面: {} {} - {}", status, uri, message);
            response.getWriter().write("{\"code\":" + (status != null ? status : 500) + ",\"message\":\"" + message.replace("\"", "'") + "\",\"data\":null}");
            response.getWriter().flush();
            return;
        }

        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("text/html;charset=UTF-8");
        ClassPathResource resource = new ClassPathResource("static/index.html");
        String content = new String(FileCopyUtils.copyToByteArray(resource.getInputStream()), StandardCharsets.UTF_8);
        response.getWriter().write(content);
        response.getWriter().flush();
    }
}