package git.jogindermikael.apigateway;

import java.time.Duration;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.test.web.reactive.server.WebTestClient;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@AutoConfigureWebTestClient
class Phase4GatewayTest {
    @Autowired RouteLocator routes;
    @Autowired WebTestClient client;

    @Test void upgradedGatewayLoadsComplianceAndFhirRoutes() {
        Set<String> ids = routes.getRoutes().map(route -> route.getId()).collectList()
                .block(Duration.ofSeconds(10)).stream().collect(Collectors.toSet());
        assertTrue(ids.containsAll(Set.of("fhir-patient-route", "compliance-route")));
    }

    @Test void phase4RoutesRejectAnonymousRequestsBeforeForwarding() {
        client.get().uri("/api/fhir/metadata").exchange().expectStatus().isUnauthorized();
        client.get().uri("/api/compliance/cases").exchange().expectStatus().isUnauthorized();
        client.post().uri("/api/compliance/privacy/consents").exchange().expectStatus().isUnauthorized();
    }
}
