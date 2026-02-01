package id.naturalsmp.naturalFun.trader;

import id.naturalsmp.naturalFun.NaturalFun;
import id.naturalsmp.naturalFun.utils.ColorUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Merchant;
import org.bukkit.inventory.Merchant;
import org.bukkit.inventory.MerchantRecipe;
import org.bukkit.Registry;
import org.bukkit.NamespacedKey;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class TraderManager {

    private final NaturalFun plugin;
    private File file;
    private FileConfiguration config;
    private final Map<String, TraderData> traders = new HashMap<>();

    public TraderManager(NaturalFun plugin) {
        this.plugin = plugin;
        load();
    }

    public void load() {
        file = new File(plugin.getDataFolder(), "traders.yml");
        if (!file.exists()) {
            plugin.saveResource("traders.yml", false);
        }
        config = YamlConfiguration.loadConfiguration(file);

        traders.clear();
        ConfigurationSection section = config.getConfigurationSection("traders");
        if (section == null)
            return;

        for (String id : section.getKeys(false)) {
            try {
                String name = section.getString(id + ".name", "Trader");
                ConfigurationSection locSec = section.getConfigurationSection(id + ".location");
                if (locSec == null)
                    continue;

                World world = Bukkit.getWorld(locSec.getString("world"));
                double x = locSec.getDouble("x");
                double y = locSec.getDouble("y");
                double z = locSec.getDouble("z");
                float yaw = (float) locSec.getDouble("yaw");
                float pitch = (float) locSec.getDouble("pitch");
                Location loc = new Location(world, x, y, z, yaw, pitch);

                String professionStr = section.getString(id + ".profession", "FARMER");
                Villager.Profession profession = Registry.VILLAGER_PROFESSION
                        .get(NamespacedKey.minecraft(professionStr.toLowerCase()));
                if (profession == null)
                    profession = Villager.Profession.FARMER;

                List<MerchantRecipe> recipes = new ArrayList<>();
                // Load trades logic could be complex, for now we will handle it simple or skip
                // Ideally we load trades here

                traders.put(id, new TraderData(id, name, loc, profession, recipes));

            } catch (Exception e) {
                plugin.getLogger().warning("Failed to load trader " + id);
                e.printStackTrace();
            }
        }
    }

    public void save() {
        // Save logic to file
        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void createTrader(String id, String displayName, Location loc, Villager.Profession profession) {
        config.set("traders." + id + ".name", displayName);
        config.set("traders." + id + ".profession", profession.getKey().getKey().toUpperCase());

        String path = "traders." + id + ".location";
        config.set(path + ".world", loc.getWorld().getName());
        config.set(path + ".x", loc.getX());
        config.set(path + ".y", loc.getY());
        config.set(path + ".z", loc.getZ());
        config.set(path + ".yaw", loc.getYaw());
        config.set(path + ".pitch", loc.getPitch());

        save();

        // Spawn entity
        spawnTraderEntity(id, displayName, loc, profession);

        load(); // Reload to memory
    }

    public void removeTrader(String id) {
        if (traders.containsKey(id)) {
            // Remove entity logic: Iterate near entities?
            // Since we didn't store UUID, we might need to find by name or location
            // For now, let's assume valid manual remove or use command logic to kill nearby

            // Just remove from config
            config.set("traders." + id, null);
            save();
            traders.remove(id);
        }
    }

    public void spawnTraderEntity(String id, String name, Location loc, Villager.Profession profession) {
        Villager v = (Villager) loc.getWorld().spawnEntity(loc, EntityType.VILLAGER);
        v.setProfession(profession);
        v.setCustomName(ColorUtils.colorize(name));
        v.setCustomNameVisible(true);
        v.setAI(false);
        v.setInvulnerable(true);
        v.setCollidable(false);
        // Add PersistentDataContainer to identify it as our trader?
        // For simplicity, we match by Location or Name in Listener
    }

    public boolean isTrader(Villager v) {
        // Simple check: exists in our map?
        for (TraderData data : traders.values()) {
            if (data.location.getWorld().equals(v.getWorld()) &&
                    data.location.distanceSquared(v.getLocation()) < 1.0) {
                return true;
            }
            // Or check name
            if (v.getCustomName() != null && v.getCustomName().equals(ColorUtils.colorize(data.name))) {
                return true;
            }
        }
        return false;
    }

    public void openTrade(Player p, Villager v) {
        // Find which trader
        TraderData found = null;
        for (TraderData data : traders.values()) {
            if (v.getCustomName() != null && v.getCustomName().equals(ColorUtils.colorize(data.name))) {
                found = data;
                break;
            }
        }

        if (found != null) {
            Merchant merchant = Bukkit.createMerchant(ColorUtils.miniMessage(found.name));
            // Add recipes
            // For demo: Add a dummy trade depending on config
            // In real impl, parse 'trades' section

            List<MerchantRecipe> recipes = new ArrayList<>();
            // Example: Diamond -> Dirt
            MerchantRecipe recipe = new MerchantRecipe(new ItemStack(Material.DIRT, 64), 999);
            recipe.addIngredient(new ItemStack(Material.DIAMOND, 1));
            recipes.add(recipe);

            merchant.setRecipes(recipes);
            p.openMerchant(merchant, true);
        }
    }

    private static class TraderData {
        String id;
        String name;
        Location location;
        Villager.Profession profession;
        List<MerchantRecipe> recipes; // Cached

        public TraderData(String id, String name, Location location, Villager.Profession profession,
                List<MerchantRecipe> recipes) {
            this.id = id;
            this.name = name;
            this.location = location;
            this.profession = profession;
            this.recipes = recipes;
        }
    }
}
