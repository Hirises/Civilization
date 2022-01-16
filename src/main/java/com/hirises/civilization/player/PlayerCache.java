package com.hirises.civilization.player;

import com.hirises.civilization.config.ConfigManager;
import com.hirises.core.store.IPlayerCache;

import java.util.UUID;

public class PlayerCache implements IPlayerCache {
    private UUID uuid;
    private long money;
    private int kill;

    public PlayerCache(UUID uuid){
        this.uuid = uuid;
    }

    @Override
    public void onLoad() {
        long defaultMoney = ConfigManager.config.get(Long.class, "기본금");
        this.money = ConfigManager.cache.getOrDefault(Long.class, defaultMoney, uuid.toString() + ".돈");
        this.kill = ConfigManager.cache.getOrDefault(Integer.class, 0, uuid.toString() + ".킬");
    }

    @Override
    public void onSave() {
        ConfigManager.cache.set(uuid.toString() + ".돈", this.money);
        ConfigManager.cache.set(uuid.toString() + ".킬", this.kill);
    }

    @Override
    public void onUnload() {

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
        PlayerListener.updateScoreBoard(uuid);
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
