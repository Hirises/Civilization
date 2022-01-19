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
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityPortalEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.world.ChunkLoadEvent;

public class WorldListener implements Listener {


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
                Location location = Civilization.world_end.get().getSpawnLocation();
                NMSSupport.setBlocks(location, -2, -1, -2, 5, 1, 5, Material.OBSIDIAN);
                NMSSupport.setBlocks(location, -2, 0, -2, 5, 5, 5, Material.AIR);
                entity.teleport(location);
                break;
            }
        }
    }
}
