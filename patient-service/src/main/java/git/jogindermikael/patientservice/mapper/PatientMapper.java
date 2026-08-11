package git.jogindermikael.patientservice.mapper;

import git.jogindermikael.patientservice.dto.PatientResponseDTO;
import git.jogindermikael.patientservice.model.Patient;

public class PatientMapper {
    public static PatientResponseDTO toDto(Patient patient){
        PatientResponseDTO patientResponseDTO = new PatientResponseDTO();

        patientResponseDTO.setId(patient.getId().toString());
        patientResponseDTO.setName(patient.getName());
        patientResponseDTO.setAddress(patient.getAddress());
        patientResponseDTO.setEmail(patient.getEmail());
        patientResponseDTO.setDateOfBirth(patient.getDateOfBirth().toString());

        return patientResponseDTO;
    }
}
