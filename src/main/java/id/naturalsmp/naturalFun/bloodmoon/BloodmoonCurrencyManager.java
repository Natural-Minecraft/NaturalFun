package id.naturalsmp.naturalFun.bloodmoon;

import id.naturalsmp.naturalFun.NaturalFun;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BloodmoonCurrencyManager {

    private final NaturalFun plugin;
    private File file;
    private FileConfiguration config;
    private final Map<UUID, Integer> balances = new HashMap<>();

    public BloodmoonCurrencyManager(NaturalFun plugin) {
        this.plugin = plugin;
        load();
    }

    public void load() {
        file = new File(plugin.getDataFolder(), "bloodmoon_currency.yml");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        config = YamlConfiguration.loadConfiguration(file);
        
        balances.clear();
        if (config.contains("balances")) {
            for (String uuidStr : config.getConfigurationSection("balances").getKeys(false)) {
                try {
                    balances.put(UUID.fromString(uuidStr), config.getInt("balances." + uuidStr));
                } catch (IllegalArgumentException ignored) {}
            }
        }
    }

    public void save() {
        for (Map.Entry<UUID, Integer> entry : balances.entrySet()) {
            config.set("balances." + entry.getKey().toString(), entry.getValue());
        }
        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int getBalance(UUID playerUUID) {
        return balances.getOrDefault(playerUUID, 0);
    }

    public void setBalance(UUID playerUUID, int amount) {
        balances.put(playerUUID, Math.max(0, amount));
        save();
    }

    public void addBalance(UUID playerUUID, int amount) {
        setBalance(playerUUID, getBalance(playerUUID) + amount);
    }

    public void takeBalance(UUID playerUUID, int amount) {
        setBalance(playerUUID, getBalance(playerUUID) - amount);
    }
    
    public boolean hasBalance(UUID playerUUID, int amount) {
        return getBalance(playerUUID) >= amount;
    }
}
