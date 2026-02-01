package id.naturalsmp.naturalFun.bloodmoon;

import id.naturalsmp.naturalFun.utils.ColorUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class BloodmoonShopCommand implements CommandExecutor {

    private final BloodmoonShopGUI shopGUI;

    public BloodmoonShopCommand(BloodmoonShopGUI shopGUI) {
        this.shopGUI = shopGUI;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ColorUtils.miniMessage("<red>Only players can use this command."));
            return true;
        }
        Player player = (Player) sender;

        if (args.length > 0 && args[0].equalsIgnoreCase("editor")) {
            if (!player.hasPermission("bloodmoon.admin")) {
                player.sendMessage(ColorUtils.miniMessage("<red>No permission."));
                return true;
            }
            shopGUI.openEditor(player, 0);
            return true;
        }

        shopGUI.openShop(player, 0);
        return true;
    }
}
