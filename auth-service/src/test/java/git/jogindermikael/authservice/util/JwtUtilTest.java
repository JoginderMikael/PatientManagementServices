package git.jogindermikael.authservice.util;

import git.jogindermikael.authservice.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtUtilTest {

    private static final String SECRET = "MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY=";

    @Test
    void issuesShortLivedIdentityAndAuthorizationClaims() {
        UUID userId = UUID.randomUUID();
        User user = new User();
        user.setId(userId);
        user.setEmail("admin@example.test");
        user.setRole("ADMIN");
        JwtUtil jwtUtil = new JwtUtil(SECRET, "patient-management-auth", "patient-management-api", Duration.ofMinutes(15));

        String token = jwtUtil.generateToken(user);
        Claims claims = Jwts.parser()
                .verifyWith(Keys.hmacShaKeyFor(Base64.getDecoder().decode(SECRET.getBytes(StandardCharsets.UTF_8))))
                .build()
                .parseSignedClaims(token)
                .getPayload();

        assertEquals(userId.toString(), claims.getSubject());
        assertEquals(userId.toString(), claims.get("user_id", String.class));
        assertEquals("admin@example.test", claims.get("email", String.class));
        assertEquals("ADMIN", claims.get("role", String.class));
        assertEquals("patient-management-auth", claims.getIssuer());
        assertTrue(claims.getAudience().contains("patient-management-api"));
        assertNotNull(claims.getId());
        assertTrue(claims.getExpiration().getTime() - claims.getIssuedAt().getTime() <= Duration.ofMinutes(15).toMillis());
        assertDoesNotThrow(() -> jwtUtil.validateToken(token));
    }

    @Test
    void rejectsTokenForAnotherAudience() {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail("admin@example.test");
        user.setRole("ADMIN");
        JwtUtil issuer = new JwtUtil(SECRET, "patient-management-auth", "patient-management-api", Duration.ofMinutes(15));
        JwtUtil otherAudience = new JwtUtil(SECRET, "patient-management-auth", "other-api", Duration.ofMinutes(15));

        assertThrows(JwtException.class, () -> otherAudience.validateToken(issuer.generateToken(user)));
    }
}
