package com.hirises.civilization.util;
import com.hirises.core.data.unit.DataUnit;
import com.hirises.core.store.YamlStore;
import com.hirises.core.util.Pair;
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
    ChunkData minChunk;
    String world;
    String type;
    boolean placed;

    public Structure(){

    }

    public Structure(String type, String world, Location loc1, Location loc2, boolean placed){
        this(type, world, Math.min(loc1.getBlockX(), loc2.getBlockX()), Math.min(loc1.getBlockZ(), loc2.getBlockZ()),
                Math.max(loc1.getBlockX(), loc2.getBlockX()), Math.max(loc1.getBlockZ(), loc2.getBlockZ()), placed);
    }

    public Structure(String type, String world, int minX, int minZ, int maxX, int maxZ, boolean placed){
        this.minX = minX;
        this.minZ = minZ;
        this.maxX = maxX;
        this.maxZ = maxZ;
        this.world = world;
        this.placed = placed;
        this.type = type;
        Pair<Integer, Integer> chunkPos = NMSSupport.toChunk(minX, minZ);
        this.minChunk = new ChunkData(world, chunkPos.getLeft(), chunkPos.getRight());
    }

    public void place(){
        World world = CivilizationWorld.getByName(this.world).get();
        int y = world.getHighestBlockYAt(minX, minZ, HeightMap.MOTION_BLOCKING_NO_LEAVES);
        y += 1;
        NMSSupport.pasteStructure(NMSSupport.getStructure(type), new Location(world, minX - 1, y, minZ - 1));
        placed = true;
    }

    @Override
    public void load(YamlStore yml, String root) {
        this.minX = yml.get(Integer.class, root + ".minX");
        this.minZ = yml.get(Integer.class, root + ".minZ");
        this.maxX = yml.get(Integer.class, root + ".maxX");
        this.maxZ = yml.get(Integer.class, root + ".maxZ");
        this.world = yml.get(String.class, root + ".world");
        this.type = yml.get(String.class, root + ".type");
        this.placed = yml.get(Boolean.class, root + ".placed");
        Pair<Integer, Integer> chunkPos = NMSSupport.toChunk(minX, minZ);
        this.minChunk = new ChunkData(world, chunkPos.getLeft(), chunkPos.getRight());
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

    public ChunkData getMinChunk() {
        return minChunk;
    }
}
