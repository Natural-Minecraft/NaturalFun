package id.naturalsmp.naturalFun.colorgame;

import id.naturalsmp.naturalFun.NaturalFun;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class ColorGameLeaderboard {

    public record Entry(String name, int wins, int games, int totalCorrect) {}

    private final NaturalFun plugin;
    private final File file;
    private FileConfiguration data;

    public ColorGameLeaderboard(NaturalFun plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "colorgame_leaderboard.yml");
        reload();
    }

    public void reload() {
        data = YamlConfiguration.loadConfiguration(file);
    }

    public void recordWin(Player player) {
        String key = player.getUniqueId().toString();
        data.set(key + ".name", player.getName());
        data.set(key + ".wins", data.getInt(key + ".wins", 0) + 1);
        data.set(key + ".games", data.getInt(key + ".games", 0) + 1);
        data.set(key + ".totalCorrect", data.getInt(key + ".totalCorrect", 0) + 5);
        save();
    }

    public void recordGame(Player player, int correct) {
        String key = player.getUniqueId().toString();
        data.set(key + ".name", player.getName());
        data.set(key + ".games", data.getInt(key + ".games", 0) + 1);
        data.set(key + ".totalCorrect", data.getInt(key + ".totalCorrect", 0) + correct);
        save();
    }

    public List<Entry> getTop(int n) {
        List<Entry> entries = new ArrayList<>();
        for (String key : data.getKeys(false)) {
            String name = data.getString(key + ".name", "Unknown");
            int wins = data.getInt(key + ".wins", 0);
            int games = data.getInt(key + ".games", 0);
            int total = data.getInt(key + ".totalCorrect", 0);
            entries.add(new Entry(name, wins, games, total));
        }
        return entries.stream()
                .sorted(Comparator.comparingInt(Entry::wins).reversed()
                        .thenComparingInt(Entry::totalCorrect).reversed())
                .limit(n)
                .collect(Collectors.toList());
    }

    private void save() {
        try {
            data.save(file);
        } catch (IOException e) {
            plugin.getLogger().warning("[ColorGame] Gagal menyimpan leaderboard: " + e.getMessage());
        }
    }
}
