package com.hirises.civilization.player;

import com.destroystokyo.paper.event.player.PlayerJumpEvent;
import com.hirises.civilization.Civilization;
import com.hirises.civilization.config.ConfigManager;
import com.hirises.civilization.config.Keys;
import com.hirises.civilization.gui.MainGUI;
import com.hirises.civilization.gui.PrizeViewGUI;
import com.hirises.civilization.world.NMSSupport;
import com.hirises.core.data.TimeUnit;
import com.hirises.core.display.Display;
import com.hirises.core.display.ScoreBoardHandler;
import com.hirises.core.event.GUIUpdateEvent;
import com.hirises.core.store.NBTTagStore;
import com.hirises.core.util.ItemUtil;
import com.hirises.core.util.Util;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.Objective;
import org.checkerframework.checker.units.qual.C;

import java.util.*;

public class PlayerHandler implements Listener {

    //region method

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

    //endregion

    //region events

    @EventHandler
    public void onJoin(PlayerJoinEvent event){
        Player player = event.getPlayer();

        if(Civilization.isStart()){
            if(player.getWorld() == null || NMSSupport.inValidWorld(player)){
                //새로 입장시
                Civilization.resetPlayer(player);
                Civilization.prepareNewPlayer(player);
                Civilization.getNewSpawnPoint(player, false);
                ConfigManager.cacheStore.save(player);
                ConfigManager.cache.save();
                ConfigManager.saveStructures();
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
    public void run(PlayerMoveEvent event){
        if(!Civilization.isStart()){
            return;
        }
        Player player = event.getPlayer();
        if(player.isSwimming()){
            PlayerCache cache = ConfigManager.getCache(player.getUniqueId());
            cache.operateStamina(ConfigManager.StaminaData.swimmingStamina);
        }
        if(player.isSprinting()){
            PlayerCache cache = ConfigManager.getCache(player.getUniqueId());
            cache.operateStamina(ConfigManager.StaminaData.runStamina);
        }
    }

    @EventHandler
    public void jump(PlayerJumpEvent event){
        if(!Civilization.isStart()){
            return;
        }
        Player player = event.getPlayer();
        PlayerCache cache = ConfigManager.getCache(player.getUniqueId());
        cache.operateStamina(ConfigManager.StaminaData.jumpStamina);
    }

    @EventHandler
    public void mining(BlockBreakEvent event){
        if(!Civilization.isStart()){
            return;
        }
        Player player = event.getPlayer();
        PlayerCache cache = ConfigManager.getCache(player.getUniqueId());
        cache.operateStamina(ConfigManager.StaminaData.miningStamina);
    }

    @EventHandler
    public void drinking(PlayerItemConsumeEvent event){
        ItemStack item = event.getItem();
        Player player = event.getPlayer();

        if (ItemUtil.isExist(item)) {
            if(NBTTagStore.containKey(item, Keys.StaminaHeal.toString())) {
                ConfigManager.getCache(player.getUniqueId()).operateStamina(NBTTagStore.get(item, Keys.StaminaHeal.toString(), Integer.class));
            }else if (ConfigManager.StaminaData.staminaHealMap.containsKey(item.getType())) {
                ConfigManager.getCache(player.getUniqueId()).operateStamina(ConfigManager.StaminaData.staminaHealMap.get(item.getType()));
            }else if (item.hasItemMeta() && item.getItemMeta() instanceof PotionMeta) {
                ConfigManager.getCache(player.getUniqueId()).operateStamina(ConfigManager.StaminaData.drinkingStamina);
            }
        }
    }

    @EventHandler
    public void attackAndDealt(EntityDamageByEntityEvent event){
        if(!Civilization.isStart()){
            return;
        }
        Entity damager = event.getDamager();
        Entity taker = event.getEntity();
        if(damager instanceof Player){
            PlayerCache cache = ConfigManager.getCache(damager.getUniqueId());
            cache.operateStamina(ConfigManager.StaminaData.attackStamina);
        }
        if(taker instanceof Player){
            PlayerCache cache = ConfigManager.getCache(taker.getUniqueId());
            cache.operateStamina(ConfigManager.StaminaData.hitStamina);
        }
    }

    @EventHandler
    public void useMoneyItem(PlayerInteractEvent event){
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
    public void openQuickMenu(PlayerSwapHandItemsEvent event){
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
        int count = (new Random()).nextInt(max - min + 1) + min;

        List<Integer> flipBag = new ArrayList<>();
        for(int i = 0; i < content.length; i++){
            flipBag.add(i);
        }
        Random random = new Random();
        for(int i = 0; i < content.length; i++){
            int index = random.nextInt(flipBag.size());
            int ran = flipBag.get(index);
            flipBag.remove(index);

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

    //endregion
}
