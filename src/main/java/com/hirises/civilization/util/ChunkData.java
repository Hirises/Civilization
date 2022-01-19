package com.hirises.civilization.util;

import java.util.Objects;

public record ChunkData(String world, int x, int y) {

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public String getWorld() {
        return world;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChunkData chunkData = (ChunkData) o;
        return getX() == chunkData.getX() && getY() == chunkData.getY() && Objects.equals(getWorld(), chunkData.getWorld());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getX(), getY(), getWorld());
    }
}
