package gemesil.fasterpathways.core;

import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Manages a player's movement-speed attribute modifier (Spigot 1.21.8).
 * Uses a deterministic UUID per player to avoid duplicates and ensure safe replace/remove.
 */
public final class BoostService {

    private static final String SPEED_MODIFIER_NAME_PREFIX = "fasterpathways.speed:";
    private static final String LEGACY_NAME = "FasterPathways"; // cleanup from older builds

    /**
     * Apply or update a +X% speed modifier. No-ops if the same value is already applied.
     */
    public void applySpeedMultiplier(final Player player, final double multiplier) {
        final AttributeInstance attr = player.getAttribute(Attribute.MOVEMENT_SPEED);
        if (attr == null) return;

        final double amount = Math.max(0.0, multiplier);
        final UUID targetId = stableModifierUuid(player);
        final String targetName = speedModifierNameFor(player);

        // Check if the correct modifier is already present (same UUID + amount).
        final AttributeModifier current = findByUuid(attr, targetId);
        if (current != null
                && current.getOperation() == AttributeModifier.Operation.ADD_SCALAR
                && Math.abs(current.getAmount() - amount) < 1e-9) {
            return; // already correct
        }

        // Remove any prior copies that match our UUID or known names, then add the new value.
        removeSpeedModifiers(attr, targetId, targetName);

        // Spigot exposes only the UUID constructor at runtime
        final AttributeModifier updated = new AttributeModifier(
                targetId,
                targetName,
                amount,
                AttributeModifier.Operation.ADD_SCALAR
        );
        attr.addModifier(updated);
    }

    /**
     * Remove the speed modifier for this player (if present).
     */
    public void removeSpeedMultiplier(final Player player) {
        final AttributeInstance attr = player.getAttribute(Attribute.MOVEMENT_SPEED);
        if (attr == null) return;

        final UUID targetId = stableModifierUuid(player);
        final String targetName = speedModifierNameFor(player);
        removeSpeedModifiers(attr, targetId, targetName);
    }

    // ---------- helpers ----------

    private static String speedModifierNameFor(final Player player) {
        return SPEED_MODIFIER_NAME_PREFIX + player.getUniqueId();
    }

    private static UUID stableModifierUuid(final Player player) {
        final String seed = "fasterpathways:" + player.getUniqueId();
        return UUID.nameUUIDFromBytes(seed.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Find an existing modifier by UUID (uses deprecated getter, isolated here).
     */
    private static AttributeModifier findByUuid(final AttributeInstance attr, final UUID uuid) {
        for (AttributeModifier m : attr.getModifiers()) {
            if (uuidEquals(m, uuid)) {
                return m;
            }
        }
        return null;
    }

    /**
     * Remove any modifier that matches our UUID or our known names (current + legacy).
     */
    private static void removeSpeedModifiers(final AttributeInstance attr, final UUID uuid, final String name) {
        final List<AttributeModifier> toRemove = new ArrayList<>();
        for (AttributeModifier m : attr.getModifiers()) {
            final boolean sameUuid = uuidEquals(m, uuid);
            final boolean sameName = Objects.equals(m.getName(), name) || Objects.equals(m.getName(), LEGACY_NAME);
            if (sameUuid || sameName) {
                toRemove.add(m);
            }
        }
        for (AttributeModifier m : toRemove) {
            attr.removeModifier(m);
        }
    }

    /**
     * Compare via deprecated getter; isolated so the rest of the code stays modern.
     */
    private static boolean uuidEquals(final AttributeModifier modifier, final UUID expected) {
        return modifier.getUniqueId().equals(expected);
    }
}
