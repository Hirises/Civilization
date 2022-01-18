package com.hirises.civilization.player;

import com.hirises.civilization.Civilization;
import com.hirises.civilization.config.ConfigManager;
import com.hirises.civilization.config.Keys;
import com.hirises.civilization.gui.MainGUI;
import com.hirises.civilization.gui.PrizeViewGUI;
import com.hirises.core.data.TimeUnit;
import com.hirises.core.display.ScoreBoardHandler;
import com.hirises.core.event.GUIUpdateEvent;
import com.hirises.core.store.NBTTagStore;
import com.hirises.core.util.ItemUtil;
import com.hirises.core.util.Util;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPortalEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;
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

    public static boolean inValidWorld(Player player){
        World world = player.getWorld();
        return !Civilization.world.equals(world) && !Civilization.world_nether.equals(world) && !Civilization.world_end.equals(world);
    }

    @EventHandler
    public void onPortal(EntityPortalEvent event){
        if(!Civilization.isStart()){
            return;
        }
        Location location = event.getTo();
        switch (location.getWorld().getName()){
            case "world":
                location.setWorld(Civilization.world);
                break;
            case "world_nether":
                location.setWorld(Civilization.world_nether);
                break;
            case "world_end":
                location.setWorld(Civilization.world_end);
                break;
        }
        event.setTo(location);
    }

    @EventHandler
    public void onPortal(PlayerPortalEvent event){
        if(!Civilization.isStart()){
            return;
        }
        Location location = event.getTo();
        Util.logging(location);
        switch (location.getWorld().getName()){
            case "world":
                location.setWorld(Civilization.world);
                break;
            case "world_nether":
                location.setWorld(Civilization.world_nether);
                break;
            case "world_end":
                location.setWorld(Civilization.world_end);
                break;
        }
        event.setTo(location);
        Util.logging(location);
        event.setCanCreatePortal(true);
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
            event.setRespawnLocation(cache.getSpawn());
        }else{
            event.setRespawnLocation(spawn);
        }

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
