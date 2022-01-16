package com.hirises.civilization.config;

public enum Keys{
    EnableItem("civilization_item_enabled"),
    ProjectileLauncher("civilization_projectile_lunch"),
    ;

    private String key;

    Keys(String key){
        this.key = key;
    }

    @Override
    public String toString() {
        return key;
    }
}
