package com.hirises.civilization.data;

import java.util.Objects;

public record ChunkData(String world, int x, int z) {

    public int getX() {
        return x;
    }

    public int getZ() {
        return z;
    }

    public String getWorld() {
        return world;
    }

    public boolean isLoaded() {
        return CivilizationWorld.getByName(world).get().isChunkLoaded(x, z);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChunkData chunkData = (ChunkData) o;
        return getX() == chunkData.getX() && getZ() == chunkData.getZ() && Objects.equals(getWorld(), chunkData.getWorld());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getX(), getZ(), getWorld());
    }

    @Override
    public String toString() {
        return "ChunkData{" +
                "world='" + world + '\'' +
                ", x=" + x +
                ", z=" + z +
                '}';
    }
}
