import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

/** Verifies that a real auth-service token passes gateway and downstream JWT validation. */
@EnabledIfSystemProperty(named = "runLiveIntegrationTests", matches = "true")
class AuthGatewayCompatibilityTest {

    @BeforeAll
    static void setUp() {
        RestAssured.baseURI = "http://localhost:4004";
    }

    @Test
    void authIssuedPatientTokenIsAcceptedByGateway() {
        String token = given()
                .contentType("application/json")
                .body("""
                        {
                          "email": "patient.one@example.test",
                          "password": "DevOnly!234"
                        }
                        """)
                .when()
                .post("/auth/login")
                .then()
                .statusCode(200)
                .body("token", notNullValue())
                .extract()
                .path("token");

        given()
                .header("Authorization", "Bearer " + token)
                .when()
                .get("/api/portal/patients/10000000-0000-4000-8000-000000000003/overview")
                .then()
                .statusCode(200)
                .body("patientId", equalTo("10000000-0000-4000-8000-000000000003"));
    }
}
