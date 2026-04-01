package dev.bhaskar.anchorkillfix;

import dev.bhaskar.anchorkillfix.commands.ReloadCommand;
import dev.bhaskar.anchorkillfix.listeners.AnchorDamageListener;
import dev.bhaskar.anchorkillfix.listeners.PlayerDeathListener;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Main plugin entry point for AnchorKillFix.
 */
public final class AnchorKillFix extends JavaPlugin {
    private final Map<UUID, UUID> anchorAttackers = new HashMap<>();
    private final Map<UUID, Long> attackerTimestamps = new HashMap<>();
    private ConfigManager configManager;

    /**
     * Gets the shared victim-to-attacker map.
     *
     * @return victim-to-attacker map
     */
    public Map<UUID, UUID> getAnchorAttackers() {
        return anchorAttackers;
    }

    /**
     * Tracks or refreshes attacker information for a victim.
     *
     * @param victim   victim UUID
     * @param attacker attacker UUID
     */
    public void trackAttacker(final UUID victim, final UUID attacker) {
        if (victim == null || attacker == null) {
            return;
        }

        anchorAttackers.put(victim, attacker);
        attackerTimestamps.put(victim, System.currentTimeMillis());
    }

    /**
     * Removes tracking data for a specific victim.
     *
     * @param victim victim UUID
     */
    public void clearVictim(final UUID victim) {
        if (victim == null) {
            return;
        }

        anchorAttackers.remove(victim);
        attackerTimestamps.remove(victim);
    }

    /**
     * Removes tracking references for a disconnected player.
     *
     * @param playerId player UUID
     */
    public void cleanupPlayer(final UUID playerId) {
        if (playerId == null) {
            return;
        }

        clearVictim(playerId);
        final Iterator<Map.Entry<UUID, UUID>> iterator = anchorAttackers.entrySet().iterator();
        while (iterator.hasNext()) {
            final Map.Entry<UUID, UUID> entry = iterator.next();
            if (playerId.equals(entry.getValue())) {
                attackerTimestamps.remove(entry.getKey());
                iterator.remove();
            }
        }
    }

    /**
     * Gets the configuration manager.
     *
     * @return configuration manager
     */
    public ConfigManager getConfigManager() {
        return Objects.requireNonNull(configManager, "configManager");
    }

    @Override
    public void onEnable() {
        configManager = new ConfigManager(this);
        configManager.load();

        Bukkit.getPluginManager().registerEvents(new AnchorDamageListener(this, configManager), this);
        Bukkit.getPluginManager().registerEvents(new PlayerDeathListener(this, configManager), this);

        final ReloadCommand reloadCommand = new ReloadCommand(this, configManager);
        final PluginCommand command = getCommand("anchorkillfix");
        if (command != null) {
            command.setExecutor(reloadCommand);
            command.setTabCompleter(reloadCommand);
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                final long now = System.currentTimeMillis();
                final Iterator<Map.Entry<UUID, Long>> iterator = attackerTimestamps.entrySet().iterator();
                while (iterator.hasNext()) {
                    final Map.Entry<UUID, Long> entry = iterator.next();
                    if (now - entry.getValue() >= 30_000L) {
                        anchorAttackers.remove(entry.getKey());
                        iterator.remove();
                    }
                }
            }
        }.runTaskTimer(this, 600L, 600L);

        final String version = getDescription().getVersion();
        final String radiusLine = String.format("║  Radius      : %.1f blocks            ║", configManager.getRadius());
        getLogger().info("╔═══════════════════════════════════════╗");
        getLogger().info(String.format("║         AnchorKillFix v%s          ║", version));
        getLogger().info("║        by Bhaskar | dev.bhaskar       ║");
        getLogger().info("║   Crediting anchor kills since 2025   ║");
        getLogger().info("╠═══════════════════════════════════════╣");
        getLogger().info("║  API         : Paper 1.21.x           ║");
        getLogger().info("║  Status      : Enabled ✔              ║");
        getLogger().info(radiusLine);
        getLogger().info("╚═══════════════════════════════════════╝");
    }

    @Override
    public void onDisable() {
        anchorAttackers.clear();
        attackerTimestamps.clear();
        getLogger().info("[AnchorKillFix] Disabled. Anchor kill tracking stopped.");
    }
}
