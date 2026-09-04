package git.jogindermikael.authservice.util;

import git.jogindermikael.authservice.model.User;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;


@Component
public class JwtUtil {

    private final Key secretKey;
    private final String issuer;
    private final String audience;
    private final Duration accessTokenTtl;

    public JwtUtil(@Value("${jwt.secret}") String secret,
                   @Value("${jwt.issuer}") String issuer,
                   @Value("${jwt.audience}") String audience,
                   @Value("${jwt.access-token-ttl}") Duration accessTokenTtl) {
        byte[] keyBytes = Base64.getDecoder().decode(secret.getBytes(StandardCharsets.UTF_8));
        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
        this.issuer = issuer;
        this.audience = audience;
        this.accessTokenTtl = accessTokenTtl;
    }

    public String generateToken(User user) {
        Instant issuedAt = Instant.now();
        return Jwts.builder()
                .issuer(issuer)
                .subject(user.getId().toString())
                .audience().add(audience).and()
                .id(UUID.randomUUID().toString())
                .claim("user_id", user.getId().toString())
                .claim("email", user.getEmail())
                .claim("role", user.getRole())
                .claim("roles", List.of(user.getRole()))
                .claim("scope", user.getRole().toLowerCase(Locale.ROOT))
                .issuedAt(Date.from(issuedAt))
                .expiration(Date.from(issuedAt.plus(accessTokenTtl)))
                .signWith(secretKey)
                .compact();
    }

    public void validateToken(String token) {
        try{
            var claims = Jwts.parser().verifyWith((SecretKey) secretKey)
                    .requireIssuer(issuer)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            if (!claims.getAudience().contains(audience)) {
                throw new JwtException("Invalid JWT audience");
            }
        } catch (SignatureException e) {
            throw new JwtException("Invalid JWT signature");
        } catch (JwtException e){
            throw new JwtException("Invalid JWT token");
        }
    }
}
