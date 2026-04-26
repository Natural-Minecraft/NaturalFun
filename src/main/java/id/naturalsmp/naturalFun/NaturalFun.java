package id.naturalsmp.naturalFun;

import id.naturalsmp.naturalFun.bloodmoon.*;
import id.naturalsmp.naturalFun.colorgame.ColorGameCommand;
import id.naturalsmp.naturalFun.colorgame.ColorGameExpansion;
import id.naturalsmp.naturalFun.colorgame.ColorGameLeaderboard;
import id.naturalsmp.naturalFun.colorgame.ColorGameListener;
import id.naturalsmp.naturalFun.colorgame.ColorGameManager;
import id.naturalsmp.naturalFun.emoji.EmojiChatListener;
import id.naturalsmp.naturalFun.emoji.EmojiCommand;
import id.naturalsmp.naturalFun.emoji.EmojiGUI;
import id.naturalsmp.naturalFun.emoji.EmojiManager;
import id.naturalsmp.naturalFun.fun.FunCommand;
import id.naturalsmp.naturalFun.fun.FunListener;
import id.naturalsmp.naturalFun.trader.TraderCommand;
import id.naturalsmp.naturalFun.trader.TraderListener;
import id.naturalsmp.naturalFun.trader.TraderManager;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public final class NaturalFun extends JavaPlugin {

    private static NaturalFun instance;
    private BloodmoonManager bloodmoonManager;
    private LeaderboardManager leaderboardManager;
    private TraderManager traderManager;
    private EmojiManager emojiManager;
    private FileConfiguration messagesConfig;

    @Override
    public void onEnable() {
        instance = this;

        // Config
        saveDefaultConfig();
        loadMessages();

        // --- Modules ---

        // 1. Bloodmoon
        SafezoneManager.init(this);
        bloodmoonManager = new BloodmoonManager(this);
        leaderboardManager = new LeaderboardManager(this);
        BloodmoonCurrencyManager currencyManager = new BloodmoonCurrencyManager(this);
        BloodmoonShopManager shopManager = new BloodmoonShopManager(this);
        BloodmoonShopGUI shopGUI = new BloodmoonShopGUI(shopManager, currencyManager);

        getServer().getPluginManager().registerEvents(new BloodmoonListener(this, bloodmoonManager, leaderboardManager, currencyManager),
                this);
        getServer().getPluginManager().registerEvents(shopGUI, this);

        BloodmoonAdminGUI adminGUI = new BloodmoonAdminGUI(this, bloodmoonManager, shopGUI);
        getServer().getPluginManager().registerEvents(adminGUI, this);
        getServer().getPluginManager().registerEvents(new EditorInputListener(), this);

        getCommand("bloodmoon").setExecutor(new BloodmoonCommand(bloodmoonManager, leaderboardManager, adminGUI));
        getCommand("givebmcoin").setExecutor(new GiveBMCoinCommand(currencyManager));
        getCommand("bmshop").setExecutor(new BloodmoonShopCommand(shopGUI));
        getCommand("bmleaderboard").setExecutor(new BMLeaderboardCommand(leaderboardManager));

        BMEditItemCommand editCmd = new BMEditItemCommand(this);
        getCommand("bmsetprice").setExecutor(editCmd);
        getCommand("bmsetstock").setExecutor(editCmd);
        getCommand("bmsetrarity").setExecutor(editCmd);

        // 2. Fun
        FunCommand funCmd = new FunCommand(this);
        getCommand("gg").setExecutor(funCmd);
        getCommand("noob").setExecutor(funCmd);
        getServer().getPluginManager().registerEvents(new FunListener(this), this);

        // 3. Trader
        traderManager = new TraderManager(this);
        getServer().getPluginManager().registerEvents(new TraderListener(traderManager), this);
        getCommand("traderadmin").setExecutor(new TraderCommand(this, traderManager));

        // 4. Emoji System (ItemsAdder Integration)
        this.emojiManager = new EmojiManager(this);
        EmojiCommand emojiCmd = new EmojiCommand(this);
        getCommand("emoji").setExecutor(emojiCmd);
        getCommand("emoji").setTabCompleter(emojiCmd);
        getServer().getPluginManager().registerEvents(new EmojiGUI(this), this);
        getServer().getPluginManager().registerEvents(new EmojiChatListener(this), this);
        getLogger().info("Emoji System: ENABLED (ItemsAdder: " +
                (getServer().getPluginManager().getPlugin("ItemsAdder") != null) + ")");

        // 5. Color Game (Tebak Warna)
        ColorGameLeaderboard colorLeaderboard = new ColorGameLeaderboard(this);
        ColorGameManager colorGameManager = new ColorGameManager(this, colorLeaderboard);
        ColorGameCommand colorGameCmd = new ColorGameCommand(this, colorGameManager);
        getServer().getPluginManager().registerEvents(new ColorGameListener(this, colorGameManager), this);
        getCommand("colorgame").setExecutor(colorGameCmd);
        getCommand("colorgame").setTabCompleter(colorGameCmd);
        getCommand("content").setExecutor(colorGameCmd);
        // Register PlaceholderAPI expansion if present
        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new ColorGameExpansion(colorLeaderboard).register();
            getLogger().info("Color Game: PlaceholderAPI expansion registered!");
        }
        getLogger().info("Color Game: ENABLED");

        // Listen for ItemsAdder load completion to re-resolve font images
        if (getServer().getPluginManager().getPlugin("ItemsAdder") != null) {
            getServer().getPluginManager().registerEvents(new Listener() {
                @EventHandler
                public void onItemsAdderLoad(dev.lone.itemsadder.api.Events.ItemsAdderLoadDataEvent event) {
                    if (emojiManager != null) {
                        emojiManager.onItemsAdderReady();
                    }
                }
            }, this);
        }

        getLogger().info("NaturalFun has been enabled with all features!");
    }

    @Override
    public void onDisable() {
        if (leaderboardManager != null)
            leaderboardManager.save();
        if (traderManager != null)
            traderManager.save();
    }

    private void loadMessages() {
        File file = new File(getDataFolder(), "messages.yml");
        if (!file.exists()) {
            saveResource("messages.yml", false);
        }
        messagesConfig = YamlConfiguration.loadConfiguration(file);
    }

    public FileConfiguration getMessagesConfig() {
        if (messagesConfig == null)
            loadMessages();
        return messagesConfig;
    }

    public EmojiManager getEmojiManager() {
        return emojiManager;
    }

    public static NaturalFun getInstance() {
        return instance;
    }
}
