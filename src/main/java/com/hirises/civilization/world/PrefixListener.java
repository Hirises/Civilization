package com.hirises.civilization.world;

import com.hirises.civilization.config.ConfigManager;
import com.hirises.civilization.data.PrefixType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public class PrefixListener implements Listener {
    @EventHandler
    public void onDeath(PlayerDeathEvent event){
        Player player = event.getPlayer();
        if(!ConfigManager.givenPrefix(PrefixType.First_Death)){
            ConfigManager.givePrefix(PrefixType.First_Death, player.getUniqueId());
        }

        Player killer = player.getKiller();
        if(!ConfigManager.givenPrefix(PrefixType.First_Kill)){
            if(killer != null && killer instanceof Player){
                ConfigManager.givePrefix(PrefixType.First_Kill, killer.getUniqueId());
            }
        }
    }
}
