package git.jogindermikael.staffdashboardservice.model;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record StaffDashboard(UUID staffId, LocalDate date, List<ClinicalRound> rounds,
        List<StaffTask> tasks) {
}
