package edu.fafu.config;

import cn.dev33.satoken.stp.StpUtil;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Order(-100)
public class LocalAdminFilter implements Filter {

    private static final String ADMIN_ROLE = "admin";
    private static final int LOCAL_ADMIN_ID = 0;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;

        if (isLocalIp(req) && !StpUtil.isLogin() && isAdminRoute(req.getRequestURI())) {
            StpUtil.login(LOCAL_ADMIN_ID);
            StpUtil.getSession().set("role", ADMIN_ROLE);
        }

        chain.doFilter(request, response);
    }

    private boolean isAdminRoute(String uri) {
        return uri.startsWith("/manager/") || uri.startsWith("/page/manager/") || uri.startsWith("/config/");
    }

    private boolean isLocalIp(HttpServletRequest request) {
        // 只信任底层 TCP 连接的真实远端地址；绝不信任客户端可伪造的
        // X-Forwarded-For / X-Real-IP 等请求头，避免被远程诱导为"本机"从而绕过管理员鉴权
        String ip = request.getRemoteAddr();
        return "127.0.0.1".equals(ip) || "0:0:0:0:0:0:0:1".equals(ip) || "localhost".equals(ip);
    }
}