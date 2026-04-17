package id.naturalsmp.naturalFun.colorgame;

import id.naturalsmp.naturalFun.NaturalFun;
import id.naturalsmp.naturalFun.utils.ChatUtils;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ColorGameCommand implements CommandExecutor, TabCompleter {

    private final NaturalFun plugin;
    private final ColorGameManager manager;

    public ColorGameCommand(NaturalFun plugin, ColorGameManager manager) {
        this.plugin  = plugin;
        this.manager = manager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        // ── /content ──────────────────────────────────────────────────────────────
        if (label.equalsIgnoreCase("content")) {
            if (!(sender instanceof Player p)) {
                sender.sendMessage("Only players can use this command.");
                return true;
            }
            String worldName = manager.getWorldName();
            World world = Bukkit.getWorld(worldName);
            if (world == null) {
                p.sendMessage(ChatUtils.toComponent(
                        "<red>World '<white>" + worldName + "<red>' tidak ditemukan di server!"));
                return true;
            }
            p.teleport(world.getSpawnLocation());
            p.sendMessage(ChatUtils.toComponent(
                    "<gradient:#00AAFF:#AA00FF><b>🎨 Selamat datang di Content World!</b></gradient>"
                    + " <gray>Masuk ke zona untuk bergabung dengan game!"));
            return true;
        }

        // ── /colorgame / /tebakwarna ──────────────────────────────────────────────
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        return switch (args[0].toLowerCase()) {
            case "leaderboard", "lb", "top" -> {
                showLeaderboard(sender);
                yield true;
            }
            case "reset" -> {
                if (!sender.hasPermission("naturalfun.admin")) {
                    sender.sendMessage(ChatUtils.toComponent(
                            "<red>✖ Kamu tidak punya izin untuk ini!"));
                    yield true;
                }
                manager.resetArena();
                manager.getSetters().clear();
                manager.getGuessers().clear();
                sender.sendMessage(ChatUtils.toComponent(
                        "<green>✔ Arena Tebak Warna berhasil direset!"));
                yield true;
            }
            case "info", "status" -> {
                sendStatus(sender);
                yield true;
            }
            default -> {
                sendHelp(sender);
                yield true;
            }
        };
    }

    // ── Sub-command helpers ───────────────────────────────────────────────────────

    private void showLeaderboard(CommandSender sender) {
        List<ColorGameLeaderboard.Entry> top = manager.getLeaderboard().getTop(10);

        sender.sendMessage(ChatUtils.toComponent(""));
        sender.sendMessage(ChatUtils.toComponent(
                "<gradient:#FFD700:#FFA500><b>╔══════════════════════════════════╗</b></gradient>"));
        sender.sendMessage(ChatUtils.toComponent(
                "<gradient:#FFD700:#FFA500><b>║  🎨  TEBAK WARNA LEADERBOARD  🎨 ║</b></gradient>"));
        sender.sendMessage(ChatUtils.toComponent(
                "<gradient:#FFD700:#FFA500><b>╚══════════════════════════════════╝</b></gradient>"));

        if (top.isEmpty()) {
            sender.sendMessage(ChatUtils.toComponent("  <gray>Belum ada data leaderboard."));
        } else {
            String[] medals = {"🥇", "🥈", "🥉"};
            for (int i = 0; i < top.size(); i++) {
                ColorGameLeaderboard.Entry e = top.get(i);
                String medal = i < 3 ? medals[i] : "<gray>#" + (i + 1);
                double avg = e.games() > 0 ? (double) e.totalCorrect() / e.games() : 0;
                sender.sendMessage(ChatUtils.toComponent(
                        "  " + medal + " <yellow>" + e.name()
                        + " <white>— <green>" + e.wins() + " menang"
                        + " <dark_gray>(" + e.games() + " game, avg "
                        + String.format("%.1f", avg) + "/5)"));
            }
        }
        sender.sendMessage(ChatUtils.toComponent(""));
    }

    private void sendStatus(CommandSender sender) {
        ColorGameManager.GameState state = manager.getState();
        String stateStr = switch (state) {
            case IDLE      -> "<gray>Menunggu Penentu mengisi slot";
            case COUNTDOWN -> "<yellow>⏳ Countdown berlangsung!";
            case ACTIVE    -> "<green>🎨 Game sedang berlangsung!";
            case SCORING   -> "<gold>📊 Fase penilaian";
        };
        sender.sendMessage(ChatUtils.toComponent(
                "<gradient:#00AAFF:#AA00FF><b>🎨 Tebak Warna Status</b></gradient>\n"
                + "  Status  : " + stateStr + "\n"
                + "  Penentu : <white>" + manager.getSetters().size() + " player\n"
                + "  Penebak : <white>" + manager.getGuessers().size() + " player"));
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(ChatUtils.toComponent(
                "\n<gradient:#00AAFF:#AA00FF><b>🎨 TEBAK WARNA</b></gradient> <dark_gray>by NaturalSMP\n"
                + "  <yellow>/content               <gray>— Teleport ke Content World\n"
                + "  <yellow>/colorgame leaderboard <gray>— Lihat papan peringkat\n"
                + "  <yellow>/colorgame status      <gray>— Lihat status game\n"
                + "  <yellow>/colorgame reset       <gray>— Reset arena (admin)"));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd,
                                      String label, String[] args) {
        if (args.length == 1) {
            return Arrays.asList("leaderboard", "status", "reset");
        }
        return Collections.emptyList();
    }
}
