package id.naturalsmp.naturalFun.colorgame;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * PlaceholderAPI expansion for NaturalFun Color Game.
 *
 * Placeholders:
 *   %naturalfun_colorblock_player_top_1%   ... %naturalfun_colorblock_player_top_10%
 *   %naturalfun_colorblock_wins_top_1%     ... %naturalfun_colorblock_wins_top_10%
 *   %naturalfun_colorblock_wins%           — total wins of the requesting player
 */
public class ColorGameExpansion extends PlaceholderExpansion {

    private final ColorGameLeaderboard leaderboard;

    public ColorGameExpansion(ColorGameLeaderboard leaderboard) {
        this.leaderboard = leaderboard;
    }

    @Override public @NotNull String getIdentifier() { return "naturalfun"; }
    @Override public @NotNull String getAuthor()     { return "NaturalSMP"; }
    @Override public @NotNull String getVersion()    { return "1.0.0"; }
    @Override public boolean persist()               { return true; }

    @Override
    public @Nullable String onRequest(OfflinePlayer player, @NotNull String params) {
        // %naturalfun_colorblock_player_top_N%
        if (params.startsWith("colorblock_player_top_")) {
            int rank = parseRank(params, "colorblock_player_top_");
            if (rank < 1 || rank > 10) return null;
            List<ColorGameLeaderboard.Entry> top = leaderboard.getTop(10);
            if (rank > top.size()) return "Belum ada!";
            return top.get(rank - 1).name();
        }

        // %naturalfun_colorblock_wins_top_N%  (leaderboard wins by rank)
        if (params.startsWith("colorblock_wins_top_")) {
            int rank = parseRank(params, "colorblock_wins_top_");
            if (rank < 1 || rank > 10) return null;
            List<ColorGameLeaderboard.Entry> top = leaderboard.getTop(10);
            if (rank > top.size()) return "Belum ada!";
            return String.valueOf(top.get(rank - 1).wins());
        }

        // %naturalfun_colorblock_wins%  (wins of the requesting player)
        if (params.equals("colorblock_wins")) {
            if (player == null) return "Belum ada!";
            int wins = leaderboard.getPlayerWins(player.getUniqueId());
            return wins == 0 ? "Belum ada!" : String.valueOf(wins);
        }

        return null;
    }

    private int parseRank(String params, String prefix) {
        try {
            return Integer.parseInt(params.substring(prefix.length()));
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}
