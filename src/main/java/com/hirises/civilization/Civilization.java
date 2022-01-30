package com.hirises.civilization;

import com.hirises.civilization.command.OPCommand;
import com.hirises.civilization.command.UserCommand;
import com.hirises.civilization.config.ConfigManager;
import com.hirises.civilization.data.AbilityType;
import com.hirises.civilization.data.Structure;
import com.hirises.civilization.data.StructureInfo;
import com.hirises.civilization.player.PlayerCache;
import com.hirises.civilization.player.PlayerHandler;
import com.hirises.civilization.data.CivilizationWorld;
import com.hirises.civilization.world.*;
import com.hirises.core.data.AlertUnit;
import com.hirises.core.data.ItemStackUnit;
import com.hirises.core.data.TimeUnit;
import com.hirises.core.display.ScoreBoardHandler;
import com.hirises.core.store.YamlStore;
import com.hirises.core.task.CancelableTask;
import com.hirises.core.util.Util;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.Objective;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public final class Civilization extends JavaPlugin{

    //region fields

    public static final String WORLD_NAME = "Civilization";
    public static final String WORLD_NETHER_NAME = "Civilization_Nether";
    public static final String WORLD_END_NAME = "Civilization_TheEnd";

    private static Civilization plugin;
    private static boolean isStart;
    private static boolean isFinish;
    public static CivilizationWorld world;
    public static CivilizationWorld world_nether;
    public static CivilizationWorld world_end;
    public volatile static Boolean onProgress;

    public static int worldBorderRadius;

    //endregion

    //region overrides

    @Override
    public void onEnable() {
        // Plugin startup logic
        plugin = this;
        onProgress = false;

        try{
            isStart = (new YamlStore(plugin, "Saves/state.yml")).get(Boolean.class, "start");
            isFinish = (new YamlStore(plugin, "Saves/state.yml")).get(Boolean.class, "finish");
            if(isStart()){
                WorldCreator creator = new WorldCreator(WORLD_NAME);
                world = new CivilizationWorld(Bukkit.createWorld(creator.type(WorldType.NORMAL).environment(World.Environment.NORMAL)));

                WorldCreator creator_nether = new WorldCreator(WORLD_NETHER_NAME);
                world_nether = new CivilizationWorld(Bukkit.createWorld(creator_nether.type(WorldType.NORMAL).environment(World.Environment.NETHER)));

                WorldCreator creator_theEnd = new WorldCreator(WORLD_END_NAME);
                world_end = new CivilizationWorld(Bukkit.createWorld(creator_theEnd.type(WorldType.NORMAL).environment(World.Environment.THE_END)));
            }

            ConfigManager.init();
        }catch (Exception e){
            Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "---------------------   경고!   ---------------------");
            Bukkit.getConsoleSender().sendMessage(ChatColor.DARK_RED + "플러그인을 로드하는 도중 오류가 발생하였습니다.");
            Bukkit.getConsoleSender().sendMessage(ChatColor.DARK_RED + "서버 폴더의 plugins/Civilization/Saves 폴더와 " +
                    "Civilization, Civilization_Nether, Civilization_TheEnd 폴더를 전부 삭제하신 후 다시 시도해보세요.");
            Bukkit.getConsoleSender().sendMessage(ChatColor.DARK_RED + "해당 현상이 반복되면 서버를 다시 생성해주세요");
            Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "--------------------------------------------------");
            e.printStackTrace();
            Bukkit.getPluginManager().disablePlugin(plugin);
        }

        worldBorderRadius = ConfigManager.config.get(Integer.class, "월드지름") / 2;

        getCommand("menu").setExecutor(new UserCommand());
        getCommand("money").setExecutor(new UserCommand());
        getCommand("civilization").setExecutor(new OPCommand());

        Bukkit.getPluginManager().registerEvents(new WorldListener(), plugin);
        Bukkit.getPluginManager().registerEvents(new PrefixListener(), plugin);
        Bukkit.getPluginManager().registerEvents(new AbilityListener(), plugin);
        Bukkit.getPluginManager().registerEvents(new StructureListener(), plugin);
        Bukkit.getPluginManager().registerEvents(new ItemListener(), plugin);
        Bukkit.getPluginManager().registerEvents(new PlayerHandler(), plugin);

        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            if(!isStart()){
                return;
            }
            Util.broadcast(new TextComponent(ChatColor.YELLOW + "저장중입니다..."));
            ConfigManager.cacheStore.saveAll();
        }, 5 * 60 * 20, 5 * 60 * 20);

        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            if(!isStart()){
                return;
            }
            for(Player player : Bukkit.getOnlinePlayers()){
                PlayerCache cache = ConfigManager.getCache(player.getUniqueId());
                cache.operateStamina((ConfigManager.StaminaData.healStamina  + cache.getStaminaReduce(AbilityType.Sense, "회복")) / 4);
                if(!player.isSprinting() && !player.isJumping() && player.isSneaking()){
                    cache.operateStamina((ConfigManager.StaminaData.additionalHealStamina  + cache.getStaminaReduce(AbilityType.Sense, "웅크리기")) / 4);
                }
            }
        }, 5, 5);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        try{
            ConfigManager.save();
        }catch (Exception e) {
            Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "---------------------   경고!   ---------------------");
            Bukkit.getConsoleSender().sendMessage(ChatColor.DARK_RED + "플러그인을 저장하는 도중 오류가 발생하였습니다.");
            Bukkit.getConsoleSender().sendMessage(ChatColor.DARK_RED + "서버 폴더의 plugins/Civilization/Saves 폴더와 " +
                    "Civilization, Civilization_Nether, Civilization_TheEnd 폴더를 전부 삭제하신 후 다시 시도해보세요.");
            Bukkit.getConsoleSender().sendMessage(ChatColor.DARK_RED + "해당 현상이 반복되면 서버를 다시 생성해주세요");
            Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "--------------------------------------------------");
            e.printStackTrace();
        }
    }

    //endregion

    //region GameControl

    public static void resetGame(){
        Bukkit.getScheduler().runTaskLater(Civilization.getInst(), () -> {
            if(onProgress){
                return;
            }
            onProgress = true;

            Util.broadcast(new TextComponent(ChatColor.DARK_RED + "---------------------   주의!   ---------------------"));
            Util.broadcast(new TextComponent(ChatColor.DARK_RED + "진행중 서버를 종료하지 마세요. 치명적인 오류가 발생할 수 있습니다."));
            Util.broadcast(new TextComponent(ChatColor.DARK_RED + "--------------------------------------------------"));
            Util.broadcast(new TextComponent(ChatColor.RED + "게임을 초기화합니다..."));
            ConfigManager.shopItem.clear();
            ConfigManager.structureList.clear();
            ConfigManager.data.removeKey("자유시장");
            ConfigManager.data.removeKey("구조물");
            ConfigManager.data.removeKey("지옥문");
            ConfigManager.data.save();
            ConfigManager.cacheStore.removeAll();
            ConfigManager.cacheStore.unloadAll();
            for(String key : ConfigManager.cache.getKeys("")){
                ConfigManager.cache.removeKey(key);
            }
            ConfigManager.allUser.clear();
            ConfigManager.saveUsers();
            ConfigManager.resetPrefix();
            ConfigManager.cache.save();

            if(isStart){
                Util.broadcast(new TextComponent(ChatColor.YELLOW + "플레이어를 초기화시킵니다..."));
                for(Player player : Bukkit.getOnlinePlayers()){
                    resetPlayer(player);
                    ScoreBoardHandler.hide(player, ScoreBoardHandler.getOrNew(player));
                }

                Util.broadcast(new TextComponent(ChatColor.YELLOW + "플레이어를 이동시킵니다..."));
                World overWorld = Bukkit.getWorld("world");
                for(Player player : world.get().getPlayers()){
                    player.teleport(overWorld.getSpawnLocation());
                }
                Bukkit.unloadWorld(world.get(), false);
                for(Player player : world_nether.get().getPlayers()){
                    player.teleport(overWorld.getSpawnLocation());
                }
                Bukkit.unloadWorld(world_nether.get(), false);
                for(Player player : world_end.get().getPlayers()){
                    player.teleport(overWorld.getSpawnLocation());
                }
                Bukkit.unloadWorld(world_end.get(), false);

                Util.broadcast(new TextComponent(ChatColor.YELLOW + "월드 제거중..."));
                File file = plugin.getDataFolder();
                String path = file.getAbsolutePath();
                path = path.substring(0, path.lastIndexOf("\\"));
                path = path.substring(0, path.lastIndexOf("\\"));
                path += "\\" + world.getName();
                deleteWalk(Path.of(path));

                path = file.getAbsolutePath();
                path = path.substring(0, path.lastIndexOf("\\"));
                path = path.substring(0, path.lastIndexOf("\\"));
                path += "\\" + world_nether.getName();
                deleteWalk(Path.of(path));

                path = file.getAbsolutePath();
                path = path.substring(0, path.lastIndexOf("\\"));
                path = path.substring(0, path.lastIndexOf("\\"));
                path += "\\" + world_end.getName();
                deleteWalk(Path.of(path));
            }

            Util.broadcast(new TextComponent(ChatColor.YELLOW + "마무리 중..."));
            isStart = false;
            isFinish = false;
            ConfigManager.state.set("start", false);
            ConfigManager.state.set("finish", false);
            ConfigManager.state.save();

            Util.broadcast(new TextComponent(ChatColor.GREEN + "완료!"));

            onProgress = false;
        }, 20);
    }

    public static void startGame() {
        resetGame();

        Bukkit.getScheduler().runTaskLater(Civilization.getInst(), () -> {
            if(onProgress){
                return;
            }
            onProgress = true;

            Util.broadcast(new TextComponent(ChatColor.DARK_RED + "---------------------   주의!   ---------------------"));
            Util.broadcast(new TextComponent(ChatColor.DARK_RED + "진행중 서버를 종료하지 마세요. 치명적인 오류가 발생할 수 있습니다."));
            Util.broadcast(new TextComponent(ChatColor.DARK_RED + "--------------------------------------------------"));
            Util.broadcast(new TextComponent(ChatColor.RED + "새로운 게임을 시작합니다..."));
            int worldSize = ConfigManager.config.get(Integer.class, "월드지름");

            Util.broadcast(new TextComponent(ChatColor.YELLOW + "월드를 생성합니다... " + ChatColor.GRAY + "1/3"));
            WorldCreator creator = new WorldCreator(WORLD_NAME);
            world = new CivilizationWorld(Bukkit.createWorld(creator.type(WorldType.NORMAL).environment(World.Environment.NORMAL)), worldSize);
            world.get().setGameRule(GameRule.KEEP_INVENTORY, true);
            world.get().setGameRule(GameRule.NATURAL_REGENERATION, false);
            world.get().setDifficulty(Difficulty.HARD);
            world.get().setSpawnLocation(0, 0, 0);

            Bukkit.getScheduler().runTaskLater(Civilization.getInst(), () -> {
                Util.broadcast(new TextComponent(ChatColor.YELLOW + "월드를 생성합니다... " + ChatColor.GRAY + "2/3"));
                WorldCreator creator_nether = new WorldCreator(WORLD_NETHER_NAME);
                world_nether = new CivilizationWorld(Bukkit.createWorld(creator_nether.type(WorldType.NORMAL).environment(World.Environment.NETHER)), (float)worldSize / 8);
                world_nether.get().setGameRule(GameRule.KEEP_INVENTORY, true);
                world_nether.get().setGameRule(GameRule.NATURAL_REGENERATION, false);
                world_nether.get().setDifficulty(Difficulty.HARD);
                world_nether.get().setSpawnLocation(0, 0, 0);

                Bukkit.getScheduler().runTaskLater(Civilization.getInst(), () -> {
                    Util.broadcast(new TextComponent(ChatColor.YELLOW + "월드를 생성합니다... " + ChatColor.GRAY + "3/3"));
                    WorldCreator creator_theEnd = new WorldCreator(WORLD_END_NAME);
                    world_end = new CivilizationWorld(Bukkit.createWorld(creator_theEnd.type(WorldType.NORMAL).environment(World.Environment.THE_END)), 1000);
                    world_end.get().setGameRule(GameRule.KEEP_INVENTORY, true);
                    world_end.get().setGameRule(GameRule.NATURAL_REGENERATION, false);
                    world_end.get().setDifficulty(Difficulty.HARD);
                    world_end.get().setSpawnLocation(0, 0, 0);

                    Map<Player, Location> spawn = new HashMap<>();
                    for(Player player : Bukkit.getOnlinePlayers()){
                        spawn.put(player, getNewSpawnPoint(player, true));
                    }

                    WorldBorder border = world.get().getWorldBorder();
                    border.setCenter(0, 0);
                    border.setSize(world.getSize());

                    WorldBorder border_nether = world_nether.get().getWorldBorder();
                    border_nether.setCenter(0, 0);
                    border_nether.setSize(world.getSize());

                    WorldBorder border_end = world_end.get().getWorldBorder();
                    border_end.setCenter(0, 0);
                    border_end.setSize(world.getSize());

                    Util.broadcast(new TextComponent(ChatColor.YELLOW + "구조물을 배치합니다... "));
                    genStructure();

                    new CancelableTask(plugin, 20, 20){
                        int count = 30;
                        @Override
                        public void run() {
                            if(count <= 0){
                                ConfigManager.state.set("start", true);
                                ConfigManager.state.set("finish", false);
                                ConfigManager.state.save();
                                isStart = true;
                                isFinish = false;
                                Util.broadcast(new TextComponent(ChatColor.YELLOW + "플레이어를 이동시킵니다..."));
                                for(Player player : spawn.keySet()){
                                    if(player.isOnline()){
                                        resetPlayer(player);
                                        prepareNewPlayer(player);
                                        player.teleport(spawn.get(player));
                                    }
                                }
                                for(Player player : Bukkit.getOnlinePlayers().stream().filter(value -> NMSSupport.inValidWorld(value)).collect(Collectors.toList())){
                                    resetPlayer(player);
                                    prepareNewPlayer(player);
                                    getNewSpawnPoint(player, false);
                                }

                                Util.broadcast(new TextComponent(ChatColor.YELLOW + "마무리 중..."));
                                ConfigManager.cacheStore.checkExistAll();
                                ConfigManager.cacheStore.saveAll();
                                ConfigManager.saveStructures();

                                Util.broadcast(new TextComponent(ChatColor.GREEN + "완료!"));
                                onProgress = false;
                                cancel();
                                return;
                            }

                            Util.broadcast(new TextComponent(ChatColor.YELLOW + "월드를 초기화합니다...   " + ChatColor.GRAY + count-- + "s left"));
                        }
                    };
                }, 20);
            }, 20);
        }, 40);
    }

    public static void endGame(UUID winner){
        isFinish = true;
        ConfigManager.state.set("finish", true);
        ConfigManager.state.save();

        AlertUnit winnerAlert = ConfigManager.config.getOrDefault(new AlertUnit(), "승리");
        String name = Bukkit.getOfflinePlayer(winner).getName();
        for(Player player : Bukkit.getOnlinePlayers()){
            winnerAlert.play(player, Util.toRemap("winner", name));
        }

        new CancelableTask(Civilization.getInst(), 2, 2){
            World end = Civilization.world_end.get();
            int count = 0;
            @Override
            public void run() {
                Location location = new Location(end, new Random().nextInt(100) - 50 + 0.5, 45, new Random().nextInt(100) - 50 + 0.5);
                Block block = location.getBlock();
                if(block == null || block.getType().equals(Material.AIR) || block.getType().equals(Material.OBSIDIAN)){
                    return;
                }
                location.setY(end.getHighestBlockYAt(location.getBlockX(), location.getBlockZ()) + 1);
                NMSSupport.spawnFireWork(location);

                count += 1;
                if(count >= 30 * 10){
                    cancel();
                    return;
                }
            }
        };
    }

    //endregion

    //region addon

    public static void genStructure(){
        for(StructureInfo info : ConfigManager.structureData.getSafeDataUnitMap().values()){
            for(int i = 0; i < info.getCount(); i++){
                NMSSupport.lazyPlaceStructure(info);
            }
        }
    }

    public static void resetPlayer(Player player){
        ConfigManager.cacheStore.remove(player);
        ConfigManager.cacheStore.unload(player);
        for(PotionEffectType effect : PotionEffectType.values()){
            player.removePotionEffect(effect);
        }
        player.getInventory().clear();
        player.setExp(0);
        player.setLevel(ConfigManager.config.get(Integer.class, "시작레벨"));
        player.setGameMode(GameMode.SURVIVAL);
    }

    public static Location getNewSpawnPoint(Player player, boolean asynchronous){
        Location spawn = NMSSupport.getRandomLocation(world, 1, 1, true);

        Structure structure = NMSSupport.lazyPlaceStructure(ConfigManager.structureData.get("spawn"), spawn);

        spawn.add(0.5, 0, 0.5);
        if (asynchronous) {
            world.get().loadChunk(spawn.getChunk());
        }else{
            if(!structure.isPlaced()){
                structure.place();
            }
            player.teleport(spawn);
        }
        player.setBedSpawnLocation(spawn, true);
        ConfigManager.getCache(player.getUniqueId()).setSpawn(spawn);

        return spawn;
    }

    public static void prepareNewPlayer(Player player){
        int invincibleTime = (int) ConfigManager.config.getOrDefault(new TimeUnit(), "초반무적").getTick();

        player.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, invincibleTime, 0, false, false, true));
        player.addPotionEffect(new PotionEffect(PotionEffectType.WATER_BREATHING, invincibleTime, 0, false, false, true));
        player.addPotionEffect(new PotionEffect(PotionEffectType.SATURATION, invincibleTime, 2, false, false, true));
        player.addPotionEffect(new PotionEffect(PotionEffectType.FAST_DIGGING, invincibleTime, 2, false, false, true));
        player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, invincibleTime, 2, false, false, true));
        player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, invincibleTime, 9, false, false, true));

        for(String key : ConfigManager.config.getKeys("기본템")){
            ItemStack item = ConfigManager.config.getOrDefault(new ItemStackUnit(), "기본템." + key).getItem();
            player.getInventory().addItem(item);
        }

        Objective board = ScoreBoardHandler.getOrNew(player);
        board.setDisplayName("Civilization");
        ScoreBoardHandler.show(player, board);
        PlayerHandler.updateScoreBoard(player);

        ConfigManager.allUser.add(player.getUniqueId());
        ConfigManager.saveUsers();
    }

    //endregion

    private static void deleteWalk(Path path){
        try {
            Files.walk(path).forEach(p -> {
                if(path.equals(p)){
                    return;
                }
                File f = p.toFile();
                if(f.isDirectory()){
                    deleteWalk(p);
                }
                f.delete();
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
        File f = path.toFile();
        f.delete();
    }

    public static Civilization getInst() {
        return plugin;
    }

    public static boolean isStart() {
        return isStart;
    }

    public static boolean isFinish(){
        return isFinish;
    }

    public static boolean isRunning(){
        return isStart && !isFinish;
    }
}
