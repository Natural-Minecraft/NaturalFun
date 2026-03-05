package id.naturalsmp.naturalFun.emoji;

import id.naturalsmp.naturalFun.NaturalFun;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

/**
 * Listens to chat events and replaces emoji triggers with ItemsAdder font
 * characters.
 * Uses NORMAL priority so it runs after most chat plugins but before renderers.
 */
public class EmojiChatListener implements Listener {

    private final NaturalFun plugin;
    private final LegacyComponentSerializer legacySection = LegacyComponentSerializer.legacySection();

    public EmojiChatListener(NaturalFun plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onChat(AsyncChatEvent event) {
        EmojiManager manager = plugin.getEmojiManager();
        if (manager == null || !manager.isEnabled())
            return;

        Player player = event.getPlayer();

        // Serialize the message to legacy text for emoji replacement
        String messageText = legacySection.serialize(event.message());

        // Parse emojis
        String parsed = manager.parseEmojis(player, messageText);

        // If no change, skip
        if (parsed.equals(messageText))
            return;

        // Update the message
        event.message(legacySection.deserialize(parsed));
    }
}
