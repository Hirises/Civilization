package com.hirises.civilization.config;

import com.hirises.civilization.Civilization;
import com.hirises.civilization.data.*;
import com.hirises.civilization.player.PlayerCache;
import com.hirises.civilization.world.NMSSupport;
import com.hirises.civilization.world.WorldListener;
import com.hirises.core.data.GUIShapeUnit;
import com.hirises.core.data.ItemStackUnit;
import com.hirises.core.data.LootTableUnit;
import com.hirises.core.data.unit.DataCache;
import com.hirises.core.data.unit.DirDataCache;
import com.hirises.core.display.unit.ActionBarUnit;
import com.hirises.core.store.PlayerCacheStore;
import com.hirises.core.store.YamlStore;
import com.hirises.core.util.ItemUtil;
import com.hirises.core.util.Pair;
import com.hirises.core.util.Util;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;
import org.checkerframework.checker.units.qual.A;

import java.util.*;
import java.util.stream.Collectors;

public class ConfigManager {
    public static YamlStore config = new YamlStore(Civilization.getInst(), "config.yml");
    public static YamlStore prefix = new YamlStore(Civilization.getInst(), "Saves/prefix.yml");
    public static int killRange;
    public static DataCache<GUIShapeUnit> menu = new DataCache<>(new YamlStore(Civilization.getInst(), "menu.yml"), "", GUIShapeUnit::new);
    public static DataCache<StructureInfo> structureData = new DataCache<>(config, "구조물", StructureInfo::new);
    public static DataCache<PrefixInfo> prefixInfo = new DataCache<>(prefix, "", PrefixInfo::new);
    public static PlayerCacheStore<PlayerCache> cacheStore;

    public static YamlStore cache = new YamlStore(Civilization.getInst(), "Saves/cache.yml");
    public static YamlStore data = new YamlStore(Civilization.getInst(), "Saves/data.yml");
    public static YamlStore state = new YamlStore(Civilization.getInst(), "Saves/state.yml");
    public static DirDataCache<LootTableUnit> lootTable = new DirDataCache(Civilization.getInst(), "Schematics", LootTableUnit::new);
    public static List<FreeShopItemUnit> shopItem = new ArrayList<>();
    public static final Map<ChunkData, Structure> structureList = new HashMap<>();

    public static List<UUID> allUser = new ArrayList<>();
    private static ItemStack moneyItem;
    public static int defaultStamina;
    public static int runStamina;
    public static int jumpStamina;
    public static int attackStamina;
    public static int hitStamina;
    public static int miningStamina;
    public static int healStamina;
    public static int debuff1Stamina;
    public static int debuff2Stamina;
    public static ActionBarUnit staminaActionBar;

    public static void init(){
        state.load(true);
        config.load(true);
        cache.load(true);
        data.load(true);
        prefix.load(true);
        lootTable.load();
        killRange = config.get(Integer.class, "현상금.범위");

        StructureInfo.defaultPointOffset = new Vector3(config.get(Integer.class, "offset.point.x"), config.get(Integer.class, "offset.point.y"), config.get(Integer.class, "offset.point.z"));
        StructureInfo.defaultCenterOffset = new Vector3(config.get(Integer.class, "offset.center.x"), config.get(Integer.class, "offset.center.y"), config.get(Integer.class, "offset.center.z"));

        menu.load();
        structureData.load();
        prefixInfo.load();

        cacheStore = new PlayerCacheStore<>(PlayerCache::new);
        cacheStore.checkExistAll();

        shopItem.clear();
        for(String key : data.getKeys("자유시장")){
            shopItem.add(data.getOrDefault(new FreeShopItemUnit(), "자유시장." + key));
        }
        structureList.clear();
        for(String key : data.getKeys("구조물")){
            addStructure(data.getOrDefault(new Structure(), "구조물." + key));
        }

        moneyItem = config.getOrDefault(new ItemStackUnit(), "돈").getItem();

        String uuid = state.get(String.class, "lastHitEnderDragon");
        WorldListener.LastHitEnderDragon = uuid.trim().equalsIgnoreCase("") ? null : UUID.fromString(uuid);
        if(cache.containKey("모든참여자")){
            allUser = cache.getConfig().getStringList("모든참여자").stream().map(s -> UUID.fromString(s)).collect(Collectors.toList());
        }

        defaultStamina = config.get(Integer.class, "스테미나.기본");
        runStamina = config.get(Integer.class, "스테미나.달리기");
        jumpStamina = config.get(Integer.class, "스테미나.점프");
        attackStamina = config.get(Integer.class, "스테미나.공격");
        hitStamina = config.get(Integer.class, "스테미나.데미지");
        miningStamina = config.get(Integer.class, "스테미나.채광");
        healStamina = config.get(Integer.class, "스테미나.초당회복");
        debuff1Stamina = config.get(Integer.class, "스테미나.디버프1");
        debuff2Stamina = config.get(Integer.class, "스테미나.디버프2");
        staminaActionBar = config.getOrDefault(new ActionBarUnit(), "스테미나.엑션바");
    }

    public static void save(){
        saveShopItem();
        cacheStore.saveAll();
        saveStructures();
        if(WorldListener.LastHitEnderDragon != null){
            state.set("lastHitEnderDragon", WorldListener.LastHitEnderDragon.toString());
        }
        saveUsers();
        savePrefix();
    }

    public static Structure addStructure(StructureInfo info, Location loc1, Location loc2, boolean placed){
        Structure structure = new Structure(info, loc1, loc2, placed);
        addStructure(structure);
        return structure;
    }

    public static void addStructure(Structure structure){
        Pair<Integer, Integer> minChunkPos = NMSSupport.toChunk(structure.getMinX(), structure.getMinZ());
        Pair<Integer, Integer> maxChunkPos = NMSSupport.toChunk(structure.getMaxX(), structure.getMaxZ());

        for(int x = minChunkPos.getLeft(); x <= maxChunkPos.getLeft(); x++){
            for(int z = minChunkPos.getRight(); z <= maxChunkPos.getRight(); z++){
                structureList.put(new ChunkData(structure.getStructureInfo().getWorldName(), x, z), structure);
            }
        }
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

    public static void saveStructures(){
        int i = 0;
        for(Structure structure : structureList.values()){
            data.upsert(structure, "구조물." + i);
            i++;
        }
        data.save();
    }

    public static void saveUsers(){
        cache.set("모든참여자", allUser.stream().map(value -> value.toString()).collect(Collectors.toList()));
    }

    public static void savePrefix(){
        for(PrefixInfo info : prefixInfo.getSafeDataUnitMap().values()){
            prefix.upsert(info, String.valueOf(info.getId()));
        }
    }
}
