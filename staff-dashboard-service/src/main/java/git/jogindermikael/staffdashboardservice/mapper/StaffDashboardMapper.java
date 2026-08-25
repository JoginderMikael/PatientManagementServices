package git.jogindermikael.staffdashboardservice.mapper;

import git.jogindermikael.staffdashboardservice.dto.StaffDashboardDtos.*;
import git.jogindermikael.staffdashboardservice.model.StaffDashboardModels.*;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
public class StaffDashboardMapper {
    public ClinicalRound toRound(ClinicalRoundRequest request) {
        return new ClinicalRound(UUID.randomUUID(), request.staffId(), request.patientId(), request.unit(), request.notes(), "OPEN", Instant.now());
    }

    public PatientChartSummary toChart(PatientChartRequest request) {
        UUID id = request.id() == null ? UUID.randomUUID() : request.id();
        return new PatientChartSummary(id, request.patientId(), request.summary(), request.riskLevel(), Instant.now());
    }

    public StaffTask toTask(StaffTaskRequest request) {
        return new StaffTask(UUID.randomUUID(), request.assigneeId(), request.patientId(), request.title(), request.priority(), "OPEN", Instant.now());
    }

    public StaffTask toCompletedTask(StaffTask task) {
        return new StaffTask(task.id(), task.assigneeId(), task.patientId(), task.title(), task.priority(), "COMPLETED", Instant.now());
    }
}
