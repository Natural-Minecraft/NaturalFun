package id.naturalsmp.naturalFun.trader;

import id.naturalsmp.naturalFun.NaturalFun;
import id.naturalsmp.naturalFun.utils.ChatUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.jetbrains.annotations.NotNull;

public class TraderCommand implements CommandExecutor {

    private final NaturalFun plugin;
    private final TraderManager manager;

    public TraderCommand(NaturalFun plugin, TraderManager manager) {
        this.plugin = plugin;
        this.manager = manager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label,
            @NotNull String[] args) {
        if (!sender.hasPermission("naturalfun.admin")) {
            sender.sendMessage(ChatUtils.toComponent("<red>No permission."));
            return true;
        }

        if (args.length < 1) {
            sender.sendMessage(ChatUtils.toComponent("<red>Usage: /traderadmin <create|remove|reload> ..."));
            return true;
        }

        if (args[0].equalsIgnoreCase("reload")) {
            manager.load();
            String msg = plugin.getMessagesConfig().getString("trader.reloaded", "<green>Traders reloaded!");
            sender.sendMessage(ChatUtils.toComponent(msg));
            return true;
        }

        if (sender instanceof Player) {
            Player p = (Player) sender;
            if (args[0].equalsIgnoreCase("create")) {
                if (args.length < 3) {
                    p.sendMessage(ChatUtils.toComponent("<red>Usage: /traderadmin create <id> <name>"));
                    return true;
                }
                String id = args[1];
                String name = args[2].replace("_", " ");

                manager.createTrader(id, name, p.getLocation(), Villager.Profession.FARMER);
                String msg = plugin.getMessagesConfig().getString("trader.created", "<green>Trader created: %id%")
                        .replace("%id%", id);
                p.sendMessage(ChatUtils.toComponent(msg));

            } else if (args[0].equalsIgnoreCase("remove")) {
                if (args.length < 2) {
                    p.sendMessage(ChatUtils.toComponent("<red>Usage: /traderadmin remove <id>"));
                    return true;
                }
                String id = args[1];
                manager.removeTrader(id);
                String msg = plugin.getMessagesConfig().getString("trader.removed", "<red>Trader removed: %id%")
                        .replace("%id%", id);
                p.sendMessage(ChatUtils.toComponent(msg));
            }
        }

        return true;
    }
}
