package id.naturalsmp.naturalFun.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.ChatColor;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChatUtils {

    private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");
    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
    private static final LegacyComponentSerializer SECTION_SERIALIZER = LegacyComponentSerializer.legacySection();

    /**
     * Translates MiniMessage tags, hex codes (&#RRGGBB), and legacy codes (&a).
     */
    public static String colorize(String message) {
        if (message == null || message.isEmpty())
            return "";

        String result = message;

        // 1. Process MiniMessage
        if (result.contains("<")) {
            try {
                result = SECTION_SERIALIZER.serialize(MINI_MESSAGE.deserialize(result));
            } catch (Exception ignored) {
            }
        }

        // 2. Process Hex
        Matcher matcher = HEX_PATTERN.matcher(result);
        StringBuffer buffer = new StringBuffer();
        while (matcher.find()) {
            try {
                matcher.appendReplacement(buffer, net.md_5.bungee.api.ChatColor.of("#" + matcher.group(1)).toString());
            } catch (Exception e) {
                matcher.appendReplacement(buffer, Matcher.quoteReplacement(matcher.group()));
            }
        }
        result = matcher.appendTail(buffer).toString();

        return ChatColor.translateAlternateColorCodes('&', result);
    }

    /**
     * Converts string to Adventure Component.
     */
    public static Component toComponent(String message) {
        if (message == null || message.isEmpty())
            return Component.empty();
        return SECTION_SERIALIZER.deserialize(colorize(message));
    }

    /**
     * Serializes Component to legacy string with section symbol (§).
     */
    public static String serialize(Component component) {
        if (component == null)
            return "";
        return SECTION_SERIALIZER.serialize(component);
    }

    public static List<String> colorize(List<String> list) {
        if (list == null)
            return null;
        List<String> colored = new ArrayList<>();
        for (String s : list)
            colored.add(colorize(s));
        return colored;
    }
}
