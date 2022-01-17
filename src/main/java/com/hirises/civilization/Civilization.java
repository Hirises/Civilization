package com.hirises.civilization;

import com.hirises.civilization.command.UserCommand;
import com.hirises.civilization.config.ConfigManager;
import com.hirises.civilization.player.PlayerListener;
import com.hirises.core.event.CoreInitEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public final class Civilization extends JavaPlugin implements Listener {

    private static Civilization plugin;

    @Override
    public void onEnable() {
        // Plugin startup logic
        plugin = this;

        getCommand("menu").setExecutor(new UserCommand());
        getCommand("money").setExecutor(new UserCommand());

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
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        ConfigManager.saveShopItem();
        ConfigManager.cacheStore.saveAll();
    }

    public static Civilization getInst() {
        return plugin;
    }
}
