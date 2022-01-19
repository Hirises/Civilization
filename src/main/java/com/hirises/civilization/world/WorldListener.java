package com.hirises.civilization.world;

import com.hirises.civilization.Civilization;
import com.hirises.civilization.config.ConfigManager;
import com.hirises.civilization.data.ChunkData;
import com.hirises.civilization.data.Structure;
import com.hirises.core.util.ItemUtil;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityPortalEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.world.ChunkLoadEvent;

import java.util.UUID;

public class WorldListener implements Listener {

    public static UUID LastHitEnderDragon;

    @EventHandler
    public void onEnderDragonKilled(EntityDeathEvent event){
        if(!Civilization.isRunning()){
            return;
        }
        Entity entity = event.getEntity();
        if(entity.getType().equals(EntityType.ENDER_DRAGON)){
            Civilization.endGame(LastHitEnderDragon);
        }
    }

    @EventHandler
    public void onEnderDragonKilled(EntityDamageByEntityEvent event){
        if(!Civilization.isRunning()){
            return;
        }
        Entity entity = event.getEntity();
        if(entity.getType().equals(EntityType.ENDER_DRAGON)){
            Entity damager = event.getDamager();
            if(damager instanceof Player){
                LastHitEnderDragon = damager.getUniqueId();
            }
        }
    }

    @EventHandler
    public void onEnderDragonKilled(EntityDamageByBlockEvent event){
        if(!Civilization.isRunning()){
            return;
        }
        Entity entity = event.getEntity();
        if(entity.getType().equals(EntityType.ENDER_DRAGON)){
            if(entity.isDead()){
                Civilization.endGame(LastHitEnderDragon);
            }
        }
    }

    @EventHandler
    public void chunkLoading(ChunkLoadEvent event){
        if(!Civilization.isStart()){
            return;
        }
        Chunk chunk = event.getChunk();
        ChunkData chunkData = new ChunkData(chunk.getWorld().getName(), chunk.getX(), chunk.getZ());
        if(NMSSupport.isConflict(chunkData, "")){
            Structure structure = ConfigManager.structureList.get(chunkData);
            if(!structure.isPlaced() && structure.getMinChunk().equals(chunkData)){
                structure.place();
            }
        }
    }

    @EventHandler
    public void disablePortal(PlayerInteractEvent event){
        if(!Civilization.isStart()){
            return;
        }
        if(event.getAction().equals(Action.RIGHT_CLICK_BLOCK)){
            if(ItemUtil.isExist(event.getItem()) && event.getItem().getType().equals(Material.ENDER_EYE)){
                Block block = event.getClickedBlock();
                if(block != null && block.getType().equals(Material.END_PORTAL_FRAME)){
                    if(!NMSSupport.isConflict(block.getWorld().getName(), block.getLocation(), "crack")){
                        event.setCancelled(true);
                    }
                }
            }
        }
    }

    @EventHandler
    public void onPortal(EntityPortalEvent event){
        if(!Civilization.isStart()){
            return;
        }
        event.setCancelled(true);

        runPortal(event.getEntity(), event.getFrom(), event.getTo().getWorld().getName());
    }

    @EventHandler
    public void onPortal(PlayerPortalEvent event){
        if(!Civilization.isStart()){
            return;
        }
        event.setCancelled(true);

        runPortal(event.getPlayer(), event.getFrom(), event.getTo().getWorld().getName());
    }

    private static void runPortal(Entity entity, Location from, String targetWorld){
        switch (targetWorld){
            case "world_nether":{
                if(from.getWorld().getName().equalsIgnoreCase("Civilization_Nether")){
                    entity.teleport(NetherPortal.getPortal(Civilization.world.getName(), from));
                }else{
                    entity.teleport(NetherPortal.getPortal(Civilization.world_nether.getName(), from));
                }
                break;
            }
            case "world_the_end":{
                Location location = new Location(Civilization.world_end.get(), 100.5, 49,0.5);
                NMSSupport.setBlocks(location, -2, -1, -2, 5, 1, 5, Material.OBSIDIAN);
                NMSSupport.setBlocks(location, -2, 0, -2, 5, 4, 5, Material.AIR);
                entity.teleport(location);
                if(entity instanceof Player){
                    if(LastHitEnderDragon == null){
                        LastHitEnderDragon = entity.getUniqueId();
                    }
                }
                break;
            }
        }
    }
}
