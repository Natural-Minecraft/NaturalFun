package id.naturalsmp.naturalFun.colorgame;

import id.naturalsmp.naturalFun.NaturalFun;
import id.naturalsmp.naturalFun.utils.ChatUtils;
import org.bukkit.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ColorGameListener implements Listener {

    private final NaturalFun plugin;
    private final ColorGameManager manager;

    public ColorGameListener(NaturalFun plugin, ColorGameManager manager) {
        this.plugin  = plugin;
        this.manager = manager;
    }

    // ── Zone Detection ────────────────────────────────────────────────────────────

    @EventHandler(priority = EventPriority.MONITOR)
    public void onMove(PlayerMoveEvent e) {
        // Only fire when the player actually changes block coordinates
        if (e.getFrom().getBlockX() == e.getTo().getBlockX()
         && e.getFrom().getBlockY() == e.getTo().getBlockY()
         && e.getFrom().getBlockZ() == e.getTo().getBlockZ()) return;

        evaluateZone(e.getPlayer(), e.getTo());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onTeleport(PlayerTeleportEvent e) {
        evaluateZone(e.getPlayer(), e.getTo());
    }

    private void evaluateZone(org.bukkit.entity.Player p, Location to) {
        if (to == null) return;
        boolean inSetter  = manager.inSetterZone(to);
        boolean inGuesser = manager.inGuesserZone(to);
        boolean wasSetter  = manager.isSetter(p.getUniqueId());
        boolean wasGuesser = manager.isGuesser(p.getUniqueId());

        if (inSetter && !wasSetter) {
            manager.assignSetter(p);
        } else if (inGuesser && !wasGuesser) {
            manager.assignGuesser(p);
        } else if (!inSetter && !inGuesser && (wasSetter || wasGuesser)) {
            manager.removeRole(p);
            p.sendMessage(ChatUtils.toComponent("<gray>Kamu keluar dari arena Tebak Warna."));
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        manager.removeRole(e.getPlayer());
    }

    // ── Block Protection ──────────────────────────────────────────────────────────

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBreak(BlockBreakEvent e) {
        org.bukkit.entity.Player p = e.getPlayer();
        Location loc = e.getBlock().getLocation();

        if (!manager.inWorld(loc)) return; // only manage content world

        boolean isSetter  = manager.isSetter(p.getUniqueId());
        boolean isGuesser = manager.isGuesser(p.getUniqueId());

        // Setter can break their own slots only while IDLE (to rearrange)
        if (isSetter && manager.isSetterSlot(loc)
                && manager.getState() == ColorGameManager.GameState.IDLE) {
            return; // allow
        }

        // Guesser can break their own slots only while ACTIVE (to rearrange guess)
        if (isGuesser && manager.isGuesserSlot(loc)
                && manager.getState() == ColorGameManager.GameState.ACTIVE) {
            return; // allow
        }

        e.setCancelled(true);
        p.sendMessage(ChatUtils.toComponent("<red>✖ Kamu tidak bisa menghancurkan blok di sini!"));
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlace(BlockPlaceEvent e) {
        org.bukkit.entity.Player p = e.getPlayer();
        Location loc = e.getBlock().getLocation();

        if (!manager.inWorld(loc)) return;

        boolean isSetter  = manager.isSetter(p.getUniqueId());
        boolean isGuesser = manager.isGuesser(p.getUniqueId());
        Material placed   = e.getBlockPlaced().getType();

        // ── Setter places in setter slots during IDLE ──────────────────────────
        if (isSetter && manager.isSetterSlot(loc)) {
            if (manager.getState() != ColorGameManager.GameState.IDLE) {
                e.setCancelled(true);
                p.sendMessage(ChatUtils.toComponent("<red>Tidak bisa mengubah jawaban sekarang!"));
                return;
            }
            if (!manager.isColorBlock(placed)) {
                e.setCancelled(true);
                p.sendMessage(ChatUtils.toComponent("<red>Hanya blok warna yang bisa ditaruh di sini!"));
                return;
            }
            // Allow; schedule slot check 1 tick later (block is placed)
            Bukkit.getScheduler().runTaskLater(plugin, manager::checkSetterSlotsAndTrigger, 1L);
            return;
        }

        // ── Guesser places in guesser slots during ACTIVE ─────────────────────
        if (isGuesser && manager.isGuesserSlot(loc)
                && manager.getState() == ColorGameManager.GameState.ACTIVE) {
            if (!manager.isColorBlock(placed)) {
                e.setCancelled(true);
                p.sendMessage(ChatUtils.toComponent("<red>Hanya blok warna yang bisa ditaruh di sini!"));
            }
            return; // allow colour blocks
        }

        // Anything else in the content world → cancel
        e.setCancelled(true);
        if (isSetter || isGuesser) {
            p.sendMessage(ChatUtils.toComponent("<red>✖ Letakkan blok di slot yang tersedia!"));
        }
    }

    // ── Paper Corrector Click ─────────────────────────────────────────────────────

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        // Fire only for main hand, and only left/right click
        if (e.getHand() != EquipmentSlot.HAND) return;

        org.bukkit.entity.Player p = e.getPlayer();
        if (!manager.isSetter(p.getUniqueId())) return;

        ItemStack item = p.getInventory().getItemInMainHand();
        if (item.getType() != Material.PAPER) return;

        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasCustomModelData()) return;

        int cmd = meta.getCustomModelData();
        if (cmd < 2551 || cmd > 2557) return;

        e.setCancelled(true);

        int score = manager.announcePaperScore(p, cmd);
        if (score < 0) {
            p.sendMessage(ChatUtils.toComponent("<red>Game belum aktif atau sudah selesai!"));
        }
    }
}
