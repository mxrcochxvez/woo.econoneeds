package com.worldoforbis.listeners;

import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;

import com.worldoforbis.utils.Utils;

public class PlayerListener implements Listener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        event.getPlayer()
                .sendMessage(Utils.template("Welcome to the server %s", event.getPlayer().getName()));
    }
}
