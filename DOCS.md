Econoneeds is a lightweight economy plugin for Spigot and Paper servers running Minecraft 1.21.1 or higher.


FEATURES

Player balance tracking and management
Player to player money transfers
Server leaderboard showing richest players
Item selling system with configurable prices
Admin tools for managing player balances
Persistent data storage using player UUIDs


COMMANDS

Player Commands

/eco bal - Check your balance
/eco pay player amount - Send money to another player
/eco top - View the richest players on the server
/eco sell amount - Sell the item you are holding

Admin Commands (requires econoneeds.admin permission)

/eco give player amount - Give money to a player
/eco take player amount - Take money from a player
/eco set player amount - Set a player balance
/eco check player - Check a player balance


PERMISSIONS

econoneeds.admin - Grants access to admin commands (default: op)


CONFIGURATION

Item sell prices can be configured in plugins/Econoneeds/prices.yml which is automatically generated on first startup with default prices for common items including ores, wood, crops, and mob drops. Edit this file to add new items or change prices, then restart your server to apply changes.

Player balances are stored in plugins/Econoneeds/economy.yml using player UUIDs so balances persist even if players change their name.


INSTALLATION

Download the jar file and place it in your server plugins folder. Restart your server. Configuration files will be generated automatically.
