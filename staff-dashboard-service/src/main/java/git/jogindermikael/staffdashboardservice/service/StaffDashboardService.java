package git.jogindermikael.staffdashboardservice.service;

import git.jogindermikael.staffdashboardservice.dto.StaffDashboardDtos.*;
import git.jogindermikael.staffdashboardservice.mapper.StaffDashboardMapper;
import git.jogindermikael.staffdashboardservice.model.StaffDashboardModels.*;
import git.jogindermikael.staffdashboardservice.repository.StaffDashboardRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class StaffDashboardService {
    private final StaffDashboardRepository repository;
    private final StaffDashboardMapper mapper;

    public StaffDashboardService(StaffDashboardRepository repository, StaffDashboardMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    public StaffDashboard dailyDashboard(UUID staffId) {
        return new StaffDashboard(staffId, LocalDate.now(),
                repository.findRounds().stream().filter(item -> item.staffId().equals(staffId)).toList(),
                repository.findTasks().stream().filter(item -> item.assigneeId().equals(staffId)).toList());
    }

    public ClinicalRound createRound(ClinicalRoundRequest request) { return repository.saveRound(mapper.toRound(request)); }
    public PatientChartSummary upsertChart(PatientChartRequest request) { return repository.saveChart(mapper.toChart(request)); }
    public List<PatientChartSummary> chartsForPatient(UUID patientId) { return repository.findCharts().stream().filter(chart -> chart.patientId().equals(patientId)).toList(); }
    public StaffTask createTask(StaffTaskRequest request) { return repository.saveTask(mapper.toTask(request)); }

    public StaffTask completeTask(UUID id) {
        StaffTask task = repository.findTaskById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found"));
        return repository.saveTask(mapper.toCompletedTask(task));
    }
}
