package git.jogindermikael.inventorypharmacyservice.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record MedicationStock(UUID id, String name, String ndcCode, int quantityOnHand,
        BigDecimal unitCost, Instant updatedAt) {
}
