package gemesil.fasterpathways;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;

public final class FasterPathways extends JavaPlugin implements Listener {

    private PotionEffect speedEffect;
    private Set<Material> speedBlocks;
    private String actionBarMessage;
    private boolean showActionBar;

    @Override
    public void onEnable() {
        // Save default config if it doesn't exist
        saveDefaultConfig();

        // Load configuration
        loadConfiguration();

        // Register events
        getServer().getPluginManager().registerEvents(this, this);

        getLogger().info("FasterPathways has been enabled!");
    }

    private void loadConfiguration() {
        // Reload config
        reloadConfig();
        FileConfiguration config = getConfig();

        // Load speed effect settings
        int speedLevel = config.getInt("speed.level", 1);
        int speedDuration = config.getInt("speed.duration", 2);
        speedEffect = new PotionEffect(PotionEffectType.SPEED, speedDuration * 20, speedLevel - 1);

        // Load action bar settings
        actionBarMessage = ChatColor.translateAlternateColorCodes('&',
                config.getString("messages.actionBar", "&eYou're moving faster!"));
        showActionBar = config.getBoolean("messages.showActionBar", true);

        // Load speed blocks
        speedBlocks = new HashSet<>();
        for (String blockName : config.getStringList("speedBlocks")) {
            try {
                Material material = Material.valueOf(blockName.toUpperCase());
                speedBlocks.add(material);
            } catch (IllegalArgumentException e) {
                getLogger().log(Level.WARNING, "Invalid block material in config: " + blockName);
            }
        }

        // Warn if no valid blocks are configured
        if (speedBlocks.isEmpty()) {
            getLogger().warning("No valid speed blocks configured! Adding DIRT_PATH as default.");
            speedBlocks.add(Material.DIRT_PATH);
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        // Only process if player actually moved to a different block
        if (event.getTo() != null && event.getTo().getBlock().equals(event.getFrom().getBlock())) {
            return;
        }

        final Player player = event.getPlayer();
        final Block block = player.getLocation().getBlock();
        final Block relativeBlock = block.getRelative(BlockFace.DOWN);

        // Check if player is on a speed block
        if (speedBlocks.contains(block.getType()) || speedBlocks.contains(relativeBlock.getType())) {
            player.addPotionEffect(speedEffect);

            if (showActionBar) {
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                        TextComponent.fromLegacyText(actionBarMessage));
            }
        }
    }
}