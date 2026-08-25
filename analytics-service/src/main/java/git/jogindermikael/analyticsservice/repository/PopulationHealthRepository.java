package git.jogindermikael.analyticsservice.repository;

import git.jogindermikael.analyticsservice.model.PopulationHealthModels.*;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class PopulationHealthRepository {
    private final ConcurrentHashMap<UUID, HealthTrend> trends = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<UUID, ChronicDiseaseCohort> cohorts = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<UUID, RegulatoryReport> reports = new ConcurrentHashMap<>();

    public HealthTrend saveTrend(HealthTrend trend) { trends.put(trend.id(), trend); return trend; }
    public Collection<HealthTrend> findTrends() { return trends.values(); }
    public ChronicDiseaseCohort saveCohort(ChronicDiseaseCohort cohort) { cohorts.put(cohort.id(), cohort); return cohort; }
    public Collection<ChronicDiseaseCohort> findCohorts() { return cohorts.values(); }
    public RegulatoryReport saveReport(RegulatoryReport report) { reports.put(report.id(), report); return report; }
    public Collection<RegulatoryReport> findReports() { return reports.values(); }
}
