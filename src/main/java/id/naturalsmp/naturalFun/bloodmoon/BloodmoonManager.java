package id.naturalsmp.naturalFun.bloodmoon;

import id.naturalsmp.naturalFun.NaturalFun;
import id.naturalsmp.naturalFun.utils.ChatUtils;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;

public class BloodmoonManager {

    private final NaturalFun plugin;
    private BukkitTask bloodmoonTask;
    private BukkitTask autoCheckTask;
    private long remainingTime = 0;
    private World activeWorld = null;

    private FileConfiguration messagesConfig;
    private boolean isBloodmoonActive = false;
    private BossBar bossBar;

    public BloodmoonManager(NaturalFun plugin) {
        this.plugin = plugin;
        loadMessages();
        scheduleAutoStart();
    }

    public void reload() {
        loadMessages();
    }

    private void scheduleAutoStart() {
        if (autoCheckTask != null) autoCheckTask.cancel();
        autoCheckTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (isBloodmoonActive) return;

                int chance = plugin.getConfig().getInt("bloodmoon.chance-percent", 5);

                for (World world : Bukkit.getWorlds()) {
                    if (world.getEnvironment() != World.Environment.NORMAL) continue;
                    long time = world.getTime();
                    // Check at dusk (13000-13400)
                    if (time >= 13000 && time < 13400) {
                        if (Math.random() * 100 < chance) {
                            startBloodmoon(world);
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 400L, 400L);
    }

    private void loadMessages() {
        File file = new File(plugin.getDataFolder(), "messages.yml");
        if (!file.exists()) {
            plugin.saveResource("messages.yml", false);
        }
        messagesConfig = YamlConfiguration.loadConfiguration(file);
    }

    public boolean isBloodmoonActive() { return isBloodmoonActive; }
    public long getRemainingTime() { return remainingTime; }
    public World getActiveWorld() { return activeWorld; }

    /** Called by PlayerJoinEvent to sync BossBar for late joiners */
    public void addPlayerToBossBar(Player player) {
        if (!isBloodmoonActive || bossBar == null) return;
        if (!bossBar.getPlayers().contains(player)) {
            bossBar.addPlayer(player);
        }

        // Send explanation message to late joiner
        String joinMsg = messagesConfig.getString("bloodmoon.join-during",
                "<red><bold>☠ BLOODMOON SEDANG AKTIF!</bold></red> <gray>Waspada! Monster jauh lebih kuat malam ini. Sisa waktu: <red>%time%</red>. Bunuh Shadow Assassin untuk koin!");
        player.sendMessage(ChatUtils.toComponent(joinMsg.replace("%time%", getFormattedTime())));
    }

    public void startBloodmoon(World world) {
        if (isBloodmoonActive) return;

        isBloodmoonActive = true;
        activeWorld = world;
        world.setTime(14000);
        world.setGameRule(org.bukkit.GameRule.DO_DAYLIGHT_CYCLE, false);
        world.setStorm(false);

        String msg = messagesConfig.getString("bloodmoon.start", "<red><bold>☠ BLOODMOON DIMULAI!</bold></red>");
        Bukkit.broadcast(ChatUtils.toComponent(msg));

        // Send lore/explanation to all players
        String desc = messagesConfig.getString("bloodmoon.description",
                "<gray>Malam Berdarah telah tiba! Monster menjadi jauh lebih kuat dan berbahaya. Shadow Assassin akan muncul - bunuh mereka untuk mendapatkan <red>Bloodmoon Coin</red><gray>! Gunakan koin di /bmshop.");
        Bukkit.broadcast(ChatUtils.toComponent(desc));

        remainingTime = plugin.getConfig().getInt("bloodmoon.duration-seconds", 900);
        final int totalDuration = (int) remainingTime;

        // Create BossBar
        String bossBarTitle = messagesConfig.getString("bloodmoon.bossbar-title",
                "<red><bold>☠ BLOODMOON</bold></red> <white>%time%</white>");
        bossBar = Bukkit.createBossBar(
                ChatUtils.serialize(ChatUtils.toComponent(bossBarTitle.replace("%time%", getFormattedTime()))),
                BarColor.RED,
                BarStyle.SOLID);
        bossBar.setVisible(true);
        Bukkit.getOnlinePlayers().forEach(bossBar::addPlayer);

        if (bloodmoonTask != null) bloodmoonTask.cancel();
        bloodmoonTask = new BukkitRunnable() {
            @Override
            public void run() {
                remainingTime--;
                world.setTime(14000);

                if (bossBar != null) {
                    String title = bossBarTitle.replace("%time%", getFormattedTime());
                    bossBar.setTitle(ChatUtils.serialize(ChatUtils.toComponent(title)));
                    bossBar.setProgress(Math.max(0.0, Math.min(1.0, (double) remainingTime / totalDuration)));

                    // Always sync all online players (handles late joiners)
                    Bukkit.getOnlinePlayers().forEach(p -> {
                        if (!bossBar.getPlayers().contains(p)) {
                            bossBar.addPlayer(p);
                        }
                    });
                }

                if (remainingTime <= 0) {
                    stopBloodmoon(world);
                }
            }
        }.runTaskTimer(plugin, 20L, 20L);
    }

    public void stopBloodmoon(World world) {
        if (!isBloodmoonActive) return;

        isBloodmoonActive = false;
        activeWorld = null;

        if (bloodmoonTask != null) {
            bloodmoonTask.cancel();
            bloodmoonTask = null;
        }

        if (bossBar != null) {
            bossBar.removeAll();
            bossBar = null;
        }

        world.setTime(0);
        world.setGameRule(org.bukkit.GameRule.DO_DAYLIGHT_CYCLE, true);

        String msg = messagesConfig.getString("bloodmoon.end", "<green><bold>Bloodmoon telah berakhir. Selamat!</bold></green>");
        Bukkit.broadcast(ChatUtils.toComponent(msg));
    }

    public String getFormattedTime() {
        long min = remainingTime / 60;
        long sec = remainingTime % 60;
        return String.format("%02d:%02d", min, sec);
    }

    public FileConfiguration getMessages() { return messagesConfig; }
}
