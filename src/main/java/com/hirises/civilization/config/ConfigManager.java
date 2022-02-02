package com.hirises.civilization.config;

import com.hirises.civilization.Civilization;
import com.hirises.civilization.data.*;
import com.hirises.civilization.player.PlayerCache;
import com.hirises.civilization.world.NMSSupport;
import com.hirises.civilization.world.WorldListener;
import com.hirises.core.data.AlertUnit;
import com.hirises.core.data.GUIShapeUnit;
import com.hirises.core.data.ItemStackUnit;
import com.hirises.core.data.LootTableUnit;
import com.hirises.core.data.unit.DataCache;
import com.hirises.core.data.unit.DirDataCache;
import com.hirises.core.display.Display;
import com.hirises.core.display.unit.ActionBarUnit;
import com.hirises.core.display.unit.MessageUnit;
import com.hirises.core.store.PlayerCacheStore;
import com.hirises.core.store.YamlStore;
import com.hirises.core.util.ItemUtil;
import com.hirises.core.util.Pair;
import com.hirises.core.util.Util;
import org.bukkit.*;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginManager;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

public class ConfigManager {
    public static YamlStore config = new YamlStore(Civilization.getInst(), "config.yml");
    public static YamlStore prefix = new YamlStore(Civilization.getInst(), "prefix.yml");
    public static YamlStore ability = new YamlStore(Civilization.getInst(), "ability.yml");
    public static YamlStore craft = new YamlStore(Civilization.getInst(), "craft.yml");
    public static YamlStore cache = new YamlStore(Civilization.getInst(), "Saves/cache.yml");
    public static YamlStore data = new YamlStore(Civilization.getInst(), "Saves/data.yml");
    public static YamlStore state = new YamlStore(Civilization.getInst(), "Saves/state.yml");

    public static DataCache<GUIShapeUnit> menu = new DataCache<>(new YamlStore(Civilization.getInst(), "menu.yml"), "", GUIShapeUnit::new);
    public static DataCache<AbilityInfo> abilityInfo = new DataCache<>(ability, "숙련도", AbilityInfo::new);
    public static DataCache<StructureInfo> structureData = new DataCache<>(config, "구조물", StructureInfo::new);
    public static DirDataCache<LootTableUnit> lootTable = new DirDataCache(Civilization.getInst(), "Schematics", LootTableUnit::new);
    public static PlayerCacheStore<PlayerCache> cacheStore;

    public static List<FreeShopItemUnit> shopItem = new ArrayList<>();  //자유시장 아이템
    public static final Map<ChunkData, Structure> structureList = new HashMap<>();  //설치된 구조물 리스트
    public static Map<PrefixType, PrefixInfo> prefixInfoMap = new HashMap<>();  //칭호 상태 맵
    private static final Map<PrefixType, UUID> prefixFinisher = new HashMap<>();    //칭호 획득한 사람
    public static List<UUID> allUser = new ArrayList<>();   //접속했었던 모든 플레이어 리스트

    //숙련도별 제한 맵
    public static Map<Material, Map<AbilityType, Integer>> rightClickLimitMap = new HashMap<>();
    public static Map<EntityType, Map<AbilityType, Integer>> entityRightClickLimitMap = new HashMap<>();
    public static Map<Material, Map<AbilityType, Integer>> placeLimitMap = new HashMap<>();
    public static Map<Material, Map<AbilityType, Integer>> craftLimitMap = new HashMap<>();
    public static Map<String, Map<AbilityType, Integer>> magicCraftLimitMap = new HashMap<>();

    //기타 읽어올 자료들
    private static ItemStack moneyItem;
    public static ItemStack abilityItem;
    public static ItemStack prefixItem;
    public static MessageUnit lackLevelMessage;
    public static AlertUnit prefixGainMessage;
    public static int killRange;

    //region data classes

    //스테미나 관련된 상수들 (너무 많아서 한곳에 모아둠)
    public class StaminaData {
        public static int defaultStamina;
        public static int runStamina;
        public static int swimmingStamina;
        public static int jumpStamina;
        public static int attackStamina;
        public static int bowAttackStamina;
        public static int hitStamina;
        public static int miningStamina;
        public static int placeStamina;
        public static int healStamina;
        public static int additionalHealStamina;
        public static int drinkingStamina;
        public static int debuff1Stamina;
        public static int debuff2Stamina;
        public static int buffStamina;
        public static int staminaBarLength;
        public static ActionBarUnit staminaActionBar;
        public static Map<Material, Integer> staminaHealMap;


        public static void load(){
            defaultStamina = config.get(Integer.class, "스테미나.기본");
            runStamina = config.get(Integer.class, "스테미나.달리기");
            swimmingStamina = config.get(Integer.class, "스테미나.수영");
            jumpStamina = config.get(Integer.class, "스테미나.점프");
            attackStamina = config.get(Integer.class, "스테미나.공격");
            bowAttackStamina = config.get(Integer.class, "스테미나.원거리공격");
            hitStamina = config.get(Integer.class, "스테미나.데미지");
            miningStamina = config.get(Integer.class, "스테미나.채광");
            placeStamina = config.get(Integer.class, "스테미나.설치");
            drinkingStamina = config.get(Integer.class, "스테미나.마시기");
            healStamina = config.get(Integer.class, "스테미나.초당회복");
            additionalHealStamina = config.get(Integer.class, "스테미나.웅크리기");
            debuff1Stamina = config.get(Integer.class, "스테미나.디버프1");
            debuff2Stamina = config.get(Integer.class, "스테미나.디버프2");
            buffStamina = config.get(Integer.class, "스테미나.버프");
            staminaActionBar = config.getOrDefault(new ActionBarUnit(), "스테미나.엑션바");
            staminaBarLength = config.get(Integer.class, "스테미나.엑션바.길이");
            staminaHealMap = new HashMap<>();
            for(String key : config.getKeys("스테미나.먹기")){
                staminaHealMap.put(Material.valueOf(key), config.get(Integer.class, "스테미나.먹기." + key));
            }
        }
    }

    //endregion

    /**
     * 모든 정보를 읽어옵니다.
     * 기존 정보를 모두 덮어씌웁니다
     */
    public static void load(){
        File file = Civilization.getInst().getDataFolder().toPath().resolve("Schematics").toFile();
        if(!file.isDirectory() || !file.exists()){
            file.mkdirs();
            Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "---------------------   경고!   ---------------------");
            Bukkit.getConsoleSender().sendMessage(ChatColor.DARK_RED + "플러그인을 로드하는 도중 오류가 발생하였습니다.");
            Bukkit.getConsoleSender().sendMessage(ChatColor.DARK_RED + "서버 폴더의 plugins/Civilization/Schematics 폴더에 " +
                            "구조물 파일을 추가해주세요");
            Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "--------------------------------------------------");
            Bukkit.getPluginManager().disablePlugin(Civilization.getInst());
            return;
        }

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
        abilityInfo.load();

        cacheStore = new PlayerCacheStore<>(PlayerCache::new);
        cacheStore.checkExistAll();


        DataCache<PrefixInfo> prefixInfo = new DataCache<>(prefix, "칭호", PrefixInfo::new);
        prefixInfo.load();
        prefixInfoMap.clear();
        for(PrefixInfo info : prefixInfo.getSafeDataUnitMap().values()){
            prefixInfoMap.put(info.getType(), info);
        }
        shopItem.clear();
        for(String key : data.getKeys("자유시장")){
            shopItem.add(data.getOrDefault(new FreeShopItemUnit(), "자유시장." + key));
        }
        structureList.clear();
        for(String key : data.getKeys("구조물")){
            addStructure(data.getOrDefault(new Structure(), "구조물." + key));
        }
        prefixFinisher.clear();
        for(PrefixType type : PrefixType.values()){
            String rawUUID = cache.getOrDefault(String.class, "", "칭호." + type.getKey());
            prefixFinisher.put(type, rawUUID.trim().isEmpty() ? null : UUID.fromString(rawUUID));
        }
        rightClickLimitMap.clear();
        for(String typeKey : ability.getKeys("제한.우클")){
            AbilityType type = AbilityType.valueOf(typeKey);
            for(String matKey : ability.getKeys("제한.우클." + typeKey)){
                Material mat = Material.valueOf(matKey);
                rightClickLimitMap.putIfAbsent(mat, new HashMap<>());
                rightClickLimitMap.get(mat)
                        .put(type, ability.get(Integer.class, "제한.우클." + typeKey +"." + matKey));
            }
        }
        entityRightClickLimitMap.clear();
        for(String typeKey : ability.getKeys("제한.엔티티")){
            AbilityType type = AbilityType.valueOf(typeKey);
            for(String matKey : ability.getKeys("제한.엔티티." + typeKey)){
                EntityType entityType = EntityType.valueOf(matKey);
                entityRightClickLimitMap.putIfAbsent(entityType, new HashMap<>());
                entityRightClickLimitMap.get(entityType)
                        .put(type, ability.get(Integer.class, "제한.엔티티." + typeKey +"." + matKey));
            }
        }
        placeLimitMap.clear();
        for(String typeKey : ability.getKeys("제한.설치")){
            AbilityType type = AbilityType.valueOf(typeKey);
            for(String matKey : ability.getKeys("제한.설치." + typeKey)){
                Material mat = Material.valueOf(matKey);
                placeLimitMap.putIfAbsent(mat, new HashMap<>());
                placeLimitMap.get(mat)
                        .put(type, ability.get(Integer.class, "제한.설치." + typeKey +"." + matKey));
            }
        }
        craftLimitMap.clear();
        for(String typeKey : ability.getKeys("제한.제작")){
            AbilityType type = AbilityType.valueOf(typeKey);
            for(String matKey : ability.getKeys("제한.제작." + typeKey)){
                Material mat = Material.valueOf(matKey);
                craftLimitMap.putIfAbsent(mat, new HashMap<>());
                craftLimitMap.get(mat)
                        .put(type, ability.get(Integer.class, "제한.제작." + typeKey +"." + matKey));
            }
        }
        magicCraftLimitMap.clear();
        for(String typeKey : ability.getKeys("제한.특수제작")){
            AbilityType type = AbilityType.valueOf(typeKey);
            for(String key : ability.getKeys("제한.특수제작." + typeKey)){
                magicCraftLimitMap.putIfAbsent(key, new HashMap<>());
                magicCraftLimitMap.get(key)
                        .put(type, ability.get(Integer.class, "제한.특수제작." + typeKey + "." + key));
            }
        }

        DataCache<RecipeInfo> recipes = new DataCache<>(craft, "", RecipeInfo::new);
        recipes.load();
        Bukkit.resetRecipes();
        recipes.getSafeDataUnitMap().values().forEach(value -> value.register());

        moneyItem = config.getOrDefault(new ItemStackUnit(), "돈").getItem();
        abilityItem = ability.getOrDefault(new ItemStackUnit(), "기본아이템").getItem();
        prefixItem = prefix.getOrDefault(new ItemStackUnit(), "기본아이템").getItem();
        lackLevelMessage = ability.getOrDefault(new MessageUnit(), "메세지");
        prefixGainMessage = config.getOrDefault(new AlertUnit(), "칭호획득");

        String uuid = state.get(String.class, "lastHitEnderDragon");
        WorldListener.LastHitEnderDragon = uuid.trim().equalsIgnoreCase("") ? null : UUID.fromString(uuid);
        if(cache.containKey("모든참여자")){
            allUser = cache.getConfig().getStringList("모든참여자").stream().map(s -> UUID.fromString(s)).collect(Collectors.toList());
        }

        StaminaData.load();
    }

    /**
     * 현재 정보를 저장합니다.
     */
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

    /**
     * 해당 위치에 구조물 정보를 추가합니다.
     * @param info 추가할 구조물 정보
     * @param loc1 최소 위치
     * @param loc2 최대 위치
     * @param placed 현재 초기화 되어있는지 여부 (설치 되어있는지 여부)
     * @return 추가한 구조물
     */
    public static Structure addStructure(StructureInfo info, Location loc1, Location loc2, boolean placed){
        Structure structure = new Structure(info, loc1, loc2, placed);
        addStructure(structure);
        return structure;
    }

    /**
     * 해당 구조물 정보를 추가합니다.
     * @param structure 추가할 구조물
     * @return 추가된 구조물
     */
    public static Structure addStructure(Structure structure){
        //최대, 최소위치 계산
        Pair<Integer, Integer> minChunkPos = NMSSupport.toChunk(structure.getMinX(), structure.getMinZ());
        Pair<Integer, Integer> maxChunkPos = NMSSupport.toChunk(structure.getMaxX(), structure.getMaxZ());

        //청크별로 돌면서 추가
        for(int x = minChunkPos.getLeft(); x <= maxChunkPos.getLeft(); x++){
            for(int z = minChunkPos.getRight(); z <= maxChunkPos.getRight(); z++){
                structureList.put(new ChunkData(structure.getStructureInfo().getWorldName(), x, z), structure);
            }
        }

        return structure;
    }

    public static PlayerCache getCache(UUID uuid){
        return cacheStore.get(uuid);
    }

    public static PlayerCache getCache(Player player){
        return getCache(player.getUniqueId());
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
        for(PrefixType type : prefixFinisher.keySet()){
            UUID uuid = prefixFinisher.get(type);
            cache.upsert(uuid == null ? "" : uuid.toString(), "칭호." + type.getKey());
        }
        cache.save();
    }

    public static boolean givenPrefix(PrefixType type){
        return prefixFinisher.getOrDefault(type, null) != null;
    }

    public static boolean hasPrefix(PrefixType type, UUID uuid){
        if(!givenPrefix(type)){
            return false;
        }
        return prefixFinisher.get(type).equals(uuid);
    }

    public static void givePrefix(PrefixType type, UUID uuid){
        prefixFinisher.put(type, uuid);
        Player target = Bukkit.getPlayer(uuid);
        PrefixInfo info = prefixInfoMap.get(type);
        if(target != null){
            target.giveExp(info.getExp());
        }

        String name = Bukkit.getOfflinePlayer(uuid).getName();
        String prefix = info.getName();
        for(Player player : Bukkit.getOnlinePlayers()){
            prefixGainMessage.play(player, Util.toRemap("name", name, "prefix", prefix));
        }
    }

    public static void resetPrefix(){
        for(PrefixType type : prefixFinisher.keySet()){
            prefixFinisher.put(type, null);
        }
        cache.removeKey("칭호");
    }
}
