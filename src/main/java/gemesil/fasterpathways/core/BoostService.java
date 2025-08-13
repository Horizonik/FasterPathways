package gemesil.fasterpathways.core;

import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;

import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Applies and clears the movement speed attribute modifier in a stable, idempotent way.
 * Uses a deterministic UUID per player so re-application does not stack.
 */
public final class BoostService {

    private static final String MODIFIER_NAME = "FasterPathways";
    private final Map<UUID, Double> appliedMultiplierByPlayer = new HashMap<>();

    /**
     * Ensures the player has the specified multiplier applied.
     * If a different value is already present, it will be replaced.
     *
     * @param player     target player
     * @param multiplier +0.20 for +20% speed
     */
    public void ensureAttribute(final Player player, final double multiplier) {
        final double clamped = Math.max(0.0, multiplier);
        final Double current = appliedMultiplierByPlayer.get(player.getUniqueId());
        if (current != null && Math.abs(current - clamped) < 1e-9) {
            return; // already applied with the same value
        }

        clearAttributeModifier(player);
        addAttributeModifier(player, clamped);
    }

    /**
     * Removes the plugin's attribute modifier from the player if present.
     *
     * @param player target player
     */
    public void clearAttributeModifier(final Player player) {
        final AttributeInstance instance = player.getAttribute(Attribute.MOVEMENT_SPEED);
        if (instance == null) return;

        final UUID modifierId = stableUuidFor(player.getUniqueId());
        instance.getModifiers().stream()
                .filter(mod -> mod.getUniqueId().equals(modifierId) || MODIFIER_NAME.equals(mod.getName()))
                .toList()
                .forEach(instance::removeModifier);

        appliedMultiplierByPlayer.remove(player.getUniqueId());
    }

    /**
     * Clears internal bookkeeping. Should be called on plugin disable.
     */
    public void clearAll() {
        appliedMultiplierByPlayer.clear();
    }

    private void addAttributeModifier(final Player player, final double multiplier) {
        final AttributeInstance instance = player.getAttribute(Attribute.MOVEMENT_SPEED);
        if (instance == null) return;

        final UUID modifierId = stableUuidFor(player.getUniqueId());
        final AttributeModifier modifier = new AttributeModifier(
                modifierId,
                MODIFIER_NAME,
                multiplier,
                AttributeModifier.Operation.ADD_SCALAR
        );
        instance.addModifier(modifier);
        appliedMultiplierByPlayer.put(player.getUniqueId(), multiplier);
    }

    private static UUID stableUuidFor(final UUID playerUniqueId) {
        final String seed = "fasterpathways-" + playerUniqueId;
        return UUID.nameUUIDFromBytes(seed.getBytes(StandardCharsets.UTF_8));
    }
}
