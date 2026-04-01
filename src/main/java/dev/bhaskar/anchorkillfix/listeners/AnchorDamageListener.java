package dev.bhaskar.anchorkillfix.listeners;

import dev.bhaskar.anchorkillfix.AnchorKillFix;
import dev.bhaskar.anchorkillfix.ConfigManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Tracks respawn anchor interactions and anchor block damage context.
 */
public final class AnchorDamageListener implements Listener {
    private final AnchorKillFix plugin;
    private final ConfigManager configManager;

    /**
     * Creates a new damage listener.
     *
     * @param plugin        plugin instance
     * @param configManager configuration manager
     */
    public AnchorDamageListener(final AnchorKillFix plugin, final ConfigManager configManager) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
        this.configManager = Objects.requireNonNull(configManager, "configManager");
    }

    /**
     * Captures respawn anchor interactions and records nearby possible victims.
     *
     * @param event interaction event
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerInteract(final PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        final Block clicked = event.getClickedBlock();
        if (clicked == null || clicked.getType() != Material.RESPAWN_ANCHOR) {
            return;
        }

        final Player attacker = event.getPlayer();
        final double radius = configManager.getRadius();
        final double radiusSquared = radius * radius;

        for (final Player nearby : attacker.getWorld().getPlayers()) {
            if (nearby.getUniqueId().equals(attacker.getUniqueId())) {
                continue;
            }
            if (nearby.getLocation().distanceSquared(clicked.getLocation()) <= radiusSquared) {
                plugin.trackAttacker(nearby.getUniqueId(), attacker.getUniqueId());
            }
        }
    }

    /**
     * Confirms anchor block damage belongs to the previously tracked anchor user.
     *
     * @param event block damage event
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityDamageByBlock(final EntityDamageByBlockEvent event) {
        final Block damager = event.getDamager();
        if (!(event.getEntity() instanceof Player victim) || damager == null || damager.getType() != Material.RESPAWN_ANCHOR) {
            return;
        }

        final Map<UUID, UUID> attackers = plugin.getAnchorAttackers();
        final UUID attackerId = attackers.get(victim.getUniqueId());
        if (attackerId == null) {
            return;
        }

        final Player attacker = Bukkit.getPlayer(attackerId);
        if (attacker == null || !attacker.isOnline() || !attacker.getWorld().equals(victim.getWorld())) {
            plugin.clearVictim(victim.getUniqueId());
            return;
        }

        final double radius = configManager.getRadius();
        final double radiusSquared = radius * radius;
        if (attacker.getLocation().distanceSquared(damager.getLocation()) > radiusSquared) {
            plugin.clearVictim(victim.getUniqueId());
            return;
        }

        plugin.trackAttacker(victim.getUniqueId(), attacker.getUniqueId());
    }

    /**
     * Clears disconnected players from tracking structures.
     *
     * @param event quit event
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(final PlayerQuitEvent event) {
        plugin.cleanupPlayer(event.getPlayer().getUniqueId());
    }
}
