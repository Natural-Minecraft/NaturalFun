package id.naturalsmp.naturalFun.bloodmoon;

import id.naturalsmp.naturalFun.NaturalFun;
import id.naturalsmp.naturalFun.utils.ChatUtils;
import id.naturalsmp.naturalFun.utils.GUIUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class BloodmoonAdminGUI implements Listener {

    private final NaturalFun plugin;
    private final BloodmoonManager bloodmoonManager;
    private final BloodmoonShopGUI shopGUI;

    public BloodmoonAdminGUI(NaturalFun plugin, BloodmoonManager bloodmoonManager, BloodmoonShopGUI shopGUI) {
        this.plugin = plugin;
        this.bloodmoonManager = bloodmoonManager;
        this.shopGUI = shopGUI;
    }

    public void open(Player player) {
        Inventory inv = GUIUtils.createGUI(new AdminHolder(), 27,
                "<gradient:#8B0000:#FF0000>Bloodmoon Admin</gradient>");
        fillInventory(inv);
        player.openInventory(inv);
    }

    private void fillInventory(Inventory inv) {
        // 1. Toggle Bloodmoon (Slot 11)
        boolean isActive = bloodmoonManager.isBloodmoonActive();
        Material toggleIcon = isActive ? Material.REDSTONE_TORCH : Material.LEVER;
        String toggleName = isActive ? "<red>Stop Bloodmoon" : "<green>Start Bloodmoon";
        ItemStack toggle = GUIUtils.createItem(toggleIcon, toggleName,
                "<gray>Status: " + (isActive ? "<red>Active" : "<green>Inactive"),
                "",
                "<yellow>Click to toggle!");
        inv.setItem(11, toggle);

        // 2. Mob Settings (Elite Mobs) (Slot 13)
        boolean eliteEnabled = plugin.getConfig().getBoolean("bloodmoon.elite-mobs.enabled", true);
        ItemStack mobs = GUIUtils.createItem(Material.ZOMBIE_HEAD, "<red>Elite Mobs Settings",
                "<gray>Enabled: " + (eliteEnabled ? "<green>True" : "<red>False"),
                "<gray>Multiplier: <yellow>"
                        + plugin.getConfig().getDouble("bloodmoon.elite-mobs.health-multiplier", 3.0) + "x",
                "",
                "<yellow>Click to toggle enable/disable");
        inv.setItem(13, mobs);

        // 3. Shop Editor (Slot 15)
        ItemStack shop = GUIUtils.createItem(Material.EMERALD, "<green>Shop Editor",
                "<gray>Manage items in the",
                "<gray>Bloodmoon Shop.",
                "",
                "<yellow>Click to open editor");
        inv.setItem(15, shop);

        // 4. Shadow Assassin Spawn Rate (Slot 22)
        double currentChance = plugin.getConfig().getDouble("bloodmoon.shadow-assassin.chance", 0.01);
        int chancePercent = (int) (currentChance * 100);
        ItemStack rate = GUIUtils.createItem(Material.WITHER_SKELETON_SKULL, "<red>Shadow Assassin Rate",
                "<gray>Current Chance: <yellow>" + chancePercent + "%",
                "",
                "<green>Left-Click to +1%",
                "<red>Right-Click to -1%");
        inv.setItem(22, rate);

        GUIUtils.fillEmpty(inv);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof AdminHolder))
            return;
        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player))
            return;
        Player p = (Player) event.getWhoClicked();

        int slot = event.getRawSlot();

        if (slot == 11) { // Toggle
            if (bloodmoonManager.isBloodmoonActive()) {
                bloodmoonManager.stopBloodmoon(p.getWorld());
            } else {
                bloodmoonManager.startBloodmoon(p.getWorld());
            }
            open(p); // Refresh
        } else if (slot == 13) { // Elite Mobs Toggle
            boolean current = plugin.getConfig().getBoolean("bloodmoon.elite-mobs.enabled", true);
            plugin.getConfig().set("bloodmoon.elite-mobs.enabled", !current);
            plugin.saveConfig();
            open(p); // Refresh
        } else if (slot == 15) { // Shop Editor
            shopGUI.openEditor(p, 0);
        } else if (slot == 22) { // Shadow Assassin Rate
            double currentChance = plugin.getConfig().getDouble("bloodmoon.shadow-assassin.chance", 0.01);

            if (event.isLeftClick()) {
                currentChance += 0.01;
                if (currentChance > 1.0)
                    currentChance = 1.0;
            } else if (event.isRightClick()) {
                currentChance -= 0.01;
                if (currentChance < 0.0)
                    currentChance = 0.0;
            }

            currentChance = Math.round(currentChance * 100.0) / 100.0;
            plugin.getConfig().set("bloodmoon.shadow-assassin.chance", currentChance);
            plugin.saveConfig();
            open(p); // Refresh
        }
    }

    public static class AdminHolder implements InventoryHolder {
        @Override
        public @NotNull Inventory getInventory() {
            return null;
        }
    }
}
