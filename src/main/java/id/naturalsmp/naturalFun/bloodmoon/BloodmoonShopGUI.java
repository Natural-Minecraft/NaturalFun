package id.naturalsmp.naturalFun.bloodmoon;

import id.naturalsmp.naturalFun.bloodmoon.BloodmoonShopManager.ShopItem;
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

public class BloodmoonShopGUI implements Listener {

    private final BloodmoonShopManager shopManager;
    private final BloodmoonCurrencyManager currencyManager;

    public BloodmoonShopGUI(BloodmoonShopManager shopManager, BloodmoonCurrencyManager currencyManager) {
        this.shopManager = shopManager;
        this.currencyManager = currencyManager;
    }

    public void openShop(Player player, int page) {
        Inventory inv = GUIUtils.createGUI(new ShopHolder(false, page), 54,
                "<gradient:#00008B:#ADD8E6>Bloodmoon Shop</gradient>");
        fillInventory(inv, page, false);
        player.openInventory(inv);
    }

    public void openEditor(Player player, int page) {
        Inventory inv = GUIUtils.createGUI(new ShopHolder(true, page), 54,
                "<gradient:#8B0000:#FF4500>Shop Editor</gradient>");
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
                List<Component> lore = meta.hasLore() ? (meta.lore() != null ? meta.lore() : new ArrayList<>())
                        : new ArrayList<>();

                lore.add(Component.empty());
                lore.add(ChatUtils.toComponent("<gray>Price: <yellow>" + item.getPrice() + " Coins"));
                lore.add(ChatUtils.toComponent("<gray>Stock: <yellow>" + item.getStock()));
                lore.add(ChatUtils.toComponent("<gray>Rarity: <aqua>" + item.getRarity()));
                lore.add(Component.empty());

                if (isEditor) {
                    lore.add(ChatUtils.toComponent("<red>Right-Click to Delete"));
                    lore.add(ChatUtils.toComponent("<gray>ID: " + item.getId()));
                } else {
                    lore.add(ChatUtils.toComponent("<yellow>Click to buy!"));
                }

                meta.lore(lore);
                displayItem.setItemMeta(meta);
            }
            inv.setItem(i - startIndex, displayItem);
        }

        if (page > 0) {
            inv.setItem(45, GUIUtils.createItem(Material.ARROW, "<yellow>Previous Page"));
        }

        if ((page + 1) * 45 < items.size()) {
            inv.setItem(53, GUIUtils.createItem(Material.ARROW, "<yellow>Next Page"));
        }

        if (isEditor) {
            inv.setItem(49, GUIUtils.createItem(Material.EMERALD_BLOCK, "<green><b>Add Held Item</b>",
                    "<gray>Click to add item in your hand"));
        }

        GUIUtils.fillEmpty(inv);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player))
            return;
        Player player = (Player) event.getWhoClicked();
        Inventory inv = event.getInventory();

        if (!(inv.getHolder() instanceof ShopHolder))
            return;
        ShopHolder holder = (ShopHolder) inv.getHolder();

        event.setCancelled(true);

        int slot = event.getRawSlot();
        if (slot > 53)
            return;

        if (slot == 45) {
            if (holder.page > 0) {
                if (holder.isEditor)
                    openEditor(player, holder.page - 1);
                else
                    openShop(player, holder.page - 1);
            }
            return;
        }
        if (slot == 53) {
            if (inv.getItem(53) != null && inv.getItem(53).getType() == Material.ARROW) {
                if (holder.isEditor)
                    openEditor(player, holder.page + 1);
                else
                    openShop(player, holder.page + 1);
            }
            return;
        }

        if (holder.isEditor) {
            if (slot == 49) {
                ItemStack hand = player.getInventory().getItemInMainHand();
                if (hand == null || hand.getType().isAir()) {
                    player.sendMessage(ChatUtils.toComponent("<red>You must hold an item to add it."));
                    return;
                }

                shopManager.addShopItem(hand.clone(), 100, 10, "Common");
                player.sendMessage(ChatUtils.toComponent("<green>Item added! Price: 100, Stock: 10"));
                openEditor(player, holder.page);
                return;
            }

            ItemStack clicked = inv.getItem(slot);
            if (clicked != null && slot < 45) {
                int index = holder.page * 45 + slot;
                List<ShopItem> items = shopManager.getShopItems();
                if (index >= items.size())
                    return;

                ShopItem item = items.get(index);

                if (event.isRightClick() && !event.isShiftClick()) {
                    shopManager.removeShopItem(item.getId());
                    player.sendMessage(ChatUtils.toComponent("<red>Item removed."));
                    openEditor(player, holder.page);
                    return;
                }

                if (event.isShiftClick() && event.isLeftClick()) {
                    player.closeInventory();
                    player.sendMessage(ChatUtils.toComponent("<yellow>Type new PRICE in chat (or 'cancel'):"));
                    EditorInputListener.awaitInput(player, (input) -> {
                        try {
                            int val = Integer.parseInt(input);
                            if (val < 0)
                                throw new NumberFormatException();
                            item.setPrice(val);
                            shopManager.save();
                            player.sendMessage(ChatUtils.toComponent("<green>Price updated to " + val));
                        } catch (Exception e) {
                            player.sendMessage(ChatUtils.toComponent("<red>Invalid number."));
                        }
                        openEditor(player, holder.page);
                    });
                    return;
                }

                if (event.isShiftClick() && event.isRightClick()) {
                    player.closeInventory();
                    player.sendMessage(ChatUtils.toComponent("<yellow>Type new STOCK in chat (or 'cancel'):"));
                    EditorInputListener.awaitInput(player, (input) -> {
                        try {
                            int val = Integer.parseInt(input);
                            item.setStock(val);
                            shopManager.save();
                            player.sendMessage(ChatUtils.toComponent("<green>Stock updated to " + val));
                        } catch (Exception e) {
                            player.sendMessage(ChatUtils.toComponent("<red>Invalid number."));
                        }
                        openEditor(player, holder.page);
                    });
                    return;
                }
            }
            return;
        }

        if (!holder.isEditor) {
            ItemStack clicked = inv.getItem(slot);
            if (clicked != null && slot < 45) {
                int index = holder.page * 45 + slot;
                List<ShopItem> items = shopManager.getShopItems();
                if (index < items.size()) {
                    ShopItem item = items.get(index);

                    if (item.getStock() <= 0) {
                        player.sendMessage(ChatUtils.toComponent("<red>Out of stock!"));
                        return;
                    }

                    if (currencyManager.hasBalance(player.getUniqueId(), item.getPrice())) {
                        currencyManager.takeBalance(player.getUniqueId(), item.getPrice());
                        item.decreaseStock(1);
                        shopManager.save();
                        player.getInventory().addItem(item.getItemStack().clone());
                        player.sendMessage(
                                ChatUtils.toComponent("<green>Purchased for <yellow>" + item.getPrice() + " coins!"));
                        openShop(player, holder.page);
                    } else {
                        player.sendMessage(ChatUtils.toComponent("<red>Not enough Bloodmoon Coins!"));
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

        @Override
        public @NotNull Inventory getInventory() {
            return null;
        }
    }
}
