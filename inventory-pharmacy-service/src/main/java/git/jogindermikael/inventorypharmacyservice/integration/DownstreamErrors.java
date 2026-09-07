package git.jogindermikael.inventorypharmacyservice.integration;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestControllerAdvice
public class DownstreamErrors {
  @ExceptionHandler(org.springframework.web.client.RestClientException.class)
  public ResponseEntity<ProblemDetail> unavailable() {
    return ResponseEntity.status(502)
        .body(
            ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_GATEWAY,
                "Downstream workflow failed; retry using the same reference"));
  }

  @ExceptionHandler(org.springframework.dao.DataIntegrityViolationException.class)
  public ResponseEntity<ProblemDetail> conflict() {
    return ResponseEntity.status(409)
        .body(
            ProblemDetail.forStatusAndDetail(
                HttpStatus.CONFLICT, "A conflicting or invalid record already exists"));
  }
}
