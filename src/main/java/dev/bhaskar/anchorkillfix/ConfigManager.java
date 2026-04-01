package dev.bhaskar.anchorkillfix;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.Objects;

/**
 * Manages plugin configuration access and reload behavior.
 */
public final class ConfigManager {
    private final AnchorKillFix plugin;
    private volatile FileConfiguration config;

    /**
     * Creates a new configuration manager.
     *
     * @param plugin plugin instance
     */
    public ConfigManager(final AnchorKillFix plugin) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
    }

    /**
     * Loads the plugin configuration from disk.
     */
    public void load() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        config = plugin.getConfig();
    }

    /**
     * Reloads the plugin configuration from disk.
     */
    public void reload() {
        plugin.reloadConfig();
        config = plugin.getConfig();
    }

    /**
     * Gets the configured detection radius.
     *
     * @return detection radius in blocks
     */
    public double getRadius() {
        final FileConfiguration current = config;
        return current == null ? 12.0D : current.getDouble("settings.detection-radius", 12.0D);
    }

    /**
     * Checks whether debug logging is enabled.
     *
     * @return true when debug logging is enabled
     */
    public boolean isDebug() {
        final FileConfiguration current = config;
        return current != null && current.getBoolean("settings.debug", false);
    }

    /**
     * Checks whether kill logging is enabled.
     *
     * @return true when kill logging is enabled
     */
    public boolean isLogKills() {
        final FileConfiguration current = config;
        return current == null || current.getBoolean("settings.log-kills", true);
    }

    /**
     * Gets a message from configuration with color codes translated.
     *
     * @param key message key inside messages section
     * @return translated message
     */
    public String getMessage(final String key) {
        final FileConfiguration current = config;
        if (current == null) {
            return "";
        }

        final String message = current.getString("messages." + key, "");
        return ChatColor.translateAlternateColorCodes('&', message == null ? "" : message);
    }
}
