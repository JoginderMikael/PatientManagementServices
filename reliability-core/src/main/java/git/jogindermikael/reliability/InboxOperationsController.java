package git.jogindermikael.reliability;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/internal/reliability/inbox")
@PreAuthorize("hasRole('ADMIN')")
public class InboxOperationsController {
  private final ReliableEventInbox inbox;
  private final Duration lease;

  public InboxOperationsController(ReliableEventInbox inbox, Duration lease) {
    this.inbox = inbox;
    this.lease = lease;
  }

  @GetMapping
  public List<ReliableEventInbox.InboxRecord> list(
      @RequestParam(required = false) String status,
      @RequestParam(defaultValue = "0") int offset,
      @RequestParam(defaultValue = "50") int limit) {
    return inbox.list(status, offset, limit);
  }

  @PostMapping("/reconcile")
  public Map<String, Integer> reconcile() {
    return Map.of("recoveredClaims", inbox.recoverExpiredClaims(lease));
  }
}
