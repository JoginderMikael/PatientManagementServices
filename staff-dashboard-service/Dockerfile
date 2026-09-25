# syntax=docker/dockerfile:1
FROM maven:3.9.9-eclipse-temurin-21 AS builder

WORKDIR /workspace

COPY pom.xml .
COPY reliability-core/pom.xml reliability-core/pom.xml
COPY api-gateway/pom.xml api-gateway/pom.xml
COPY auth-service/pom.xml auth-service/pom.xml
COPY patient-service/pom.xml patient-service/pom.xml
COPY billing-service/pom.xml billing-service/pom.xml
COPY analytics-service/pom.xml analytics-service/pom.xml
COPY appointment-service/pom.xml appointment-service/pom.xml
COPY ehr-service/pom.xml ehr-service/pom.xml
COPY insurance-service/pom.xml insurance-service/pom.xml
COPY inventory-pharmacy-service/pom.xml inventory-pharmacy-service/pom.xml
COPY notification-service/pom.xml notification-service/pom.xml
COPY audit-compliance-service/pom.xml audit-compliance-service/pom.xml
COPY patient-portal-service/pom.xml patient-portal-service/pom.xml
COPY staff-dashboard-service/pom.xml staff-dashboard-service/pom.xml
COPY infrastructure/pom.xml infrastructure/pom.xml
COPY integration-tests/pom.xml integration-tests/pom.xml

RUN --mount=type=cache,id=patient-management-maven-v2,target=/root/.m2,sharing=locked \
    for attempt in 1 2 3; do \
        if mvn -pl staff-dashboard-service -am dependency:resolve dependency:resolve-plugins -DincludeScope=runtime -DskipTests -B; then exit 0; fi; \
        echo "Maven download failed (attempt $attempt/3); retrying..."; \
        sleep $((attempt * 5)); \
    done; \
    exit 1

COPY reliability-core/src reliability-core/src
COPY staff-dashboard-service/src staff-dashboard-service/src

RUN --mount=type=cache,id=patient-management-maven-v2,target=/root/.m2,sharing=locked \
    mvn -pl staff-dashboard-service -am clean package -DskipTests

FROM eclipse-temurin:21-jre AS runner

WORKDIR /app

COPY --from=builder /workspace/staff-dashboard-service/target/staff-dashboard-service-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 4017

USER 10001:10001

ENTRYPOINT ["java", "-jar", "app.jar"]
