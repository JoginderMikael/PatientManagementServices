package git.jogindermikael.staffdashboardservice;

import static org.junit.jupiter.api.Assertions.*;

import git.jogindermikael.staffdashboardservice.dto.StaffDashboardDtos.*;
import git.jogindermikael.staffdashboardservice.service.*;
import java.time.*;
import java.util.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;

@SpringBootTest(properties = {"app.audit.enabled=false", "grpc.server.port=-1"})
class Phase3StaffTest {

  @Autowired StaffDashboardService service;
  @Autowired TaskWorkflowService tasks;
  @Autowired org.springframework.jdbc.core.JdbcTemplate jdbc;

  @Test
  @WithMockUser(roles = "ADMIN")
  void overdueTasksEscalateOnceAndKeepHistory() {
    var task =
        service.createTask(
            new StaffTaskRequest(
                UUID.randomUUID(), UUID.randomUUID(), "Synthetic follow-up", "URGENT"));
    jdbc.update(
        "UPDATE task_workflow SET due_at=? WHERE task_id=?",
        Instant.now().minusSeconds(10).atOffset(ZoneOffset.UTC),
        task.id());
    tasks.escalate();
    assertTrue(tasks.queue("CLINICIAN", 200).stream().anyMatch(v -> task.id().equals(v.get("id"))));
    tasks.escalate();
    assertEquals(
        1,
        tasks.history(task.id()).stream().filter(v -> "ESCALATED".equals(v.get("action"))).count());
    service.completeTask(task.id());
    assertFalse(
        tasks.queue("CLINICIAN", 200).stream().anyMatch(v -> task.id().equals(v.get("id"))));
  }

  @Test
  @WithMockUser(roles = "BILLING_STAFF")
  void roleCannotReadClinicalQueue() {
    assertThrows(
        org.springframework.security.access.AccessDeniedException.class,
        () -> tasks.queue("CLINICIAN", 50));
    assertThrows(
        org.springframework.security.access.AccessDeniedException.class,
        () -> service.dailyDashboard(UUID.randomUUID()));
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void clinicalHandoffIsIdempotent() {
    var command =
        new TaskWorkflowService.ClinicalTask(
            UUID.randomUUID().toString(),
            UUID.randomUUID(),
            UUID.randomUUID(),
            "Critical synthetic result");
    assertEquals(tasks.clinical(command).get("id"), tasks.clinical(command).get("id"));
  }
}
