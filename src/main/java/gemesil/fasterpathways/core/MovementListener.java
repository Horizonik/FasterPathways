package gemesil.fasterpathways.core;

import gemesil.fasterpathways.core.ConfigManager.BlockBoost;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.Optional;

public final class MovementListener implements Listener {

    private final ConfigManager config;
    private final BoostService boostService;

    public MovementListener(final ConfigManager config, final BoostService boostService) {
        this.config = config;
        this.boostService = boostService;
    }

    @EventHandler
    public void onPlayerMove(final PlayerMoveEvent event) {
        if (event.getTo() == null) return;

        final Player player = event.getPlayer();
        if (config.isWorldDisabled(player.getWorld())) {
            boostService.removeSpeedMultiplier(player);
            return;
        }

        final Optional<BlockBoost> boost = resolveBoostFor(player);
        if (boost.isPresent()) {
            final double multiplier = boost.get().multiplier();
            boostService.applySpeedMultiplier(player, multiplier);

            if (config.isShowActionBar()) {
                final String legacy = org.bukkit.ChatColor
                        .translateAlternateColorCodes('&', config.getActionBarMessage());

                // If you want a default color applied, use the 2-arg overload:
                // final TextComponent comp = TextComponent.fromLegacy(legacy, ChatColor.WHITE);

                final BaseComponent comp = TextComponent.fromLegacy(legacy);
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, comp); // accepts a single BaseComponent
            }
        } else {
            boostService.removeSpeedMultiplier(player);
        }
    }

    @EventHandler
    public void onTeleport(final PlayerTeleportEvent event) {
        boostService.removeSpeedMultiplier(event.getPlayer());
    }

    @EventHandler
    public void onWorldChange(final PlayerChangedWorldEvent event) {
        boostService.removeSpeedMultiplier(event.getPlayer());
    }

    @EventHandler
    public void onQuit(final PlayerQuitEvent event) {
        boostService.removeSpeedMultiplier(event.getPlayer());
    }

    private Optional<BlockBoost> resolveBoostFor(final Player player) {
        final Block feet = player.getLocation().getBlock();
        final Block below = feet.getRelative(BlockFace.DOWN);

        final Material feetType = feet.getType();
        final Material belowType = below.getType();

        final Optional<BlockBoost> byFeet = config.findBoost(feetType);
        return byFeet.isPresent() ? byFeet : config.findBoost(belowType);
    }
}
