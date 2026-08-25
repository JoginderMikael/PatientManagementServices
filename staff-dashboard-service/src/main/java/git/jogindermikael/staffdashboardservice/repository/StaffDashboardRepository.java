package git.jogindermikael.staffdashboardservice.repository;

import git.jogindermikael.staffdashboardservice.model.StaffDashboardModels.*;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class StaffDashboardRepository {
    private final ConcurrentHashMap<UUID, ClinicalRound> rounds = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<UUID, PatientChartSummary> charts = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<UUID, StaffTask> tasks = new ConcurrentHashMap<>();

    public ClinicalRound saveRound(ClinicalRound round) { rounds.put(round.id(), round); return round; }
    public Collection<ClinicalRound> findRounds() { return rounds.values(); }
    public PatientChartSummary saveChart(PatientChartSummary chart) { charts.put(chart.id(), chart); return chart; }
    public Collection<PatientChartSummary> findCharts() { return charts.values(); }
    public StaffTask saveTask(StaffTask task) { tasks.put(task.id(), task); return task; }
    public Optional<StaffTask> findTaskById(UUID id) { return Optional.ofNullable(tasks.get(id)); }
    public Collection<StaffTask> findTasks() { return tasks.values(); }
}
