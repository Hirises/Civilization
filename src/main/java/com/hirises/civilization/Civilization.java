package com.hirises.civilization;

import com.hirises.civilization.command.OPCommand;
import com.hirises.civilization.command.UserCommand;
import com.hirises.civilization.config.ConfigManager;
import com.hirises.civilization.player.PlayerListener;
import com.hirises.core.data.TimeUnit;
import com.hirises.core.display.ScoreBoardHandler;
import com.hirises.core.event.CoreInitEvent;
import com.hirises.core.event.GUIGetEvent;
import com.hirises.core.task.CancelableTask;
import com.hirises.core.util.Util;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.Objective;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

public final class Civilization extends JavaPlugin implements Listener {

    private static Civilization plugin;
    private static boolean isStart;
    public static World world;
    public static World world_nether;
    public static World world_end;
    public volatile static Boolean onProgress;

    @Override
    public void onEnable() {
        // Plugin startup logic
        plugin = this;
        onProgress = false;

        getCommand("menu").setExecutor(new UserCommand());
        getCommand("money").setExecutor(new UserCommand());
        getCommand("civilization").setExecutor(new OPCommand());

        Bukkit.getPluginManager().registerEvents(plugin, plugin);
        Bukkit.getPluginManager().registerEvents(new PlayerListener(), plugin);

        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            Bukkit.broadcast(new TextComponent(ChatColor.YELLOW + "저장중입니다..."));
            ConfigManager.cacheStore.saveAll();
            ConfigManager.cacheStore.checkExistAll();
        }, 5 * 60 * 20, 5 * 60 * 20);
    }

    @EventHandler
    public void onCoreInit(CoreInitEvent event){
        ConfigManager.init();
        isStart = ConfigManager.save.get(Boolean.class, "start");
        if(isStart()){
            world = Bukkit.createWorld(new WorldCreator("Civilization"));
            world_nether = Bukkit.createWorld(new WorldCreator("Civilization_Nether"));
            world_end = Bukkit.createWorld(new WorldCreator("Civilization_TheEnd"));
        }
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

            Bukkit.broadcast(new TextComponent(ChatColor.RED + "게임을 초기화합니다..."));
            ConfigManager.save.removeKey("자유시장");
            ConfigManager.save.save();
            ConfigManager.cacheStore.removeAll();
            ConfigManager.cacheStore.checkExistAll();
            ConfigManager.cacheStore.saveAll();
            if(isStart){
                Bukkit.broadcast(new TextComponent(ChatColor.YELLOW + "플레이어를 이동시킵니다..."));
                World overWorld = Bukkit.getWorld("world");
                for(Player player : world.getPlayers()){
                    player.teleport(overWorld.getSpawnLocation());
                }
                Bukkit.unloadWorld(world, false);
                for(Player player : world_nether.getPlayers()){
                    player.teleport(overWorld.getSpawnLocation());
                }
                Bukkit.unloadWorld(world_nether, false);
                for(Player player : world_end.getPlayers()){
                    player.teleport(overWorld.getSpawnLocation());
                }
                Bukkit.unloadWorld(world_end, false);

                Bukkit.broadcast(new TextComponent(ChatColor.YELLOW + "플레이어를 초기화시킵니다..."));
                for(Player player : Bukkit.getOnlinePlayers()){
                    resetPlayer(player);
                }

                Bukkit.broadcast(new TextComponent(ChatColor.YELLOW + "월드 제거중..."));
                File file = plugin.getDataFolder();
                String path = file.getAbsolutePath();
                path = path.substring(0, path.lastIndexOf("\\"));
                path = path.substring(0, path.lastIndexOf("\\"));
                path += "\\Civilization";
                deleteWalk(Path.of(path));

                path = file.getAbsolutePath();
                path = path.substring(0, path.lastIndexOf("\\"));
                path = path.substring(0, path.lastIndexOf("\\"));
                path += "\\Civilization_Nether";
                deleteWalk(Path.of(path));

                path = file.getAbsolutePath();
                path = path.substring(0, path.lastIndexOf("\\"));
                path = path.substring(0, path.lastIndexOf("\\"));
                path += "\\Civilization_TheEnd";
                deleteWalk(Path.of(path));
            }

            Bukkit.broadcast(new TextComponent(ChatColor.YELLOW + "마무리 중..."));
            isStart = false;
            ConfigManager.save.set("start", false);
            ConfigManager.save.save();

            Bukkit.broadcast(new TextComponent(ChatColor.GREEN + "완료!"));

            onProgress = false;
        }, 20);
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

    public static void resetPlayer(Player player){
        ConfigManager.cacheStore.remove(player);
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

            Bukkit.broadcast(new TextComponent(ChatColor.GREEN + "새로운 게임을 시작합니다..."));
            int worldSize = ConfigManager.config.get(Integer.class, "월드지름");

            Bukkit.broadcast(new TextComponent(ChatColor.YELLOW + "월드를 생성합니다... " + ChatColor.GRAY + "1/3"));
            WorldCreator creator = new WorldCreator("Civilization");
            creator.type(WorldType.NORMAL);
            creator.environment(World.Environment.NORMAL);
            world = Bukkit.createWorld(creator);
            WorldBorder border = world.getWorldBorder();
            border.setCenter(0, 0);
            border.setSize(worldSize);

            Bukkit.getScheduler().runTaskLater(Civilization.getInst(), () -> {
                Bukkit.broadcast(new TextComponent(ChatColor.YELLOW + "월드를 생성합니다... " + ChatColor.GRAY + "2/3"));
                WorldCreator creator_nether = new WorldCreator("Civilization_Nether");
                creator.type(WorldType.NORMAL);
                creator_nether.environment(World.Environment.NETHER);
                world_nether = Bukkit.createWorld(creator_nether);
                WorldBorder border_nether = world_nether.getWorldBorder();
                border_nether.setCenter(0, 0);
                border_nether.setSize(worldSize / 8);

                Bukkit.getScheduler().runTaskLater(Civilization.getInst(), () -> {
                    Bukkit.broadcast(new TextComponent(ChatColor.YELLOW + "월드를 생성합니다... " + ChatColor.GRAY + "3/3"));
                    WorldCreator creator_theEnd = new WorldCreator("Civilization_TheEnd");
                    creator_theEnd.type(WorldType.NORMAL);
                    creator_theEnd.environment(World.Environment.THE_END);
                    world_end = Bukkit.createWorld(creator_theEnd);
                    WorldBorder border_end = world_nether.getWorldBorder();
                    border_end.setCenter(0, 0);
                    border_end.setSize(500);

                    Map<Player, Location> spawn = new HashMap<>();
                    for(Player player : Bukkit.getOnlinePlayers()){
                        spawn.put(player, getNewSpawnPoint(player, true));
                    }

                    new CancelableTask(Civilization.getInst(), 0 , 60){
                        int count = 0;
                        @Override
                        public void run() {
                            if(count >= 100){
                                ConfigManager.save.set("start", true);
                                ConfigManager.save.save();
                                isStart = true;
                                Bukkit.broadcast(new TextComponent(ChatColor.YELLOW + "플레이어를 이동시킵니다..."));
                                for(Player player : spawn.keySet()){
                                    if(player.isOnline()){
                                        prepareNewPlayer(player);
                                        player.teleport(spawn.get(player));
                                    }
                                }
                                for(Player player : Bukkit.getOnlinePlayers().stream().filter(value -> PlayerListener.inValidWorld(value)).collect(Collectors.toList())){
                                    prepareNewPlayer(player);
                                    getNewSpawnPoint(player, false);
                                }
                                Bukkit.broadcast(new TextComponent(ChatColor.GREEN + "완료!"));
                                cancel();
                                onProgress = false;
                                return;
                            }
                            count += 10;
                            Bukkit.broadcast(new TextComponent(ChatColor.YELLOW + "월드를 초기화합니다... " + ChatColor.GRAY + count + "/100"));
                        }
                    };
                }, 20);
            }, 20);
        }, 40);
    }

    public static Location getNewSpawnPoint(Player player, boolean asynchronous){
        int spawnRadius = ConfigManager.config.get(Integer.class, "월드지름") / 2;
        Location location = world.getWorldBorder().getCenter();
        Location spawn = location.clone().add(new Random().nextInt(spawnRadius * 2) - spawnRadius, 0, new Random().nextInt(spawnRadius * 2)  - spawnRadius);
        spawn.setY(world.getHighestBlockYAt(spawn.getBlockX(), spawn.getBlockZ()));
        while (spawn.getBlock().getType().equals(Material.WATER) || spawn.getBlock().getType().equals(Material.LAVA)){
            spawn.add(0, 1, 0);
        }
        spawn.clone().add(0, -1 ,0).getBlock().setType(Material.BEDROCK);
        spawn.setWorld(world);
        if (asynchronous) {
            world.loadChunk(spawn.getChunk());
        }else{
            player.teleport(spawn);
        }
        player.setBedSpawnLocation(spawn, true);
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
    }

    public static Civilization getInst() {
        return plugin;
    }
}
