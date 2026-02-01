package id.naturalsmp.naturalFun.bloodmoon;

import id.naturalsmp.naturalFun.utils.ColorUtils;
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

    // Support both modern Paper and legacy Spigot chat events just in case,
    // but typically AsyncChatEvent is preferred on newer versions.
    // However, for broad compatibility without knowing the exact API version (user
    // said Windows/Gradle but checking imports usually helps),
    // I will try to use the most standard one.
    // The codebase uses `net.kyori.adventure` so likely Paper or modern Spigot.

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChat(io.papermc.paper.event.player.AsyncChatEvent event) {
        if (event.isCancelled())
            return;
        Player player = event.getPlayer();
        if (awaiting.containsKey(player.getUniqueId())) {
            event.setCancelled(true);
            String message = PlainTextComponentSerializer.plainText().serialize(event.message());

            Consumer<String> callback = awaiting.remove(player.getUniqueId());

            // Run callback on main thread just in case it modifies Bukkit API (Inventory
            // etc)
            Bukkit.getScheduler().runTask(
                    id.naturalsmp.naturalFun.NaturalFun.getPlugin(id.naturalsmp.naturalFun.NaturalFun.class), () -> {
                        if (message.equalsIgnoreCase("cancel")) {
                            player.sendMessage(ColorUtils.miniMessage("<red>Cancelled."));
                        } else {
                            callback.accept(message);
                        }
                    });
        }
    }

    // Fallback for non-Paper servers if needed (keeping it simple with just one for
    // now, assuming Paper/Purpur due to Adventure constraints)
    // Actually, NaturalCore seemingly used Spigot but imports Adventure.
    // Let's rely on AsyncChatEvent if Paper, or AsyncPlayerChatEvent if Spigot.
    // To be safe, I'll add AsyncPlayerChatEvent too.

    @EventHandler(priority = EventPriority.LOWEST)
    public void onLegacyChat(AsyncPlayerChatEvent event) {
        if (event.isCancelled())
            return;
        Player player = event.getPlayer();
        if (awaiting.containsKey(player.getUniqueId())) {
            event.setCancelled(true);
            String message = event.getMessage();

            Consumer<String> callback = awaiting.remove(player.getUniqueId());

            Bukkit.getScheduler().runTask(
                    id.naturalsmp.naturalFun.NaturalFun.getPlugin(id.naturalsmp.naturalFun.NaturalFun.class), () -> {
                        if (message.equalsIgnoreCase("cancel")) {
                            player.sendMessage(ColorUtils.miniMessage("<red>Cancelled."));
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
