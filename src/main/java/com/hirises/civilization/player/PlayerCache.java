package com.hirises.civilization.player;

import com.hirises.civilization.Civilization;
import com.hirises.civilization.config.ConfigManager;
import com.hirises.core.display.Display;
import com.hirises.core.store.IPlayerCache;
import com.hirises.core.util.Util;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Set;
import java.util.UUID;

public class PlayerCache implements IPlayerCache {
    private UUID uuid;
    private long money;
    private int kill;
    private Location spawn;
    private int stamina;
    private Player player;

    public PlayerCache(UUID uuid){
        this.uuid = uuid;
    }

    @Override
    public void onLoad() {
        long defaultMoney = ConfigManager.config.get(Long.class, "기본금");
        this.spawn = ConfigManager.cache.getConfig().getLocation(uuid.toString() + ".스폰");
        this.money = ConfigManager.cache.getOrDefault(Long.class, defaultMoney, uuid.toString() + ".돈");
        this.kill = ConfigManager.cache.getOrDefault(Integer.class, 0, uuid.toString() + ".킬");
        this.stamina = ConfigManager.cache.getOrDefault(Integer.class, ConfigManager.StaminaData.defaultStamina, uuid.toString() + ".스테미나");
        this.player = Bukkit.getPlayer(uuid);
    }

    @Override
    public void onSave() {
        ConfigManager.cache.getConfig().set(uuid.toString() + ".스폰", this.spawn);
        ConfigManager.cache.set(uuid.toString() + ".돈", this.money);
        ConfigManager.cache.set(uuid.toString() + ".킬", this.kill);
        ConfigManager.cache.set(uuid.toString() + ".스테미나", this.stamina);
    }

    @Override
    public void onUnload() {

    }

    @Override
    public void onRemove() {
        ConfigManager.cache.removeKey(uuid.toString());
    }

    public Location getSpawn() {
        return spawn;
    }

    public void setSpawn(Location spawn) {
        this.spawn = spawn;
    }

    public void operateMoney(long amount){
        setMoney(getMoney() + amount);
    }

    public boolean hasMoney(long amount){
        return getMoney() >= amount;
    }

    public long getMoney() {
        return money;
    }

    public void setMoney(long money) {
        this.money = money;
        PlayerHandler.updateScoreBoard(uuid);
    }

    public int getKill() {
        return kill;
    }

    public void setKill(int kill) {
        this.kill = kill;
    }

    public void addKill(){
        if(getKill() < 0){
            setKill(0);
        }else if(getKill() < ConfigManager.killRange){
            setKill(getKill() + 1);
        }
    }

    public void reduceKill(){
        if(getKill() > 0){
            setKill(0);
        }else if(getKill() > -ConfigManager.killRange){
            setKill(getKill() - 1);
        }
    }

    public long getKillRewardModifier(){
        return ConfigManager.config.get(Long.class, "현상금." + kill);
    }

    public int getStamina() {
        return stamina;
    }

    public void setStamina(int stamina) {
        if(stamina < 0){
            stamina = 0;
        }
        if(stamina > ConfigManager.StaminaData.defaultStamina){
            stamina = ConfigManager.StaminaData.defaultStamina;
        }
        this.stamina = stamina;
        if(player != null){
            if(this.stamina <= ConfigManager.StaminaData.debuff2Stamina){
                Bukkit.getScheduler().runTask(Civilization.getInst(), () -> {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 40, 1, false, false, true));
                    player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING, 40, 1, false, false, true));
                    player.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 40, 1, false, false, true));
                    player.addPotionEffect(new PotionEffect(PotionEffectType.HUNGER, 40, 1, false, false, true));
                });
                Display.sendDisplayUnit(player, ConfigManager.StaminaData.staminaActionBar, Util.toRemap("amount", getStaminaBar(ChatColor.RED, this.stamina)));
            }else if(this.stamina <= ConfigManager.StaminaData.debuff1Stamina){
                Bukkit.getScheduler().runTask(Civilization.getInst(), () -> {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 40, 0, false, false, true));
                    player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING, 40, 0, false, false, true));
                    player.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 40, 0, false, false, true));
                });
                Display.sendDisplayUnit(player, ConfigManager.StaminaData.staminaActionBar, Util.toRemap("amount", getStaminaBar(ChatColor.YELLOW, this.stamina)));
            }else if(this.stamina <= ConfigManager.StaminaData.buffStamina){
                Display.sendDisplayUnit(player, ConfigManager.StaminaData.staminaActionBar, Util.toRemap("amount", getStaminaBar(ChatColor.GREEN, this.stamina)));
            }else{
                Display.sendDisplayUnit(player, ConfigManager.StaminaData.staminaActionBar, Util.toRemap("amount", getStaminaBar(ChatColor.AQUA, this.stamina)));
            }
        }
    }

    private String getStaminaBar(ChatColor color, int amount){
        StringBuilder builder = new StringBuilder();
        builder.append(color);
        amount = (amount * ConfigManager.StaminaData.staminaBarLength) / ConfigManager.StaminaData.defaultStamina;
        for(int i = 0; i < ConfigManager.StaminaData.staminaBarLength; i++){
            if(i < amount){
                builder.append("|");
            }else{
                break;
            }
        }
        return builder.toString();
    }

    public void operateStamina(int amount){
        setStamina(getStamina() + amount);
    }
}
