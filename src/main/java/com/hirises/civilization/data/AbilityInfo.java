package com.hirises.civilization.data;

import com.hirises.core.data.unit.DataUnit;
import com.hirises.core.store.YamlStore;
import org.bukkit.Material;

public class AbilityInfo implements DataUnit {
    AbilityType type;
    String howToGet;
    String effectString;
    Material item;
    int maxLevel;
    int cost;

    @Override
    public void load(YamlStore yml, String root) {
        this.type = AbilityType.valueOf(yml.get(String.class, root + ".타입"));
        this.howToGet = yml.get(String.class, root + ".획득법");
        this.effectString = yml.get(String.class, root + ".효과");
        this.item = Material.valueOf(yml.get(String.class, root + ".코드"));
        this.maxLevel = yml.get(Integer.class, root + ".최대레벨");
        this.cost = yml.get(Integer.class, root + ".코스트");
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
}
