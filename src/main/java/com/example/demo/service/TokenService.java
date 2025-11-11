package com.example.demo.service;

import com.example.demo.model.Token;
import com.example.demo.repository.TokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class TokenService {
    
    @Autowired
    private TokenRepository tokenRepository;
    
    public String createToken(int daysValid) {
        String tokenValue = UUID.randomUUID().toString();
        LocalDateTime expiresAt = LocalDateTime.now().plusDays(daysValid);
        Token token = new Token(tokenValue, expiresAt);
        tokenRepository.save(token);
        return tokenValue;
    }
    
    public boolean isValidToken(String tokenValue) {
        Optional<Token> tokenOpt = tokenRepository.findByTokenValue(tokenValue);
        
        if (tokenOpt.isEmpty())
            return false;
        Token token = tokenOpt.get();
        if (!token.isValid() || token.isExpired())
            return false;
        return true;
    }

    @Transactional
    public boolean invalidateToken(String tokenValue) {
        Optional<Token> tokenOpt = tokenRepository.findByTokenValue(tokenValue);
        
        if (tokenOpt.isEmpty())
            return false;
        Token token = tokenOpt.get();
        token.setValid(false);
        tokenRepository.save(token);
        return true;
    }
    
    @Transactional
    public void cleanupExpiredTokens() {
        tokenRepository.deleteByExpiresAtBefore(LocalDateTime.now());
    }
}