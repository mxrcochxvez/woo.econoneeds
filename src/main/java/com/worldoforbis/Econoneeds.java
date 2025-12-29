package com.worldoforbis;

import org.bukkit.plugin.java.JavaPlugin;
import com.worldoforbis.economy.EconomyManager;
import com.worldoforbis.economy.ItemPriceManager;
import com.worldoforbis.commands.EcoCommand;
import com.worldoforbis.commands.TestCommand;
import com.worldoforbis.listeners.PlayerListener;

public class Econoneeds extends JavaPlugin {

    @Override
    public void onEnable() {
        // Initialize economy system
        EconomyManager.initialize(this);
        ItemPriceManager.initialize(this);

        // Register commands
        EcoCommand ecoCommand = new EcoCommand();
        getCommand("eco").setExecutor(ecoCommand);
        getCommand("eco").setTabCompleter(ecoCommand);
        getCommand("test").setExecutor(new TestCommand());

        // Register listeners
        getServer().getPluginManager().registerEvents(new PlayerListener(), this);

        getLogger().info("Econoneeds has been enabled!");
    }

    @Override
    public void onDisable() {
        // Save economy data before shutdown
        EconomyManager.getInstance().save();

        getLogger().info("Econoneeds has been disabled!");
    }
}
