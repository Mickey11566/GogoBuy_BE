package com.example.demo.service;

import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.stereotype.Service;

import io.jsonwebtoken.Jwts;

@Service
public class JwtService {
	private final SecretKey SECRET_KEY = Jwts.SIG.HS256.key().build();
	
	// 生成開通用的 Token (有效期 24 小時)
    public String createActivationToken(String userId) {
        return Jwts.builder()
                .subject(userId)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 1))
                .signWith(SECRET_KEY)
                .compact();
    }
    
 // 解析並驗證 Token
    public String parseActivationToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(SECRET_KEY)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .getSubject(); // 返回的就是 UUID
        } catch (Exception e) {
            return null; // Token 無效或過期
        }
    }
}
