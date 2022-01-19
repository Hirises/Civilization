package com.hirises.civilization.util;
import com.hirises.core.data.unit.DataUnit;
import com.hirises.core.store.YamlStore;
import org.bukkit.HeightMap;
import org.bukkit.Location;
import org.bukkit.World;

public class Structure implements DataUnit {
    int minX;
    int minY;
    int minZ;
    int maxX;
    int maxY;
    int maxZ;
    String world;
    String type;
    boolean placed;

    public Structure(){

    }

    public Structure(String type, String world, Location loc1, Location loc2, boolean placed){
        minX = Math.min(loc1.getBlockX(), loc2.getBlockX());
        minZ = Math.min(loc1.getBlockZ(), loc2.getBlockZ());
        maxX = Math.max(loc1.getBlockX(), loc2.getBlockX());
        maxZ = Math.max(loc1.getBlockZ(), loc2.getBlockZ());
        this.world = world;
        this.type = type;
        this.placed = placed;
    }

    public Structure(String type, String world, int minX, int minZ, int maxX, int maxZ, boolean placed){
        this.minX = minX;
        this.minZ = minZ;
        this.maxX = maxX;
        this.maxZ = maxZ;
        this.world = world;
        this.placed = placed;
    }

    public void place(){
        World world = CivilizationWorld.getByName(this.world).get();
        int y = world.getHighestBlockYAt(minX, minZ, HeightMap.MOTION_BLOCKING_NO_LEAVES);
        NMSSupport.pasteStructure(NMSSupport.getStructure(type), new Location(world, minX - 1, y, minZ - 1));
        placed = true;
    }

    @Override
    public void load(YamlStore yml, String root) {
        minX = yml.get(Integer.class, root + ".minX");
        minZ = yml.get(Integer.class, root + ".minZ");
        maxX = yml.get(Integer.class, root + ".maxX");
        maxZ = yml.get(Integer.class, root + ".maxZ");
        world = yml.get(String.class, root + ".world");
        type = yml.get(String.class, root + ".type");
        placed = yml.get(Boolean.class, root + ".placed");
    }

    @Override
    public void save(YamlStore yml, String root) {
        yml.upsert(minX, root + ".minX");
        yml.upsert(minZ, root + ".minZ");
        yml.upsert(maxX, root + ".maxX");
        yml.upsert(maxZ, root + ".maxZ");
        yml.upsert(world, root + ".world");
        yml.upsert(type, root + ".type");
        yml.upsert(placed, root + ".placed");
    }

    public int getMinX() {
        return minX;
    }

    public int getMinZ() {
        return minZ;
    }

    public int getMaxX() {
        return maxX;
    }

    public int getMaxZ() {
        return maxZ;
    }

    public String getWorld() {
        return world;
    }

    public String getType() {
        return type;
    }

    public boolean isPlaced() {
        return placed;
    }
}
