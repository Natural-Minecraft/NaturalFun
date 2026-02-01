package id.naturalsmp.naturalFun.bloodmoon;

import id.naturalsmp.naturalFun.NaturalFun;
import id.naturalsmp.naturalFun.utils.ChatUtils;
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
    private final BloodmoonAdminGUI adminGUI;

    public BloodmoonCommand(BloodmoonManager bloodmoonManager, LeaderboardManager leaderboardManager,
            BloodmoonAdminGUI adminGUI) {
        this.bloodmoonManager = bloodmoonManager;
        this.leaderboardManager = leaderboardManager;
        this.adminGUI = adminGUI;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label,
            @NotNull String[] args) {
        if (args.length == 0) {
            if (sender instanceof Player && sender.hasPermission("bloodmoon.admin")) {
                adminGUI.open((Player) sender);
                return true;
            }

            if (bloodmoonManager.isBloodmoonActive()) {
                sender.sendMessage(ChatUtils.toComponent(
                        "<red>Bloodmoon is currently ACTIVE! Time left: " + bloodmoonManager.getFormattedTime()));
            } else {
                sender.sendMessage(ChatUtils.toComponent("<green>Bloodmoon is inactive."));
            }
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "start":
                if (!sender.hasPermission("bloodmoon.admin"))
                    return true;
                if (sender instanceof Player) {
                    bloodmoonManager.startBloodmoon(((Player) sender).getWorld());
                } else {
                    bloodmoonManager.startBloodmoon(Bukkit.getWorlds().get(0));
                }
                break;
            case "stop":
                if (!sender.hasPermission("bloodmoon.admin"))
                    return true;
                if (sender instanceof Player) {
                    bloodmoonManager.stopBloodmoon(((Player) sender).getWorld());
                } else {
                    bloodmoonManager.stopBloodmoon(Bukkit.getWorlds().get(0));
                }
                break;
            case "top":
                sender.sendMessage(ChatUtils
                        .toComponent("<gradient:#ADD8E6:#00008B><b>--- Bloodmoon Top Kills ---</b></gradient>"));
                int i = 1;
                for (Map.Entry<String, Integer> entry : leaderboardManager.getTopKills(10)) {
                    String name = Bukkit.getOfflinePlayer(UUID.fromString(entry.getKey())).getName();
                    if (name == null)
                        name = "Unknown";
                    sender.sendMessage(
                            ChatUtils.toComponent("<gray>" + i + ". " + name + ": <yellow>" + entry.getValue()));
                    i++;
                }
                break;
        }
        return true;
    }
}
