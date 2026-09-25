package git.jogindermikael.appointmentservice;

import static org.junit.jupiter.api.Assertions.*;

import git.jogindermikael.appointmentservice.dto.*;
import git.jogindermikael.appointmentservice.service.AppointmentService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(
    properties = {
      "app.audit.enabled=false",
      "app.reliability.publish-initial-delay-ms=3600000"
    })
class Phase6AppointmentOperationsTest {
  @Autowired AppointmentService service;

  @Test
  void supportsResourcesReschedulingArrivalAndWaitlistPromotion() {
    UUID doctor = UUID.randomUUID();
    UUID patient = UUID.randomUUID();
    var room =
        service.registerResource(
            new AppointmentResourceRequest("ROOM", "Synthetic room", UUID.randomUUID()));
    LocalDateTime start = LocalDateTime.now().plusDays(20).withSecond(0).withNano(0);

    var booked =
        service.bookAppointment(
            new AppointmentRequest(patient, doctor, start.minusDays(1), "Review", 30));
    var rescheduled =
        service.reschedule(
            booked.id(), new RescheduleRequest(start, 30, Set.of(room.getId())));
    assertEquals("RESCHEDULED", rescheduled.status());
    assertEquals("CHECKED_IN", service.checkIn(booked.id()).status());
    assertEquals("COMPLETED", service.checkOut(booked.id()).status());

    var waiting =
        service.joinWaitlist(
            new WaitlistRequest(
                UUID.randomUUID(), UUID.randomUUID(), LocalDate.now().plusDays(21), "Earlier slot"));
    var promoted =
        service.promoteWaitlist(
            waiting.id(),
            new WaitlistPromotionRequest(start.plusDays(1), 30, Set.of(room.getId())));
    assertEquals("BOOKED", promoted.status());
    assertEquals("PROMOTED", service.listWaitlist().stream()
        .filter(item -> item.id().equals(waiting.id())).findFirst().orElseThrow().getStatus());
  }

  @Test
  void recurringSeriesIsCreatedAtomically() {
    LocalDateTime start = LocalDateTime.now().plusDays(40).withSecond(0).withNano(0);
    var series =
        service.bookRecurring(
            new RecurringAppointmentRequest(
                UUID.randomUUID(),
                UUID.randomUUID(),
                start,
                "Therapy",
                45,
                3,
                7,
                "FOLLOW_UP",
                null,
                null,
                Set.of()));
    assertEquals(3, series.size());
    assertNotNull(series.getFirst().getRecurrenceGroupId());
    assertTrue(series.stream().allMatch(item -> item.getRecurrenceGroupId().equals(series.getFirst().getRecurrenceGroupId())));
  }
}
