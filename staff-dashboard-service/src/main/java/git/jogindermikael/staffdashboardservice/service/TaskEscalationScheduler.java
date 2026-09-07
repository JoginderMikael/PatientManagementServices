package git.jogindermikael.staffdashboardservice.service;

import org.springframework.scheduling.annotation.*;
import org.springframework.stereotype.Component;

@Component
@EnableScheduling
public class TaskEscalationScheduler {
  private final TaskWorkflowService service;

  public TaskEscalationScheduler(TaskWorkflowService service) {
    this.service = service;
  }

  @Scheduled(fixedDelayString = "${app.tasks.escalation-delay-ms:60000}")
  public void escalate() {
    service.escalate();
  }
}
