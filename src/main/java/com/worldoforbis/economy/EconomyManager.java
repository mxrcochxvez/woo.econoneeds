package com.worldoforbis.economy;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.*;

/**
 * Manages economy data storage using YAML files.
 * Stores currency balances per player UUID.
 */
public class EconomyManager {

    private static EconomyManager instance;
    private final JavaPlugin plugin;
    private File dataFile;
    private FileConfiguration dataConfig;
    private static final DecimalFormat CURRENCY_FORMAT = new DecimalFormat("#,##0.00");

    private EconomyManager(JavaPlugin plugin) {
        this.plugin = plugin;
        setupDataFile();
    }

    public static void initialize(JavaPlugin plugin) {
        if (instance == null) {
            instance = new EconomyManager(plugin);
        }
    }

    public static EconomyManager getInstance() {
        if (instance == null) {
            throw new IllegalStateException("EconomyManager has not been initialized!");
        }
        return instance;
    }

    private void setupDataFile() {
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }

        dataFile = new File(plugin.getDataFolder(), "economy.yml");
        if (!dataFile.exists()) {
            try {
                dataFile.createNewFile();
                plugin.getLogger().info("Created economy.yml");
            } catch (IOException e) {
                plugin.getLogger().severe("Could not create economy.yml: " + e.getMessage());
            }
        }

        dataConfig = YamlConfiguration.loadConfiguration(dataFile);
    }

    public void save() {
        try {
            dataConfig.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save economy.yml: " + e.getMessage());
        }
    }

    public void reload() {
        dataConfig = YamlConfiguration.loadConfiguration(dataFile);
    }

    // ==================== CURRENCY METHODS ====================

    public double getBalance(UUID uuid) {
        return dataConfig.getDouble("players." + uuid.toString() + ".balance", 0.0);
    }

    public void setBalance(UUID uuid, double amount) {
        dataConfig.set("players." + uuid.toString() + ".balance", amount);
        save();
    }

    public double addBalance(UUID uuid, double amount) {
        double newBalance = getBalance(uuid) + amount;
        setBalance(uuid, newBalance);
        return newBalance;
    }

    public boolean removeBalance(UUID uuid, double amount) {
        double currentBalance = getBalance(uuid);
        if (currentBalance < amount) {
            return false;
        }
        setBalance(uuid, currentBalance - amount);
        return true;
    }

    public boolean hasBalance(UUID uuid, double amount) {
        return getBalance(uuid) >= amount;
    }

    // ==================== UTILITY METHODS ====================

    public static String formatCurrency(double amount) {
        return "$" + CURRENCY_FORMAT.format(amount);
    }

    public List<Map.Entry<UUID, Double>> getTopBalances(int limit) {
        Map<UUID, Double> balances = new HashMap<>();

        if (dataConfig.getConfigurationSection("players") != null) {
            for (String uuidString : dataConfig.getConfigurationSection("players").getKeys(false)) {
                try {
                    UUID uuid = UUID.fromString(uuidString);
                    double balance = getBalance(uuid);
                    if (balance > 0) {
                        balances.put(uuid, balance);
                    }
                } catch (IllegalArgumentException ignored) {
                }
            }
        }

        List<Map.Entry<UUID, Double>> sorted = new ArrayList<>(balances.entrySet());
        sorted.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));

        return sorted.subList(0, Math.min(limit, sorted.size()));
    }

    public static String getPlayerName(UUID uuid) {
        OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
        String name = player.getName();
        return name != null ? name : "Unknown";
    }
}
