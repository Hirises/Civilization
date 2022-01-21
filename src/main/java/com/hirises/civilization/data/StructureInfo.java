package com.hirises.civilization.data;

import com.hirises.civilization.config.ConfigManager;
import com.hirises.core.data.LootTableUnit;
import com.hirises.core.data.unit.DataUnit;
import com.hirises.core.store.YamlStore;

import java.util.List;
import java.util.Random;

public class StructureInfo implements DataUnit {
    public static Vector3 defaultPointOffset;
    public static Vector3 defaultCenterOffset;

    private String prefix;
    private int count;
    private String world;
    private LootTableUnit loots;
    private List<String> variants;
    private String rootKey;
    private Vector3 pointOffset;
    private Vector3 centerOffset;

    public StructureInfo(){

    }

    public StructureInfo(String prefix, int count, String world, LootTableUnit loots, List<String> variants, String rootKey, Vector3 pointOffset, Vector3 centerOffset) {
        this.prefix = prefix;
        this.count = count;
        this.world = world;
        this.loots = loots;
        this.variants = variants;
        this.rootKey = rootKey;
        this.pointOffset = pointOffset;
        this.centerOffset = centerOffset;
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
        if(yml.containKey(rootKey + ".offset.point")){
            int x = yml.get(Integer.class, rootKey + ".offset.point.x");
            int y = yml.get(Integer.class, rootKey + ".offset.point.y");
            int z = yml.get(Integer.class, rootKey + ".offset.point.z");
            this.pointOffset = new Vector3(x, y, z);
        }else{
            this.pointOffset = defaultPointOffset;
        }
        if(yml.containKey(rootKey + ".offset.center")){
            int x = yml.get(Integer.class, rootKey + ".offset.center.x");
            int y = yml.get(Integer.class, rootKey + ".offset.center.y");
            int z = yml.get(Integer.class, rootKey + ".offset.center.z");
            this.centerOffset = new Vector3(x, y, z);
        }else{
            this.centerOffset = defaultCenterOffset;
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

    public Vector3 getCenterOffset() {
        return centerOffset;
    }

    public Vector3 getPointOffset() {
        return pointOffset;
    }
}
