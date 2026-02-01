package id.naturalsmp.naturalFun.bloodmoon;

import id.naturalsmp.naturalFun.NaturalFun;
import id.naturalsmp.naturalFun.utils.ColorUtils;
import net.kyori.adventure.text.Component;
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
    private final int BLOODMOON_DURATION = 20 * 60; // 20 minutes in seconds

    public BloodmoonManager(NaturalFun plugin) {
        this.plugin = plugin;
        loadMessages();
        // startScheduler(); // No longer polling world time constantly
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

        // Setup World
        world.setTime(14000); // Night
        world.setGameRule(org.bukkit.GameRule.DO_DAYLIGHT_CYCLE, false); // Freeze time
        world.setStorm(false);

        String msg = messagesConfig.getString("bloodmoon.start", "<red>Bloodmoon Started!");
        Bukkit.broadcast(ColorUtils.miniMessage(msg));

        remainingTime = BLOODMOON_DURATION;

        bloodmoonTask = new BukkitRunnable() {
            @Override
            public void run() {
                remainingTime--;

                // Keep time fixed at night just in case
                world.setTime(14000);

                if (remainingTime <= 0) {
                    stopBloodmoon(world);
                }
            }
        };
        bloodmoonTask.runTaskTimer(plugin, 20L, 20L); // Run every second
    }

    public void stopBloodmoon(World world) {
        if (!isBloodmoonActive)
            return;

        isBloodmoonActive = false;

        if (bloodmoonTask != null && !bloodmoonTask.isCancelled()) {
            bloodmoonTask.cancel();
        }

        // Restore World
        world.setTime(0); // Day
        world.setGameRule(org.bukkit.GameRule.DO_DAYLIGHT_CYCLE, true); // Defrost time

        String msg = messagesConfig.getString("bloodmoon.end", "<green>Bloodmoon Ended!");
        Bukkit.broadcast(ColorUtils.miniMessage(msg));
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
