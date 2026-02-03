package id.naturalsmp.naturalFun.utils;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class GUIUtils {

    public static Inventory createGUI(InventoryHolder holder, int size, String title) {
        return Bukkit.createInventory(holder, size, ChatUtils.toComponent(title));
    }

    public static ItemStack createItem(Material material, String name, List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(ChatUtils.toComponent(name));
            if (lore != null && !lore.isEmpty()) {
                List<Component> loreComponents = new ArrayList<>();
                for (String line : lore) {
                    loreComponents.add(ChatUtils.toComponent(line));
                }
                meta.lore(loreComponents);
            }
            item.setItemMeta(meta);
        }
        return item;
    }

    public static ItemStack createItem(Material material, String name, String... lore) {
        return createItem(material, name, List.of(lore));
    }

    public static ItemStack createFiller(Material material) {
        ItemStack filler = new ItemStack(material);
        ItemMeta meta = filler.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.empty());
            filler.setItemMeta(meta);
        }
        return filler;
    }

    public static void fillEmpty(Inventory inv) {
        ItemStack filler = createFiller(Material.BLACK_STAINED_GLASS_PANE);
        for (int i = 0; i < inv.getSize(); i++) {
            if (inv.getItem(i) == null)
                inv.setItem(i, filler);
        }
    }
}
