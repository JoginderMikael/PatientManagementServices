package git.jogindermikael.patientservice.service;

import git.jogindermikael.patientservice.dto.PatientRequestDTO;
import git.jogindermikael.patientservice.dto.PatientResponseDTO;
import git.jogindermikael.patientservice.exception.EmailAlreadyExistsException;
import git.jogindermikael.patientservice.mapper.PatientMapper;
import git.jogindermikael.patientservice.model.Patient;
import git.jogindermikael.patientservice.repository.PatientRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

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

}
