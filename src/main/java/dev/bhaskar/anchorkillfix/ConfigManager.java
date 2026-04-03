package dev.bhaskar.anchorkillfix;

import org.bukkit.ChatColor;

import java.util.Objects;

public final class ConfigManager {

    private final AnchorKillFix plugin;

    public ConfigManager(final AnchorKillFix plugin) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
    }

    public void load() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
    }

    public double getRadius() {
        return plugin.getConfig().getDouble("settings.detection-radius", 12.0);
    }

    public boolean isDebug() {
        return plugin.getConfig().getBoolean("settings.debug", false);
    }

    public boolean isLogKills() {
        return plugin.getConfig().getBoolean("settings.log-kills", true);
    }

    public String getMessage(final String key) {
        final String raw = plugin.getConfig().getString("messages." + key, "");
        return ChatColor.translateAlternateColorCodes('&', raw);
    }

    public String getPrefix() {
        return getMessage("prefix");
    }
}