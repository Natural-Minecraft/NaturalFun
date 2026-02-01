package id.naturalsmp.naturalFun.bloodmoon;

import id.naturalsmp.naturalFun.utils.ChatUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.UUID;

public class BMLeaderboardCommand implements CommandExecutor {

    private final LeaderboardManager leaderboardManager;

    public BMLeaderboardCommand(LeaderboardManager leaderboardManager) {
        this.leaderboardManager = leaderboardManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label,
            @NotNull String[] args) {
        sender.sendMessage(
                ChatUtils.toComponent("<gradient:#ADD8E6:#00008B><b>--- Bloodmoon Top Kills ---</b></gradient>"));
        int i = 1;
        for (Map.Entry<String, Integer> entry : leaderboardManager.getTopKills(10)) {
            String name = Bukkit.getOfflinePlayer(UUID.fromString(entry.getKey())).getName();
            if (name == null)
                name = "Unknown";
            sender.sendMessage(ChatUtils.toComponent("<gray>" + i + ". " + name + ": <yellow>" + entry.getValue()));
            i++;
        }
        return true;
    }
}
