package com.hirises.civilization.config;

import com.hirises.civilization.Civilization;
import com.hirises.civilization.gui.FreeShopItemUnit;
import com.hirises.civilization.player.PlayerCache;
import com.hirises.civilization.util.Structure;
import com.hirises.core.data.GUIShapeUnit;
import com.hirises.core.data.ItemStackUnit;
import com.hirises.core.data.LootTableUnit;
import com.hirises.core.data.unit.DataCache;
import com.hirises.core.data.unit.DirDataCache;
import com.hirises.core.store.PlayerCacheStore;
import com.hirises.core.store.YamlStore;
import com.hirises.core.util.ItemUtil;
import com.hirises.core.util.Util;
import com.sk89q.worldedit.math.BlockVector3;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class ConfigManager {
    public static YamlStore config = new YamlStore(Civilization.getInst(), "config.yml");
    public static int killRange;
    public static DataCache<GUIShapeUnit> menu = new DataCache<>(new YamlStore(Civilization.getInst(), "menu.yml"), "", GUIShapeUnit::new);
    public static PlayerCacheStore<PlayerCache> cacheStore;

    public static YamlStore cache = new YamlStore(Civilization.getInst(), "Saves/cache.yml");
    public static YamlStore data = new YamlStore(Civilization.getInst(), "Saves/data.yml");
    public static YamlStore state = new YamlStore(Civilization.getInst(), "Saves/state.yml");
    public static DirDataCache lootTable = new DirDataCache(Civilization.getInst(), "Schematics", LootTableUnit::new);
    public static List<FreeShopItemUnit> shopItem = new ArrayList<>();
    public static final Map<String, List<Structure>> structureList = new HashMap<>();

    private static ItemStack moneyItem;

    public static void init(){
        state.load(true);
        config.load(true);
        cache.load(true);
        data.load(true);
        lootTable.load();
        killRange = config.get(Integer.class, "현상금.범위");
        for(String key : data.getKeys("자유시장")){
            shopItem.add(data.getOrDefault(new FreeShopItemUnit(), "자유시장." + key));
        }
        structureList.put("Civilization", new ArrayList<>());
        structureList.put("Civilization_Nether", new ArrayList<>());
        structureList.put("Civilization_TheEnd", new ArrayList<>());
        for(String key : data.getKeys("구조물")){
            String world = data.get(String.class, "구조물." + key + ".world");
            structureList.get(world).add(data.getOrDefault(new Structure(), "구조물." + key));
        }

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

    public static void saveShopItem(){
        data.removeKey("자유시장");
        int i = 0;
        for(FreeShopItemUnit item : shopItem){
            data.upsert(item, "자유시장." + i);
            i++;
        }
    }

    public static void saveStructure(){
        int i = 0;
        for(String world : structureList.keySet()){
            for(Structure structure : structureList.get(world)){
                data.upsert(structure, "구조물." + i);
                i++;
            }
        }
        data.save();
    }

    public static void addStructure(String type, String world, Location loc1, Location loc2){
        structureList.get(world).add(new Structure(type, world, loc1, loc2));
    }

    public static boolean isConflict(String world, BlockVector3 vector3){
        return isConflict(world, new Location(Civilization.world.get(), vector3.getBlockX(), vector3.getBlockY(), vector3.getBlockZ()));
    }

    public static boolean isConflict(String world, Location location){
        for(Structure structure : structureList.get(world)){
            int x = location.getBlockX();
            int z = location.getBlockZ();

            if(structure.getMinX() <= x && x <= structure.getMaxX() && structure.getMinZ() <= z && z <= structure.getMaxZ()){
                return true;
            }
        }
        return false;
    }

    public static boolean isConflict(String world, Location location, String type){
        for(Structure structure : structureList.get(world)){
            if(structure.getType().startsWith(type)){
                int x = location.getBlockX();
                int z = location.getBlockZ();

                if(structure.getMinX() <= x && x <= structure.getMaxX() && structure.getMinZ() <= z && z <= structure.getMaxZ()){
                    return true;
                }
            }
        }
        return false;
    }
}
