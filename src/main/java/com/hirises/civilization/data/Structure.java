package com.hirises.civilization.data;
import com.hirises.civilization.config.ConfigManager;
import com.hirises.civilization.world.NMSSupport;
import com.hirises.core.data.LootTableUnit;
import com.hirises.core.data.unit.DataUnit;
import com.hirises.core.store.YamlStore;
import com.hirises.core.util.Pair;
import com.hirises.core.util.Util;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.math.BlockVector3;
import org.bukkit.HeightMap;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.inventory.ItemStack;

public class Structure implements DataUnit {
    int minX;
    int minY;
    int minZ;
    int maxX;
    int maxY;
    int maxZ;
    ChunkData minChunk;
    StructureInfo info;
    String variant;
    boolean placed;

    public Structure(){

    }

    public Structure(StructureInfo info, Location loc1, Location loc2, boolean placed){
        this(info, info.getRandomVariant(),
                Math.min(loc1.getBlockX(), loc2.getBlockX()), Math.min(loc1.getBlockY(), loc2.getBlockY()), Math.min(loc1.getBlockZ(), loc2.getBlockZ()),
                Math.max(loc1.getBlockX(), loc2.getBlockX()), Math.max(loc1.getBlockY(), loc2.getBlockY()), Math.max(loc1.getBlockZ(),
                        loc2.getBlockZ()), placed);
    }

    public Structure(StructureInfo structureInfo, String variant, int minX, int miny, int minZ, int maxX, int maxY, int maxZ, boolean placed){
        this.minX = minX;
        this.minY = miny;
        this.minZ = minZ;
        this.maxX = maxX;
        this.minY = maxY;
        this.maxZ = maxZ;
        this.info = structureInfo;
        this.placed = placed;
        this.variant = variant;
        Pair<Integer, Integer> chunkPos = NMSSupport.toChunk(minX, minZ);
        this.minChunk = new ChunkData(info.getWorldName(), chunkPos.getLeft(), chunkPos.getRight());
    }

    public void place(){
        World world = info.getWorld().get();
        int y = world.getHighestBlockYAt(minX, minZ, HeightMap.MOTION_BLOCKING_NO_LEAVES);
        y += 1;
        Clipboard clipboard = NMSSupport.getStructure(getType());
        Vector3 offset = info.getPointOffset();
        Location place = new Location(world, minX + offset.getX(), y  + offset.getY() + info.getCenterOffset().getY(), minZ  + offset.getZ());
        this.minY = y + info.getCenterOffset().getY();
        this.maxY = this.minY + clipboard.getRegion().getHeight() - 1;

        NMSSupport.pasteStructure(clipboard, place);

        if(info.getLoots() != null){
            LootTableUnit lootTable = info.getLoots();
            for(int curX = minX; curX <= maxX; curX++){
                for(int curY = minY; curY <= maxY; curY++){
                    for(int curZ = minZ; curZ <= maxZ; curZ++){
                        Block block = world.getBlockAt(curX, curY, curZ);

                        if(block.getType().equals(Material.CHEST)){
                            Chest chest = (Chest) block.getState();
                            chest.getInventory().setContents(lootTable.getRandomly().toArray(new ItemStack[0]));
                        }
                    }
                }
            }
        }

        placed = true;
    }

    @Override
    public void load(YamlStore yml, String root) {
        this.minX = yml.get(Integer.class, root + ".minX");
        this.minY = yml.get(Integer.class, root + ".minY");
        this.minZ = yml.get(Integer.class, root + ".minZ");
        this.maxX = yml.get(Integer.class, root + ".maxX");
        this.maxY = yml.get(Integer.class, root + ".maxY");
        this.maxZ = yml.get(Integer.class, root + ".maxZ");
        this.info = ConfigManager.structureData.get(yml.get(String.class, root + ".info"));
        this.variant = yml.get(String.class, root + ".variant");
        this.placed = yml.get(Boolean.class, root + ".placed");
        Pair<Integer, Integer> chunkPos = NMSSupport.toChunk(minX, minZ);
        this.minChunk = new ChunkData(info.getWorldName(), chunkPos.getLeft(), chunkPos.getRight());
    }

    @Override
    public void save(YamlStore yml, String root) {
        yml.upsert(minX, root + ".minX");
        yml.upsert(minY, root + ".minY");
        yml.upsert(minZ, root + ".minZ");
        yml.upsert(maxX, root + ".maxX");
        yml.upsert(maxY, root + ".maxY");
        yml.upsert(maxZ, root + ".maxZ");
        yml.upsert(info.getRootKey(), root + ".info");
        yml.upsert(variant, root + ".variant");
        yml.upsert(placed, root + ".placed");
    }

    public int getMinX() {
        return minX;
    }

    public int getMinY() {
        return minY;
    }

    public int getMinZ() {
        return minZ;
    }

    public int getMaxX() {
        return maxX;
    }

    public int getMaxY() {
        return maxY;
    }

    public int getMaxZ() {
        return maxZ;
    }

    public StructureInfo getStructureInfo() {
        return info;
    }

    public String getVariant() {
        return variant;
    }

    public String getType(){
        return info.getPrefix() + variant;
    }

    public boolean isPlaced() {
        return placed;
    }

    public ChunkData getMinChunk() {
        return minChunk;
    }
}
