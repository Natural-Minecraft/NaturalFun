package id.naturalsmp.naturalFun.fun;

import id.naturalsmp.naturalFun.NaturalFun;
import id.naturalsmp.naturalFun.utils.ChatUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class FunCommand implements CommandExecutor {

    private final NaturalFun plugin;
    private final Map<UUID, Long> cooldowns = new HashMap<>();

    public FunCommand(NaturalFun plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label,
            @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        Player player = (Player) sender;
        UUID uuid = player.getUniqueId();
        int cooldownTime = plugin.getConfig().getInt("fun.commands.cooldown", 30);

        if (cooldowns.containsKey(uuid)) {
            long secondsLeft = ((cooldowns.get(uuid) / 1000) + cooldownTime) - (System.currentTimeMillis() / 1000);
            if (secondsLeft > 0) {
                String msg = plugin.getMessagesConfig().getString("fun.cooldown", "<red>Wait %time%s!</red>");
                player.sendMessage(ChatUtils.toComponent(msg.replace("%time%", String.valueOf(secondsLeft))));
                return true;
            }
        }

        String msgKey = label.equalsIgnoreCase("gg") ? "fun.gg" : "fun.noob";
        String rawMsg = plugin.getMessagesConfig().getString(msgKey, "Message not found.");
        rawMsg = rawMsg.replace("%player%", player.getName());

        Bukkit.broadcast(ChatUtils.toComponent(rawMsg));

        cooldowns.put(uuid, System.currentTimeMillis());
        return true;
    }
}
