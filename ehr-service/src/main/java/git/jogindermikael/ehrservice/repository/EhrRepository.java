package git.jogindermikael.ehrservice.repository;

import git.jogindermikael.ehrservice.model.*;
import org.springframework.stereotype.Repository;
import java.util.Collection; import java.util.UUID;

@Repository
public class EhrRepository {
    private final MedicalHistoryRepository histories; private final DiagnosisRepository diagnoses; private final PrescriptionRepository prescriptions;
    private final LabResultRepository labs; private final VaccinationRepository vaccinations;
    public EhrRepository(MedicalHistoryRepository histories,DiagnosisRepository diagnoses,PrescriptionRepository prescriptions,LabResultRepository labs,VaccinationRepository vaccinations){this.histories=histories;this.diagnoses=diagnoses;this.prescriptions=prescriptions;this.labs=labs;this.vaccinations=vaccinations;}
    public MedicalHistory saveHistory(MedicalHistory value){return histories.save(value);} public Collection<MedicalHistory> findHistories(){return histories.findAll();}
    public Diagnosis saveDiagnosis(Diagnosis value){return diagnoses.save(value);} public Collection<Diagnosis> findDiagnoses(){return diagnoses.findAll();}
    public Prescription savePrescription(Prescription value){return prescriptions.save(value);} public Collection<Prescription> findPrescriptions(){return prescriptions.findAll();}
    public LabResult saveLabResult(LabResult value){return labs.save(value);} public Collection<LabResult> findLabResults(){return labs.findAll();}
    public VaccinationRecord saveVaccination(VaccinationRecord value){return vaccinations.save(value);} public Collection<VaccinationRecord> findVaccinations(){return vaccinations.findAll();}
}
