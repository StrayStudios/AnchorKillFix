package dev.bhaskar.anchorkillfix.listeners;

import dev.bhaskar.anchorkillfix.AnchorKillFix;
import dev.bhaskar.anchorkillfix.ConfigManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Comparator;
import java.util.Objects;

public final class AnchorDamageListener implements Listener {

    private final AnchorKillFix plugin;
    private final ConfigManager configManager;

    public AnchorDamageListener(final AnchorKillFix plugin, final ConfigManager configManager) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
        this.configManager = Objects.requireNonNull(configManager, "configManager");
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityDamageByBlock(final EntityDamageByBlockEvent event) {
        if (!(event.getEntity() instanceof Player victim)) return;

        final Block damager = event.getDamager();
        if (damager == null || damager.getType() != Material.RESPAWN_ANCHOR) return;

        final double radius = configManager.getRadius();
        final double radiusSquared = radius * radius;
        final Location anchorLoc = damager.getLocation();

        Bukkit.getOnlinePlayers().stream()
            .filter(p -> !p.getUniqueId().equals(victim.getUniqueId()))
            .filter(p -> p.getWorld().equals(victim.getWorld()))
            .filter(p -> p.getLocation().distanceSquared(anchorLoc) <= radiusSquared)
            .min(Comparator.comparingDouble(p -> p.getLocation().distanceSquared(anchorLoc)))
            .ifPresent(attacker -> plugin.trackAttacker(victim.getUniqueId(), attacker.getUniqueId()));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(final PlayerQuitEvent event) {
        plugin.cleanupPlayer(event.getPlayer().getUniqueId());
    }
}