package com.mark.map

import com.mark.utils.Buffer
import java.util.*

class Region {

    private val width = 104
    private val length = 104
    private val tileHeights: Array<Array<IntArray>> = Array(4) { Array(width + 1) { IntArray(length + 1) } }
    val tileFlags: Array<Array<ByteArray>> = Array(4) { Array(width) { ByteArray(length) } }
    private val underlays: Array<Array<ShortArray>> = Array(4) { Array(width) { ShortArray(length) } }
    private val overlays: Array<Array<ShortArray>> = Array(4) { Array(width) { ShortArray(length) } }
    private val overlayShapes: Array<Array<ByteArray>> = Array(4) { Array(width) { ByteArray(length) } }
    private val overlayOrientations: Array<Array<ByteArray>> = Array(4) { Array(width) { ByteArray(length) } }
    private var manualTileHeight: Array<Array<ByteArray>> = Array(4) { Array(width) { ByteArray(length) } }

    fun unpackTiles(data: ByteArray, dX: Int, dY: Int, regionX: Int, regionY: Int) {
        val buffer = Buffer(data)
        for (plane in 0..3) {
            for (x in 0..63) {
                for (y in 0..63) {
                    readTile(buffer, x + dX, y + dY, plane, regionX, regionY)
                }
            }
        }
        setHeights()
    }

    private fun setHeights() {
        for (plane in 0..3) {
            for (y in 0..length) {
                tileHeights[plane][width][y] = tileHeights[plane][width - 1][y]
            }
            for (x in 0..width) {
                tileHeights[plane][x][length] = tileHeights[plane][x][length - 1]
            }
        }
    }

    private fun readTile(buffer: Buffer, x: Int, y: Int, z: Int, regionX: Int, regionY: Int) {
        if (x in 0..63 && y in 0..63) {
            tileFlags[z][x][y] = 0
            do {
                val type = buffer.readUByte()
                if (type == 0) {
                    manualTileHeight[z][x][y] = 0
                    if (z == 0) {
                        tileHeights[0][x][y] = -calculateHeight(0xe3b7b + x + regionX, 0x87cce + y + regionY) * 8
                    } else {
                        tileHeights[z][x][y] = tileHeights[z - 1][x][y] - 240
                    }
                    return
                } else if (type == 1) {
                    manualTileHeight[z][x][y] = 1
                    var height = buffer.readUByte()
                    if (height == 1) {
                        height = 0
                    }
                    if (z == 0) {
                        tileHeights[0][x][y] = -height * 8
                    } else {
                        tileHeights[z][x][y] = tileHeights[z - 1][x][y] - height * 8
                    }
                    return
                } else if (type <= 49) {
                    overlays[z][x][y] = buffer.readUByte().toShort()
                    overlayShapes[z][x][y] = ((type - 2) / 4).toByte()
                    overlayOrientations[z][x][y] = (type - 2 + 0 and 3).toByte()
                } else if (type <= 81) {
                    tileFlags[z][x][y] = (type - 49).toByte()
                } else {
                    underlays[z][x][y] = (type - 81).toShort()
                }
            } while (true)
        }
        do {
            val opcode1 = buffer.readUByte()
            if (opcode1 == 0) {
                break
            } else if (opcode1 == 1) {
                buffer.readUByte()
                return
            } else if (opcode1 <= 49) {
                buffer.readUShort()
            }
        } while (true)
    }

    fun saveTerrainBlock(): ByteArray {
        val buffer = Buffer(ByteArray(131072))
        for (plane in 0..3) {
            for (x in 0..63) {
                for (y in 0..63) {
                    saveTerrainTile(plane, x, y, buffer)
                }
            }
        }
        return buffer.payload.copyOf(buffer.position)
    }

    private fun saveTerrainTile(y: Int, x: Int, z: Int, buffer: Buffer) {
        if (overlays[y][x][z].toInt() != 0) {
            buffer.writeShort(overlayShapes[y][x][z] * 4 + (overlayOrientations[y][x][z].toInt() and 3) + 2)
            buffer.writeShort(overlays[y][x][z].toInt())
        }
        if (tileFlags[y][x][z].toInt() != 0) {
            buffer.writeShort(tileFlags[y][x][z] + 49)
        }
        if (underlays[y][x][z].toInt() != 0) {
            buffer.writeShort(underlays[y][x][z] + 81)
        }
        if (manualTileHeight[y][x][z].toInt() == 1 || y == 0) {
            buffer.writeShort(1)
            if (y == 0) {
                buffer.writeByte(-tileHeights[y][x][z] / 8)
            } else {
                buffer.writeByte(-(tileHeights[y][x][z] - tileHeights[y - 1][x][z]) / 8)
            }
        } else {
            buffer.writeShort(0)
        }
    }

    companion object {

        private var COSINE = IntArray(2048)
        private fun calculateHeight(x: Int, y: Int): Int {
            var height = interpolatedNoise(x + 45365, y + 0x16713, 4) - 128 + (interpolatedNoise(
                x + 10294,
                y + 37821,
                2
            ) - 128 shr 1) + (interpolatedNoise(x, y, 1) - 128 shr 2)
            height = (height * 0.3).toInt() + 35
            if (height < 10) {
                height = 10
            } else if (height > 60) {
                height = 60
            }
            return height
        }


        private fun interpolatedNoise(x: Int, y: Int, frequencyReciprocal: Int): Int {
            val l = x / frequencyReciprocal
            val i1 = x and frequencyReciprocal - 1
            val j1 = y / frequencyReciprocal
            val k1 = y and frequencyReciprocal - 1
            val l1 = smoothNoise(l, j1)
            val i2 = smoothNoise(l + 1, j1)
            val j2 = smoothNoise(l, j1 + 1)
            val k2 = smoothNoise(l + 1, j1 + 1)
            val l2 = interpolate(l1, i2, i1, frequencyReciprocal)
            val i3 = interpolate(j2, k2, i1, frequencyReciprocal)
            return interpolate(l2, i3, k1, frequencyReciprocal)
        }

        private fun interpolate(a: Int, b: Int, angle: Int, frequencyReciprocal: Int): Int {
            val cosine = 0x10000 - COSINE[angle * 1024 / frequencyReciprocal] shr 1
            return (a * (0x10000 - cosine) shr 16) + (b * cosine shr 16)
        }

        private fun smoothNoise(x: Int, y: Int): Int {
            val corners = calculateNoise(x - 1, y - 1) + calculateNoise(x + 1, y - 1) + calculateNoise(
                x - 1,
                y + 1
            ) + calculateNoise(x + 1, y + 1)
            val sides = calculateNoise(x - 1, y) + calculateNoise(x + 1, y) + calculateNoise(x, y - 1) + calculateNoise(
                x,
                y + 1
            )
            val center = calculateNoise(x, y)
            return corners / 16 + sides / 8 + center / 4
        }

        private fun calculateNoise(x: Int, y: Int): Int {
            var k = x + y * 57
            k = k shl 13 xor k
            val l = k * (k * k * 15731 + 0xc0ae5) + 0x5208dd0d and 0x7fffffff
            return l shr 19 and 0xff
        }
    }
}