package id.naturalsmp.naturalFun.colorgame;

import id.naturalsmp.naturalFun.NaturalFun;
import id.naturalsmp.naturalFun.utils.ChatUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitTask;

import java.time.Duration;
import java.util.*;
import java.util.function.Consumer;

public class ColorGameManager {

    public enum GameState { IDLE, COUNTDOWN, ACTIVE, SCORING }

    // ── Constants ─────────────────────────────────────────────────────────────────

    /** X positions for the 7 answer slots (-3 to 3). */
    public static final int[] SLOT_X = {-3, -2, -1, 0, 1, 2, 3};

    public static final int SETTER_Y  = 74;
    public static final int GUESSER_Y = 76;
    public static final int SLOT_Z    = 4;

    // Guesser zone: (-14, 75, 4) to (14, 85, 13)
    private static final int[] GZ_MIN = {-14, 75,  4};
    private static final int[] GZ_MAX = { 14, 85, 13};

    // Setter zone: (-4, 74, -7) to (4, 79, 1)
    private static final int[] SZ_MIN = {-4, 74, -7};
    private static final int[] SZ_MAX = { 4, 79,  1};

    /** The 7 colour blocks used in the game. */
    public static final Material[] COLORS = {
            Material.DIAMOND_BLOCK,
            Material.GOLD_BLOCK,
            Material.EMERALD_BLOCK,
            Material.REDSTONE_BLOCK,
            Material.NETHERITE_BLOCK,
            Material.IRON_BLOCK,
            Material.COPPER_BLOCK
    };

    // ── State ─────────────────────────────────────────────────────────────────────

    private final NaturalFun plugin;
    private final ColorGameLeaderboard leaderboard;

    private GameState state = GameState.IDLE;
    private final Set<UUID> setters      = new HashSet<>();
    private final Set<UUID> guessers     = new HashSet<>();
    private final Set<UUID> adminBypass  = new HashSet<>();  // bypass world protection
    private final Material[] setterAnswer = new Material[SLOT_X.length];
    private BukkitTask countdownTask;

    // ── Constructor ───────────────────────────────────────────────────────────────

    public ColorGameManager(NaturalFun plugin, ColorGameLeaderboard leaderboard) {
        this.plugin = plugin;
        this.leaderboard = leaderboard;
    }

    // ── World / Zone helpers ──────────────────────────────────────────────────────

    public String getWorldName() {
        return plugin.getConfig().getString("colorgame.world", "content");
    }

    public World getWorld() {
        return Bukkit.getWorld(getWorldName());
    }

    public boolean inWorld(Location l) {
        return l.getWorld() != null && l.getWorld().getName().equals(getWorldName());
    }

    public boolean inGuesserZone(Location l) {
        if (!inWorld(l)) return false;
        int x = l.getBlockX(), y = l.getBlockY(), z = l.getBlockZ();
        return x >= GZ_MIN[0] && x <= GZ_MAX[0]
            && y >= GZ_MIN[1] && y <= GZ_MAX[1]
            && z >= GZ_MIN[2] && z <= GZ_MAX[2];
    }

    public boolean inSetterZone(Location l) {
        if (!inWorld(l)) return false;
        int x = l.getBlockX(), y = l.getBlockY(), z = l.getBlockZ();
        return x >= SZ_MIN[0] && x <= SZ_MAX[0]
            && y >= SZ_MIN[1] && y <= SZ_MAX[1]
            && z >= SZ_MIN[2] && z <= SZ_MAX[2];
    }

    public boolean isGuesserSlot(Location l) {
        if (!inWorld(l)) return false;
        if (l.getBlockY() != GUESSER_Y || l.getBlockZ() != SLOT_Z) return false;
        for (int x : SLOT_X) if (l.getBlockX() == x) return true;
        return false;
    }

    public boolean isSetterSlot(Location l) {
        if (!inWorld(l)) return false;
        if (l.getBlockY() != SETTER_Y || l.getBlockZ() != SLOT_Z) return false;
        for (int x : SLOT_X) if (l.getBlockX() == x) return true;
        return false;
    }

    public boolean isColorBlock(Material m) {
        for (Material c : COLORS) if (c == m) return true;
        return false;
    }

    // ── Role assignment ───────────────────────────────────────────────────────────

    public void assignGuesser(Player p) {
        if (guessers.contains(p.getUniqueId())) return;
        setters.remove(p.getUniqueId());
        guessers.add(p.getUniqueId());
        if (state == GameState.ACTIVE || state == GameState.SCORING) {
            giveGuesserOrdered(p);
        } else {
            giveGuesserPreview(p);
        }
        p.showTitle(Title.title(
                ChatUtils.toComponent("<gradient:#00FFFF:#00AAFF><b>🎨 PENEBAK</b></gradient>"),
                ChatUtils.toComponent("<gray>Tebak susunan warna bloknya!"),
                Title.Times.times(Duration.ofMillis(300), Duration.ofMillis(2000), Duration.ofMillis(700))
        ));
        p.sendMessage(ChatUtils.toComponent(
                "<gradient:#00FFFF:#00AAFF><b>🎨 Kamu adalah Penebak!</b></gradient>"
                + " <gray>Susun blok warna yang benar di slot atas!"));
    }

    public void assignSetter(Player p) {
        if (setters.contains(p.getUniqueId())) return;
        guessers.remove(p.getUniqueId());
        setters.add(p.getUniqueId());
        if (state == GameState.ACTIVE || state == GameState.SCORING) {
            giveSetterOrdered(p);
        } else {
            giveSetterPreview(p);
        }
        p.showTitle(Title.title(
                ChatUtils.toComponent("<gradient:#FFFF00:#AAFF00><b>🔧 PENENTU</b></gradient>"),
                ChatUtils.toComponent("<gray>Susun blok jawabanmu di slot bawah!"),
                Title.Times.times(Duration.ofMillis(300), Duration.ofMillis(2000), Duration.ofMillis(700))
        ));
        p.sendMessage(ChatUtils.toComponent(
                "<gradient:#FFFF00:#AAFF00><b>🔧 Kamu adalah Penentu!</b></gradient>"
                + " <gray>Susun jawabanmu, lalu klik kertas angka untuk beri skor!"));
    }

    public void removeRole(Player p) {
        guessers.remove(p.getUniqueId());
        setters.remove(p.getUniqueId());
    }

    // ── Item helpers ──────────────────────────────────────────────────────────────

    /**
     * Preview: give all 7 colour blocks (addItem). Used when player first enters
     * the guesser zone (before game starts). Blocks cannot be placed yet.
     */
    public void giveGuesserPreview(Player p) {
        p.getInventory().clear();
        for (Material mat : COLORS) p.getInventory().addItem(colorBlock(mat));
    }

    /**
     * Ordered: place each colour block into hotbar slots 0-6 explicitly.
     * Called when game transitions to ACTIVE so the hotbar is clean 1→7.
     */
    public void giveGuesserOrdered(Player p) {
        p.getInventory().clear();
        for (int i = 0; i < COLORS.length; i++) {
            p.getInventory().setItem(i, colorBlock(COLORS[i]));
        }
    }

    /** @deprecated Use giveGuesserPreview or giveGuesserOrdered explicitly. */
    public void giveGuesserItems(Player p) { giveGuesserPreview(p); }

    /**
     * Preview: give all 7 colour blocks. Used when setter enters zone (IDLE).
     * They need these to fill their answer slots.
     */
    public void giveSetterPreview(Player p) {
        p.getInventory().clear();
        for (Material mat : COLORS) p.getInventory().addItem(colorBlock(mat));
    }

    /**
     * Ordered: place corrector papers (CMD 25561-25567) into hotbar slots 0-6.
     * Called when game transitions to ACTIVE — setter no longer needs colour blocks,
     * they now use papers to announce the score.
     */
    public void giveSetterOrdered(Player p) {
        p.getInventory().clear();
        for (int i = 1; i <= 7; i++) {
            ItemStack paper = new ItemStack(Material.PAPER);
            ItemMeta meta = paper.getItemMeta();
            if (meta != null) {
                meta.setCustomModelData(25560 + i); // 25561-25567
                meta.displayName(ChatUtils.toComponent(
                        "<yellow><b>✅ " + i + " Blok Benar</b></yellow>"));
                List<Component> lore = new ArrayList<>();
                lore.add(ChatUtils.toComponent(
                        "<gray>Klik kiri/kanan untuk umumkan skor <white>" + i + "</white>!"));
                meta.lore(lore);
                paper.setItemMeta(meta);
            }
            p.getInventory().setItem(i - 1, paper); // slot 0-6
        }
    }

    /** @deprecated Use giveSetterPreview or giveSetterOrdered explicitly. */
    public void giveSetterItems(Player p) { giveSetterPreview(p); }

    public static ItemStack colorBlock(Material mat) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(ChatUtils.toComponent("<white><b>" + colorName(mat) + "</b></white>"));
            item.setItemMeta(meta);
        }
        return item;
    }

    public static String colorName(Material mat) {
        return switch (mat) {
            case DIAMOND_BLOCK   -> "💎 Berlian";
            case GOLD_BLOCK      -> "🟡 Emas";
            case EMERALD_BLOCK   -> "💚 Zamrud";
            case REDSTONE_BLOCK  -> "❤ Redstone";
            case NETHERITE_BLOCK -> "⬛ Netherite";
            case IRON_BLOCK      -> "⚙ Besi";
            case COPPER_BLOCK    -> "🟫 Tembaga";
            default              -> mat.name();
        };
    }

    // ── Game flow ─────────────────────────────────────────────────────────────────

    /**
     * Called (via scheduler, 1 tick after block place) to check if all setter
     * slots are filled with colour blocks. If yes, start countdown.
     */
    public void checkSetterSlotsAndTrigger() {
        if (state != GameState.IDLE) return;
        World w = getWorld();
        if (w == null) return;

        for (int x : SLOT_X) {
            if (!isColorBlock(w.getBlockAt(x, SETTER_Y, SLOT_Z).getType())) return;
        }

        // Read answer
        for (int i = 0; i < SLOT_X.length; i++) {
            setterAnswer[i] = w.getBlockAt(SLOT_X[i], SETTER_Y, SLOT_Z).getType();
        }
        startCountdown();
    }

    private void startCountdown() {
        state = GameState.COUNTDOWN;
        int secs = plugin.getConfig().getInt("colorgame.countdown", 5);
        final int[] tick = {secs};

        broadcast(ChatUtils.toComponent(
                "<gradient:#FFFF00:#AAFF00><b>⏳ Game dimulai dalam " + secs + " detik!</b></gradient>"));

        countdownTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (tick[0] <= 0) {
                countdownTask.cancel();
                countdownTask = null;
                startGame();
                return;
            }
            int t = tick[0];
            eachInWorld(p -> {
                p.showTitle(Title.title(
                        ChatUtils.toComponent("<gradient:#FFFF00:#AAFF00><b>" + t + "</b></gradient>"),
                        ChatUtils.toComponent("<gray>Siapkan tebakanmu!"),
                        Title.Times.times(Duration.ZERO, Duration.ofMillis(900), Duration.ofMillis(100))
                ));
                float pitch = 0.8f + (0.15f * (secs - t));
                p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, pitch);
            });
            tick[0]--;
        }, 0L, 20L);
    }

    private void startGame() {
        state = GameState.ACTIVE;

        // Clear guesser slots
        World w = getWorld();
        if (w != null) {
            for (int x : SLOT_X) w.getBlockAt(x, GUESSER_Y, SLOT_Z).setType(Material.AIR);
        }

        // Give guessers their ordered hotbar (slot 0→6 = block 1→7)
        for (UUID uid : guessers) {
            Player p = Bukkit.getPlayer(uid);
            if (p != null) giveGuesserOrdered(p);
        }

        // Give setters corrector papers now that answer slots are locked
        for (UUID uid : setters) {
            Player p = Bukkit.getPlayer(uid);
            if (p != null) giveSetterOrdered(p);
        }

        eachInWorld(p -> {
            p.showTitle(Title.title(
                    ChatUtils.toComponent("<gradient:#00FFFF:#00FF80><b>🎨 MULAI!</b></gradient>"),
                    ChatUtils.toComponent("<gray>Susun blok warnamu di slot atas!"),
                    Title.Times.times(Duration.ofMillis(200), Duration.ofMillis(2500), Duration.ofMillis(700))
            ));
            p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1.5f);
        });
        broadcast(ChatUtils.toComponent(
                "<gradient:#00FFFF:#00FF80><b>🎨 GAME DIMULAI!</b></gradient>"
                + " <gray>Penebak, susun blok warnamu sekarang!"));
    }

    /**
     * Called when a setter clicks a corrector paper (CMD 25561-25567).
     * Returns the announced score, or -1 if invalid context.
     */
    public int announcePaperScore(Player setter, int cmd) {
        if (!setters.contains(setter.getUniqueId())) return -1;
        if (state != GameState.ACTIVE && state != GameState.SCORING) return -1;
        state = GameState.SCORING;

        int score = cmd - 25560; // 25561 → 1

        // Build title based on score (max is 7)
        String titleStr;
        if (score >= 7) {
            titleStr = "<gradient:#00FF00:#FFFF00><b>🎉 PERFECT! 7/7 BENAR!</b></gradient>";
        } else if (score >= 4) {
            titleStr = "<gradient:#FFFF00:#FFA500><b>✨ " + score + "/7 Benar!</b></gradient>";
        } else {
            titleStr = "<gradient:#FF5500:#FF0000><b>❌ Hanya " + score + "/7 Benar</b></gradient>";
        }

        eachInWorld(p -> {
            p.showTitle(Title.title(
                    ChatUtils.toComponent(titleStr),
                    ChatUtils.toComponent("<gray>Diumumkan oleh <white>" + setter.getName()),
                    Title.Times.times(Duration.ofMillis(200), Duration.ofMillis(4000), Duration.ofMillis(1000))
            ));
            Sound snd = score >= 7 ? Sound.UI_TOAST_CHALLENGE_COMPLETE
                    : score >= 4  ? Sound.ENTITY_EXPERIENCE_ORB_PICKUP
                    : Sound.ENTITY_VILLAGER_NO;
            p.playSound(p.getLocation(), snd, 1f, 1f);
        });

        broadcast(ChatUtils.toComponent(
                "<gradient:#FFFF00:#FFD700><b>📊 HASIL:</b></gradient>"
                + " <white>" + score + "/7 blok benar!"
                + " <gray>(diumumkan oleh " + setter.getName() + ")"));

        // Answer reveal
        StringBuilder reveal = new StringBuilder("<gray>Jawaban penentu: ");
        for (Material m : setterAnswer) {
            if (m != null) reveal.append(colorName(m)).append(" ");
        }
        broadcast(ChatUtils.toComponent(reveal.toString().trim()));

        // Leaderboard update (perfect = all 7 correct)
        if (score >= 7) {
            spawnFireworks();
            for (UUID uid : guessers) {
                Player gp = Bukkit.getPlayer(uid);
                if (gp != null) leaderboard.recordWin(gp);
            }
        } else {
            for (UUID uid : guessers) {
                Player gp = Bukkit.getPlayer(uid);
                if (gp != null) leaderboard.recordGame(gp, score);
            }
        }

        // Auto-reset after 6 seconds
        Bukkit.getScheduler().runTaskLater(plugin, this::resetArena, 120L);
        return score;
    }

    public void resetArena() {
        if (countdownTask != null) {
            countdownTask.cancel();
            countdownTask = null;
        }
        state = GameState.IDLE;
        Arrays.fill(setterAnswer, null);

        World w = getWorld();
        if (w != null) {
            for (int x : SLOT_X) {
                w.getBlockAt(x, SETTER_Y,  SLOT_Z).setType(Material.AIR);
                w.getBlockAt(x, GUESSER_Y, SLOT_Z).setType(Material.AIR);
            }
        }

        // Re-give items to active players
        for (UUID uid : setters) {
            Player p = Bukkit.getPlayer(uid);
            if (p != null) giveSetterPreview(p);
        }
        for (UUID uid : guessers) {
            Player p = Bukkit.getPlayer(uid);
            if (p != null) giveGuesserPreview(p);
        }

        broadcast(ChatUtils.toComponent(
                "<gray>🔄 Arena direset. Penentu, isi kembali blok jawabanmu!"));
    }

    // ── Effects ───────────────────────────────────────────────────────────────────

    private void spawnFireworks() {
        World w = getWorld();
        if (w == null) return;
        Location center = new Location(w, 0, 80, 8.5);
        Color[] palette = {Color.YELLOW, Color.AQUA, Color.LIME, Color.FUCHSIA, Color.ORANGE};
        for (int i = 0; i < 5; i++) {
            final Color c1 = palette[i % palette.length];
            final Color c2 = palette[(i + 2) % palette.length];
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                Firework fw = center.getWorld().spawn(center, Firework.class);
                FireworkMeta fm = fw.getFireworkMeta();
                fm.addEffect(FireworkEffect.builder()
                        .withColor(c1, c2)
                        .withFade(Color.WHITE)
                        .with(FireworkEffect.Type.BALL_LARGE)
                        .trail(true)
                        .build());
                fm.setPower(1);
                fw.setFireworkMeta(fm);
            }, i * 15L);
        }
    }

    // ── Broadcast / forEach helpers ───────────────────────────────────────────────

    private void broadcast(Component msg) {
        World w = getWorld();
        if (w != null) w.getPlayers().forEach(p -> p.sendMessage(msg));
    }

    private void eachInWorld(Consumer<Player> action) {
        World w = getWorld();
        if (w != null) w.getPlayers().forEach(action);
    }

    // ── Getters ───────────────────────────────────────────────────────────────────

    public GameState getState()                  { return state; }
    public Set<UUID> getSetters()                { return setters; }
    public Set<UUID> getGuessers()               { return guessers; }
    public ColorGameLeaderboard getLeaderboard() { return leaderboard; }
    public boolean isSetter(UUID uid)            { return setters.contains(uid); }
    public boolean isGuesser(UUID uid)           { return guessers.contains(uid); }
    public boolean isAdminBypass(UUID uid)       { return adminBypass.contains(uid); }

    /** Toggles admin bypass for world modification. Returns new state (true = enabled). */
    public boolean toggleAdminBypass(UUID uid) {
        if (adminBypass.contains(uid)) {
            adminBypass.remove(uid);
            return false;
        }
        adminBypass.add(uid);
        return true;
    }
}
