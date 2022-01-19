package com.hirises.civilization.util;

import com.hirises.civilization.Civilization;
import com.hirises.civilization.config.ConfigManager;
import com.hirises.core.store.YamlStore;
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
import org.bukkit.Location;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public final class NMSSupport {
    public static ChunkData toChunkData(String world, BlockVector3 vector3){
        Pair<Integer, Integer> chunkPos = toChunk(vector3.getX(), vector3.getZ());
        return new ChunkData(world, chunkPos.getLeft(), chunkPos.getRight());
    }

    public static ChunkData toChunkData(Location location){
        Pair<Integer, Integer> chunkPos = toChunk(location.getX(), location.getZ());
        return new ChunkData(location.getWorld().getName(), chunkPos.getLeft(), chunkPos.getRight());
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

    public static void lazyPlaceStructure(CivilizationWorld world, String type){
        Clipboard clipboard = getStructure(type);

        Region region = clipboard.getRegion();
        int width = region.getWidth() - 1;
        int length = region.getLength() - 1;
        Location location = Civilization.getRandomLocation(width, length,false, world);
        ConfigManager.addStructure(type, world.getName(), location, location.clone().add(width - 1, 0, length - 1));
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
