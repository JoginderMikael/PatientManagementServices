package git.jogindermikael.reliability;

import git.jogindermikael.reliability.dto.OutboxRecord;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/internal/reliability/outbox")
@PreAuthorize("hasRole('ADMIN')")
public class OutboxOperationsController {
  private final ReliableOutboxOperations operations;
  private final Duration claimLease;

  public OutboxOperationsController(ReliableOutboxOperations operations, Duration claimLease) {
    this.operations = operations;
    this.claimLease = claimLease;
  }

  @GetMapping
  public List<OutboxRecord> list(
      @RequestParam(required = false) String status,
      @RequestParam(defaultValue = "0") int offset,
      @RequestParam(defaultValue = "50") int limit) {
    return operations.list(status, offset, limit);
  }

  @PostMapping("/{id}/replay")
  @ResponseStatus(HttpStatus.ACCEPTED)
  public Map<String, Object> replay(@PathVariable UUID id) {
    if (!operations.replay(id)) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Event is not replayable");
    }
    return Map.of("eventId", id, "status", "PENDING");
  }

  @PostMapping("/reconcile")
  public Map<String, Integer> reconcile() {
    return Map.of(
        "recoveredClaims", operations.recoverExpiredClaims(claimLease),
        "pending", operations.pendingCount());
  }
}
