package com.heslin.postopia.service.jwt;

import java.util.Date;
import java.util.function.Function;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.heslin.postopia.model.User;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Service
public class JWTService {

    @Value("${postopia.jwt.secret}")
    private String secret;

    @Value("${postopia.jwt.expiration}")
    private Long jwtExpiration;

    @Value("${postopia.jwt.refresh-token.expiration}")
    private Long refreshExpiration;

    @Value("${postopia.jwt.issuer}")
    private String issuer;

    private SecretKey getSigningKey() {
        byte[] keyBytes = secret.getBytes();
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public Long extractUserId(String token) {
        return Long.valueOf(extractClaim(token, Claims::getSubject));
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(token).getPayload();
    }

    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public String generateToken(User user) {
        return createToken(user.getId().toString(), jwtExpiration);
    }

    public String generateRefreshToken(User user) {
        return createToken(user.getId().toString(), refreshExpiration);
    }

    public String generateToken(Long id) {
        return createToken(id.toString(), jwtExpiration);
    }

    public String refresh(String token) {
        if (validateToken(token)) {
            return generateToken(extractUserId(token));
        }
        return null;
    }

    private String createToken(String subject, Long expiration) {
        return Jwts.builder()
                    .subject(subject)
                    .issuedAt(new Date(System.currentTimeMillis()))
                    .expiration(new Date(System.currentTimeMillis() + expiration))
                    .issuer(issuer)
                    .signWith(getSigningKey(), Jwts.SIG.HS256)
                    .compact();
    }

    public Boolean validateToken(String token) {
        return !isTokenExpired(token);
    }


}