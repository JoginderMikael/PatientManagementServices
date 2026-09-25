package git.jogindermikael.inventorypharmacyservice.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record SupplyItem(UUID id, String name, int quantityOnHand, int reorderThreshold,
        BigDecimal unitCost, Instant updatedAt) {
}
