package dev.bhaskar.anchorkillfix.commands;

import dev.bhaskar.anchorkillfix.AnchorKillFix;
import dev.bhaskar.anchorkillfix.ConfigManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class ReloadCommand implements CommandExecutor, TabCompleter {

    private final AnchorKillFix plugin;
    private final ConfigManager configManager;

    public ReloadCommand(final AnchorKillFix plugin, final ConfigManager configManager) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
        this.configManager = Objects.requireNonNull(configManager, "configManager");
    }

    @Override
    public boolean onCommand(final CommandSender sender, final Command command,
                             final String label, final String[] args) {
        if (!sender.hasPermission("anchorkillfix.reload")) {
            sender.sendMessage(configManager.getPrefix() + " " + configManager.getMessage("no-permission"));
            return true;
        }

        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            configManager.load();
            sender.sendMessage(configManager.getPrefix() + " " + configManager.getMessage("reload-success"));
            return true;
        }

        sender.sendMessage(configManager.getPrefix() + " Usage: /anchorkillfix reload");
        return true;
    }

    @Override
    public List<String> onTabComplete(final CommandSender sender, final Command command,
                                      final String alias, final String[] args) {
        if (args.length == 1) {
            return Collections.singletonList("reload");
        }
        return Collections.emptyList();
    }
}