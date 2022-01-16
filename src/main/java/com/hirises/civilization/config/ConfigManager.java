package com.hirises.civilization.config;

import com.hirises.civilization.Civilization;
import com.hirises.civilization.player.PlayerCache;
import com.hirises.core.data.GUIShapeUnit;
import com.hirises.core.data.unit.DataCache;
import com.hirises.core.store.PlayerCacheStore;
import com.hirises.core.store.YamlStore;

public class ConfigManager {
    public static YamlStore cache = new YamlStore(Civilization.getInst(), "cache.yml", true);

    public static DataCache<GUIShapeUnit> menu = new DataCache<>(new YamlStore(Civilization.getInst(), "menu.yml", true), "", GUIShapeUnit::new);

    public static PlayerCacheStore<PlayerCache> cacheStore = new PlayerCacheStore<>(PlayerCache::new);

    public static void init(){
        cache.load(true);

        menu.load();

        cacheStore.checkExistAll();
    }
}
