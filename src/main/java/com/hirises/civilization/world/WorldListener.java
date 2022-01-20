package com.hirises.civilization.world;

import com.hirises.civilization.Civilization;
import com.hirises.civilization.config.ConfigManager;
import com.hirises.civilization.data.ChunkData;
import com.hirises.civilization.data.Structure;
import com.hirises.core.util.ItemUtil;
import com.hirises.core.util.Util;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
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
        try{
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
        }catch (Exception e) {
            Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "---------------------   경고!   ---------------------");
            Bukkit.getConsoleSender().sendMessage(ChatColor.DARK_RED + "플러그인을 실행하는 도중 오류가 발생하였습니다.");
            Bukkit.getConsoleSender().sendMessage(ChatColor.DARK_RED + "서버 폴더의 plugins/Civilization/Saves 폴더와 " +
                    "Civilization, Civilization_Nether, Civilization_TheEnd 폴더를 전부 삭제하신 후 다시 시도해보세요.");
            Bukkit.getConsoleSender().sendMessage(ChatColor.DARK_RED + "해당 현상이 반복되면 서버를 다시 생성해주세요");
            Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "--------------------------------------------------");
            e.printStackTrace();
            Bukkit.getPluginManager().disablePlugin(Civilization.getInst());
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
                    if(!NMSSupport.isConflict(block.getLocation(), "crack")){
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
