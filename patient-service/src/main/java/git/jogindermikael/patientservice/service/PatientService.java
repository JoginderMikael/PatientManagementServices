package git.jogindermikael.patientservice.service;

import git.jogindermikael.patientservice.dto.PatientRequestDTO;
import git.jogindermikael.patientservice.dto.PatientResponseDTO;
import git.jogindermikael.patientservice.exception.EmailAlreadyExistsException;
import git.jogindermikael.patientservice.exception.PatientNotFoundException;
import git.jogindermikael.patientservice.mapper.PatientMapper;
import git.jogindermikael.patientservice.model.Patient;
import git.jogindermikael.patientservice.repository.PatientRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class PatientService {
    private final PatientRepository patientRepository;

    public PatientService(PatientRepository patientRepository) {
        this.patientRepository = patientRepository;
    }

    public List<PatientResponseDTO> getPatients(){
        List<Patient> patients = patientRepository.findAll();

        return patients.stream().map(PatientMapper::toDto).toList();
    }

    public PatientResponseDTO createPatient(PatientRequestDTO  patientRequestDTO){

        if (patientRepository.existsByEmail(patientRequestDTO.getEmail())) {
            throw  new EmailAlreadyExistsException("A patient with this email already exists "
                    + patientRequestDTO.getEmail() );
        }

        Patient newPatient = patientRepository.save(PatientMapper.toModel(patientRequestDTO));
        return PatientMapper.toDto(newPatient);
    }

    public PatientResponseDTO updatePatient(UUID id, PatientRequestDTO  patientRequestDTO){

        Patient patient = patientRepository.findById(id).orElseThrow(
                () -> new PatientNotFoundException("Patient not found with the ID: " + id));

        if (patientRepository.existsByEmail(patientRequestDTO.getEmail())) {
            throw  new EmailAlreadyExistsException("A patient with this email already exists "
                    + patientRequestDTO.getEmail() );
        }

        patient.setName(patientRequestDTO.getName());
        patient.setEmail(patientRequestDTO.getEmail());
        patient.setAddress(patientRequestDTO.getAddress());
        patient.setDateOfBirth(LocalDate.parse(patientRequestDTO.getDateOfBirth()));

        Patient updatedPatient = patientRepository.save(patient);

        return PatientMapper.toDto(updatedPatient);
    }

}
