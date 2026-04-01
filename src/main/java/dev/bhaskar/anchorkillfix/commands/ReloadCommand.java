package dev.bhaskar.anchorkillfix.commands;

import dev.bhaskar.anchorkillfix.AnchorKillFix;
import dev.bhaskar.anchorkillfix.ConfigManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * Handles AnchorKillFix command execution and tab completion.
 */
public final class ReloadCommand implements CommandExecutor, TabCompleter {
    private final AnchorKillFix plugin;
    private final ConfigManager configManager;

    /**
     * Creates a new command handler.
     *
     * @param plugin        plugin instance
     * @param configManager configuration manager
     */
    public ReloadCommand(final AnchorKillFix plugin, final ConfigManager configManager) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
        this.configManager = Objects.requireNonNull(configManager, "configManager");
    }

    /**
     * Executes the management command.
     *
     * @param sender  command sender
     * @param command command instance
     * @param label   command label
     * @param args    command arguments
     * @return true when command was handled
     */
    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        if (args.length != 1 || !"reload".equalsIgnoreCase(args[0])) {
            return false;
        }

        if (!sender.hasPermission("anchorkillfix.reload")) {
            sender.sendMessage(configManager.getMessage("prefix") + " " + configManager.getMessage("no-permission"));
            return true;
        }

        configManager.reload();
        sender.sendMessage(configManager.getMessage("prefix") + " " + configManager.getMessage("reload-success"));
        if (configManager.isDebug()) {
            plugin.getLogger().info("[AnchorKillFix] Configuration reloaded by " + sender.getName());
        }
        return true;
    }

    /**
     * Provides tab completion for command arguments.
     *
     * @param sender  command sender
     * @param command command instance
     * @param alias   command alias
     * @param args    current arguments
     * @return tab completion suggestions
     */
    @Override
    public List<String> onTabComplete(final CommandSender sender, final Command command, final String alias, final String[] args) {
        if (args.length == 1) {
            final String current = args[0].toLowerCase(Locale.ROOT);
            if ("reload".startsWith(current)) {
                return Collections.singletonList("reload");
            }
        }
        return Collections.emptyList();
    }
}
