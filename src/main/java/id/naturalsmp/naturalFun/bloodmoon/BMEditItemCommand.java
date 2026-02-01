package id.naturalsmp.naturalFun.bloodmoon;

import id.naturalsmp.naturalFun.NaturalFun;
import id.naturalsmp.naturalFun.utils.ColorUtils;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

public class BMEditItemCommand implements CommandExecutor {

    private final NaturalFun plugin;

    public BMEditItemCommand(NaturalFun plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) return true;
        Player player = (Player) sender;
        
        if (!player.hasPermission("bloodmoon.admin")) {
            player.sendMessage(ColorUtils.miniMessage("<red>No permission."));
            return true;
        }

        ItemStack item = player.getInventory().getItemInMainHand();
        if (item.getType().isAir()) {
            player.sendMessage(ColorUtils.miniMessage("<red>You must hold an item."));
            return true;
        }

        if (args.length < 1) {
            player.sendMessage(ColorUtils.miniMessage("<red>Usage: /" + label + " <value>"));
            return true;
        }

        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        
        String action = label.toLowerCase();
        // Determine action based on alias or label (assuming registered separately or handled here)
        // I will register separate commands: bmsetprice, bmsetstock, bmsetrarity pointing to this executor
        
        if (action.equals("bmsetprice")) {
             try {
                 int price = Integer.parseInt(args[0]);
                 pdc.set(new NamespacedKey(plugin, "shop_price"), PersistentDataType.INTEGER, price);
                 player.sendMessage(ColorUtils.miniMessage("<green>Price set to " + price));
             } catch (NumberFormatException e) {
                 player.sendMessage(ColorUtils.miniMessage("<red>Invalid number."));
             }
        } else if (action.equals("bmsetstock")) {
             try {
                 int stock = Integer.parseInt(args[0]);
                 pdc.set(new NamespacedKey(plugin, "shop_stock"), PersistentDataType.INTEGER, stock);
                 player.sendMessage(ColorUtils.miniMessage("<green>Stock set to " + stock));
             } catch (NumberFormatException e) {
                 player.sendMessage(ColorUtils.miniMessage("<red>Invalid number."));
             }
        } else if (action.equals("bmsetrarity")) {
             String rarity = args[0];
             pdc.set(new NamespacedKey(plugin, "shop_rarity"), PersistentDataType.STRING, rarity);
             player.sendMessage(ColorUtils.miniMessage("<green>Rarity set to " + rarity));
        }
        
        item.setItemMeta(meta);
        return true;
    }
}
