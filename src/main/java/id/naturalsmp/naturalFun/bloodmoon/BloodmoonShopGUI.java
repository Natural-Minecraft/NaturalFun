package id.naturalsmp.naturalFun.bloodmoon;

import id.naturalsmp.naturalFun.bloodmoon.BloodmoonShopManager.ShopItem;
import id.naturalsmp.naturalFun.utils.ColorUtils;
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

public class BloodmoonShopGUI implements Listener {

    private final BloodmoonShopManager shopManager;
    private final BloodmoonCurrencyManager currencyManager;

    public BloodmoonShopGUI(BloodmoonShopManager shopManager, BloodmoonCurrencyManager currencyManager) {
        this.shopManager = shopManager;
        this.currencyManager = currencyManager;
    }

    public void openShop(Player player, int page) {
        Inventory inv = Bukkit.createInventory(new ShopHolder(false, page), 54, ColorUtils.miniMessage("<gradient:#00008B:#ADD8E6>Bloodmoon Shop</gradient>"));
        fillInventory(inv, page, false);
        player.openInventory(inv);
    }

    public void openEditor(Player player, int page) {
        Inventory inv = Bukkit.createInventory(new ShopHolder(true, page), 54, ColorUtils.miniMessage("<gradient:#8B0000:#FF4500>Shop Editor</gradient>"));
        fillInventory(inv, page, true);
        player.openInventory(inv);
    }

    private void fillInventory(Inventory inv, int page, boolean isEditor) {
        List<ShopItem> items = shopManager.getShopItems();
        int startIndex = page * 45;
        int endIndex = Math.min(startIndex + 45, items.size());

        for (int i = startIndex; i < endIndex; i++) {
            ShopItem item = items.get(i);
            ItemStack displayItem = item.getItemStack().clone();
            ItemMeta meta = displayItem.getItemMeta();
            if (meta != null) {
                List<Component> lore = meta.hasLore() ? meta.lore() : new ArrayList<>();
                if (lore == null) lore = new ArrayList<>();

                lore.add(Component.text(""));
                lore.add(ColorUtils.miniMessage("<gray>Price: <yellow>" + item.getPrice() + " Coins"));
                lore.add(ColorUtils.miniMessage("<gray>Stock: <yellow>" + item.getStock()));
                lore.add(ColorUtils.miniMessage("<gray>Rarity: <aqua>" + item.getRarity()));
                lore.add(Component.text(""));
                
                if (isEditor) {
                     lore.add(ColorUtils.miniMessage("<red>Right-Click to Delete"));
                     lore.add(ColorUtils.miniMessage("<gray>ID: " + item.getId()));
                } else {
                     lore.add(ColorUtils.miniMessage("<yellow>Click to buy!"));
                }

                meta.lore(lore);
                displayItem.setItemMeta(meta);
            }
            inv.setItem(i - startIndex, displayItem);
        }

        // Navigation
        if (page > 0) {
            ItemStack back = new ItemStack(Material.ARROW);
            ItemMeta meta = back.getItemMeta();
            meta.displayName(ColorUtils.miniMessage("<yellow>Previous Page"));
            back.setItemMeta(meta);
            inv.setItem(45, back);
        }

        if ((page + 1) * 45 < items.size()) {
            ItemStack next = new ItemStack(Material.ARROW);
            ItemMeta meta = next.getItemMeta();
            meta.displayName(ColorUtils.miniMessage("<yellow>Next Page"));
            next.setItemMeta(meta);
            inv.setItem(53, next);
        }
        
        // Editor Add Button
        if (isEditor) {
            ItemStack addItem = new ItemStack(Material.EMERALD_BLOCK);
            ItemMeta addMeta = addItem.getItemMeta();
            addMeta.displayName(ColorUtils.miniMessage("<green><b>Add Held Item</b>"));
            List<Component> lore = new ArrayList<>();
            lore.add(ColorUtils.miniMessage("<gray>Click to add item in your hand"));
            addMeta.lore(lore);
            addItem.setItemMeta(addMeta);
            inv.setItem(49, addItem);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        Inventory inv = event.getInventory();
        
        if (!(inv.getHolder() instanceof ShopHolder)) return;
        ShopHolder holder = (ShopHolder) inv.getHolder();
        
        event.setCancelled(true);
        
        int slot = event.getRawSlot();
        if (slot > 53) return; // Clicked in player inventory

        // Navigation
        if (slot == 45) { // Previous
             if (holder.page > 0) {
                 if (holder.isEditor) openEditor(player, holder.page - 1);
                 else openShop(player, holder.page - 1);
             }
             return;
        }
        if (slot == 53) { // Next
             // Logic to check if there is a next page?
             // We can just rely on the button being there
             if (inv.getItem(53) != null) {
                 if (holder.isEditor) openEditor(player, holder.page + 1);
                 else openShop(player, holder.page + 1);
             }
             return;
        }
        
        // Editor Actions
        if (holder.isEditor) {
            if (slot == 49) { // Add Item
                ItemStack hand = player.getInventory().getItemInMainHand();
                if (hand == null || hand.getType().isAir()) {
                    player.sendMessage(ColorUtils.miniMessage("<red>You must hold an item to add it."));
                    return;
                }
                
                int price = 100;
                int stock = 10;
                String rarity = "Common";
                
                ItemMeta meta = hand.getItemMeta();
                if (meta != null) {
                    org.bukkit.persistence.PersistentDataContainer pdc = meta.getPersistentDataContainer();
                    org.bukkit.NamespacedKey priceKey = new org.bukkit.NamespacedKey(id.naturalsmp.naturalFun.NaturalFun.getPlugin(id.naturalsmp.naturalFun.NaturalFun.class), "shop_price");
                    org.bukkit.NamespacedKey stockKey = new org.bukkit.NamespacedKey(id.naturalsmp.naturalFun.NaturalFun.getPlugin(id.naturalsmp.naturalFun.NaturalFun.class), "shop_stock");
                    org.bukkit.NamespacedKey rarityKey = new org.bukkit.NamespacedKey(id.naturalsmp.naturalFun.NaturalFun.getPlugin(id.naturalsmp.naturalFun.NaturalFun.class), "shop_rarity");
                    
                    if (pdc.has(priceKey, org.bukkit.persistence.PersistentDataType.INTEGER)) {
                        price = pdc.get(priceKey, org.bukkit.persistence.PersistentDataType.INTEGER);
                    }
                    if (pdc.has(stockKey, org.bukkit.persistence.PersistentDataType.INTEGER)) {
                        stock = pdc.get(stockKey, org.bukkit.persistence.PersistentDataType.INTEGER);
                    }
                    if (pdc.has(rarityKey, org.bukkit.persistence.PersistentDataType.STRING)) {
                        rarity = pdc.get(rarityKey, org.bukkit.persistence.PersistentDataType.STRING);
                    }
                }
                
                shopManager.addShopItem(hand.clone(), price, stock, rarity);
                player.sendMessage(ColorUtils.miniMessage("<green>Item added! Price: " + price + ", Stock: " + stock + ", Rarity: " + rarity));
                openEditor(player, holder.page);
                return;
            }
            
            // Delete Item (Right Click or Shift Right?)
            ItemStack clicked = inv.getItem(slot);
            if (clicked != null && slot < 45) {
                if (event.isRightClick()) {
                    // Find item ID from manager (match index)
                    int index = holder.page * 45 + slot;
                    List<ShopItem> items = shopManager.getShopItems();
                    if (index < items.size()) {
                        ShopItem item = items.get(index);
                        shopManager.removeShopItem(item.getId());
                        player.sendMessage(ColorUtils.miniMessage("<red>Item removed."));
                        openEditor(player, holder.page);
                    }
                }
            }
            return;
        }
        
        // Shop Buy Action
        if (!holder.isEditor) {
             ItemStack clicked = inv.getItem(slot);
             if (clicked != null && slot < 45) {
                 int index = holder.page * 45 + slot;
                 List<ShopItem> items = shopManager.getShopItems();
                 if (index < items.size()) {
                     ShopItem item = items.get(index);
                     
                     if (item.getStock() <= 0) {
                         player.sendMessage(ColorUtils.miniMessage("<red>Out of stock!"));
                         return;
                     }
                     
                     if (currencyManager.hasBalance(player.getUniqueId(), item.getPrice())) {
                         currencyManager.takeBalance(player.getUniqueId(), item.getPrice());
                         item.decreaseStock(1);
                         shopManager.save(); // Save stock change
                         
                         // Give item
                         player.getInventory().addItem(item.getItemStack().clone());
                         player.sendMessage(ColorUtils.miniMessage("<green>Purchased for <yellow>" + item.getPrice() + " coins!"));
                         
                         openShop(player, holder.page); // Refresh GUI
                     } else {
                         player.sendMessage(ColorUtils.miniMessage("<red>Not enough Bloodmoon Coins!"));
                     }
                 }
             }
        }
    }

    public static class ShopHolder implements InventoryHolder {
        private final boolean isEditor;
        private final int page;
        public ShopHolder(boolean isEditor, int page) { 
            this.isEditor = isEditor; 
            this.page = page;
        }
        @Override public @NotNull Inventory getInventory() { return null; }
    }
}
