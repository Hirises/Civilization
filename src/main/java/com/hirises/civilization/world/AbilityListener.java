package com.hirises.civilization.world;

import com.hirises.civilization.Civilization;
import com.hirises.civilization.config.ConfigManager;
import com.hirises.civilization.config.Keys;
import com.hirises.civilization.data.AbilityType;
import com.hirises.civilization.player.PlayerCache;
import com.hirises.core.display.Display;
import com.hirises.core.store.MetaDataStore;
import com.hirises.core.util.Pair;
import com.hirises.core.util.Util;
import org.bukkit.Bukkit;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.Breedable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityBreedEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.spigotmc.event.entity.EntityMountEvent;

public class AbilityListener implements Listener {
    @EventHandler
    public void onCraft(CraftItemEvent event){
        if(!Civilization.isStart()){
            return;
        }
        Player player = (Player) event.getWhoClicked();
        PlayerCache cache = ConfigManager.getCache(player.getUniqueId());
        if(!cache.checkAbilityLevel(ConfigManager.craftLimitMap, event.getRecipe().getResult().getType())){
            event.setCancelled(true);
            Pair<AbilityType, Integer> reason = cache.getLackAbilityLevel(ConfigManager.craftLimitMap, event.getRecipe().getResult().getType());
            Display.sendDisplayUnit(player, ConfigManager.lackLevelMessage,
                    Util.toRemap("type", reason.getLeft().getName(), "level", String.valueOf(reason.getRight())));
        }
    }

    @EventHandler
    public void onClick(PlayerInteractEvent event){
        if(!Civilization.isStart()){
            return;
        }
        if(event.getAction().equals(Action.RIGHT_CLICK_BLOCK)){
            Player player = event.getPlayer();
            PlayerCache cache = ConfigManager.getCache(player.getUniqueId());
            if(!cache.checkAbilityLevel(ConfigManager.rightClickLimitMap, event.getClickedBlock().getType())){
                event.setCancelled(true);
                Pair<AbilityType, Integer> reason = cache.getLackAbilityLevel(ConfigManager.rightClickLimitMap, event.getClickedBlock().getType());
                Display.sendDisplayUnit(player, ConfigManager.lackLevelMessage,
                        Util.toRemap("type", reason.getLeft().getName(), "level", String.valueOf(reason.getRight())));
            }
        }
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent event){
        if(!Civilization.isStart()){
            return;
        }
        Player player = event.getPlayer();
        PlayerCache cache = ConfigManager.getCache(player.getUniqueId());
        if(!cache.checkAbilityLevel(ConfigManager.placeLimitMap, event.getItemInHand().getType())){
            event.setCancelled(true);
            Pair<AbilityType, Integer> reason = cache.getLackAbilityLevel(ConfigManager.placeLimitMap, event.getItemInHand().getType());
            Display.sendDisplayUnit(player, ConfigManager.lackLevelMessage,
                    Util.toRemap("type", reason.getLeft().getName(), "level", String.valueOf(reason.getRight())));
        }
    }

    @EventHandler
    public void onBreed(EntityBreedEvent event){
        if(!Civilization.isStart()){
            return;
        }
        if(event.getBreeder() instanceof Player){
            Player player = (Player) event.getBreeder();
            PlayerCache cache = ConfigManager.getCache(player.getUniqueId());
            final Entity entity = event.getEntity();
            if(!cache.checkAbilityLevel(ConfigManager.entityRightClickLimitMap, entity.getType())){
                event.setCancelled(true);
                if(((Breedable)event.getMother()).canBreed()){
                    Pair<AbilityType, Integer> reason = cache.getLackAbilityLevel(ConfigManager.entityRightClickLimitMap, event.getEntity().getType());
                    Display.sendDisplayUnit(player, ConfigManager.lackLevelMessage,
                            Util.toRemap("type", reason.getLeft().getName(), "level", String.valueOf(reason.getRight())));
                    Breedable father = ((Breedable)event.getFather());
                    Breedable mother = ((Breedable)event.getMother());
                    father.setBreed(false);
                    mother.setBreed(false);
                    Bukkit.getScheduler().runTaskLater(Civilization.getInst(), () -> {
                        father.setBreed(true);
                        mother.setBreed(true);
                    }, 5);
                }
            }
        }
    }

    @EventHandler
    public void onMount(EntityMountEvent event){
        if(!Civilization.isStart()){
            return;
        }
        if(event.getEntity() instanceof Player){
            Player player = (Player) event.getEntity();
            PlayerCache cache = ConfigManager.getCache(player.getUniqueId());
            final Entity entity = event.getMount();
            if(!cache.checkAbilityLevel(ConfigManager.entityRightClickLimitMap, entity.getType())){
                event.setCancelled(true);
                Pair<AbilityType, Integer> reason = cache.getLackAbilityLevel(ConfigManager.entityRightClickLimitMap, entity.getType());
                Display.sendDisplayUnit(player, ConfigManager.lackLevelMessage,
                        Util.toRemap("type", reason.getLeft().getName(), "level", String.valueOf(reason.getRight())));
            }
        }
    }
}
