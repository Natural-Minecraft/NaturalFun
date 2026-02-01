package id.naturalsmp.naturalFun.bloodmoon;

import id.naturalsmp.naturalFun.NaturalFun;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class LeaderboardManager {

    private final NaturalFun plugin;
    private File file;
    private FileConfiguration config;
    private final Map<String, Integer> kills = new HashMap<>();

    public LeaderboardManager(NaturalFun plugin) {
        this.plugin = plugin;
        load();
    }

    public void load() {
        file = new File(plugin.getDataFolder(), "bloodmoon_leaderboard.yml");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        config = YamlConfiguration.loadConfiguration(file);

        kills.clear();
        if (config.contains("kills")) {
            for (String uuid : config.getConfigurationSection("kills").getKeys(false)) {
                kills.put(uuid, config.getInt("kills." + uuid));
            }
        }
    }

    public void save() {
        for (Map.Entry<String, Integer> entry : kills.entrySet()) {
            config.set("kills." + entry.getKey(), entry.getValue());
        }
        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addKill(Player player) {
        String uuid = player.getUniqueId().toString();
        kills.put(uuid, kills.getOrDefault(uuid, 0) + 1);
        save();
    }

    public List<Map.Entry<String, Integer>> getTopKills(int limit) {
        return kills.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }
}
