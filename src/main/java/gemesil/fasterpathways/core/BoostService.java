package gemesil.fasterpathways.core;

import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

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
        final UUID playerId = player.getUniqueId(); // this is fine on your API
        final double clamped = Math.max(0.0, multiplier);

        final Double current = appliedMultiplierByPlayer.get(playerId);
        if (current != null && Math.abs(current - clamped) < 1e-9) return;

        clearAttributeModifier(player);
        addAttributeModifier(player, clamped);
    }

    /**
     * Removes the plugin's attribute modifier from the player if present.
     * Avoids deprecated AttributeModifier#getUniqueId() by removing by name.
     *
     * @param player target player
     */
    public void clearAttributeModifier(final Player player) {
        final AttributeInstance instance = player.getAttribute(Attribute.MOVEMENT_SPEED);
        if (instance == null) return;

        // Remove any modifier we previously added by NAME (non-deprecated path)
        instance.getModifiers().stream()
                .filter(mod -> MODIFIER_NAME.equals(mod.getName()))
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

        final UUID modifierId = stableModifierUuidFor(player.getUniqueId());

        final AttributeModifier modifier = new AttributeModifier(
                modifierId,
                MODIFIER_NAME,
                multiplier,
                AttributeModifier.Operation.ADD_SCALAR
        );

        instance.addModifier(modifier);
        appliedMultiplierByPlayer.put(player.getUniqueId(), multiplier);
    }

    /**
     * Deterministic UUID for this plugin's modifier per player.
     */
    private static UUID stableModifierUuidFor(final UUID playerUuid) {
        final String seed = "fasterpathways-" + playerUuid;
        return UUID.nameUUIDFromBytes(seed.getBytes(StandardCharsets.UTF_8));
    }
}
