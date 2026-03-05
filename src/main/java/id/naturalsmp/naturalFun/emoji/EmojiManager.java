package id.naturalsmp.naturalFun.emoji;

import id.naturalsmp.naturalFun.NaturalFun;
import id.naturalsmp.naturalFun.utils.ChatUtils;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * EmojiManager - Manages emoji registry using ItemsAdder font images.
 * 
 * Each emoji entry in chatemojis.yml maps trigger text (e.g. ":smile:")
 * to an ItemsAdder font_image ID (e.g. "twitteremojis:smile").
 * At runtime, the font image character is resolved via ItemsAdder API.
 */
public class EmojiManager {

    private static EmojiManager instance;
    private final NaturalFun plugin;

    // Map: trigger -> EmojiData
    private final Map<String, EmojiData> emojiRegistry = new LinkedHashMap<>();

    // Compiled pattern for matching
    private Pattern emojiPattern;

    // Config
    private File emojiFile;
    private FileConfiguration emojiConfig;
    private boolean enabled = true;
    private boolean itemsAdderAvailable = false;
    private boolean itemsAdderReady = false;

    public EmojiManager(NaturalFun plugin) {
        this.plugin = plugin;
        instance = this;
        this.itemsAdderAvailable = Bukkit.getPluginManager().getPlugin("ItemsAdder") != null;
        loadEmojis();
    }

    public static EmojiManager getInstance() {
        return instance;
    }

    /**
     * Load emoji config from chatemojis.yml
     */
    public void loadEmojis() {
        emojiRegistry.clear();

        if (!new File(plugin.getDataFolder(), "chatemojis.yml").exists()) {
            plugin.saveResource("chatemojis.yml", false);
        }

        emojiFile = new File(plugin.getDataFolder(), "chatemojis.yml");
        emojiConfig = YamlConfiguration.loadConfiguration(emojiFile);

        enabled = emojiConfig.getBoolean("enabled", true);

        ConfigurationSection emojiSection = emojiConfig.getConfigurationSection("list");
        if (emojiSection == null) {
            plugin.getLogger().warning("Emoji list not found in chatemojis.yml!");
            buildPattern();
            return;
        }

        for (String key : emojiSection.getKeys(false)) {
            ConfigurationSection emoji = emojiSection.getConfigurationSection(key);
            if (emoji == null)
                continue;

            String fontImageId = emoji.getString("font_image", "");
            String fallbackChar = emoji.getString("fallback", "?");
            List<String> triggers = emoji.getStringList("triggers");
            String permission = emoji.getString("permission", "");

            // Resolve the actual character from ItemsAdder (only if data is ready)
            String resolvedChar = itemsAdderReady ? resolveCharacter(fontImageId, fallbackChar) : fallbackChar;

            EmojiData data = new EmojiData(key, resolvedChar, fontImageId, fallbackChar, permission);

            for (String trigger : triggers) {
                emojiRegistry.put(trigger.toLowerCase(), data);
            }
        }

        buildPattern();
        plugin.getLogger().info("Loaded " + emojiSection.getKeys(false).size() + " emojis with "
                + emojiRegistry.size() + " triggers (ItemsAdder: " + itemsAdderAvailable
                + ", Ready: " + itemsAdderReady + ")");
    }

    /**
     * Resolve the font image character from ItemsAdder API.
     * Falls back to the fallback character if ItemsAdder is not available.
     */
    private String resolveCharacter(String fontImageId, String fallback) {
        if (!itemsAdderAvailable || fontImageId == null || fontImageId.isEmpty()) {
            return fallback;
        }

        try {
            dev.lone.itemsadder.api.FontImages.FontImageWrapper wrapper = new dev.lone.itemsadder.api.FontImages.FontImageWrapper(
                    fontImageId);
            if (wrapper.exists()) {
                String str = wrapper.getString();
                if (str != null && !str.isEmpty()) {
                    return str;
                }
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to resolve font image '" + fontImageId + "': " + e.getMessage());
        }

        return fallback;
    }

    /**
     * Called when ItemsAdder finishes loading to re-resolve all characters.
     */
    public void onItemsAdderReady() {
        this.itemsAdderAvailable = true;
        this.itemsAdderReady = true;
        loadEmojis();
        plugin.getLogger().info("ItemsAdder ready! Re-resolved all emoji font images.");
    }

    private void buildPattern() {
        if (emojiRegistry.isEmpty()) {
            emojiPattern = null;
            return;
        }

        StringBuilder patternBuilder = new StringBuilder();
        for (String trigger : emojiRegistry.keySet()) {
            if (patternBuilder.length() > 0) {
                patternBuilder.append("|");
            }
            patternBuilder.append(Pattern.quote(trigger));
        }

        emojiPattern = Pattern.compile("(" + patternBuilder.toString() + ")", Pattern.CASE_INSENSITIVE);
    }

    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Parse message and replace trigger text with emoji characters.
     */
    public String parseEmojis(Player player, String message) {
        if (!enabled)
            return message;
        if (!player.hasPermission("naturalsmp.emoji.use"))
            return message;
        if (emojiPattern == null || message == null || message.isEmpty())
            return message;

        Matcher matcher = emojiPattern.matcher(message);
        StringBuffer result = new StringBuffer();

        while (matcher.find()) {
            String trigger = matcher.group(1).toLowerCase();
            EmojiData emoji = emojiRegistry.get(trigger);

            if (emoji != null) {
                if (emoji.hasPermission() && !player.hasPermission(emoji.getPermission())
                        && !player.hasPermission("naturalsmp.emoji.*")) {
                    continue;
                }
                matcher.appendReplacement(result, Matcher.quoteReplacement(emoji.getCharacter()));
            }
        }
        matcher.appendTail(result);

        return result.toString();
    }

    /**
     * Parse emoji without permission check (admin/force).
     */
    public String parseEmojisForce(String message) {
        if (emojiPattern == null || message == null || message.isEmpty())
            return message;

        Matcher matcher = emojiPattern.matcher(message);
        StringBuffer result = new StringBuffer();

        while (matcher.find()) {
            String trigger = matcher.group(1).toLowerCase();
            EmojiData emoji = emojiRegistry.get(trigger);
            if (emoji != null) {
                matcher.appendReplacement(result, Matcher.quoteReplacement(emoji.getCharacter()));
            }
        }
        matcher.appendTail(result);

        return result.toString();
    }

    public Map<String, EmojiData> getEmojiRegistry() {
        return new LinkedHashMap<>(emojiRegistry);
    }

    /**
     * Inner class to store emoji data.
     */
    public static class EmojiData {
        private final String name;
        private final String character;
        private final String fontImageId;
        private final String fallback;
        private final String permission;

        public EmojiData(String name, String character, String fontImageId, String fallback, String permission) {
            this.name = name;
            this.character = character;
            this.fontImageId = fontImageId;
            this.fallback = fallback;
            this.permission = permission;
        }

        public String getName() {
            return name;
        }

        public String getCharacter() {
            return character;
        }

        public String getFontImageId() {
            return fontImageId;
        }

        public String getFallback() {
            return fallback;
        }

        public String getPermission() {
            return permission;
        }

        public boolean hasPermission() {
            return permission != null && !permission.isEmpty();
        }
    }
}
