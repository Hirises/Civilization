package com.hirises.civilization.player;

import com.hirises.civilization.config.ConfigManager;
import com.hirises.core.store.IPlayerCache;
import org.bukkit.Location;

import java.util.UUID;

public class PlayerCache implements IPlayerCache {
    private UUID uuid;
    private long money;
    private int kill;
    private Location spawn;

    public PlayerCache(UUID uuid){
        this.uuid = uuid;
    }

    @Override
    public void onLoad() {
        long defaultMoney = ConfigManager.config.get(Long.class, "기본금");
        this.spawn = ConfigManager.cache.getConfig().getLocation(uuid.toString() + ".스폰");
        this.money = ConfigManager.cache.getOrDefault(Long.class, defaultMoney, uuid.toString() + ".돈");
        this.kill = ConfigManager.cache.getOrDefault(Integer.class, 0, uuid.toString() + ".킬");
    }

    @Override
    public void onSave() {
        ConfigManager.cache.getConfig().set(uuid.toString() + ".스폰", this.spawn);
        ConfigManager.cache.set(uuid.toString() + ".돈", this.money);
        ConfigManager.cache.set(uuid.toString() + ".킬", this.kill);
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
}
