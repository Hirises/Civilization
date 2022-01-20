package com.hirises.civilization.data;

import com.hirises.civilization.config.ConfigManager;
import com.hirises.core.data.LootTableUnit;
import com.hirises.core.data.unit.DataUnit;
import com.hirises.core.store.YamlStore;

import java.util.List;
import java.util.Random;

public class StructureInfo implements DataUnit {
    String prefix;
    int count;
    String world;
    LootTableUnit loots;
    List<String> variants;
    String rootKey;

    public StructureInfo(){

    }

    public StructureInfo(String prefix, int count, String world, LootTableUnit loots, List<String> variants, String rootKey) {
        this.prefix = prefix;
        this.count = count;
        this.world = world;
        this.loots = loots;
        this.variants = variants;
        this.rootKey = rootKey;
    }

    @Override
    public void load(YamlStore yml, String rootKey) {
        this.rootKey = rootKey.substring(rootKey.lastIndexOf(".") + 1);
        this.prefix = yml.get(String.class, rootKey + ".name");
        this.count = yml.get(Integer.class, rootKey + ".count");
        this.world = yml.get(String.class, rootKey + ".world");
        if(yml.containKey(rootKey + ".loots")){
            this.loots = ConfigManager.lootTable.get(yml.get(String.class, rootKey + ".loots"));
        }else{
            this.loots = null;
        }
        this.variants = yml.getConfig().getStringList(rootKey + ".variants");
    }

    @Override
    public void save(YamlStore yamlStore, String s) {
        //ignore
    }

    public int getCount() {
        return count;
    }

    public CivilizationWorld getWorld(){
        return CivilizationWorld.getByName(world);
    }

    public String getWorldName() {
        return world;
    }

    public LootTableUnit getLoots() {
        return loots;
    }

    public List<String> getVariants() {
        return variants;
    }

    public String getPrefix() {
        return prefix;
    }

    public String getRandomName(){
        return prefix + getRandomVariant();
    }

    public String getRandomVariant(){
        return variants.get(new Random().nextInt(variants.size()));
    }

    public String getRootKey() {
        return rootKey;
    }
}
