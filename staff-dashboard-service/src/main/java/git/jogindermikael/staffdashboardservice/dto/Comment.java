package git.jogindermikael.staffdashboardservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record Comment(@NotBlank @Size(max = 2000) String text) {
}
