package id.naturalsmp.naturalFun.bloodmoon;

import id.naturalsmp.naturalFun.NaturalFun;
import id.naturalsmp.naturalFun.utils.ColorUtils;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.Random;

public class BloodmoonListener implements Listener {

    private final NaturalFun plugin;
    private final BloodmoonManager bloodmoonManager;
    private final LeaderboardManager leaderboardManager;
    private final Random random = new Random();

    public BloodmoonListener(NaturalFun plugin, BloodmoonManager bloodmoonManager, LeaderboardManager leaderboardManager) {
        this.plugin = plugin;
        this.bloodmoonManager = bloodmoonManager;
        this.leaderboardManager = leaderboardManager;
    }

    @EventHandler
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        if (!bloodmoonManager.isBloodmoonActive()) return;
        if (event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.CUSTOM) return;

        double chance = plugin.getConfig().getDouble("bloodmoon.shadow-assassin.chance", 0.01);
        if (random.nextDouble() < chance && event.getEntity() instanceof Monster) {
            spawnShadowAssassin(event.getLocation().getWorld(), event.getLocation());
            event.setCancelled(true); // Cancel original spawn
        }
    }
    
    private void spawnShadowAssassin(World world, org.bukkit.Location loc) {
        WitherSkeleton assassin = (WitherSkeleton) world.spawnEntity(loc, EntityType.WITHER_SKELETON);
        
        String name = plugin.getConfig().getString("bloodmoon.shadow-assassin.name", "Shadow Assassin");
        assassin.setCustomName(ColorUtils.colorize(name));
        assassin.setCustomNameVisible(true);
        
        // Stats
        double health = plugin.getConfig().getDouble("bloodmoon.shadow-assassin.health", 100.0);
        double damage = plugin.getConfig().getDouble("bloodmoon.shadow-assassin.damage", 20.0);
        
        if (assassin.getAttribute(Attribute.MAX_HEALTH) != null) {
            assassin.getAttribute(Attribute.MAX_HEALTH).setBaseValue(health);
            assassin.setHealth(health);
        }
        if (assassin.getAttribute(Attribute.ATTACK_DAMAGE) != null) {
            assassin.getAttribute(Attribute.ATTACK_DAMAGE).setBaseValue(damage);
        }
        
        // Equipment
        String matName = plugin.getConfig().getString("bloodmoon.shadow-assassin.equipment.main-hand", "NETHERITE_SWORD");
        Material mat = Material.getMaterial(matName);
        if (mat != null) {
            assassin.getEquipment().setItemInMainHand(new ItemStack(mat));
        }
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!bloodmoonManager.isBloodmoonActive()) return;

        if (event.getDamager() instanceof Monster && event.getEntity() instanceof Player) {
            double multiplier = plugin.getConfig().getDouble("bloodmoon.mob-damage-multiplier", 3.0);
            event.setDamage(event.getDamage() * multiplier);
        }
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        if (!bloodmoonManager.isBloodmoonActive()) return;
        
        if (event.getEntity() instanceof WitherSkeleton) {
            WitherSkeleton ws = (WitherSkeleton) event.getEntity();
            String name = plugin.getConfig().getString("bloodmoon.shadow-assassin.name", "Shadow Assassin");
            
            if (ws.getCustomName() != null && ws.getCustomName().equals(ColorUtils.colorize(name))) {
                
                double coinChance = plugin.getConfig().getDouble("bloodmoon.coins.chance", 1.0);
                if (random.nextDouble() <= coinChance) {
                    dropBloodmoonCoin(event);
                }
                
                if (ws.getKiller() != null) {
                     leaderboardManager.addKill(ws.getKiller());
                }
            }
        }
    }

    private void dropBloodmoonCoin(EntityDeathEvent event) {
        String matName = plugin.getConfig().getString("bloodmoon.coins.material", "GOLD_NUGGET");
        Material mat = Material.getMaterial(matName);
        if (mat == null) mat = Material.GOLD_NUGGET;
        
        ItemStack coin = new ItemStack(mat);
        ItemMeta meta = coin.getItemMeta();
        String coinName = plugin.getConfig().getString("bloodmoon.coins.name", "Bloodmoon Coin");
        meta.setDisplayName(ColorUtils.colorize(coinName));
        
        int cmd = plugin.getConfig().getInt("bloodmoon.coins.custom-model-data", 0);
        if (cmd > 0) {
            meta.setCustomModelData(cmd);
        }
        
        coin.setItemMeta(meta);
        
        event.getDrops().add(coin);
        if (event.getEntity().getKiller() != null) {
            String msg = bloodmoonManager.getMessages().getString("bloodmoon.coin-drop");
            event.getEntity().getKiller().sendMessage(ColorUtils.miniMessage(msg));
        }
    }

    @EventHandler
    public void onCommandPreprocess(PlayerCommandPreprocessEvent event) {
        if (!bloodmoonManager.isBloodmoonActive()) return;
        
        String msg = event.getMessage().toLowerCase();
        List<String> blocked = plugin.getConfig().getStringList("bloodmoon.blocked-commands");
        
        for (String cmd : blocked) {
            if (msg.startsWith(cmd.toLowerCase())) {
                if (!SafezoneManager.isInSafezone(event.getPlayer())) {
                     event.setCancelled(true);
                     String blockMsg = bloodmoonManager.getMessages().getString("bloodmoon.command-blocked");
                     event.getPlayer().sendMessage(ColorUtils.miniMessage(blockMsg));
                     return;
                }
            }
        }
    }
}
