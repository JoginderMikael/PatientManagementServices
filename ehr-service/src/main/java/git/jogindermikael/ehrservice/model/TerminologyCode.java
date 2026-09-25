package git.jogindermikael.ehrservice.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.Instant;
import java.util.Arrays;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Entity
@Table(name = "clinical_terminology_code")
public class TerminologyCode {
    @Id private UUID id;
    @Column(nullable = false) private String systemUri;
    @Column(nullable = false) private String code;
    @Column(nullable = false) private String display;
    @Column(nullable = false) private String resourceTypes;
    @Column(nullable = false) private boolean active = true;
    @Column(nullable = false) private Instant updatedAt;
    @Version private long lockVersion;

    protected TerminologyCode() {}

    public TerminologyCode(UUID id, String systemUri, String code, String display, Set<String> types) {
        this.id = id; this.systemUri = systemUri; this.code = code; this.display = display;
        this.resourceTypes = types.stream().map(String::toUpperCase).sorted().collect(Collectors.joining(","));
        this.updatedAt = Instant.now();
    }

    public UUID getId() { return id; }
    public String getSystemUri() { return systemUri; }
    public String getCode() { return code; }
    public String getDisplay() { return display; }
    public String getResourceTypes() { return resourceTypes; }
    public boolean isActive() { return active; }
    public Instant getUpdatedAt() { return updatedAt; }
    public long getLockVersion() { return lockVersion; }
    public boolean supports(String type) {
        return active && Arrays.asList(resourceTypes.split(",")).contains(type.toUpperCase());
    }
}
