package com.worldoforbis.economy;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Manages item sell prices from a YAML config file.
 * Prices can be edited by server admins.
 */
public class ItemPriceManager {

    private static ItemPriceManager instance;
    private final JavaPlugin plugin;
    private File pricesFile;
    private FileConfiguration pricesConfig;

    private ItemPriceManager(JavaPlugin plugin) {
        this.plugin = plugin;
        setupPricesFile();
    }

    public static void initialize(JavaPlugin plugin) {
        if (instance == null) {
            instance = new ItemPriceManager(plugin);
        }
    }

    public static ItemPriceManager getInstance() {
        if (instance == null) {
            throw new IllegalStateException("ItemPriceManager has not been initialized!");
        }
        return instance;
    }

    private void setupPricesFile() {
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }

        pricesFile = new File(plugin.getDataFolder(), "prices.yml");
        if (!pricesFile.exists()) {
            try {
                pricesFile.createNewFile();
                plugin.getLogger().info("Created prices.yml");
                createDefaults();
            } catch (IOException e) {
                plugin.getLogger().severe("Could not create prices.yml: " + e.getMessage());
            }
        }

        pricesConfig = YamlConfiguration.loadConfiguration(pricesFile);
    }

    private void createDefaults() {
        pricesConfig = YamlConfiguration.loadConfiguration(pricesFile);

        // Ores and Ingots
        setDefaultPrice(Material.DIAMOND, 100.0);
        setDefaultPrice(Material.EMERALD, 75.0);
        setDefaultPrice(Material.GOLD_INGOT, 50.0);
        setDefaultPrice(Material.IRON_INGOT, 25.0);
        setDefaultPrice(Material.COPPER_INGOT, 10.0);
        setDefaultPrice(Material.COAL, 5.0);
        setDefaultPrice(Material.LAPIS_LAZULI, 8.0);
        setDefaultPrice(Material.REDSTONE, 4.0);
        setDefaultPrice(Material.QUARTZ, 6.0);

        // Raw Ores
        setDefaultPrice(Material.RAW_IRON, 15.0);
        setDefaultPrice(Material.RAW_GOLD, 30.0);
        setDefaultPrice(Material.RAW_COPPER, 5.0);

        // Wood
        setDefaultPrice(Material.OAK_LOG, 10.0);
        setDefaultPrice(Material.SPRUCE_LOG, 10.0);
        setDefaultPrice(Material.BIRCH_LOG, 10.0);
        setDefaultPrice(Material.JUNGLE_LOG, 10.0);
        setDefaultPrice(Material.ACACIA_LOG, 10.0);
        setDefaultPrice(Material.DARK_OAK_LOG, 10.0);
        setDefaultPrice(Material.MANGROVE_LOG, 10.0);
        setDefaultPrice(Material.CHERRY_LOG, 12.0);

        // Stone
        setDefaultPrice(Material.COBBLESTONE, 1.0);
        setDefaultPrice(Material.STONE, 2.0);
        setDefaultPrice(Material.GRANITE, 2.0);
        setDefaultPrice(Material.DIORITE, 2.0);
        setDefaultPrice(Material.ANDESITE, 2.0);
        setDefaultPrice(Material.DEEPSLATE, 3.0);

        // Crops
        setDefaultPrice(Material.WHEAT, 3.0);
        setDefaultPrice(Material.CARROT, 4.0);
        setDefaultPrice(Material.POTATO, 4.0);
        setDefaultPrice(Material.BEETROOT, 3.0);
        setDefaultPrice(Material.MELON_SLICE, 2.0);
        setDefaultPrice(Material.PUMPKIN, 8.0);
        setDefaultPrice(Material.SUGAR_CANE, 5.0);

        // Mob Drops
        setDefaultPrice(Material.LEATHER, 8.0);
        setDefaultPrice(Material.BONE, 3.0);
        setDefaultPrice(Material.STRING, 4.0);
        setDefaultPrice(Material.ROTTEN_FLESH, 1.0);
        setDefaultPrice(Material.SPIDER_EYE, 5.0);
        setDefaultPrice(Material.GUNPOWDER, 10.0);
        setDefaultPrice(Material.ENDER_PEARL, 25.0);
        setDefaultPrice(Material.BLAZE_ROD, 30.0);
        setDefaultPrice(Material.GHAST_TEAR, 50.0);

        // Fish
        setDefaultPrice(Material.COD, 5.0);
        setDefaultPrice(Material.SALMON, 6.0);
        setDefaultPrice(Material.TROPICAL_FISH, 15.0);
        setDefaultPrice(Material.PUFFERFISH, 20.0);

        save();
    }

    private void setDefaultPrice(Material material, double price) {
        pricesConfig.set("prices." + material.name(), price);
    }

    public void save() {
        try {
            pricesConfig.save(pricesFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save prices.yml: " + e.getMessage());
        }
    }

    public void reload() {
        pricesConfig = YamlConfiguration.loadConfiguration(pricesFile);
        plugin.getLogger().info("Reloaded prices.yml");
    }

    /**
     * Get the sell price for a material.
     * 
     * @param material The material to check
     * @return The price, or 0 if not sellable
     */
    public double getPrice(Material material) {
        return pricesConfig.getDouble("prices." + material.name(), 0.0);
    }

    /**
     * Check if an item can be sold.
     * 
     * @param material The material to check
     * @return true if the item has a price > 0
     */
    public boolean isSellable(Material material) {
        return getPrice(material) > 0;
    }

    /**
     * Get all configured prices.
     * 
     * @return Map of material names to prices
     */
    public Map<String, Double> getAllPrices() {
        Map<String, Double> prices = new HashMap<>();
        if (pricesConfig.getConfigurationSection("prices") != null) {
            for (String key : pricesConfig.getConfigurationSection("prices").getKeys(false)) {
                prices.put(key, pricesConfig.getDouble("prices." + key));
            }
        }
        return prices;
    }
}
