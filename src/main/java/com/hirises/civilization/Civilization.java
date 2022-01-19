package com.hirises.civilization;

import com.hirises.civilization.command.OPCommand;
import com.hirises.civilization.command.UserCommand;
import com.hirises.civilization.config.ConfigManager;
import com.hirises.civilization.player.PlayerListener;
import com.hirises.civilization.util.CivilizationWorld;
import com.hirises.civilization.util.NMSSupport;
import com.hirises.core.data.TimeUnit;
import com.hirises.core.display.ScoreBoardHandler;
import com.hirises.core.event.GUIGetEvent;
import com.hirises.core.store.YamlStore;
import com.hirises.core.util.Util;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.Objective;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public final class Civilization extends JavaPlugin implements Listener {

    public static final String WORLD_NAME = "Civilization";
    public static final String WORLD_NETHER_NAME = "Civilization_Nether";
    public static final String WORLD_END_NAME = "Civilization_TheEnd";

    private static Civilization plugin;
    private static boolean isStart;
    public static CivilizationWorld world;
    public static CivilizationWorld world_nether;
    public static CivilizationWorld world_end;
    public volatile static Boolean onProgress;

    public static int worldBorderRadius;
    public static final Set<Material> SPAWN_BLOCK_SET = Collections.unmodifiableSet(Arrays.asList(
            Material.AIR,
            Material.ACACIA_LEAVES,
            Material.AZALEA_LEAVES,
            Material.BIRCH_LEAVES,
            Material.DARK_OAK_LEAVES,
            Material.FLOWERING_AZALEA_LEAVES,
            Material.JUNGLE_LEAVES,
            Material.OAK_LEAVES,
            Material.SPRUCE_LEAVES,
            Material.TALL_GRASS,
            Material.LARGE_FERN,
            Material.LILAC,
            Material.SUNFLOWER,
            Material.ROSE_BUSH,
            Material.PEONY,
            Material.SUGAR_CANE,
            Material.BAMBOO,
            Material.SNOW
    ).stream().collect(Collectors.toSet()));

    @Override
    public void onEnable() {
        // Plugin startup logic
        plugin = this;
        onProgress = false;

        isStart = (new YamlStore(plugin, "Saves/state.yml")).get(Boolean.class, "start");
        if(isStart()){
            WorldCreator creator = new WorldCreator(WORLD_NAME);
            world = new CivilizationWorld(Bukkit.createWorld(creator.type(WorldType.NORMAL).environment(World.Environment.NORMAL)));

            WorldCreator creator_nether = new WorldCreator(WORLD_NETHER_NAME);
            world_nether = new CivilizationWorld(Bukkit.createWorld(creator_nether.type(WorldType.NORMAL).environment(World.Environment.NETHER)));

            WorldCreator creator_theEnd = new WorldCreator(WORLD_END_NAME);
            world_end = new CivilizationWorld(Bukkit.createWorld(creator_theEnd.type(WorldType.NORMAL).environment(World.Environment.THE_END)));
        }

        ConfigManager.init();

        worldBorderRadius = ConfigManager.config.get(Integer.class, "월드지름") / 2;

        getCommand("menu").setExecutor(new UserCommand());
        getCommand("money").setExecutor(new UserCommand());
        getCommand("civilization").setExecutor(new OPCommand());

        Bukkit.getPluginManager().registerEvents(plugin, plugin);
        Bukkit.getPluginManager().registerEvents(new PlayerListener(), plugin);

        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            if(!isStart()){
                return;
            }
            Util.broadcast(new TextComponent(ChatColor.YELLOW + "저장중입니다..."));
            ConfigManager.cacheStore.saveAll();
        }, 5 * 60 * 20, 5 * 60 * 20);
    }

    public static boolean isStart() {
        return isStart;
    }

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
            ConfigManager.state.set("start", false);
            ConfigManager.state.save();

            Util.broadcast(new TextComponent(ChatColor.GREEN + "완료!"));

            onProgress = false;
        }, 20);
    }

    public static void resetPlayer(Player player){
        ConfigManager.cacheStore.remove(player);
        ConfigManager.cacheStore.unload(player);
        for(PotionEffectType effect : PotionEffectType.values()){
            player.removePotionEffect(effect);
        }
        player.getInventory().clear();
        player.setLevel(0);
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

            Bukkit.getScheduler().runTaskLater(Civilization.getInst(), () -> {
                Util.broadcast(new TextComponent(ChatColor.YELLOW + "월드를 생성합니다... " + ChatColor.GRAY + "2/3"));
                WorldCreator creator_nether = new WorldCreator(WORLD_NETHER_NAME);
                world_nether = new CivilizationWorld(Bukkit.createWorld(creator_nether.type(WorldType.NORMAL).environment(World.Environment.NETHER)), (float)worldSize / 8);

                Bukkit.getScheduler().runTaskLater(Civilization.getInst(), () -> {
                    Util.broadcast(new TextComponent(ChatColor.YELLOW + "월드를 생성합니다... " + ChatColor.GRAY + "3/3"));
                    WorldCreator creator_theEnd = new WorldCreator(WORLD_END_NAME);
                    world_end = new CivilizationWorld(Bukkit.createWorld(creator_theEnd.type(WorldType.NORMAL).environment(World.Environment.THE_END)), 1000);

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

                    genStructure(spawn);
                }, 20);
            }, 20);
        }, 40);
    }

    public static void genStructure(Map<Player, Location> spawn){
        Bukkit.getScheduler().runTaskAsynchronously(Civilization.getInst(), () -> {
            Set<String> keys = ConfigManager.config.getKeys("구조물");
            int outer = 0;
            for(String key : keys){
                if(key.trim().equalsIgnoreCase("")){
                    continue;
                }

                String rootKey = "구조물." + key;
                CivilizationWorld world = CivilizationWorld.getByName(ConfigManager.config.get(String.class, rootKey + ".world"));
                int repeat = ConfigManager.config.get(Integer.class, rootKey + ".count");
                int inner = 0;
                for(int i = 0; i < repeat; i++){
                    Util.broadcast(new TextComponent(ChatColor.YELLOW + "월드를 초기화합니다... " + ChatColor.GRAY +
                            outer + "/" + keys.size() + ChatColor.DARK_GRAY + "(" + inner++ + "/" + repeat + ")"));

                    List<String> variants = ConfigManager.config.getConfig().getStringList(rootKey + ".variants");
                    String name = key + variants.get((new Random()).nextInt(variants.size()));
                    NMSSupport.lazyPlaceStructure(world, name);
                }
                outer++;
            }

            Bukkit.getScheduler().runTask(Civilization.getInst(), () -> {
                ConfigManager.state.set("start", true);
                ConfigManager.state.save();
                isStart = true;
                Util.broadcast(new TextComponent(ChatColor.YELLOW + "플레이어를 이동시킵니다..."));
                for(Player player : spawn.keySet()){
                    if(player.isOnline()){
                        resetPlayer(player);
                        prepareNewPlayer(player);
                        player.teleport(spawn.get(player));
                    }
                }
                for(Player player : Bukkit.getOnlinePlayers().stream().filter(value -> PlayerListener.inValidWorld(value)).collect(Collectors.toList())){
                    resetPlayer(player);
                    prepareNewPlayer(player);
                    getNewSpawnPoint(player, false);
                }

                Util.broadcast(new TextComponent(ChatColor.YELLOW + "마무리 중..."));
                ConfigManager.cacheStore.checkExistAll();
                ConfigManager.cacheStore.saveAll();
                ConfigManager.saveStructure();

                Util.broadcast(new TextComponent(ChatColor.GREEN + "완료!"));
                onProgress = false;
                return;
            });
            return;
        });
    }

    public static Location getRandomLocation(int dx, int dz, boolean safe, CivilizationWorld world){
        Location output = null;
        do{
            output = world.getCenter().add(world.getRandom().nextInt((worldBorderRadius * 2) - dx) - worldBorderRadius, 0,
                    world.getRandom().nextInt((worldBorderRadius * 2) - dz)  - worldBorderRadius);
        }while (ConfigManager.isConflict(output, ""));

        if(safe){
            long time = System.currentTimeMillis();
            output.setY(257);
            while (SPAWN_BLOCK_SET.contains(output.getBlock().getType())){
                output.add(0, -1, 0);
            }
            output.add(0, 1, 0);
        }

        return output;
    }

    public static Location getNewSpawnPoint(Player player, boolean asynchronous){
        Location spawn = getRandomLocation(1, 1, true, world);

        spawn.clone().add(0, -1, 0).getBlock().setType(Material.BEDROCK);
        spawn.clone().getBlock().setType(Material.AIR);
        spawn.clone().add(0, 1, 0).getBlock().setType(Material.AIR);

        spawn.setWorld(world.get());
        spawn.add(0.5, 0, 0.5);
        if (asynchronous) {
            world.get().loadChunk(spawn.getChunk());
        }else{
            player.teleport(spawn);
        }
        player.setBedSpawnLocation(spawn, true);
        ConfigManager.getCache(player.getUniqueId()).setSpawn(spawn);
        ConfigManager.addStructure("spawn", world.getName(), spawn.clone().add(0, -1, 0), spawn.clone().add(0, 1, 0), true);

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

        Objective board = ScoreBoardHandler.getOrNew(player);
        board.setDisplayName("Civilization");
        ScoreBoardHandler.show(player, board);
        PlayerListener.updateScoreBoard(player);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        for(Player player : Bukkit.getOnlinePlayers()){
            GUIGetEvent event = new GUIGetEvent(player.getUniqueId());
            Bukkit.getPluginManager().callEvent(event);
            if(event.getTopGUI() != null){
                event.getTopGUI().closeAll();
            }
        }
        ConfigManager.saveShopItem();
        ConfigManager.cacheStore.saveAll();
        ConfigManager.saveStructure();
    }

    public static Civilization getInst() {
        return plugin;
    }

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
}
