package com.example.user.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SignatureException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtPasswordReset {

    // Clave secreta para firmar los tokens (debe estar en Base64 y ser segura)
    @Value("${jwt.secret:defaultSecretKeyForDevelopmentOnlyNotForProduction}")
    private String secret;

    // Tiempo de expiración del token de recuperación (15 minutos)
    @Value("${jwt.reset.expiration:900000}")
    private Long jwtExpirationReset;

    // Convierte la clave secreta en Base64 a un objeto Key para firmar
    private Key getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    // Genera un token JWT de recuperación de contraseña
    public String generarToken(String username) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("accion", "recuperacion"); // Claim exclusivo para recuperación
        return createToken(claims, username);
    }

    // Crea el token JWT con los claims y subject proporcionados
    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + jwtExpirationReset))
                .signWith(getSigningKey())
                .compact();
    }

    // Extrae el username del token
    public String obtenerUsername(String token) {
        return obtenerClaim(token, Claims::getSubject);
    }

    // Obtiene la fecha de expiración del token
    public Date obtenerFechaExpiracion(String token) {
        return obtenerClaim(token, Claims::getExpiration);
    }

    // Obtiene la acción almacenada en los claims
    public String obtenerAccion(String token) {
        return obtenerClaim(token, claims -> claims.get("accion", String.class));
    }

    // Método genérico para obtener cualquier claim
    public <T> T obtenerClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = obtenerTodasLasClaims(token);
        return claimsResolver.apply(claims);
    }

    // Obtiene todos los claims del token
    private Claims obtenerTodasLasClaims(String token) {
        return Jwts.parser()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // Verifica si el token ha expirado
    public boolean esTokenExpirado(String token) {
        return obtenerFechaExpiracion(token).before(new Date());
    }

    // Valida que el token corresponda al usuario, sea de acción recuperación y no esté expirado
    public boolean validarToken(String token, String username) {
        try {
            final String tokenUsername = obtenerUsername(token);
            final String accion = obtenerAccion(token);
            return tokenUsername.equals(username) && "recuperacion".equals(accion) && !esTokenExpirado(token);
        } catch (Exception e) {
            return false;
        }
    }

    // Verifica la validez general del token (firma, formato, expiración)
    public boolean esTokenValido(String token) {
        try {
            Jwts.parser()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (SignatureException | MalformedJwtException | ExpiredJwtException |
                 UnsupportedJwtException | IllegalArgumentException e) {
            System.err.println("Token inválido: " + e.getMessage());
            return false;
        }
    }

    // Extrae el token del encabezado Authorization (elimina "Bearer ")
    public String extraerTokenDelHeader(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }
}
