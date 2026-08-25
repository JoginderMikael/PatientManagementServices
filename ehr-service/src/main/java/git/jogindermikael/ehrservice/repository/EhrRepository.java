package git.jogindermikael.ehrservice.repository;

import git.jogindermikael.ehrservice.model.EhrModels.*;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class EhrRepository {
    private final ConcurrentHashMap<UUID, MedicalHistory> histories = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<UUID, Diagnosis> diagnoses = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<UUID, Prescription> prescriptions = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<UUID, LabResult> labResults = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<UUID, VaccinationRecord> vaccinations = new ConcurrentHashMap<>();

    public MedicalHistory saveHistory(MedicalHistory history) { histories.put(history.id(), history); return history; }
    public Collection<MedicalHistory> findHistories() { return histories.values(); }
    public Diagnosis saveDiagnosis(Diagnosis diagnosis) { diagnoses.put(diagnosis.id(), diagnosis); return diagnosis; }
    public Collection<Diagnosis> findDiagnoses() { return diagnoses.values(); }
    public Prescription savePrescription(Prescription prescription) { prescriptions.put(prescription.id(), prescription); return prescription; }
    public Collection<Prescription> findPrescriptions() { return prescriptions.values(); }
    public LabResult saveLabResult(LabResult labResult) { labResults.put(labResult.id(), labResult); return labResult; }
    public Collection<LabResult> findLabResults() { return labResults.values(); }
    public VaccinationRecord saveVaccination(VaccinationRecord vaccination) { vaccinations.put(vaccination.id(), vaccination); return vaccination; }
    public Collection<VaccinationRecord> findVaccinations() { return vaccinations.values(); }
}
