package com.example.demo.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.lang.NonNull;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@Component
public class AdminInterceptor implements HandlerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(AdminInterceptor.class);

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) throws Exception {
        if (isPublicAuthRequest(request)) {
            return true;
        }

        HttpSession session = request.getSession(false);
        boolean isApiRequest = request.getRequestURI().startsWith("/users/");

        if (session == null || session.getAttribute("currentUser") == null) {
            logger.debug("No active session or no currentUser in session");
            if (isApiRequest)
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Authentication required");
            else
                response.sendRedirect("/login");
            return false;
        }
        if (Boolean.TRUE.equals(session.getAttribute("isAdmin")))
            return true;
        logger.debug("Authenticated user is not admin");
        if (isApiRequest)
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Admin access required");
        else
            response.sendRedirect("/error/403");
        return false;
    }

    private boolean isPublicAuthRequest(HttpServletRequest request) {
        String contextPath = request.getContextPath();
        String path = request.getRequestURI();
        if (contextPath != null && !contextPath.isEmpty() && path.startsWith(contextPath))
            path = path.substring(contextPath.length());
        if (path.endsWith("/") && path.length() > 1)
            path = path.substring(0, path.length() - 1);

        String method = request.getMethod();
        boolean isSignup = "/users".equals(path) && "POST".equalsIgnoreCase(method);
        boolean isLogin = "/users/login".equals(path) && "POST".equalsIgnoreCase(method);
        boolean isLogout = "/users/logout".equals(path);

        return isSignup || isLogin || isLogout;
    }
}
