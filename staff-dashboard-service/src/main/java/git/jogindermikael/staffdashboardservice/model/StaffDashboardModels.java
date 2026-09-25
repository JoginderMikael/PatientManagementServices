package git.jogindermikael.staffdashboardservice.model;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public final class StaffDashboardModels {
    private StaffDashboardModels() {
    }

    public record StaffDashboard(UUID staffId, LocalDate date, List<ClinicalRound> rounds, List<StaffTask> tasks) {
    }

    public record ClinicalRound(UUID id, UUID staffId, UUID patientId, String unit, String notes, String status,
            Instant updatedAt) {
    }

    public record PatientChartSummary(UUID id, UUID patientId, String summary, String riskLevel, Instant updatedAt) {
    }

    public record StaffTask(UUID id, UUID assigneeId, UUID patientId, String title, String priority, String status,
            Instant updatedAt) {
    }
}
