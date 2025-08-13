package gemesil.fasterpathways;

import gemesil.fasterpathways.core.BoostService;
import gemesil.fasterpathways.core.ConfigManager;
import gemesil.fasterpathways.core.MovementListener;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Entry point for the FasterPathways plugin.
 * Keeps lifecycle minimal and delegates to the core package.
 */
public final class FasterPathways extends JavaPlugin {

    private ConfigManager configManager;
    private BoostService boostService;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        reloadAll();

        // Register movement listener
        Bukkit.getPluginManager().registerEvents(
                new MovementListener(configManager, boostService),
                this
        );

        getLogger().info("FasterPathways | Enabled (attribute-only)");
    }

    @Override
    public void onDisable() {
        // Ensure no one remains boosted after disable/reload
        for (Player onlinePlayer : getServer().getOnlinePlayers()) {
            if (onlinePlayer.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED) != null) {
                boostService.clearAttributeModifier(onlinePlayer);
            }
        }
        boostService.clearAll();
    }

    /**
     * Reloads configuration and re-initializes services.
     * Call this if you later add a /reload command.
     */
    public void reloadAll() {
        reloadConfig();
        this.configManager = new ConfigManager(getConfig(), getLogger(), getServer());
        this.boostService = new BoostService();
    }
}
