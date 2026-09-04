package git.jogindermikael.patientservice.repository;

import git.jogindermikael.patientservice.model.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface PatientRepository extends JpaRepository<Patient, UUID> {

    boolean existsByEmail(String email);
    boolean existsByEmailAndIdNot(String email, UUID id);
    Optional<Patient> findByRegistrationKey(String registrationKey);
    Optional<Patient> findByMrn(String mrn);
    List<Patient> findByNameContainingIgnoreCaseAndDateOfBirth(String name, LocalDate dateOfBirth);
    List<Patient> findByPhone(String phone);
    List<Patient> findByEmailIgnoreCase(String email);
}
