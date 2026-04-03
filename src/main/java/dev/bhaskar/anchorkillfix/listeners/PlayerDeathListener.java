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
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public final class PlayerDeathListener implements Listener {

    private final AnchorKillFix plugin;
    private final ConfigManager configManager;

    public PlayerDeathListener(final AnchorKillFix plugin, final ConfigManager configManager) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
        this.configManager = Objects.requireNonNull(configManager, "configManager");
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerDeath(final PlayerDeathEvent event) {
        final Player victim = event.getEntity();
        final EntityDamageEvent lastDamage = victim.getLastDamageCause();

        if (!(lastDamage instanceof EntityDamageByBlockEvent damageByBlock)) {
            plugin.clearVictim(victim.getUniqueId());
            return;
        }

        final Block damager = damageByBlock.getDamager();
        if (damager == null || damager.getType() != Material.RESPAWN_ANCHOR) {
            plugin.clearVictim(victim.getUniqueId());
            return;
        }

        final Map<UUID, UUID> attackers = plugin.getAnchorAttackers();
        final UUID attackerId = attackers.get(victim.getUniqueId());

        if (attackerId == null) return;

        final Player attacker = Bukkit.getPlayer(attackerId);
        if (attacker == null || !attacker.isOnline() || attacker.getWorld() != victim.getWorld()) {
            plugin.clearVictim(victim.getUniqueId());
            return;
        }

        final double damage = Math.max(victim.getHealth(), 0.1D);
        victim.setLastDamageCause(new EntityDamageByEntityEvent(
            attacker,
            victim,
            EntityDamageEvent.DamageCause.ENTITY_ATTACK,
            damage
        ));

        if (configManager.isDebug()) {
            plugin.getLogger().info("[DEBUG] Anchor kill credited: "
                + attacker.getName() + " -> " + victim.getName()
                + " (damage=" + String.format("%.2f", damage) + ")");
        } else if (configManager.isLogKills()) {
            final String message = configManager.getMessage("console-kill-log")
                .replace("{attacker}", attacker.getName())
                .replace("{victim}", victim.getName());
            plugin.getLogger().info(message);
        }

        plugin.clearVictim(victim.getUniqueId());
    }
}