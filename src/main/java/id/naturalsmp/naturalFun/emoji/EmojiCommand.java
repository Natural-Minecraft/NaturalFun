package id.naturalsmp.naturalFun.emoji;

import id.naturalsmp.naturalFun.NaturalFun;
import id.naturalsmp.naturalFun.utils.ChatUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

/**
 * /emoji - Open emoji GUI
 * /emoji reload - Reload emoji config (admin only)
 */
public class EmojiCommand implements CommandExecutor, TabCompleter {

    private final NaturalFun plugin;

    public EmojiCommand(NaturalFun plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label,
            @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command is player-only.");
            return true;
        }

        Player p = (Player) sender;

        // /emoji reload (Admin)
        if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
            if (!p.hasPermission("naturalsmp.admin")) {
                p.sendMessage(ChatUtils.toComponent("&cYou don't have permission!"));
                return true;
            }

            if (plugin.getEmojiManager() != null) {
                plugin.getEmojiManager().loadEmojis();
                p.sendMessage(ChatUtils.toComponent("&aEmoji registry berhasil di-reload!"));
            }
            return true;
        }

        // Open GUI
        new EmojiGUI(plugin).openGUI(p);
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
            @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            String arg = args[0].toLowerCase();
            if (sender.hasPermission("naturalsmp.admin") && "reload".startsWith(arg)) {
                return Collections.singletonList("reload");
            }
        }
        return Collections.emptyList();
    }
}
