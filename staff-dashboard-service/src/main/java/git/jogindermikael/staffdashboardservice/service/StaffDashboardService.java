package git.jogindermikael.staffdashboardservice.service;

import git.jogindermikael.staffdashboardservice.dto.*;
import git.jogindermikael.staffdashboardservice.mapper.StaffDashboardMapper;
import git.jogindermikael.staffdashboardservice.model.*;
import git.jogindermikael.staffdashboardservice.repository.StaffDashboardRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@org.springframework.transaction.annotation.Transactional
@Service
public class StaffDashboardService {
  private final StaffDashboardRepository repository;
  private final StaffDashboardMapper mapper;
  private final StaffAccess access;
  private final TaskWorkflowService tasks;

  public StaffDashboardService(
      StaffDashboardRepository repository,
      StaffDashboardMapper mapper,
      StaffAccess access,
      TaskWorkflowService tasks) {
    this.access = access;
    this.tasks = tasks;
    this.repository = repository;
    this.mapper = mapper;
  }

  public StaffDashboard dailyDashboard(UUID staffId) {
    access.assignee(staffId);
    return new StaffDashboard(
        staffId,
        LocalDate.now(),
        repository.findRounds().stream().filter(item -> item.staffId().equals(staffId)).toList(),
        repository.findTasks().stream().filter(item -> item.assigneeId().equals(staffId)).toList());
  }

  public ClinicalRound createRound(ClinicalRoundRequest request) {
    repository.lock();
    access.assignee(request.staffId());
    return repository.saveRound(mapper.toRound(request));
  }

  public PatientChartSummary upsertChart(PatientChartRequest request) {
    repository.lock();
    return repository.saveChart(mapper.toChart(request));
  }

  public List<PatientChartSummary> chartsForPatient(UUID patientId) {
    return repository.findCharts().stream()
        .filter(chart -> chart.patientId().equals(patientId))
        .toList();
  }

  public StaffTask createTask(StaffTaskRequest request) {
    repository.lock();
    access.assignee(request.assigneeId());
    var task = repository.saveTask(mapper.toTask(request));
    tasks.initialize(task.id(), request.priority());
    return task;
  }

  public StaffTask completeTask(UUID id) {
    repository.lock();
    StaffTask task =
        repository
            .findTaskById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found"));
    tasks.authorize(id);
    if ("COMPLETED".equals(task.status())) return task;
    tasks.history(id, "COMPLETED", "");
    return repository.saveTask(mapper.toCompletedTask(task));
  }
}
