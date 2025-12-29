package com.worldoforbis.commands;

import com.worldoforbis.economy.EconomyManager;
import com.worldoforbis.economy.ItemPriceManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Unified economy command.
 * 
 * Player commands:
 * /eco bal - Check your balance
 * /eco pay <player> <amount> - Send money to a player
 * /eco top - View richest players
 * 
 * Admin commands (requires econoneeds.admin):
 * /eco give <player> <amount> - Give money to a player
 * /eco take <player> <amount> - Take money from a player
 * /eco set <player> <amount> - Set a player's balance
 * /eco check <player> - Check a player's balance
 * /eco sell <amount> - Sell item in hand
 */
public class EcoCommand implements CommandExecutor, TabCompleter {

    private static final int TOP_LIMIT = 10;

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 1) {
            sendUsage(sender);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            // Player commands
            case "bal":
            case "balance":
                return handleBalance(sender);
            case "pay":
            case "send":
                return handlePay(sender, args);
            case "top":
                return handleTop(sender);

            // Admin commands
            case "give":
                return handleGive(sender, args);
            case "take":
                return handleTake(sender, args);
            case "set":
                return handleSet(sender, args);
            case "check":
                return handleCheck(sender, args);
            case "sell":
                return handleSell(sender, args);

            default:
                sendUsage(sender);
                return true;
        }
    }

    // ==================== PLAYER COMMANDS ====================

    private boolean handleBalance(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cThis command can only be used by players. Use /eco check <player> instead.");
            return true;
        }

        Player player = (Player) sender;
        EconomyManager economy = EconomyManager.getInstance();
        double balance = economy.getBalance(player.getUniqueId());

        player.sendMessage("§aYour balance: §f" + EconomyManager.formatCurrency(balance));
        return true;
    }

    private boolean handlePay(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cThis command can only be used by players. Use /eco give instead.");
            return true;
        }

        Player player = (Player) sender;

        if (args.length < 3) {
            player.sendMessage("§cUsage: /eco pay <player> <amount>");
            return true;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            player.sendMessage("§cPlayer not found or not online: " + args[1]);
            return true;
        }

        if (target.equals(player)) {
            player.sendMessage("§cYou cannot send money to yourself!");
            return true;
        }

        double amount = parseAmount(sender, args[2]);
        if (amount < 0)
            return true;

        EconomyManager economy = EconomyManager.getInstance();

        if (!economy.hasBalance(player.getUniqueId(), amount)) {
            player.sendMessage("§cInsufficient funds! Your balance: " +
                    EconomyManager.formatCurrency(economy.getBalance(player.getUniqueId())));
            return true;
        }

        economy.removeBalance(player.getUniqueId(), amount);
        economy.addBalance(target.getUniqueId(), amount);

        String formattedAmount = EconomyManager.formatCurrency(amount);
        player.sendMessage("§aYou sent " + formattedAmount + " to §f" + target.getName());
        target.sendMessage("§aYou received " + formattedAmount + " from §f" + player.getName());

        return true;
    }

    private boolean handleTop(CommandSender sender) {
        EconomyManager economy = EconomyManager.getInstance();
        List<Map.Entry<UUID, Double>> topBalances = economy.getTopBalances(TOP_LIMIT);

        sender.sendMessage("§6§l=== Top " + TOP_LIMIT + " Richest Players ===");

        if (topBalances.isEmpty()) {
            sender.sendMessage("§7No players with balances yet.");
            return true;
        }

        int rank = 1;
        for (Map.Entry<UUID, Double> entry : topBalances) {
            String playerName = EconomyManager.getPlayerName(entry.getKey());
            String balance = EconomyManager.formatCurrency(entry.getValue());

            String rankColor;
            switch (rank) {
                case 1:
                    rankColor = "§6";
                    break;
                case 2:
                    rankColor = "§7";
                    break;
                case 3:
                    rankColor = "§c";
                    break;
                default:
                    rankColor = "§f";
                    break;
            }

            sender.sendMessage(rankColor + "#" + rank + " §f" + playerName + " §7- §a" + balance);
            rank++;
        }

        return true;
    }

    // ==================== ADMIN COMMANDS ====================

    private boolean handleGive(CommandSender sender, String[] args) {
        if (!sender.hasPermission("econoneeds.admin")) {
            sender.sendMessage("§cYou don't have permission to use this command.");
            return true;
        }

        if (args.length < 3) {
            sender.sendMessage("§cUsage: /eco give <player> <amount>");
            return true;
        }

        OfflinePlayer target = getOfflinePlayer(args[1]);
        if (target == null) {
            sender.sendMessage("§cPlayer not found: " + args[1]);
            return true;
        }

        double amount = parseAmount(sender, args[2]);
        if (amount < 0)
            return true;

        EconomyManager economy = EconomyManager.getInstance();
        double newBalance = economy.addBalance(target.getUniqueId(), amount);

        String playerName = target.getName() != null ? target.getName() : args[1];
        sender.sendMessage("§aGave " + EconomyManager.formatCurrency(amount) + " to §f" + playerName);
        sender.sendMessage("§7New balance: " + EconomyManager.formatCurrency(newBalance));

        if (target.isOnline()) {
            target.getPlayer()
                    .sendMessage("§aYou received " + EconomyManager.formatCurrency(amount) + " from the server.");
        }

        return true;
    }

    private boolean handleTake(CommandSender sender, String[] args) {
        if (!sender.hasPermission("econoneeds.admin")) {
            sender.sendMessage("§cYou don't have permission to use this command.");
            return true;
        }

        if (args.length < 3) {
            sender.sendMessage("§cUsage: /eco take <player> <amount>");
            return true;
        }

        OfflinePlayer target = getOfflinePlayer(args[1]);
        if (target == null) {
            sender.sendMessage("§cPlayer not found: " + args[1]);
            return true;
        }

        double amount = parseAmount(sender, args[2]);
        if (amount < 0)
            return true;

        EconomyManager economy = EconomyManager.getInstance();
        double currentBalance = economy.getBalance(target.getUniqueId());
        double actualTake = Math.min(amount, currentBalance);
        economy.removeBalance(target.getUniqueId(), actualTake);
        double newBalance = economy.getBalance(target.getUniqueId());

        String playerName = target.getName() != null ? target.getName() : args[1];
        sender.sendMessage("§cTook " + EconomyManager.formatCurrency(actualTake) + " from §f" + playerName);
        sender.sendMessage("§7New balance: " + EconomyManager.formatCurrency(newBalance));

        if (target.isOnline()) {
            target.getPlayer()
                    .sendMessage("§c" + EconomyManager.formatCurrency(actualTake) + " was deducted from your balance.");
        }

        return true;
    }

    private boolean handleSet(CommandSender sender, String[] args) {
        if (!sender.hasPermission("econoneeds.admin")) {
            sender.sendMessage("§cYou don't have permission to use this command.");
            return true;
        }

        if (args.length < 3) {
            sender.sendMessage("§cUsage: /eco set <player> <amount>");
            return true;
        }

        OfflinePlayer target = getOfflinePlayer(args[1]);
        if (target == null) {
            sender.sendMessage("§cPlayer not found: " + args[1]);
            return true;
        }

        double amount = parseAmount(sender, args[2]);
        if (amount < 0)
            return true;

        EconomyManager economy = EconomyManager.getInstance();
        economy.setBalance(target.getUniqueId(), amount);

        String playerName = target.getName() != null ? target.getName() : args[1];
        sender.sendMessage("§aSet §f" + playerName + "§a's balance to " + EconomyManager.formatCurrency(amount));

        if (target.isOnline()) {
            target.getPlayer().sendMessage("§eYour balance was set to " + EconomyManager.formatCurrency(amount));
        }

        return true;
    }

    private boolean handleCheck(CommandSender sender, String[] args) {
        if (!sender.hasPermission("econoneeds.admin")) {
            sender.sendMessage("§cYou don't have permission to use this command.");
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage("§cUsage: /eco check <player>");
            return true;
        }

        OfflinePlayer target = getOfflinePlayer(args[1]);
        if (target == null) {
            sender.sendMessage("§cPlayer not found: " + args[1]);
            return true;
        }

        EconomyManager economy = EconomyManager.getInstance();
        double balance = economy.getBalance(target.getUniqueId());

        String playerName = target.getName() != null ? target.getName() : args[1];
        sender.sendMessage("§6" + playerName + "§7's balance: §a" + EconomyManager.formatCurrency(balance));

        return true;
    }

    private boolean handleSell(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cThis command can only be used by players.");
            return true;
        }

        Player player = (Player) sender;
        ItemStack item = player.getInventory().getItemInMainHand();

        if (item == null || item.getType() == Material.AIR) {
            player.sendMessage("§cYou must be holding an item to sell.");
            return true;
        }

        // Check if item is sellable
        ItemPriceManager priceManager = ItemPriceManager.getInstance();
        if (!priceManager.isSellable(item.getType())) {
            player.sendMessage("§cThis item cannot be sold: §f" + item.getType().name());
            return true;
        }

        // Parse amount (default to 1 if not specified)
        int amount = 1;
        if (args.length >= 2) {
            try {
                amount = Integer.parseInt(args[1]);
                if (amount <= 0) {
                    player.sendMessage("§cAmount must be greater than zero.");
                    return true;
                }
            } catch (NumberFormatException e) {
                player.sendMessage("§cInvalid amount: " + args[1]);
                return true;
            }
        }

        if (item.getAmount() < amount) {
            player.sendMessage(
                    "§cYou don't have enough! You have §f" + item.getAmount() + "§c, tried to sell §f" + amount);
            return true;
        }

        // Calculate earnings
        double pricePerItem = priceManager.getPrice(item.getType());
        double totalEarnings = pricePerItem * amount;

        // Remove items from inventory
        int newAmount = item.getAmount() - amount;
        if (newAmount <= 0) {
            player.getInventory().setItemInMainHand(null);
        } else {
            item.setAmount(newAmount);
        }

        // Add money to player's balance
        EconomyManager economy = EconomyManager.getInstance();
        economy.addBalance(player.getUniqueId(), totalEarnings);

        // Send success message
        String itemName = item.getType().name().replace("_", " ").toLowerCase();
        player.sendMessage(
                "§aSold §f" + amount + "x " + itemName + "§a for " + EconomyManager.formatCurrency(totalEarnings));
        player.sendMessage("§7New balance: " + EconomyManager.formatCurrency(economy.getBalance(player.getUniqueId())));

        return true;
    }

    // ==================== UTILITY METHODS ====================

    private void sendUsage(CommandSender sender) {
        sender.sendMessage("§6=== Econoneeds Commands ===");
        sender.sendMessage("§e/eco bal §7- Check your balance");
        sender.sendMessage("§e/eco pay <player> <amount> §7- Send money");
        sender.sendMessage("§e/eco top §7- View richest players");
        sender.sendMessage("§e/eco sell [amount] §7- Sell item in hand");

        if (sender.hasPermission("econoneeds.admin")) {
            sender.sendMessage("§6--- Admin Commands ---");
            sender.sendMessage("§e/eco give <player> <amount> §7- Give money");
            sender.sendMessage("§e/eco take <player> <amount> §7- Take money");
            sender.sendMessage("§e/eco set <player> <amount> §7- Set balance");
            sender.sendMessage("§e/eco check <player> §7- Check player balance");
        }
    }

    private OfflinePlayer getOfflinePlayer(String name) {
        Player online = Bukkit.getPlayer(name);
        if (online != null)
            return online;

        @SuppressWarnings("deprecation")
        OfflinePlayer offline = Bukkit.getOfflinePlayer(name);

        if (offline.hasPlayedBefore() || offline.isOnline()) {
            return offline;
        }

        return null;
    }

    private double parseAmount(CommandSender sender, String amountStr) {
        try {
            double amount = Double.parseDouble(amountStr);
            if (amount <= 0) {
                sender.sendMessage("§cAmount must be greater than zero!");
                return -1;
            }
            return Math.round(amount * 100.0) / 100.0;
        } catch (NumberFormatException e) {
            sender.sendMessage("§cInvalid amount: " + amountStr);
            return -1;
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1) {
            List<String> subCommands = Arrays.asList("bal", "pay", "top", "sell");
            if (sender.hasPermission("econoneeds.admin")) {
                subCommands = Arrays.asList("bal", "pay", "top", "sell", "give", "take", "set", "check");
            }
            return subCommands.stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (args.length == 2) {
            String sub = args[0].toLowerCase();
            if (Arrays.asList("pay", "give", "take", "set", "check").contains(sub)) {
                return Bukkit.getOnlinePlayers().stream()
                        .map(Player::getName)
                        .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList());
            }
        }

        return Collections.emptyList();
    }
}
