import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class AuthIntegrationTest {
    @BeforeAll
    static void setUp() {
        RestAssured.baseURI = "http://localhost:4004";
    }

    private String login(String email, String password) {
        String loginPayload = String.format("""
                {
                    "email": "%s",
                    "password": "%s"
                }
                """, email, password);

        return given()
                .contentType("application/json")
                .body(loginPayload)
                .when()
                .post("auth/login")
                .then()
                .statusCode(200)
                .body("token", notNullValue())
                .extract()
                .jsonPath()
                .getString("token");
    }

    @Test
    public void shouldReturnTokenWithValidCredentials() {
        String token = login("testuser@test.com", "password123");
        assertNotNull(token);
    }

    @Test
    public void shouldReturnUnauthorizedOnInvalidCredentials() {
        String loginPayload = """
                {
                    "email": "invalid_user@test.com",
                    "password": "wrongpassword"
                }
                """;

        given()
                .contentType("application/json")
                .body(loginPayload)
                .when()
                .post("auth/login")
                .then()
                .statusCode(401);
    }

    @Test
    public void shouldValidateBearerTokenSuccessfully() {
        String token = login("testuser@test.com", "password123");

        given()
                .header("Authorization", "Bearer " + token)
                .when()
                .get("auth/validate")
                .then()
                .statusCode(200);
    }

    @Test
    public void shouldRejectInvalidOrMissingBearerToken() {
        given()
                .when()
                .get("auth/validate")
                .then()
                .statusCode(400);

        given()
                .header("Authorization", "Bearer not-a-real-token")
                .when()
                .get("auth/validate")
                .then()
                .statusCode(401);
    }
}
