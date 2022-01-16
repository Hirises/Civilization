package com.hirises.civilization.player;

import com.hirises.civilization.gui.MainGUI;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;

public class PlayerListener implements Listener {
    @EventHandler
    public void onShift_F(PlayerSwapHandItemsEvent event){
        Player player = event.getPlayer();
        if(player.isSneaking()){
            event.setCancelled(true);
            new MainGUI().open(player);
        }
    }
}
