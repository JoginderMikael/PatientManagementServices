package git.jogindermikael.analyticsservice.mapper;

import git.jogindermikael.analyticsservice.dto.*;
import git.jogindermikael.analyticsservice.model.*;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
public class PopulationHealthMapper {
    public HealthTrend toTrend(HealthTrendRequest request) {
        return new HealthTrend(UUID.randomUUID(), request.metric(), request.segment(), request.periodStart(), request.periodEnd(), request.value(), Instant.now());
    }

    public ChronicDiseaseCohort toCohort(ChronicDiseaseCohortRequest request) {
        return new ChronicDiseaseCohort(UUID.randomUUID(), request.condition(), request.riskLevel(), request.patientCount(), Instant.now());
    }

    public RegulatoryReport toReport(RegulatoryReportRequest request) {
        return new RegulatoryReport(UUID.randomUUID(), request.reportType(), request.periodStart(), request.periodEnd(), "GENERATED", Instant.now());
    }
}
