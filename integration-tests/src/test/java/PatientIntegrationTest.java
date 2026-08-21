import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

public class PatientIntegrationTest {

    @BeforeAll
    static void setUp() {
        RestAssured.baseURI = "http://localhost:4004";
    }

    private String login() {
        String loginPayload = """
                {
                    "email": "testuser@test.com",
                    "password": "password123"
                }
                """;

        return given()
                .contentType("application/json")
                .body(loginPayload)
                .when()
                .post("auth/login")
                .then()
                .statusCode(200)
                .extract()
                .jsonPath()
                .getString("token");
    }

    private String createPatient(String token, String email) {
        String payload = String.format("""
                {
                    "name": "Integration Test Patient %s",
                    "email": "%s",
                    "address": "123 Main Street",
                    "dateOfBirth": "1997-02-21",
                    "registeredDate": "2026-02-06"
                }
                """, UUID.randomUUID().toString().substring(0, 8), email);

        return given()
                .header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .body(payload)
                .when()
                .post("api/patients")
                .then()
                .statusCode(200)
                .body("id", notNullValue())
                .extract()
                .jsonPath()
                .getString("id");
    }

    @Test
    public void shouldReturnPatientsWithValidToken() {
        String token = login();

        given()
                .header("Authorization", "Bearer " + token)
                .when()
                .get("api/patients")
                .then()
                .statusCode(200)
                .body("$", notNullValue());
    }

    @Test
    public void shouldRejectPatientsRequestWithoutToken() {
        given()
                .when()
                .get("api/patients")
                .then()
                .statusCode(401);
    }

    @Test
    public void shouldCreatePatientWithValidToken() {
        String token = login();
        String email = "patient-" + UUID.randomUUID() + "@test.com";

        given()
                .header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .body(String.format("""
                        {
                            "name": "New Patient",
                            "email": "%s",
                            "address": "456 Health Avenue",
                            "dateOfBirth": "1990-05-10",
                            "registeredDate": "2026-02-07"
                        }
                        """, email))
                .when()
                .post("api/patients")
                .then()
                .statusCode(200)
                .body("email", equalTo(email))
                .body("name", equalTo("New Patient"));
    }

    @Test
    public void shouldUpdatePatientWithValidToken() {
        String token = login();
        String email = "patient-update-" + UUID.randomUUID() + "@test.com";
        String patientId = createPatient(token, email);
        String updatedEmail = "updated-" + UUID.randomUUID() + "@test.com";

        given()
                .header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .body(String.format("""
                        {
                            "name": "Updated Patient",
                            "email": "%s",
                            "address": "789 Updated Lane",
                            "dateOfBirth": "1988-07-18",
                            "registeredDate": "2026-02-08"
                        }
                        """, updatedEmail))
                .when()
                .put("api/patients/" + patientId)
                .then()
                .statusCode(200)
                .body("id", equalTo(patientId))
                .body("email", equalTo(updatedEmail))
                .body("name", equalTo("Updated Patient"));
    }

    @Test
    public void shouldDeletePatientWithValidToken() {
        String token = login();
        String patientId = createPatient(token, "patient-delete-" + UUID.randomUUID() + "@test.com");

        given()
                .header("Authorization", "Bearer " + token)
                .when()
                .delete("api/patients/" + patientId)
                .then()
                .statusCode(204);
    }
}
