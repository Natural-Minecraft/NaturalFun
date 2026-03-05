package id.naturalsmp.naturalFun.emoji;

import id.naturalsmp.naturalFun.NaturalFun;
import id.naturalsmp.naturalFun.utils.ChatUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class EmojiGUI implements Listener {

    private final NaturalFun plugin;
    private final EmojiManager emojiManager;

    public EmojiGUI(NaturalFun plugin) {
        this.plugin = plugin;
        this.emojiManager = EmojiManager.getInstance();
    }

    public void openGUI(Player p) {
        p.playSound(p.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_CHIME, 1.0f, 1.0f);
        Inventory inv = org.bukkit.Bukkit.createInventory(null, 54,
                ChatUtils.toComponent("&#00FFD4&lＮＡＴＵＲＡＬ &8| &#FF00D4&lＥＭＯＪＩＳ"));

        // --- GLASSMORPHISM BORDER ---
        ItemStack glass = createFiller(Material.PINK_STAINED_GLASS_PANE);
        ItemStack glass2 = createFiller(Material.MAGENTA_STAINED_GLASS_PANE);

        for (int i = 0; i < 9; i++) {
            inv.setItem(i, glass);
            inv.setItem(45 + i, glass);
        }
        for (int i = 0; i < 6; i++) {
            inv.setItem(i * 9, glass2);
            inv.setItem(i * 9 + 8, glass2);
        }

        // --- EMOJI CONTENT SLOTS (Inner 7x4) ---
        int[] contentSlots = {
                10, 11, 12, 13, 14, 15, 16,
                19, 20, 21, 22, 23, 24, 25,
                28, 29, 30, 31, 32, 33, 34,
                37, 38, 39, 40, 41, 42, 43
        };

        // Deduplicate emojis by name
        Map<String, EmojiManager.EmojiData> registry = emojiManager.getEmojiRegistry();
        Map<String, EmojiManager.EmojiData> unique = new java.util.LinkedHashMap<>();
        Map<String, String> firstTriggers = new java.util.LinkedHashMap<>();

        for (Map.Entry<String, EmojiManager.EmojiData> entry : registry.entrySet()) {
            String emojiName = entry.getValue().getName();
            if (!unique.containsKey(emojiName)) {
                unique.put(emojiName, entry.getValue());
                firstTriggers.put(emojiName, entry.getKey());
            }
        }

        int slotIdx = 0;
        for (Map.Entry<String, EmojiManager.EmojiData> entry : unique.entrySet()) {
            if (slotIdx >= contentSlots.length)
                break;

            EmojiManager.EmojiData data = entry.getValue();
            String trigger = firstTriggers.get(entry.getKey());

            boolean unlocked = !data.hasPermission() || p.hasPermission(data.getPermission());

            Material icon = unlocked ? Material.PAPER : Material.BARRIER;
            String name = unlocked ? "&#FF00D4&l" + data.getCharacter() : "&c&l🔒 LOCKED";

            List<String> lore = new ArrayList<>();
            lore.add("");
            lore.add("&7Name: &e" + data.getName());
            lore.add("&7Shortcut: &e" + trigger);
            lore.add("");
            if (unlocked) {
                lore.add("&#00FFD4&l➥ KLIK UNTUK KIRIM");
            } else {
                lore.add("&cDibutuhkan: &7" + data.getPermission());
                lore.add("&7Ajak admin untuk info lebih lanjut!");
            }

            inv.setItem(contentSlots[slotIdx], createItem(icon, name, lore, trigger));
            slotIdx++;
        }

        // Info button
        inv.setItem(49, createItem(Material.BOOK, "&#00FFD4&lCARA PAKAI",
                List.of("", "&7Cukup ketik &e:trigger: &7di chat", "&7atau klik emoji di menu ini!", ""), ""));

        p.openInventory(inv);
    }

    private ItemStack createFiller(Material mat) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.empty());
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createItem(Material mat, String name, List<String> lore, String triggerKey) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(ChatUtils.toComponent(name));
        List<Component> coloredLore = new ArrayList<>();
        for (String s : lore)
            coloredLore.add(ChatUtils.toComponent(s));
        coloredLore.add(ChatUtils.toComponent("&0id:" + triggerKey)); // Hidden ID
        meta.lore(coloredLore);
        item.setItemMeta(meta);
        return item;
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        String title = stripAllColor(e.getView().getTitle());
        if (title.contains("NATURAL") && title.contains("EMOJI")) {
            e.setCancelled(true);
            if (e.getClickedInventory() != e.getView().getTopInventory())
                return;
            if (e.getCurrentItem() == null || e.getCurrentItem().getType() == Material.AIR)
                return;

            Player p = (Player) e.getWhoClicked();
            ItemStack item = e.getCurrentItem();
            ItemMeta meta = item.getItemMeta();

            if (meta != null && meta.hasLore()) {
                @SuppressWarnings("deprecation")
                List<String> legacyLore = meta.getLore();
                if (legacyLore != null && !legacyLore.isEmpty()) {
                    String hiddenId = stripAllColor(legacyLore.get(legacyLore.size() - 1));
                    if (hiddenId.startsWith("id:")) {
                        String trigger = hiddenId.substring(3);
                        EmojiManager.EmojiData data = emojiManager.getEmojiRegistry().get(trigger);

                        if (data != null) {
                            p.closeInventory();
                            p.chat(trigger);
                            p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_YES, 1f, 1f);
                        }
                    }
                }
            }
        }
    }

    private String stripAllColor(String s) {
        if (s == null)
            return "";
        return org.bukkit.ChatColor.stripColor(
                org.bukkit.ChatColor.translateAlternateColorCodes('&', s));
    }
}
