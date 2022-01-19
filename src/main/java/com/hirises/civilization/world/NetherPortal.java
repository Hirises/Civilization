package com.hirises.civilization.world;

import com.hirises.civilization.Civilization;
import com.hirises.civilization.config.ConfigManager;
import org.bukkit.Axis;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.data.Orientable;

public class NetherPortal {

    private enum Direction{
        X,
        Z;
    }

    public static Location getPortal(String to, Location location){
        Direction direction;
        Location corner;
        if(isPortalBlock(location)){
            if(isPortalBlock(location.clone().add(-1, 0 ,0)) || isPortalBlock(location.clone().add(1, 0 ,0))){
                direction = Direction.X;
            }else{
                direction = Direction.Z;
            }
        }else{
            if(isPortalBlock(location.clone().add(-1, 0 ,0))){
                direction = Direction.Z;
                location.add(-1, 0, 0);
            }else if(isPortalBlock(location.clone().add(1, 0 ,0))){
                direction = Direction.Z;
                location.add(1, 0, 0);
            }else if(isPortalBlock(location.clone().add(0, 0 ,1))){
                direction = Direction.X;
                location.add(0, 0, 1);
            }else{
                direction = Direction.X;
                location.add(0, 0, -1);
            }
        }

        corner = analyzePortal(direction, location);
        if(direction.equals(Direction.X)){
            corner.add(-1, -1, 0);
        }else{
            corner.add(0, -1, -1);
        }
        corner.setX(corner.getBlockX());
        corner.setY(corner.getBlockY());
        corner.setZ(corner.getBlockZ());
        return findTwin(to, direction, corner);
    }

    private static boolean isPortalBlock(Location location){
        return location.getBlock().getType().equals(Material.NETHER_PORTAL);
    }

    private static Location analyzePortal(Direction direction, Location location){
        if(direction.equals(Direction.X)){
            if(isPortalBlock(location.clone().add(-1, 0 ,0))){
                return analyzePortal(Direction.X, location.clone().add(-1, 0 ,0));
            }
        }else{
            if(isPortalBlock(location.clone().add(0, 0 ,-1))){
                return analyzePortal(Direction.Z, location.clone().add(0, 0 ,-1));
            }
        }
        if(isPortalBlock(location.clone().add(0, -1 ,0))){
            return analyzePortal(Direction.X, location.clone().add(0, -1 ,0));
        }
        return location;
    }

    private static Location findTwin(String to, Direction direction, Location location){
        int findX;
        int findZ;
        Location out;
        int spawnY;
        int length;
        World world;
        int findRange;
        
        //도착 지역에 따른 변수 설정
        if(to.equalsIgnoreCase("Civilization")){
            findX = location.getBlockX() * 8;
            findZ = location.getBlockZ() * 8;
            world = Civilization.world.get();
            spawnY = (location.getBlockY() *  3) - 64;
            if(spawnY <= -54){  //월드 아래로 나가는거 방지
                spawnY = -54 + 1;
            }
            if(spawnY + 4 >= 310){  //월드 위로 나가는거 방지
                spawnY = 310 - 1 - 4;
            }
            findRange = 8 * 8;
        }else{
            findX = location.getBlockX() / 8;
            findZ = location.getBlockZ() / 8;
            world = Civilization.world_nether.get();
            spawnY = (location.getBlockY() + 64) / 3 ;
            if(spawnY <= 10){  //월드 아래로 나가는거 방지
                spawnY = 10 + 1;
            }
            if(spawnY + 4 >= 118){  //월드 위로 나가는거 방지
                spawnY = 118 - 1 - 4;
            }
            findRange = 8;
        }

        //보더 밖으로 나가는거 방지
        length = (int) world.getWorldBorder().getSize();
        if(direction.equals(Direction.X)){
            if(findX + 4 > length / 2){
                findX -= 4;
            }
        }else{
            if(findZ + 4 > length / 2){
                findZ -= 4;
            }
        }

        //지옥문 찾기
        final String prefix = "지옥문." + to + ".";
        for(int curX = -findRange; curX <= findRange; curX++){
            for(int curZ = -findRange; curZ <= findRange; curZ++){
                final String path = prefix + (findX + curX) + "/" + (findZ + curZ);
                if(ConfigManager.data.containKey(path)){
                    out = ConfigManager.data.getConfig().getLocation(path);
                    return out;
                }
            }
        }

        //없으면 생성
        Location spawn = new Location(world, findX, spawnY, findZ);
        Location cur;
        spawnTwin(direction, spawn);
        if(direction.equals(Direction.X)){
            spawn.add(1.5, 1, 0.5);
            cur = location.clone().add(1.5, 1, 0.5);
        }else{
            spawn.add(0.5, 1, 1.5);
            cur = location.clone().add(0.5, 1, 1.5);
        }

        //생성한거 저장
        ConfigManager.data.getConfig().set("지옥문." + location.getWorld().getName() + "." + location.getBlockX() + "/" + location.getBlockZ(), cur);
        ConfigManager.data.getConfig().set("지옥문." + to + "." + findX + "/" + findZ, spawn);
        ConfigManager.data.save();

        return spawn;
    }

    private static void spawnTwin(Direction direction, Location location){
        if(direction.equals(Direction.X)){
            setBlock(location, 0, 0, 0, Material.OBSIDIAN);
            setBlock(location, 1, 0, 0, Material.OBSIDIAN);
            setBlock(location, 2, 0, 0, Material.OBSIDIAN);
            setBlock(location, 3, 0, 0, Material.OBSIDIAN);

            setBlock(location, 0, 1, 0, Material.OBSIDIAN);
            setBlock(location, 3, 1, 0, Material.OBSIDIAN);

            setBlock(location, 0, 2, 0, Material.OBSIDIAN);
            setBlock(location, 3, 2, 0, Material.OBSIDIAN);

            setBlock(location, 0, 3, 0, Material.OBSIDIAN);
            setBlock(location, 3, 3, 0, Material.OBSIDIAN);

            setBlock(location, 0, 4, 0, Material.OBSIDIAN);
            setBlock(location, 1, 4, 0, Material.OBSIDIAN);
            setBlock(location, 2, 4, 0, Material.OBSIDIAN);
            setBlock(location, 3, 4, 0, Material.OBSIDIAN);


            setBlock(location, 1, 1, 0, Material.NETHER_PORTAL);
            setBlock(location, 2, 1, 0, Material.NETHER_PORTAL);

            setBlock(location, 1, 2, 0, Material.NETHER_PORTAL);
            setBlock(location, 2, 2, 0, Material.NETHER_PORTAL);

            setBlock(location, 1, 3, 0, Material.NETHER_PORTAL);
            setBlock(location, 2, 3, 0, Material.NETHER_PORTAL);
        }else{
            setBlock(location, 0, 0, 0, Material.OBSIDIAN);
            setBlock(location, 0, 0, 1, Material.OBSIDIAN);
            setBlock(location, 0, 0, 2, Material.OBSIDIAN);
            setBlock(location, 0, 0, 3, Material.OBSIDIAN);

            setBlock(location, 0, 1, 0, Material.OBSIDIAN);
            setBlock(location, 0, 1, 3, Material.OBSIDIAN);

            setBlock(location, 0, 2, 0, Material.OBSIDIAN);
            setBlock(location, 0, 2, 3, Material.OBSIDIAN);

            setBlock(location, 0, 3, 0, Material.OBSIDIAN);
            setBlock(location, 0, 3, 3, Material.OBSIDIAN);

            setBlock(location, 0, 4, 0, Material.OBSIDIAN);
            setBlock(location, 0, 4, 1, Material.OBSIDIAN);
            setBlock(location, 0, 4, 2, Material.OBSIDIAN);
            setBlock(location, 0, 4, 3, Material.OBSIDIAN);

            setBlock(location, 0, 1, 1, Material.NETHER_PORTAL);
            setBlock(location, 0, 1, 2, Material.NETHER_PORTAL);

            setBlock(location, 0, 2, 1, Material.NETHER_PORTAL);
            setBlock(location, 0, 2, 2, Material.NETHER_PORTAL);

            setBlock(location, 0, 3, 1, Material.NETHER_PORTAL);
            setBlock(location, 0, 3, 2, Material.NETHER_PORTAL);

            setAxisX(location, 0, 1, 1);
            setAxisX(location, 0, 1, 2);

            setAxisX(location, 0, 2, 1);
            setAxisX(location, 0, 2, 2);

            setAxisX(location, 0, 3, 1);
            setAxisX(location, 0, 3, 2);
        }
    }

    private static void setBlock(Location location, int x, int y, int z, Material material){
        location.clone().add(x, y, z).getBlock().setType(material);
    }

    private static void setAxisX(Location location, int x, int y, int z){
        Orientable orientable = (Orientable) location.clone().add(x, y, z).getBlock().getBlockData();
        orientable.setAxis(Axis.Z);
        location.clone().add(x, y, z).getBlock().setBlockData(orientable);
    }
}
