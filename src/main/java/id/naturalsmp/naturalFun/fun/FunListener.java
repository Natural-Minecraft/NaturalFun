package id.naturalsmp.naturalFun.fun;

import id.naturalsmp.naturalFun.NaturalFun;
import id.naturalsmp.naturalFun.utils.ColorUtils;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

public class FunListener implements Listener {

    private final NaturalFun plugin;

    public FunListener(NaturalFun plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        if (event.getEntityType() == EntityType.WARDEN) {
            Player killer = event.getEntity().getKiller();
            if (killer != null) {
                // Give XP
                int xp = plugin.getConfig().getInt("fun.warden-xp", 500);
                killer.giveExp(xp);
                
                // Log/Broadcast
                String msg = plugin.getMessagesConfig().getString("fun.warden-kill");
                if (msg != null) {
                     plugin.getServer().getConsoleSender().sendMessage("Player " + killer.getName() + " killed Warden at " + event.getEntity().getLocation());
                     // Optional broadcast if configured
                     // Bukkit.broadcast(ColorUtils.miniMessage(msg.replace("%player%", killer.getName())));
                }
            }
        }
    }
}
