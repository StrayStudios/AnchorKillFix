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

public final class AnchorKillFix extends JavaPlugin {

    private final Map<UUID, UUID> anchorAttackers = new HashMap<>();
    private final Map<UUID, Long> attackerTimestamps = new HashMap<>();
    private ConfigManager configManager;

    public Map<UUID, UUID> getAnchorAttackers() {
        return anchorAttackers;
    }

    public void trackAttacker(final UUID victim, final UUID attacker) {
        if (victim == null || attacker == null) return;
        anchorAttackers.put(victim, attacker);
        attackerTimestamps.put(victim, System.currentTimeMillis());
    }

    public void clearVictim(final UUID victim) {
        if (victim == null) return;
        anchorAttackers.remove(victim);
        attackerTimestamps.remove(victim);
    }

    public void cleanupPlayer(final UUID playerId) {
        if (playerId == null) return;
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

        printBanner();
    }

    @Override
    public void onDisable() {
        anchorAttackers.clear();
        attackerTimestamps.clear();
        getLogger().info("[AnchorKillFix] Disabled. Anchor kill tracking stopped.");
    }

    private void printBanner() {
        final String version = getDescription().getVersion();
        final String radius = String.format("%.1f", configManager.getRadius());
        final int innerWidth = 41;

        getLogger().info("╔═══════════════════════════════════════════╗");
        getLogger().info("║" + centerPad("AnchorKillFix v" + version, innerWidth) + "║");
        getLogger().info("║" + centerPad("by Bhaskar | dev.bhaskar", innerWidth) + "║");
        getLogger().info("║" + centerPad("Crediting anchor kills since 2025", innerWidth) + "║");
        getLogger().info("╠═══════════════════════════════════════════╣");
        getLogger().info("║  API         : Paper 1.21.x               ║");
        getLogger().info("║  Status      : Enabled                    ║");
        getLogger().info("║  Radius      : " + padRight(radius + " blocks", 27) + "║");
        getLogger().info("╚═══════════════════════════════════════════╝");
    }

    private String centerPad(final String text, final int width) {
        if (text.length() >= width) return text;
        final int total = width - text.length();
        final int left = total / 2;
        final int right = total - left;
        return " ".repeat(left) + text + " ".repeat(right);
    }

    private String padRight(final String text, final int width) {
        if (text.length() >= width) return text;
        return text + " ".repeat(width - text.length());
    }
}