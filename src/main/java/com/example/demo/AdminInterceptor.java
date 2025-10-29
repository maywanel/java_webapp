package com.example.demo;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.lang.NonNull;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@Component
public class AdminInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) throws Exception {
        HttpSession session = request.getSession(false);
        if (session != null)
            if (session.getAttribute("currentUser") != null)
                if ((Boolean) session.getAttribute("isAdmin") != null && (Boolean) session.getAttribute("isAdmin"))
                    return true;
        response.sendRedirect("/error/403");
        return false;
    }
}
