package com.example.demo.controller;

import com.example.demo.service.TokenService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class CookieController {

    @Autowired
    private TokenService tokenService;

    @GetMapping("/login")
    public String login(@CookieValue(value = "token", required = false) String token, HttpServletResponse response) {
        if (token != null && tokenService.isValidToken(token))
            return "already logged in";
        String newToken = tokenService.createToken(7);
        Cookie cookie = new Cookie("token", newToken);
        cookie.setMaxAge(7 * 24 * 60 * 60); // 7 days
        cookie.setPath("/");
        cookie.setSecure(false);
        cookie.setHttpOnly(true);
        response.addCookie(cookie);
        return "logged in with token: " + newToken;
    }

    @GetMapping("/logout")
    public String logout(@CookieValue(value = "token", required = false) String token, HttpServletResponse response) {
        if (token == null || !tokenService.isValidToken(token))
            return "You are not logged in";
        tokenService.invalidateToken(token);
        Cookie cookie = new Cookie("token", "");
        cookie.setMaxAge(0);
        cookie.setPath("/");
        response.addCookie(cookie);
        return "Logout successful!";
    }

    @GetMapping("/home")
    public String home(@CookieValue(value = "token", required = false) String token) {
        if (token == null)
            return "Please login first";
        if (!tokenService.isValidToken(token))
            return "Invalid token. Please login again.";
        return "Welcome to the Home Page!";
    }
}
