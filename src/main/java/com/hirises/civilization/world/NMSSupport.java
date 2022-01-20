package com.hirises.civilization.world;

import com.hirises.civilization.Civilization;
import com.hirises.civilization.config.ConfigManager;
import com.hirises.civilization.data.*;
import com.hirises.core.util.Pair;
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
import org.bukkit.*;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.FireworkMeta;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

public final class NMSSupport {
    public static final Set<Material> AVOID_SPAWN_BLOCKS = Collections.unmodifiableSet(Arrays.asList(
            Material.POWDER_SNOW
    ).stream().collect(Collectors.toSet()));

    //region util

    public static boolean inValidWorld(Player player){
        World world = player.getWorld();
        return !Civilization.world.get().equals(world) && !Civilization.world_nether.get().equals(world) && !Civilization.world_end.get().equals(world);
    }

    public static ChunkData toChunkData(String world, BlockVector3 vector3){
        Pair<Integer, Integer> chunkPos = toChunk(vector3.getX(), vector3.getZ());
        return new ChunkData(world, chunkPos.getLeft(), chunkPos.getRight());
    }

    public static ChunkData toChunkData(Location location){
        Pair<Integer, Integer> chunkPos = toChunk(location.getX(), location.getZ());
        return new ChunkData(location.getWorld().getName(), chunkPos.getLeft(), chunkPos.getRight());
    }

    public static Location toLocation(World world, BlockVector3 vector3){
        return new Location(world, vector3.getX(), vector3.getY(), vector3.getZ());
    }

    public static Pair<Integer, Integer> toChunk(double x, double z){
        int chunkX = (int) Math.floor(x / 16);
        int chunkZ = (int) Math.floor(z / 16);
        if(x < 0){
            chunkX--;
        }
        if(z < 0){
            chunkZ--;
        }
        return new Pair<>(chunkX, chunkZ);
    }

    //endregion

    //region structure

    public static Location getRandomLocation(CivilizationWorld world, int dx, int dz, boolean setY){
        Location output = null;
        int halfSize = (int)world.getHalfSize();
        do{
            output = world.getCenter().add(world.getRandom().nextInt((halfSize * 2) - dx) - halfSize, 0,
                    world.getRandom().nextInt((halfSize * 2) - dz)  - halfSize);
        }while (isConflict(output, ""));

        if(setY){
            long time = System.currentTimeMillis();
            output.setY(world.get().getHighestBlockYAt(output.getBlockX(), output.getBlockZ(), HeightMap.MOTION_BLOCKING_NO_LEAVES));
            output.add(0, 1, 0);
            while (AVOID_SPAWN_BLOCKS.contains(output.getBlock().getType())){
                output.add(0, 1, 0);
            }
        }

        return output;
    }

    public static void lazyPlaceStructure(StructureInfo data){
        String name = data.getRandomName();
        Clipboard clipboard = getStructure(name);

        Region region = clipboard.getRegion();
        int width = region.getWidth();
        int length = region.getLength();
        int height = region.getHeight();
        Location location = getRandomLocation(data.getWorld(), width, length,false);
        //offset
        Vector3 offset = data.getCenterOffset();
        Structure structure = ConfigManager.addStructure(data, location.clone().add(offset.getX(), offset.getY(), offset.getZ()),
                location.clone().add(width + offset.getX() - 1, offset.getY(), length + offset.getZ() - 1), false);
        if(structure.getMinChunk().isLoaded()){
            structure.place();
        }
    }

    public static void lazyPlaceStructure(StructureInfo data, Location location){
        String name = data.getRandomName();
        Clipboard clipboard = getStructure(name);

        Region region = clipboard.getRegion();
        int width = region.getWidth();
        int length = region.getLength();
        int height = region.getHeight();
        //offset
        Vector3 offset = data.getCenterOffset();
        Structure structure = ConfigManager.addStructure(data, location.clone().add(offset.getX(), offset.getY(), offset.getZ()),
                location.clone().add(width + offset.getX() - 1, height + offset.getY() - 1, length + offset.getZ() - 1), false);
        if(structure.getMinChunk().isLoaded()){
            structure.place();
        }
    }

    public static Clipboard getStructure(String name){
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

    public static void pasteStructure(Clipboard clipboard, Location location){
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
    
    public static boolean isConflict(String world, BlockVector3 vector3, String type){
        ChunkData chunk = NMSSupport.toChunkData(world, vector3);
        if(ConfigManager.structureList.containsKey(chunk)){
            if(ConfigManager.structureList.get(chunk).getType().startsWith(type)){
                return true;
            }
        }
        return false;
    }

    public static boolean isConflict(String world, int x, int y, int z, String type){
        Pair<Integer, Integer> chunkPos = NMSSupport.toChunk(x, z);
        ChunkData chunk = new ChunkData(world, chunkPos.getLeft(), chunkPos.getRight());
        if(ConfigManager.structureList.containsKey(chunk)){
            if(ConfigManager.structureList.get(chunk).getType().startsWith(type)){
                return true;
            }
        }
        return false;
    }

    public static boolean isConflict(Location location, String type){
        Pair<Integer, Integer> chunkPos = NMSSupport.toChunk(location.getX(), location.getZ());
        ChunkData chunk = new ChunkData(location.getWorld().getName(), chunkPos.getLeft(), chunkPos.getRight());
        if(ConfigManager.structureList.containsKey(chunk)){
            if(ConfigManager.structureList.get(chunk).getType().startsWith(type)){
                return true;
            }
        }
        return false;
    }

    public static boolean isConflict(ChunkData chunk, String type){
        if(ConfigManager.structureList.containsKey(chunk)){
            if(ConfigManager.structureList.get(chunk).getType().startsWith(type)){
                return true;
            }
        }
        return false;
    }

    //endregion

    public static void setBlocks(Location location, int x, int y, int z, int dx, int dy, int dz, Material material){
        for(int curX = x; curX < x + dx; curX++){
            for(int curY = y; curY < y + dy; curY++){
                for(int curZ = z; curZ < z + dz; curZ++){
                    location.clone().add(curX, curY, curZ).getBlock().setType(material);
                }
            }
        }
    }

    public static void spawnFireWork(Location location){
        Firework fw = location.getWorld().spawn(location, Firework.class);
        FireworkMeta meta = fw.getFireworkMeta();
        meta.addEffect(FireworkEffect.builder().withColor(Color.RED, Color.ORANGE, Color.YELLOW, Color.GREEN, Color.LIME)
                .flicker(true).trail(true).with(FireworkEffect.Type.BALL_LARGE)
                .withFade(Color.BLUE, Color.TEAL, Color.AQUA, Color.PURPLE, Color.FUCHSIA).build());
        meta.setPower(3);
        fw.setFireworkMeta(meta);
    }
}
