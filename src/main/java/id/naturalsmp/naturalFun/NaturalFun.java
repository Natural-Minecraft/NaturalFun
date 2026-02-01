package id.naturalsmp.naturalFun;

import id.naturalsmp.naturalFun.bloodmoon.BloodmoonCommand;
import id.naturalsmp.naturalFun.bloodmoon.BloodmoonListener;
import id.naturalsmp.naturalFun.bloodmoon.BloodmoonManager;
import id.naturalsmp.naturalFun.bloodmoon.LeaderboardManager;
import id.naturalsmp.naturalFun.bloodmoon.SafezoneManager;
import id.naturalsmp.naturalFun.bloodmoon.BloodmoonCurrencyManager;
import id.naturalsmp.naturalFun.bloodmoon.BloodmoonShopManager;
import id.naturalsmp.naturalFun.bloodmoon.BloodmoonShopGUI;
import id.naturalsmp.naturalFun.bloodmoon.GiveBMCoinCommand;
import id.naturalsmp.naturalFun.bloodmoon.BloodmoonShopCommand;
import id.naturalsmp.naturalFun.bloodmoon.BMLeaderboardCommand;
import id.naturalsmp.naturalFun.bloodmoon.BMEditItemCommand;
import id.naturalsmp.naturalFun.bloodmoon.BloodmoonAdminGUI;
import id.naturalsmp.naturalFun.fun.FunCommand;
import id.naturalsmp.naturalFun.fun.FunListener;
import id.naturalsmp.naturalFun.trader.TraderCommand;
import id.naturalsmp.naturalFun.trader.TraderListener;
import id.naturalsmp.naturalFun.trader.TraderManager;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public final class NaturalFun extends JavaPlugin {

    private BloodmoonManager bloodmoonManager;
    private LeaderboardManager leaderboardManager;
    private TraderManager traderManager;
    private FileConfiguration messagesConfig;

    @Override
    public void onEnable() {
        // Config
        saveDefaultConfig();
        loadMessages();

        // --- Modules ---

        // 1. Bloodmoon
        // 1. Bloodmoon
        SafezoneManager.init(this);
        bloodmoonManager = new BloodmoonManager(this);
        leaderboardManager = new LeaderboardManager(this);
        BloodmoonCurrencyManager currencyManager = new BloodmoonCurrencyManager(this);
        BloodmoonShopManager shopManager = new BloodmoonShopManager(this);
        BloodmoonShopGUI shopGUI = new BloodmoonShopGUI(shopManager, currencyManager);

        getServer().getPluginManager().registerEvents(new BloodmoonListener(this, bloodmoonManager, leaderboardManager),
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
}
