package com.hirises.civilization.config;

import com.hirises.civilization.Civilization;
import com.hirises.civilization.gui.FreeShopItemUnit;
import com.hirises.civilization.player.PlayerCache;
import com.hirises.core.data.GUIShapeUnit;
import com.hirises.core.data.ItemStackUnit;
import com.hirises.core.data.unit.DataCache;
import com.hirises.core.store.PlayerCacheStore;
import com.hirises.core.store.YamlStore;
import com.hirises.core.util.ItemUtil;
import com.hirises.core.util.Pair;
import com.hirises.core.util.Util;
import com.sk89q.worldedit.math.BlockVector3;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class ConfigManager {
    public static YamlStore config = new YamlStore(Civilization.getInst(), "config.yml");
    public static int killRange;
    public static YamlStore cache = new YamlStore(Civilization.getInst(), "cache.yml");
    public static YamlStore save = new YamlStore(Civilization.getInst(), "save.yml");
    public static List<FreeShopItemUnit> shopItem = new ArrayList<>();
    public static Map<World, List<Pair<Pair<Integer, Integer>, Pair<Integer, Integer>>>> structureList = new HashMap<>();

    public static DataCache<GUIShapeUnit> menu = new DataCache<>(new YamlStore(Civilization.getInst(), "menu.yml"), "", GUIShapeUnit::new);
    public static PlayerCacheStore<PlayerCache> cacheStore;

    private static ItemStack moneyItem;

    public static void init(){
        config.load(true);
        cache.load(true);
        save.load(true);
        killRange = config.get(Integer.class, "현상금.범위");
        for(String key : save.getKeys("자유시장")){
            shopItem.add(save.getOrDefault(new FreeShopItemUnit(), "자유시장." + key));
        }
        structureList.put(Civilization.world, new ArrayList<>());
        structureList.put(Civilization.world_nether, new ArrayList<>());
        structureList.put(Civilization.world_end, new ArrayList<>());
        for(String key : save.getKeys("구조물")){
            Pair<Integer, Integer> min = new Pair<>(save.get(Integer.class, "구조물." + key + ".minX"), save.get(Integer.class, "구조물." + key + ".minZ"));
            Pair<Integer, Integer> max = new Pair<>(save.get(Integer.class, "구조물." + key + ".maxX"), save.get(Integer.class, "구조물." + key + ".maxZ"));
            switch (save.get(String.class, "구조물." + key + ".world")){
                case "Civilization":
                    structureList.get(Civilization.world).add(new Pair<>(min, max));
                    break;
                case "Civilization_Nether":
                    structureList.get(Civilization.world_nether).add(new Pair<>(min, max));
                    break;
                case "Civilization_TheEnd":
                    structureList.get(Civilization.world_end).add(new Pair<>(min, max));
                    break;
            }
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
        save.removeKey("자유시장");
        int i = 0;
        for(FreeShopItemUnit item : shopItem){
            save.upsert(item, "자유시장." + i);
            i++;
        }
    }

    public static void saveStructure(){
        int i = 0;
        for(World world : structureList.keySet()){
            for(Pair<Pair<Integer, Integer>, Pair<Integer, Integer>> pos : structureList.get(world)){
                save.upsert(pos.getLeft().getLeft(), "구조물." + i + "minX");
                save.upsert(pos.getLeft().getRight(), "구조물." + i + "minZ");

                save.upsert(pos.getRight().getLeft(), "구조물." + i + "maxX");
                save.upsert(pos.getRight().getRight(), "구조물." + i + "maxZ");

                save.upsert(world.getName(), "구조물." + i + ".world");

                i++;
            }
        }
    }

    public static void addStructure(World world, Location loc1, Location loc2){
        Pair<Integer, Integer> min = new Pair<>(Math.min(loc1.getBlockX(), loc2.getBlockX()), Math.min(loc1.getBlockZ(), loc2.getBlockZ()));
        Pair<Integer, Integer> max = new Pair<>(Math.max(loc1.getBlockX(), loc2.getBlockX()), Math.max(loc1.getBlockZ(), loc2.getBlockZ()));
        structureList.get(world).add(new Pair<>(min, max));
    }

    public static boolean isConflict(World world, BlockVector3 vector3){
        return isConflict(new Location(world, vector3.getBlockX(), vector3.getBlockY(), vector3.getBlockZ()));
    }

    public static boolean isConflict(Location location){
        for(Pair<Pair<Integer, Integer>, Pair<Integer, Integer>> pos : structureList.get(location.getWorld())){
            int minX = pos.getLeft().getLeft();
            int minZ = pos.getLeft().getRight();
            int maxX = pos.getRight().getLeft();
            int maxZ = pos.getRight().getRight();
            int x = location.getBlockX();
            int z = location.getBlockZ();

            if(minX <= x && x <= maxX && minZ <= z && z <= maxZ){
                return true;
            }
        }
        return false;
    }
}
