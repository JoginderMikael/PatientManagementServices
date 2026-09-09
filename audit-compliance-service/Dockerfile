FROM maven:3.9.9-eclipse-temurin-21 AS builder

WORKDIR /workspace

COPY pom.xml .
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

RUN mvn -pl audit-compliance-service -am dependency:go-offline -B

COPY audit-compliance-service/src audit-compliance-service/src

RUN mvn -pl audit-compliance-service -am clean package -DskipTests

FROM eclipse-temurin:21-jre AS runner

WORKDIR /app

COPY --from=builder /workspace/audit-compliance-service/target/audit-compliance-service-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 4015

USER 10001:10001

ENTRYPOINT ["java", "-jar", "app.jar"]