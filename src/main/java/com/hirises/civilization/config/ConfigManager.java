package com.hirises.civilization.config;

import com.hirises.civilization.Civilization;
import com.hirises.civilization.player.PlayerCache;
import com.hirises.core.data.GUIShapeUnit;
import com.hirises.core.data.ItemStackUnit;
import com.hirises.core.data.unit.DataCache;
import com.hirises.core.store.PlayerCacheStore;
import com.hirises.core.store.YamlStore;
import com.hirises.core.util.ItemUtil;
import com.hirises.core.util.Util;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class ConfigManager {
    public static YamlStore config = new YamlStore(Civilization.getInst(), "config.yml");
    public static int killRange;
    public static YamlStore cache = new YamlStore(Civilization.getInst(), "cache.yml");

    public static DataCache<GUIShapeUnit> menu = new DataCache<>(new YamlStore(Civilization.getInst(), "menu.yml"), "", GUIShapeUnit::new);

    public static PlayerCacheStore<PlayerCache> cacheStore;

    private static ItemStack moneyItem;

    public static void init(){
        config.load(true);
        cache.load(true);
        killRange = config.get(Integer.class, "현상금.범위");

        menu.load();

        cacheStore = new PlayerCacheStore<>(PlayerCache::new);
        cacheStore.checkExistAll();

        moneyItem = config.getOrDefault(new ItemStackUnit(), "돈").getItem();
    }

    public static PlayerCache getCache(UUID uuid){
        return cacheStore.get(uuid);
    }

    public static ItemStack getMoneyItem(long amount){
        return ItemUtil.remapString(moneyItem.clone(), Util.toRemap("amount", String.valueOf(amount)));
    }
}
