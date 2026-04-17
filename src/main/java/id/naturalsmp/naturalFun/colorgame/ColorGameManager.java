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

    /** X positions for the 5 answer slots (centered). */
    public static final int[] SLOT_X = {-2, -1, 0, 1, 2};

    public static final int SETTER_Y  = 74;
    public static final int GUESSER_Y = 76;
    public static final int SLOT_Z    = 4;

    // Guesser zone: (-14, 75, 4) to (14, 85, 13)
    private static final int[] GZ_MIN = {-14, 75,  4};
    private static final int[] GZ_MAX = { 14, 85, 13};

    // Setter zone: (-4, 74, -7) to (4, 79, 1)
    private static final int[] SZ_MIN = {-4, 74, -7};
    private static final int[] SZ_MAX = { 4, 79,  1};

    /** The 5 colour blocks used in the game. */
    public static final Material[] COLORS = {
            Material.DIAMOND_BLOCK,
            Material.GOLD_BLOCK,
            Material.EMERALD_BLOCK,
            Material.REDSTONE_BLOCK,
            Material.NETHERITE_BLOCK
    };

    // ── State ─────────────────────────────────────────────────────────────────────

    private final NaturalFun plugin;
    private final ColorGameLeaderboard leaderboard;

    private GameState state = GameState.IDLE;
    private final Set<UUID> setters  = new HashSet<>();
    private final Set<UUID> guessers = new HashSet<>();
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
        giveGuesserItems(p);
        p.showTitle(Title.title(
                ChatUtils.toComponent("<gradient:#00AAFF:#AA00FF><b>🎨 PENEBAK</b></gradient>"),
                ChatUtils.toComponent("<gray>Tebak susunan warna bloknya!"),
                Title.Times.times(Duration.ofMillis(300), Duration.ofMillis(2000), Duration.ofMillis(700))
        ));
        p.sendMessage(ChatUtils.toComponent(
                "<gradient:#00AAFF:#AA00FF><b>🎨 Kamu adalah Penebak!</b></gradient>"
                + " <gray>Susun blok warna yang benar di slot atas!"));
    }

    public void assignSetter(Player p) {
        if (setters.contains(p.getUniqueId())) return;
        guessers.remove(p.getUniqueId());
        setters.add(p.getUniqueId());
        giveSetterItems(p);
        p.showTitle(Title.title(
                ChatUtils.toComponent("<gradient:#FF5500:#FF0000><b>🔧 PENENTU</b></gradient>"),
                ChatUtils.toComponent("<gray>Susun blok jawabanmu di slot bawah!"),
                Title.Times.times(Duration.ofMillis(300), Duration.ofMillis(2000), Duration.ofMillis(700))
        ));
        p.sendMessage(ChatUtils.toComponent(
                "<gradient:#FF5500:#FF0000><b>🔧 Kamu adalah Penentu!</b></gradient>"
                + " <gray>Susun jawabanmu, lalu klik kertas angka untuk beri skor!"));
    }

    public void removeRole(Player p) {
        guessers.remove(p.getUniqueId());
        setters.remove(p.getUniqueId());
    }

    // ── Item helpers ──────────────────────────────────────────────────────────────

    public void giveGuesserItems(Player p) {
        p.getInventory().clear();
        for (Material mat : COLORS) p.getInventory().addItem(colorBlock(mat));
    }

    public void giveSetterItems(Player p) {
        p.getInventory().clear();
        for (Material mat : COLORS) p.getInventory().addItem(colorBlock(mat));
        // Corrector papers — CMD 2551 (=1 correct) through 2557 (=7)
        for (int i = 1; i <= 7; i++) {
            ItemStack paper = new ItemStack(Material.PAPER);
            ItemMeta meta = paper.getItemMeta();
            if (meta != null) {
                meta.setCustomModelData(2550 + i);
                meta.displayName(ChatUtils.toComponent(
                        "<yellow><b>✅ " + i + " Blok Benar</b></yellow>"));
                List<Component> lore = new ArrayList<>();
                lore.add(ChatUtils.toComponent("<gray>Klik kiri/kanan untuk umumkan skor <white>" + i + "</white>!"));
                meta.lore(lore);
                paper.setItemMeta(meta);
            }
            p.getInventory().addItem(paper);
        }
    }

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
                "<gradient:#FF5500:#FFFF00><b>⏳ Game dimulai dalam " + secs + " detik!</b></gradient>"));

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
                        ChatUtils.toComponent("<gradient:#FF5500:#FFFF00><b>" + t + "</b></gradient>"),
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

        // Re-give guesser items
        for (UUID uid : guessers) {
            Player p = Bukkit.getPlayer(uid);
            if (p != null) giveGuesserItems(p);
        }

        eachInWorld(p -> {
            p.showTitle(Title.title(
                    ChatUtils.toComponent("<gradient:#00FF80:#00AAFF><b>🎨 MULAI!</b></gradient>"),
                    ChatUtils.toComponent("<gray>Susun blok warnamu di slot atas!"),
                    Title.Times.times(Duration.ofMillis(200), Duration.ofMillis(2500), Duration.ofMillis(700))
            ));
            p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1.5f);
        });
        broadcast(ChatUtils.toComponent(
                "<gradient:#00FF80:#00AAFF><b>🎨 GAME DIMULAI!</b></gradient>"
                + " <gray>Penebak, susun blok warnamu sekarang!"));
    }

    /**
     * Called when a setter clicks a corrector paper (CMD 2551-2557).
     * Returns the announced score, or -1 if invalid context.
     */
    public int announcePaperScore(Player setter, int cmd) {
        if (!setters.contains(setter.getUniqueId())) return -1;
        if (state != GameState.ACTIVE && state != GameState.SCORING) return -1;
        state = GameState.SCORING;

        int score = cmd - 2550; // 2551 → 1

        // Build title based on score
        String titleStr;
        if (score >= 5) {
            titleStr = "<gradient:#00FF00:#FFFF00><b>🎉 PERFECT! " + score + "/5 BENAR!</b></gradient>";
        } else if (score >= 3) {
            titleStr = "<gradient:#FFFF00:#FFA500><b>✨ " + score + "/5 Benar!</b></gradient>";
        } else {
            titleStr = "<gradient:#FF5500:#FF0000><b>❌ Hanya " + score + "/5 Benar</b></gradient>";
        }

        eachInWorld(p -> {
            p.showTitle(Title.title(
                    ChatUtils.toComponent(titleStr),
                    ChatUtils.toComponent("<gray>Diumumkan oleh <white>" + setter.getName()),
                    Title.Times.times(Duration.ofMillis(200), Duration.ofMillis(4000), Duration.ofMillis(1000))
            ));
            Sound snd = score >= 5 ? Sound.UI_TOAST_CHALLENGE_COMPLETE
                    : score >= 3  ? Sound.ENTITY_EXPERIENCE_ORB_PICKUP
                    : Sound.ENTITY_VILLAGER_NO;
            p.playSound(p.getLocation(), snd, 1f, 1f);
        });

        broadcast(ChatUtils.toComponent(
                "<gradient:#FFD700:#FFA500><b>📊 HASIL:</b></gradient>"
                + " <white>" + score + "/5 blok benar!"
                + " <gray>(diumumkan oleh " + setter.getName() + ")"));

        // Answer reveal
        StringBuilder reveal = new StringBuilder("<gray>Jawaban penentu: ");
        for (Material m : setterAnswer) {
            if (m != null) reveal.append(colorName(m)).append(" ");
        }
        broadcast(ChatUtils.toComponent(reveal.toString().trim()));

        // Leaderboard update
        if (score >= 5) {
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
            if (p != null) giveSetterItems(p);
        }
        for (UUID uid : guessers) {
            Player p = Bukkit.getPlayer(uid);
            if (p != null) giveGuesserItems(p);
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
}
