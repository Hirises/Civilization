package com.hirises.civilization.util;

import com.hirises.civilization.Civilization;
import com.hirises.civilization.config.ConfigManager;
import com.hirises.core.data.LootTableUnit;
import com.hirises.core.data.unit.DataUnit;
import com.hirises.core.data.unit.DirDataCache;
import com.hirises.core.store.YamlStore;
import com.hirises.core.util.Util;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.session.ClipboardHolder;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class Structure implements DataUnit {
    int minX;
    int minY;
    int minZ;
    int maxX;
    int maxY;
    int maxZ;
    String world;
    String type;

    public Structure(){

    }

    public Structure(String type, String world, Location loc1, Location loc2){
        minX = Math.min(loc1.getBlockX(), loc2.getBlockX());
        minZ = Math.min(loc1.getBlockZ(), loc2.getBlockZ());
        maxX = Math.max(loc1.getBlockX(), loc2.getBlockX());
        maxZ = Math.max(loc1.getBlockZ(), loc2.getBlockZ());
        this.world = world;
        this.type = type;
    }

    public Structure(String type, String world, int minX, int minZ, int maxX, int maxZ){
        this.minX = minX;
        this.minZ = minZ;
        this.maxX = maxX;
        this.maxZ = maxZ;
        this.world = world;
    }

    public void add(Map<ChunkData, Structure> structureList){
        for(int x = minX / 16; x <= maxX / 16; x++){
            for(int z = minZ / 16; z <= maxZ / 16; z++){
                structureList.put(new ChunkData(world, x, z), this);
            }
        }
    }

    @Override
    public void load(YamlStore yml, String root) {
        minX = yml.get(Integer.class, root + ".minX");
        minZ = yml.get(Integer.class, root + ".minZ");
        maxX = yml.get(Integer.class, root + ".maxX");
        maxZ = yml.get(Integer.class, root + ".maxZ");
        world = yml.get(String.class, root + ".world");
        type = yml.get(String.class, root + ".type");
    }

    @Override
    public void save(YamlStore yml, String root) {
        yml.upsert(minX, root + ".minX");
        yml.upsert(minZ, root + ".minZ");
        yml.upsert(maxX, root + ".maxX");
        yml.upsert(maxZ, root + ".maxZ");
        yml.upsert(world, root + ".world");
        yml.upsert(type, root + ".type");
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

    public static void randomStructure(CivilizationWorld world, YamlStore yml, DirDataCache<LootTableUnit> lootYml, String rootKey, String rawName){
        List<String> variants = yml.getConfig().getStringList(rootKey + ".variants");
        String name = rawName + variants.get((new Random()).nextInt(variants.size()));


        Region region = placeStructure(name, world);
        if(yml.containKey(rootKey + ".loots")){
            LootTableUnit lootTable = lootYml.get(yml.get(String.class, rootKey + ".loots"));
            region.forEach(vector3 -> {
                Block block = convertToLocation(world.get(), vector3).getBlock();
                if(block.getType().equals(Material.CHEST)){
                    Chest chest = (Chest) block.getState();
                    chest.getBlockInventory().setContents(lootTable.getRandomly().toArray(new ItemStack[0]));
                }
            });
        }
    }

    public static Location convertToLocation(World world, BlockVector3 vector3){
        return new Location(world, vector3.getBlockX(), vector3.getBlockY(), vector3.getBlockZ());
    }



    public static Region placeStructure(String name, CivilizationWorld world){
        Clipboard clipboard = getStructure(name);

        Region region = clipboard.getRegion();
        int width = region.getWidth() - 1;
        int length = region.getLength() - 1;
        Util.logging("------- pre location" + System.currentTimeMillis());
        Location location = Civilization.getRandomLocation(width, length,true, world);
        Util.logging("------- post location" + System.currentTimeMillis());
        ConfigManager.addStructure(name, world.getName(), location, location.clone().add(width - 1, 0, length - 1));

        pasteStructure(clipboard, location);

        return region;
    }

    private static Clipboard getStructure(String name){
        Clipboard clipboard = null;
        File file = new File(Civilization.getInst().getDataFolder().getAbsolutePath() + "/Schematics/" + name + ".schem");
        ClipboardFormat format = ClipboardFormats.findByFile(file);
        try (ClipboardReader reader = format.getReader(new FileInputStream(file))) {
            clipboard = reader.read();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return clipboard;
    }

    private static void pasteStructure(Clipboard clipboard, Location location){
        location.add(-1, 0, -1);    //보정
        try (EditSession editSession = WorldEdit.getInstance().newEditSession(new BukkitWorld(location.getWorld()))) {
            Operation operation = new ClipboardHolder(clipboard)
                    .createPaste(editSession)
                    .to(BlockVector3.at(location.getBlockX(), location.getBlockY(), location.getBlockZ()))
                    .build();
            Operations.complete(operation);
        } catch (WorldEditException e) {
            e.printStackTrace();
        }
    }
}
