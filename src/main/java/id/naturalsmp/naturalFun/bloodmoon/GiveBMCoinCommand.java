package id.naturalsmp.naturalFun.bloodmoon;

import id.naturalsmp.naturalFun.utils.ColorUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class GiveBMCoinCommand implements CommandExecutor {

    private final BloodmoonCurrencyManager currencyManager;

    public GiveBMCoinCommand(BloodmoonCurrencyManager currencyManager) {
        this.currencyManager = currencyManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("bloodmoon.admin")) {
            sender.sendMessage(ColorUtils.miniMessage("<red>You do not have permission to use this command."));
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(ColorUtils.miniMessage("<red>Usage: /givebmcoin <player> <amount>"));
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage(ColorUtils.miniMessage("<red>Player not found."));
            return true;
        }

        int amount;
        try {
            amount = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            sender.sendMessage(ColorUtils.miniMessage("<red>Invalid amount."));
            return true;
        }

        currencyManager.addBalance(target.getUniqueId(), amount);
        sender.sendMessage(ColorUtils.miniMessage("<green>Gave <yellow>" + amount + " <green>Bloodmoon Coins to <aqua>" + target.getName()));
        target.sendMessage(ColorUtils.miniMessage("<green>You received <yellow>" + amount + " <green>Bloodmoon Coins!"));
        return true;
    }
}
