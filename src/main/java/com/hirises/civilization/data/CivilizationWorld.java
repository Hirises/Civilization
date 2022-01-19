package com.hirises.civilization.data;

import com.hirises.civilization.Civilization;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.Random;

public class CivilizationWorld {
    private final World world;
    private final Location center;
    private final String name;
    private final double size;
    private final double halfSize;
    private final Random random;

    public CivilizationWorld(World world, double size){
        this.world = world;
        this.center = new Location(world, 0, 0, 0);
        this.name = world.getName();
        this.size = size;
        this.halfSize = size / 2;
        this.random = new Random(world.getSeed());
    }

    public CivilizationWorld(World world){
        this(world, world.getWorldBorder().getSize());
    }

    public World get() {
        return world;
    }

    public Location getCenter() {
        return center.clone();
    }

    public String getName() {
        return name;
    }

    public double getSize() {
        return size;
    }

    public double getHalfSize() {
        return halfSize;
    }

    public Random getRandom() {
        return random;
    }

    public static CivilizationWorld getByName(String name){
        switch (name){
            case "Civilization":
                return Civilization.world;
            case "Civilization_Nether":
                return Civilization.world_nether;
            case "Civilization_TheEnd":
                return Civilization.world_end;
        }
        return null;
    }
}
