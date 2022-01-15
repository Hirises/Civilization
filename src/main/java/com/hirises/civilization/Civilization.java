package com.hirises.civilization;

import org.bukkit.plugin.java.JavaPlugin;

public final class Civilization extends JavaPlugin {

    private static Civilization plugin;

    @Override
    public void onEnable() {
        // Plugin startup logic
        plugin = this;
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public static Civilization getInst() {
        return plugin;
    }
}
