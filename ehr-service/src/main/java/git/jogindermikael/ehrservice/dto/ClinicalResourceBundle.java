package git.jogindermikael.ehrservice.dto;

import git.jogindermikael.ehrservice.model.ClinicalProvenance;
import git.jogindermikael.ehrservice.model.ClinicalResource;
import git.jogindermikael.ehrservice.model.ClinicalResourceVersion;
import java.util.List;

public record ClinicalResourceBundle(ClinicalResource resource,
        ClinicalResourceVersion currentVersion, List<ClinicalProvenance> provenance) {
}
