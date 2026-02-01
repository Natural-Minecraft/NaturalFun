package id.naturalsmp.naturalFun.trader;

import id.naturalsmp.naturalFun.NaturalFun;
import id.naturalsmp.naturalFun.utils.ColorUtils;
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
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("naturalfun.admin")) {
            sender.sendMessage("No permission.");
            return true;
        }
        
        if (args.length < 1) {
            sender.sendMessage("Usage: /traderadmin <create|remove|reload> ...");
            return true;
        }
        
        if (args[0].equalsIgnoreCase("reload")) {
            manager.load();
            sender.sendMessage(ColorUtils.miniMessage(plugin.getMessagesConfig().getString("trader.reloaded")));
            return true;
        }
        
        if (sender instanceof Player) {
            Player p = (Player) sender;
            if (args[0].equalsIgnoreCase("create")) {
                if (args.length < 3) {
                    p.sendMessage("Usage: /traderadmin create <id> <name>");
                    return true;
                }
                String id = args[1];
                String name = args[2].replace("_", " "); // Support spaces via underscore
                
                manager.createTrader(id, name, p.getLocation(), Villager.Profession.FARMER);
                p.sendMessage(ColorUtils.miniMessage(plugin.getMessagesConfig().getString("trader.created").replace("%id%", id)));
                
            } else if (args[0].equalsIgnoreCase("remove")) {
                if (args.length < 2) {
                    p.sendMessage("Usage: /traderadmin remove <id>");
                    return true;
                }
                String id = args[1];
                manager.removeTrader(id);
                p.sendMessage(ColorUtils.miniMessage(plugin.getMessagesConfig().getString("trader.removed").replace("%id%", id)));
            }
        }
        
        return true;
    }
}
