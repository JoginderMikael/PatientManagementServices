package git.jogindermikael.analyticsservice.service;

import git.jogindermikael.analyticsservice.dto.*;
import git.jogindermikael.analyticsservice.mapper.PopulationHealthMapper;
import git.jogindermikael.analyticsservice.model.*;
import git.jogindermikael.analyticsservice.repository.PopulationHealthRepository;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
public class PopulationHealthService {
    private final PopulationHealthRepository repository;
    private final PopulationHealthMapper mapper;

    public PopulationHealthService(PopulationHealthRepository repository, PopulationHealthMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    public HealthTrend recordTrend(HealthTrendRequest request) { return repository.saveTrend(mapper.toTrend(request)); }
    public List<HealthTrend> listTrends() { return repository.findTrends().stream().sorted(Comparator.comparing(HealthTrend::createdAt)).toList(); }
    public ChronicDiseaseCohort createCohort(ChronicDiseaseCohortRequest request) { return repository.saveCohort(mapper.toCohort(request)); }
    public List<ChronicDiseaseCohort> listCohorts() { return repository.findCohorts().stream().sorted(Comparator.comparing(ChronicDiseaseCohort::createdAt)).toList(); }
    public RegulatoryReport generateReport(RegulatoryReportRequest request) { return repository.saveReport(mapper.toReport(request)); }
    public List<RegulatoryReport> listReports() { return repository.findReports().stream().sorted(Comparator.comparing(RegulatoryReport::createdAt)).toList(); }
}
