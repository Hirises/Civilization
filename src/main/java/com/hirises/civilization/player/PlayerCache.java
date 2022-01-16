package com.hirises.civilization.player;

import com.hirises.civilization.config.ConfigManager;
import com.hirises.core.store.IPlayerCache;
import com.hirises.core.util.Util;

import java.util.UUID;

public class PlayerCache implements IPlayerCache {
    private UUID uuid;
    private long money;

    public PlayerCache(UUID uuid){
        this.uuid = uuid;
    }

    @Override
    public void onLoad() {
        long defaultMoney = ConfigManager.config.get(Long.class, "기본금");
        this.money = ConfigManager.cache.getOrDefault(Long.class, defaultMoney, uuid.toString() + ".돈");
    }

    @Override
    public void onSave() {
        ConfigManager.cache.set(uuid.toString() + ".돈", this.money);
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
}
