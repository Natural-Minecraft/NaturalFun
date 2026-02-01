package id.naturalsmp.naturalFun.bloodmoon;

import id.naturalsmp.naturalFun.utils.ColorUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.UUID;

public class BloodmoonCommand implements CommandExecutor {

    private final BloodmoonManager bloodmoonManager;
    private final LeaderboardManager leaderboardManager;

    public BloodmoonCommand(BloodmoonManager bloodmoonManager, LeaderboardManager leaderboardManager) {
        this.bloodmoonManager = bloodmoonManager;
        this.leaderboardManager = leaderboardManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {
            sender.sendMessage(ColorUtils.miniMessage("<gradient:#00008B:#ADD8E6>Bloodmoon Plugin by NaturalSMP</gradient>"));
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "start":
                if (!sender.hasPermission("bloodmoon.admin")) return true;
                if (sender instanceof Player) {
                    bloodmoonManager.startBloodmoon(((Player) sender).getWorld());
                } else {
                    bloodmoonManager.startBloodmoon(Bukkit.getWorlds().get(0));
                }
                break;
            case "stop":
                if (!sender.hasPermission("bloodmoon.admin")) return true;
                if (sender instanceof Player) {
                    bloodmoonManager.stopBloodmoon(((Player) sender).getWorld());
                } else {
                    bloodmoonManager.stopBloodmoon(Bukkit.getWorlds().get(0));
                }
                break;
            case "top":
                sender.sendMessage(ColorUtils.miniMessage("<gradient:#ADD8E6:#00008B><b>--- Bloodmoon Top Kills ---</b></gradient>"));
                int i = 1;
                for (Map.Entry<String, Integer> entry : leaderboardManager.getTopKills(10)) {
                    String name = Bukkit.getOfflinePlayer(UUID.fromString(entry.getKey())).getName();
                    if (name == null) name = "Unknown";
                    sender.sendMessage(ColorUtils.miniMessage("<gray>" + i + ". " + name + ": <yellow>" + entry.getValue()));
                    i++;
                }
                break;
        }
        return true;
    }
}
