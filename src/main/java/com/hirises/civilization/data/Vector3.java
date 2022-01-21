package com.hirises.civilization.data;

public record Vector3(int x, int y, int z) {
    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getZ() {
        return z;
    }
}
