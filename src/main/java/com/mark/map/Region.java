package com.mark.map;

import com.mark.Buffer;

import java.util.Arrays;

public final class Region {


    public static int[] COSINE = new int[2048];


    private final int width;
    private final int length;
    private final int[][][] tileHeights;
    public final byte[][][] tileFlags;

    private final short[][][] underlays;
    private final short[][][] overlays;
    private final byte[][][] overlayShapes;
    private final byte[][][] overlayOrientations;
    public byte[][][] manualTileHeight;

    public Region(byte[][][] tileFlags, int[][][] tileHeights) {
        width = 104;
        length = 104;

        this.tileHeights = new int[4][width + 1][length + 1];
        this.tileFlags = new byte[4][width][length];
        underlays = new short[4][width][length];
        overlays = new short[4][width][length];
        manualTileHeight = new byte[4][width][length];
        overlayShapes = new byte[4][width][length];
        overlayOrientations = new byte[4][width][length];

    }




    public final void unpackTiles(byte[] data, int dX, int dY, int regionX, int regionY) {

        Buffer buffer = new Buffer(data);


        for (int z = 0; z < 4; z++) {
            for (int localX = 0; localX < 64; localX++) {
                for (int localY = 0; localY < 64; localY++) {
                    readTile(buffer, localX + dX, localY + dY, z, regionX, regionY, 0);
                }
            }
        }


        this.setHeights();// XXX Fix for ending of region sloping down

    }

    public void setHeights() {
        // TODO Find a better way to fix the sloping issue

        for(int z = 0;z<4;z++) {
            for(int y = 0;y<=length;y++) {
                tileHeights[z][width][y] = tileHeights[z][width - 1][y];
            }


            for(int x = 0;x<=width;x++) {
                tileHeights[z][x][length] = tileHeights[z][x][length - 1];
            }

        }

    }


    private void readTile(Buffer buffer, int x, int y, int z, int regionX, int regionY, int orientation) {// XXX

        if (x >= 0 && x < 64 && y >= 0 && y < 64) {
            tileFlags[z][x][y] = 0;
            try {
                do {
                    int type = buffer.readUByte();
                    if (type == 0) {
                        manualTileHeight[z][x][y] = 0;
                        if (z == 0) {
                            tileHeights[0][x][y] = -calculateHeight(0xe3b7b + x + regionX, 0x87cce + y + regionY) * 8;
                        } else {
                            tileHeights[z][x][y] = tileHeights[z - 1][x][y] - 240;
                        }

                        return;
                    } else if (type == 1) {
                        manualTileHeight[z][x][y] = 1;
                        int height = buffer.readUByte();
                        if (height == 1) {
                            height = 0;
                        }
                        if (z == 0) {
                            tileHeights[0][x][y] = -height * 8;
                        } else {
                            tileHeights[z][x][y] = tileHeights[z - 1][x][y] - height * 8;
                        }

                        return;
                    } else if (type <= 49) {
                        overlays[z][x][y] = (short) buffer.readUByte();
                        overlayShapes[z][x][y] = (byte) ((type - 2) / 4);
                        overlayOrientations[z][x][y] = (byte) (type - 2 + orientation & 3);
                    } else if (type <= 81) {
                        tileFlags[z][x][y] = (byte) (type - 49);
                    } else {
                        underlays[z][x][y] = (short) (type - 81);
                    }
                } while (true);
            }catch(Exception e) {}
        }

        try {
            do {
                int in = buffer.readUByte();
                if (in == 0) {
                    break;
                } else if (in == 1) {
                    buffer.readUByte();
                    return;
                } else if (in <= 49) {
                    buffer.readUShort();
                }
            } while (true);
        }catch(Exception e) {}

    }

    public byte[] save_terrain_block() {
        Buffer buffer = new Buffer(new byte[131072]);
        for (int tile_y = 0; tile_y < 4; tile_y++) {
            for (int tile_x = 0; tile_x < 64; tile_x++) {
                for (int tile_z = 0; tile_z < 64; tile_z++) {
                    save_terrain_tile(tile_y, tile_x, tile_z, buffer);
                }

            }

        }

        byte[] data = Arrays.copyOf(buffer.getPayload(), buffer.getPosition());
        return data;
    }

    private static int calculateHeight(int x, int y) {
        int height = interpolatedNoise(x + 45365, y + 0x16713, 4) - 128
                + (interpolatedNoise(x + 10294, y + 37821, 2) - 128 >> 1) + (interpolatedNoise(x, y, 1) - 128 >> 2);
        height = (int) (height * 0.3D) + 35;

        if (height < 10) {
            height = 10;
        } else if (height > 60) {
            height = 60;
        }

        return height;
    }

    private void save_terrain_tile(int y, int x, int z, Buffer buffer) {
        if (overlays[y][x][z] != 0) {
            buffer.writeShort(overlayShapes[y][x][z] * 4 + (overlayOrientations[y][x][z] & 3) + 2);
            buffer.writeShort(overlays[y][x][z]);
        }
        if (tileFlags[y][x][z] != 0) {
            buffer.writeShort(tileFlags[y][x][z] + 49);
        }
        if (underlays[y][x][z] != 0) {
            buffer.writeShort(underlays[y][x][z] + 81);
        }
        if (manualTileHeight[y][x][z] == 1 || y == 0) {
            buffer.writeShort(1);
            if (y == 0) {
                buffer.writeByte(-tileHeights[y][x][z] / 8);
            } else {
                buffer.writeByte(-(tileHeights[y][x][z] - tileHeights[y - 1][x][z]) / 8);
            }
        } else {
            buffer.writeShort(0);
        }
    }


    private static int calculateVertexHeight(int i, int j) {
        int mapHeight = (interpolatedNoise(i + 45365, j + 0x16713, 4) - 128) + (interpolatedNoise(i + 10294, j + 37821, 2) - 128 >> 1) + (interpolatedNoise(i, j, 1) - 128 >> 2);
        mapHeight = (int) ((double) mapHeight * 0.29999999999999999D) + 35;
        if (mapHeight < 10) {
            mapHeight = 10;
        } else if (mapHeight > 60) {
            mapHeight = 60;
        }
        return mapHeight;
    }

    private static int interpolatedNoise(int x, int y, int frequencyReciprocal) {
        int l = x / frequencyReciprocal;
        int i1 = x & frequencyReciprocal - 1;
        int j1 = y / frequencyReciprocal;
        int k1 = y & frequencyReciprocal - 1;
        int l1 = smoothNoise(l, j1);
        int i2 = smoothNoise(l + 1, j1);
        int j2 = smoothNoise(l, j1 + 1);
        int k2 = smoothNoise(l + 1, j1 + 1);
        int l2 = interpolate(l1, i2, i1, frequencyReciprocal);
        int i3 = interpolate(j2, k2, i1, frequencyReciprocal);
        return interpolate(l2, i3, k1, frequencyReciprocal);
    }

    private static int interpolate(int a, int b, int angle, int frequencyReciprocal) {
        int cosine = 0x10000 - COSINE[(angle * 1024) / frequencyReciprocal] >> 1;
        return (a * (0x10000 - cosine) >> 16) + (b * cosine >> 16);
    }

    private static int smoothNoise(int x, int y) {
        int corners = calculateNoise(x - 1, y - 1) + calculateNoise(x + 1, y - 1) + calculateNoise(x - 1, y + 1) + calculateNoise(x + 1, y + 1);
        int sides = calculateNoise(x - 1, y) + calculateNoise(x + 1, y) + calculateNoise(x, y - 1) + calculateNoise(x, y + 1);
        int center = calculateNoise(x, y);
        return corners / 16 + sides / 8 + center / 4;
    }

    private static int calculateNoise(int x, int y) {
        int k = x + y * 57;
        k = k << 13 ^ k;
        int l = k * (k * k * 15731 + 0xc0ae5) + 0x5208dd0d & 0x7fffffff;
        return l >> 19 & 0xff;
    }


    public final void initiateVertexHeights(int yOffset, int yLength, int xLength, int xOffset) {
        for (int y = yOffset; y <= yOffset + yLength; y++) {
            for (int x = xOffset; x <= xOffset + xLength; x++) {
                if (x >= 0 && x < width && y >= 0 && y < length) {
                    if (x == xOffset && x > 0) {
                        tileHeights[0][x][y] = tileHeights[0][x - 1][y];
                    }
                    if (x == xOffset + xLength && x < width - 1) {
                        tileHeights[0][x][y] = tileHeights[0][x + 1][y];
                    }
                    if (y == yOffset && y > 0) {
                        tileHeights[0][x][y] = tileHeights[0][x][y - 1];
                    }
                    if (y == yOffset + yLength && y < length - 1) {
                        tileHeights[0][x][y] = tileHeights[0][x][y + 1];
                    }
                }
            }
        }
    }
}