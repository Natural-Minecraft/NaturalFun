package id.naturalsmp.naturalFun.trader;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;

public class TraderListener implements Listener {

    private final TraderManager manager;

    public TraderListener(TraderManager manager) {
        this.manager = manager;
    }

    @EventHandler
    public void onInteract(PlayerInteractEntityEvent event) {
        if (event.getRightClicked().getType() == EntityType.VILLAGER) {
            Villager v = (Villager) event.getRightClicked();
            if (manager.isTrader(v)) {
                event.setCancelled(true);
                manager.openTrade(event.getPlayer(), v);
            }
        }
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (event.getEntityType() == EntityType.VILLAGER) {
            if (manager.isTrader((Villager) event.getEntity())) {
                event.setCancelled(true);
            }
        }
    }
}
