package com.hirises.civilization.player;

import com.hirises.civilization.Civilization;
import com.hirises.civilization.config.ConfigManager;
import com.hirises.civilization.config.Keys;
import com.hirises.civilization.gui.MainGUI;
import com.hirises.civilization.gui.PrizeViewGUI;
import com.hirises.civilization.util.ChunkData;
import com.hirises.civilization.util.NetherPortal;
import com.hirises.civilization.util.Structure;
import com.hirises.core.data.TimeUnit;
import com.hirises.core.display.ScoreBoardHandler;
import com.hirises.core.event.GUIUpdateEvent;
import com.hirises.core.store.NBTTagStore;
import com.hirises.core.util.ItemUtil;
import com.hirises.core.util.Util;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityPortalEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.Objective;

import java.util.Random;
import java.util.UUID;

public class PlayerListener implements Listener {
    @EventHandler
    public void onJoin(PlayerJoinEvent event){
        Player player = event.getPlayer();

        if(Civilization.isStart()){
            if(player.getWorld() == null || inValidWorld(player)){
                //새로 입장시
                Civilization.resetPlayer(player);
                Civilization.prepareNewPlayer(player);
                Civilization.getNewSpawnPoint(player, false);
                ConfigManager.cacheStore.save(player);
                ConfigManager.cache.save();
                ConfigManager.saveStructure();
            }else{
                //그냥 입장시
                Objective board = ScoreBoardHandler.getOrNew(player);
                board.setDisplayName("Civilization");
                ScoreBoardHandler.show(player, board);
                updateScoreBoard(player);
            }
        }else{
            //아직 시작 안함
            if(player.getWorld() == null){  //예전 데이터 남아있으면
                Civilization.resetPlayer(player);
                player.teleport(Bukkit.getWorld("world").getSpawnLocation());
            }
        }
    }

    @EventHandler
    public void chunkLoading(ChunkLoadEvent event){
        Chunk chunk = event.getChunk();
        ChunkData chunkData = new ChunkData(chunk.getWorld().getName(), chunk.getX(), chunk.getZ());
        Util.logging(chunkData);
        if(ConfigManager.isConflict(chunkData, "")){
            Structure structure = ConfigManager.structureList.get(chunkData);
            if(!structure.isPlaced() && structure.getMinX() == chunkData.getX() && structure.getMinZ() == chunkData.getZ()){
                structure.place();
            }
        }
    }

    public static boolean inValidWorld(Player player){
        World world = player.getWorld();
        return !Civilization.world.get().equals(world) && !Civilization.world_nether.get().equals(world) && !Civilization.world_end.get().equals(world);
    }

    @EventHandler
    public void disablePortal(PlayerInteractEvent event){
        if(event.getAction().equals(Action.RIGHT_CLICK_BLOCK)){
            if(ItemUtil.isExist(event.getItem()) && event.getItem().getType().equals(Material.ENDER_EYE)){
                Block block = event.getClickedBlock();
                if(block != null && block.getType().equals(Material.END_PORTAL_FRAME)){
                    if(!ConfigManager.isConflict(block.getWorld().getName(), block.getLocation(), "crack")){
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
        switch (event.getTo().getWorld().getName()){
            case "world_nether":{
                if(event.getFrom().getWorld().getName().equalsIgnoreCase("Civilization_Nether")){
                    event.getEntity().teleport(NetherPortal.getPortal(Civilization.world.getName(), event.getFrom()));
                }else{
                    event.getEntity().teleport(NetherPortal.getPortal(Civilization.world_nether.getName(), event.getFrom()));
                }
                break;
            }
            case "world_the_end":{
                Location location = Civilization.world_end.get().getSpawnLocation();
                setBlocks(location, -2, -1, -2, 5, 1, 5, Material.OBSIDIAN);
                setBlocks(location, -2, 0, -2, 5, 5, 5, Material.AIR);
                event.getEntity().teleport(location);
                break;
            }
        }
    }

    @EventHandler
    public void onPortal(PlayerPortalEvent event){
        if(!Civilization.isStart()){
            return;
        }
        event.setCancelled(true);
        switch (event.getTo().getWorld().getName()){
            case "world_nether":{
                if(event.getFrom().getWorld().getName().equalsIgnoreCase("Civilization_Nether")){
                    event.getPlayer().teleport(NetherPortal.getPortal(Civilization.world.getName(), event.getFrom()));
                }else{
                    event.getPlayer().teleport(NetherPortal.getPortal(Civilization.world_nether.getName(), event.getFrom()));
                }
                break;
            }
            case "world_the_end":{
                Location location = Civilization.world_end.get().getSpawnLocation();
                setBlocks(location, -2, -1, -2, 5, 1, 5, Material.OBSIDIAN);
                setBlocks(location, -2, 0, -2, 5, 5, 5, Material.AIR);
                event.getPlayer().teleport(location);
                break;
            }
        }
    }

    private static void setBlocks(Location location, int x, int y, int z, int dx, int dy, int dz, Material material){
        for(int curX = x; curX < x + dx; curX++){
            for(int curY = y; curY < y + dy; curY++){
                for(int curZ = z; curZ < z + dz; curZ++){
                    location.clone().add(curX, curY, curZ).getBlock().setType(material);
                }
            }
        }
    }

   public static void updateScoreBoard(UUID uuid){
        Player player = Bukkit.getPlayer(uuid);
        if(player != null){
            updateScoreBoard(player);
        }
    }

    public static void updateScoreBoard(Player player){
        PlayerCache cache = ConfigManager.getCache(player.getUniqueId());
        Objective board = ScoreBoardHandler.getOrNew(player);
        ScoreBoardHandler.setLine(board, 0, "돈: " + cache.getMoney());
        ScoreBoardHandler.show(player, board);
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event){
        if(!Civilization.isStart()){
            return;
        }
        if(event.getAction().isRightClick()){
            Player player = event.getPlayer();
            ItemStack item = player.getInventory().getItemInMainHand();
            if(ItemUtil.isExist(item) && NBTTagStore.containKey(item, Keys.MoneyItem.toString())){
                ConfigManager.getCache(player.getUniqueId()).operateMoney(NBTTagStore.get(item, Keys.MoneyItem.toString(), Long.class));
                player.getInventory().setItemInMainHand(ItemUtil.operateAmount(item, -1));
            }
        }
    }

    @EventHandler
    public void onShift_F(PlayerSwapHandItemsEvent event){
        if(!Civilization.isStart()){
            return;
        }
        Player player = event.getPlayer();
        if(player.isSneaking()){
            event.setCancelled(true);
            new MainGUI().open(player);
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event){
        if(!Civilization.isStart()){
            return;
        }
        Player player = event.getPlayer();
        PlayerCache cache = ConfigManager.getCache(player.getUniqueId());
        World world = player.getWorld();
        Location location = player.getLocation();
        ItemStack[] content = player.getInventory().getContents();
        int min = ConfigManager.config.get(Integer.class, "사망아이템드롭.최소");
        int max = ConfigManager.config.get(Integer.class, "사망아이템드롭.최대");
        int count = (new Random()).nextInt(max - min) + min;
        for(int i = 0; i < content.length; i++){
            int ran = (new Random()).nextInt(content.length);
            ItemStack drop = content[ran];
            if(ItemUtil.isExist(drop)){
                world.dropItemNaturally(location, drop);
                content[ran] = null;
                count--;
            }

            if(count <= 0){
                break;
            }
        }
        player.getInventory().setContents(content);

        Player killer = player.getKiller();
        if(killer == null || killer.equals(player)){
            return;
        }
        PlayerCache killerCache = ConfigManager.getCache(killer.getUniqueId());
        long killRewardModifier = cache.getKillRewardModifier();
        cache.operateMoney(ConfigManager.config.get(Long.class, "사망골드") - killRewardModifier);
        killerCache.operateMoney(ConfigManager.config.get(Long.class, "킬골드") + killRewardModifier);
        cache.reduceKill();
        killerCache.addKill();

        Bukkit.getPluginManager().callEvent(new GUIUpdateEvent(null, PrizeViewGUI.class, false));
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event){
        if(!Civilization.isStart()){
            return;
        }
        Player player = event.getPlayer();
        Location spawn = player.getBedSpawnLocation();
        if(spawn == null){
            PlayerCache cache = ConfigManager.getCache(player.getUniqueId());
            spawn = cache.getSpawn();
            spawn.getBlock().setType(Material.AIR);
            spawn.clone().add(0, 1, 0).getBlock().setType(Material.AIR);
        }
        event.setRespawnLocation(spawn);

        Bukkit.getScheduler().runTaskLater(Civilization.getInst(), () -> {
            int invincibleTime = (int) ConfigManager.config.getOrDefault(new TimeUnit(), "리스폰무적").getTick();

            player.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, invincibleTime, 0, false, false, true));
            player.addPotionEffect(new PotionEffect(PotionEffectType.WATER_BREATHING, invincibleTime, 0, false, false, true));
            player.addPotionEffect(new PotionEffect(PotionEffectType.SATURATION, invincibleTime, 2, false, false, true));
            player.addPotionEffect(new PotionEffect(PotionEffectType.FAST_DIGGING, invincibleTime, 2, false, false, true));
            player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, invincibleTime, 2, false, false, true));
            player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, invincibleTime, 9, false, false, true));
        }, 1);
    }
}
