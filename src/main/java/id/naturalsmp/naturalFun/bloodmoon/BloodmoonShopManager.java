package id.naturalsmp.naturalFun.bloodmoon;

import id.naturalsmp.naturalFun.NaturalFun;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class BloodmoonShopManager {

    private final NaturalFun plugin;
    private File file;
    private FileConfiguration config;
    private final List<ShopItem> shopItems = new ArrayList<>();

    public BloodmoonShopManager(NaturalFun plugin) {
        this.plugin = plugin;
        load();
    }

    public void load() {
        file = new File(plugin.getDataFolder(), "bloodmoon_shop.yml");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        config = YamlConfiguration.loadConfiguration(file);
        
        shopItems.clear();
        if (config.contains("items")) {
            ConfigurationSection itemsSection = config.getConfigurationSection("items");
            if (itemsSection != null) {
                for (String key : itemsSection.getKeys(false)) {
                    ItemStack itemStack = config.getItemStack("items." + key + ".item");
                    int price = config.getInt("items." + key + ".price");
                    int stock = config.getInt("items." + key + ".stock");
                    String rarity = config.getString("items." + key + ".rarity", "Common");
                    shopItems.add(new ShopItem(key, itemStack, price, stock, rarity));
                }
            }
        }
    }

    public void save() {
        config.set("items", null); // Clear old
        for (ShopItem item : shopItems) {
            String path = "items." + item.getId();
            config.set(path + ".item", item.getItemStack());
            config.set(path + ".price", item.getPrice());
            config.set(path + ".stock", item.getStock());
            config.set(path + ".rarity", item.getRarity());
        }
        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<ShopItem> getShopItems() {
        return shopItems;
    }

    public void addShopItem(ItemStack item, int price, int stock, String rarity) {
        String id = java.util.UUID.randomUUID().toString();
        shopItems.add(new ShopItem(id, item, price, stock, rarity));
        save();
    }
    
    public void removeShopItem(String id) {
        shopItems.removeIf(item -> item.getId().equals(id));
        save();
    }
    
    public ShopItem getItemById(String id) {
        for (ShopItem item : shopItems) {
            if (item.getId().equals(id)) return item;
        }
        return null;
    }

    public static class ShopItem {
        private final String id;
        private final ItemStack itemStack;
        private int price;
        private int stock;
        private String rarity;

        public ShopItem(String id, ItemStack itemStack, int price, int stock, String rarity) {
            this.id = id;
            this.itemStack = itemStack;
            this.price = price;
            this.stock = stock;
            this.rarity = rarity;
        }

        public String getId() { return id; }
        public ItemStack getItemStack() { return itemStack; }
        public int getPrice() { return price; }
        public void setPrice(int price) { this.price = price; }
        public int getStock() { return stock; }
        public void setStock(int stock) { this.stock = stock; }
        public void decreaseStock(int amount) { this.stock -= amount; }
        public String getRarity() { return rarity; }
        public void setRarity(String rarity) { this.rarity = rarity; }
    }
}
