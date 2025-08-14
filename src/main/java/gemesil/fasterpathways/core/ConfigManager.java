package gemesil.fasterpathways.core;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Loads and exposes plugin configuration with readable accessors.
 * Supports both the preferred per-block format and the legacy format.
 */
public final class ConfigManager {

    /**
     * Immutable per-block configuration entry.
     * multiplier: 0.20 means +20% movement speed.
     */
    public record BlockBoost(Material material, double multiplier) {
        public BlockBoost(final Material material, final double multiplier) {
            this.material = material;
            this.multiplier = Math.max(0.0, multiplier);
        }
    }

    private final Set<String> disabledWorldNames = new HashSet<>();
    private final Map<Material, BlockBoost> boostsByMaterial = new EnumMap<>(Material.class);
    private final boolean showActionBar;
    private final String actionBarMessage;

    /**
     * Constructs and loads configuration.
     *
     * @param cfg    Bukkit configuration
     * @param logger Plugin logger
     * @param server Bukkit server (used to warn on missing worlds)
     */
    public ConfigManager(final FileConfiguration cfg, final Logger logger, final Server server) {

        // Messages
        this.showActionBar = cfg.getBoolean("messages.showActionBar", true);
        this.actionBarMessage = ChatColor.translateAlternateColorCodes(
                '&', cfg.getString("messages.actionBar", "&eYou're moving faster!")
        );

        // Disabled worlds
        this.disabledWorldNames.addAll(cfg.getStringList("disabledWorlds"));
        for (String worldName : disabledWorldNames) {
            World world = server.getWorld(worldName);
            if (world == null) {
                logger.warning("World not found: " + worldName);
            }
        }

        // Preferred per-block format
        if (cfg.isList("blocks")) {
            for (Object rawEntry : cfg.getMapList("blocks")) {
                if (!(rawEntry instanceof Map)) continue;

                @SuppressWarnings("unchecked") final Map<String, Object> entry = (Map<String, Object>) rawEntry;

                final String materialName = String.valueOf(entry.getOrDefault("material", "")).toUpperCase(Locale.ROOT);
                if (materialName.isBlank()) continue;

                try {
                    final Material material = Material.valueOf(materialName);
                    final double multiplier = toDouble(entry.get("multiplier"), 0);
                    if (multiplier == 0) continue;

                    boostsByMaterial.put(material, new BlockBoost(material, multiplier));
                } catch (IllegalArgumentException ex) {
                    logger.log(Level.WARNING, "Invalid material in blocks: " + materialName);
                }
            }
        }
    }

    /**
     * Determines whether the given world is configured as disabled.
     *
     * @param world Bukkit world
     * @return true if boosts are disabled in this world
     */
    public boolean isWorldDisabled(final World world) {
        return disabledWorldNames.contains(world.getName());
    }

    /**
     * Returns the configured BlockBoost for the given material if any.
     *
     * @param material block type to check
     * @return Optional containing a BlockBoost if configured
     */
    public Optional<BlockBoost> findBoost(final Material material) {
        return Optional.ofNullable(boostsByMaterial.get(material));
    }

    /**
     * Indicates if the action bar should be displayed during boost.
     *
     * @return true when showing the action bar message is enabled
     */
    public boolean isShowActionBar() {
        return showActionBar;
    }

    /**
     * Returns the translated action bar message.
     *
     * @return message string
     */
    public String getActionBarMessage() {
        return actionBarMessage;
    }

    private static double toDouble(final Object value, final double def) {
        if (value instanceof Number number) return number.doubleValue();
        if (value == null) return def;
        try {
            return Double.parseDouble(String.valueOf(value));
        } catch (Exception ignored) {
            return def;
        }
    }
}
