package gemesil.fasterpathways.core;

import gemesil.fasterpathways.core.ConfigManager.BlockBoost;
import net.md_5.bungee.api.ChatMessageType;
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

/**
 * Listens for player movement and applies/removes attribute boosts
 * based on the block under the player's feet or directly below.
 */
public final class MovementListener implements Listener {

    private final ConfigManager config;
    private final BoostService boostService;

    /**
     * Creates the movement listener.
     *
     * @param config       configuration accessor
     * @param boostService attribute application service
     */
    public MovementListener(final ConfigManager config, final BoostService boostService) {
        this.config = config;
        this.boostService = boostService;
    }

    /**
     * Handles player movement. Evaluates blocks at feet and below every move.
     * Does not early-return on "same block" to avoid Bedrock effect flicker.
     *
     * @param event PlayerMoveEvent
     */
    @EventHandler
    public void onPlayerMove(final PlayerMoveEvent event) {
        if (event.getTo() == null) return;

        final Player player = event.getPlayer();
        if (config.isWorldDisabled(player.getWorld())) {
            boostService.clearAttributeModifier(player);
            return;
        }

        final Optional<BlockBoost> boost = resolveBoostFor(player);
        if (boost.isPresent()) {
            final double multiplier = boost.get().multiplier;
            boostService.ensureAttribute(player, multiplier);

            if (config.isShowActionBar()) {
                player.spigot().sendMessage(
                        ChatMessageType.ACTION_BAR,
                        TextComponent.fromLegacyText(config.getActionBarMessage())
                );
            }
        } else {
            boostService.clearAttributeModifier(player);
        }
    }

    /**
     * Clears attribute state on player teleport to avoid sticky modifiers.
     *
     * @param event PlayerTeleportEvent
     */
    @EventHandler
    public void onTeleport(final PlayerTeleportEvent event) {
        boostService.clearAttributeModifier(event.getPlayer());
    }

    /**
     * Clears attribute state on world change to avoid cross-world carryover.
     *
     * @param event PlayerChangedWorldEvent
     */
    @EventHandler
    public void onWorldChange(final PlayerChangedWorldEvent event) {
        boostService.clearAttributeModifier(event.getPlayer());
    }

    /**
     * Clears attribute state on quit.
     *
     * @param event PlayerQuitEvent
     */
    @EventHandler
    public void onQuit(final PlayerQuitEvent event) {
        boostService.clearAttributeModifier(event.getPlayer());
    }

    /**
     * Resolves the active boost for the player by checking the block at the player's feet
     * and the block directly below.
     *
     * @param player player whose position to evaluate
     * @return optional BlockBoost when configured for either block
     */
    private Optional<BlockBoost> resolveBoostFor(final Player player) {
        final Block feetBlock = player.getLocation().getBlock();
        final Block belowBlock = feetBlock.getRelative(BlockFace.DOWN);

        final Material feetType = feetBlock.getType();
        final Material belowType = belowBlock.getType();

        Optional<BlockBoost> byFeet = config.findBoost(feetType);
        if (byFeet.isPresent()) return byFeet;

        return config.findBoost(belowType);
    }
}
