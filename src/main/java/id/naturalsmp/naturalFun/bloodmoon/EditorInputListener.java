package id.naturalsmp.naturalFun.bloodmoon;

import id.naturalsmp.naturalFun.NaturalFun;
import id.naturalsmp.naturalFun.utils.ChatUtils;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

public class EditorInputListener implements Listener {

    private static final Map<UUID, Consumer<String>> awaiting = new HashMap<>();

    public static void awaitInput(Player player, Consumer<String> callback) {
        awaiting.put(player.getUniqueId(), callback);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChat(AsyncChatEvent event) {
        if (event.isCancelled())
            return;
        Player player = event.getPlayer();
        if (awaiting.containsKey(player.getUniqueId())) {
            event.setCancelled(true);
            String message = PlainTextComponentSerializer.plainText().serialize(event.message());
            Consumer<String> callback = awaiting.remove(player.getUniqueId());

            Bukkit.getScheduler().runTask(NaturalFun.getInstance(), () -> {
                if (message.equalsIgnoreCase("cancel")) {
                    player.sendMessage(ChatUtils.toComponent("<red>Cancelled."));
                } else {
                    callback.accept(message);
                }
            });
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onLegacyChat(@SuppressWarnings("deprecation") AsyncPlayerChatEvent event) {
        if (event.isCancelled())
            return;
        Player player = event.getPlayer();
        if (awaiting.containsKey(player.getUniqueId())) {
            event.setCancelled(true);
            String message = event.getMessage();
            Consumer<String> callback = awaiting.remove(player.getUniqueId());

            Bukkit.getScheduler().runTask(NaturalFun.getInstance(), () -> {
                if (message.equalsIgnoreCase("cancel")) {
                    player.sendMessage(ChatUtils.toComponent("<red>Cancelled."));
                } else {
                    callback.accept(message);
                }
            });
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        awaiting.remove(event.getPlayer().getUniqueId());
    }
}
