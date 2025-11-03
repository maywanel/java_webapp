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
        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute("currentUser") == null) {
            logger.debug("No active session or no currentUser in session; redirecting to /login");
            response.sendRedirect("/login");
            return false;
        }
        if (Boolean.TRUE.equals(session.getAttribute("isAdmin")))
            return true;
        logger.debug("Authenticated user is not admin; redirecting to /error/403");
        response.sendRedirect("/error/403");
        return false;
    }
}
