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
    private boolean isBloodmoonActive = false;
    private FileConfiguration messagesConfig;

    public BloodmoonManager(NaturalFun plugin) {
        this.plugin = plugin;
        loadMessages();
        startScheduler();
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

    private void startScheduler() {
        new BukkitRunnable() {
            @Override
            public void run() {
                checkWorldTime();
            }
        }.runTaskTimer(plugin, 100L, 100L); // Check every 5 seconds
    }

    private void checkWorldTime() {
        World world = Bukkit.getWorlds().get(0); // Assuming main world
        long time = world.getTime();
        long fullTime = world.getFullTime();
        long days = fullTime / 24000;

        int interval = plugin.getConfig().getInt("bloodmoon.interval", 10);

        if (days > 0 && days % interval == 0) {
            // It is a bloodmoon day
            if (time >= 13000 && time <= 23000) {
                if (!isBloodmoonActive) {
                    startBloodmoon(world);
                }
            } else {
                if (isBloodmoonActive) {
                    stopBloodmoon(world);
                }
            }
        } else {
            if (isBloodmoonActive) {
                stopBloodmoon(world);
            }
        }
    }

    public void startBloodmoon(World world) {
        isBloodmoonActive = true;
        world.setTime(13000); // Set to start of night
        world.setStorm(false); // Maybe clear weather? Or make it stormy? User didn't specify, but clear night is better for visibility.
        
        String msg = messagesConfig.getString("bloodmoon.start", "<red>Bloodmoon Started!");
        Bukkit.broadcast(ColorUtils.miniMessage(msg));
    }

    public void stopBloodmoon(World world) {
        isBloodmoonActive = false;
        world.setTime(0); // Set to day
        
        String msg = messagesConfig.getString("bloodmoon.end", "<green>Bloodmoon Ended!");
        Bukkit.broadcast(ColorUtils.miniMessage(msg));
    }
    
    public FileConfiguration getMessages() {
        return messagesConfig;
    }
}
