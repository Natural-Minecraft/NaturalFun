package id.naturalsmp.naturalFun.bloodmoon;

import id.naturalsmp.naturalFun.NaturalFun;
import id.naturalsmp.naturalFun.utils.ChatUtils;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;

public class BloodmoonManager {

    private final NaturalFun plugin;
    private BukkitRunnable bloodmoonTask;
    private long remainingTime = 0;
    private final int BLOODMOON_DURATION = 15 * 60; // 15 minutes in seconds

    private FileConfiguration messagesConfig;
    private boolean isBloodmoonActive = false;
    private org.bukkit.boss.BossBar bossBar;

    public BloodmoonManager(NaturalFun plugin) {
        this.plugin = plugin;
        loadMessages();
        scheduleAutoStart();
    }

    private void scheduleAutoStart() {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (isBloodmoonActive)
                    return;

                // Check every 20 seconds
                for (World world : Bukkit.getWorlds()) {
                    if (world.getEnvironment() != World.Environment.NORMAL)
                        continue;

                    long time = world.getTime();
                    // Check at dusk (13000 - 13400)
                    if (time >= 13000 && time < 13400) {
                        // 5% Chance per night
                        if (Math.random() < 0.05) {
                            startBloodmoon(world);
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 400L, 400L); // Check every 20 seconds
    }

    private void loadMessages() {
        File file = new File(plugin.getDataFolder(), "messages.yml");
        if (!file.exists()) {
            plugin.saveResource("messages.yml", false);
        }
        messagesConfig = YamlConfiguration.loadConfiguration(file);
    }

    public boolean isBloodmoonActive() {
        return isBloodmoonActive;
    }

    public long getRemainingTime() {
        return remainingTime;
    }

    public void startBloodmoon(World world) {
        if (isBloodmoonActive)
            return;

        isBloodmoonActive = true;
        world.setTime(14000);
        world.setGameRule(org.bukkit.GameRule.DO_DAYLIGHT_CYCLE, false);
        world.setStorm(false);

        String msg = messagesConfig.getString("bloodmoon.start", "<red>Bloodmoon Started!");
        Bukkit.broadcast(ChatUtils.toComponent(msg));

        remainingTime = BLOODMOON_DURATION;

        // Create BossBar
        String bossBarTitle = messagesConfig.getString("bloodmoon.bossbar-title",
                "<red><b>BLOODMOON</b></red> - <white>%time%</white>");
        bossBar = Bukkit.createBossBar(
                ChatUtils.serialize(ChatUtils.toComponent(bossBarTitle.replace("%time%", getFormattedTime()))),
                org.bukkit.boss.BarColor.RED,
                org.bukkit.boss.BarStyle.SOLID);
        bossBar.setVisible(true);
        Bukkit.getOnlinePlayers().forEach(bossBar::addPlayer);

        bloodmoonTask = new BukkitRunnable() {
            @Override
            public void run() {
                remainingTime--;
                world.setTime(14000);

                // Update BossBar
                if (bossBar != null) {
                    bossBar.setTitle(ChatUtils
                            .serialize(ChatUtils.toComponent(bossBarTitle.replace("%time%", getFormattedTime()))));
                    bossBar.setProgress(Math.max(0, Math.min(1, (double) remainingTime / BLOODMOON_DURATION)));

                    // Ensure all players see the bossbar
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
        };
        bloodmoonTask.runTaskTimer(plugin, 20L, 20L);
    }

    public void stopBloodmoon(World world) {
        if (!isBloodmoonActive)
            return;

        isBloodmoonActive = false;
        if (bloodmoonTask != null && !bloodmoonTask.isCancelled()) {
            bloodmoonTask.cancel();
        }

        if (bossBar != null) {
            bossBar.removeAll();
            bossBar = null;
        }

        world.setTime(0);
        world.setGameRule(org.bukkit.GameRule.DO_DAYLIGHT_CYCLE, true);

        String msg = messagesConfig.getString("bloodmoon.end", "<green>Bloodmoon Ended!");
        Bukkit.broadcast(ChatUtils.toComponent(msg));
    }

    public String getFormattedTime() {
        long min = remainingTime / 60;
        long sec = remainingTime % 60;
        return String.format("%02d:%02d", min, sec);
    }

    public FileConfiguration getMessages() {
        return messagesConfig;
    }
}
