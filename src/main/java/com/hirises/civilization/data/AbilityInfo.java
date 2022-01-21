package com.hirises.civilization.data;

import com.hirises.core.data.unit.DataUnit;
import com.hirises.core.store.YamlStore;
import org.bukkit.Material;

import java.util.HashMap;
import java.util.Map;

public class AbilityInfo implements DataUnit {
    private AbilityType type;
    private String howToGet;
    private String effectString;
    private Material item;
    private int maxLevel;
    private int cost;
    private Map<String, Integer> properties;

    @Override
    public void load(YamlStore yml, String root) {
        this.type = AbilityType.valueOf(yml.get(String.class, root + ".타입"));
        this.howToGet = yml.get(String.class, root + ".획득법");
        this.effectString = yml.get(String.class, root + ".효과");
        this.item = Material.valueOf(yml.get(String.class, root + ".코드"));
        this.maxLevel = yml.get(Integer.class, root + ".최대레벨");
        this.cost = yml.get(Integer.class, root + ".코스트");
        this.properties = new HashMap<>();
        for(String key : yml.getKeys(root + ".설정")){
            this.properties.put(key, yml.get(Integer.class, root + ".설정." + key));
        }
    }

    @Override
    public void save(YamlStore yamlStore, String s) {
        //ignore
    }

    public AbilityType getType() {
        return type;
    }

    public String getHowToGet() {
        return howToGet;
    }

    public String getEffectString() {
        return effectString;
    }

    public Material getItem() {
        return item;
    }

    public int getMaxLevel() {
        return maxLevel;
    }

    public int getCost() {
        return cost;
    }

    public int getProperties(String key) {
        return properties.get(key);
    }
}
