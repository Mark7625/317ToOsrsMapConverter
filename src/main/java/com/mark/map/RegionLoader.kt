package com.mark.map

import com.mark.map.RegionDefinition.Companion.X
import com.mark.map.RegionDefinition.Companion.Z
import com.mark.map.RegionDefinition.Companion.Y
import com.mark.utils.readUnsignedByte
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer

class RegionLoader(
    private val regionDefinitionCache: HashMap<Int, RegionDefinition> = HashMap()
) {

    fun loadRegion(regionId: Int,map: ByteArray): RegionDefinition? {
        val regionDefinition = RegionDefinition(regionId)
        try {
            val inputStream = ByteBuffer.wrap(map)

            for (z in 0 until Z) {
                for (x in 0 until X) {
                    for (y in 0 until Y) {
                        val tile = RegionDefinition.Tile()
                        regionDefinition.tiles[z][x][y] = tile
                        while (true) {
                            val attribute: Int = inputStream.readUnsignedByte()
                            if (attribute == 0) {
                                break
                            } else if (attribute == 1) {
                                val height: Int = inputStream.readUnsignedByte()
                                tile.cacheHeight = height
                                tile.height = height
                                break
                            } else if (attribute <= 49) {
                                tile.attrOpcode = attribute
                                tile.overlayId = inputStream.get()
                                tile.overlayPath = ((attribute - 2) / 4).toByte()
                                tile.overlayRotation = (attribute - 2 and 3).toByte()
                            } else if (attribute <= 81) {
                                tile.settings = (attribute - 49).toByte()
                            } else {
                                tile.underlayId = (attribute - 81).toByte()
                            }
                        }
                    }
                }
            }

            regionDefinition.calculateTerrain()
            regionDefinitionCache[regionId] = regionDefinition
        }catch (_: Exception) { }
        return regionDefinition

    }


    fun writeRegion(regionDefinition: RegionDefinition) : ByteArray {
        val outputStream = ByteArrayOutputStream()

        for (z in 0 until Z) {
            for (x in 0 until X) {
                for (y in 0 until Y) {
                    val tile: RegionDefinition.Tile = regionDefinition.tiles[z][x][y]!!
                    if (tile.attrOpcode > 0) {
                        outputStream.write(byteArrayOf(tile.attrOpcode.toByte(), tile.overlayId))
                    }

                    if (tile.settings > 0) {
                        outputStream.write(byteArrayOf(tile.settings.plus(49).toByte()))
                    }

                    if (tile.underlayId > 0) {
                        outputStream.write(byteArrayOf(tile.underlayId.plus(81).toByte()))
                    }

                    if (tile.cacheHeight != null) {
                        outputStream.write(byteArrayOf(1, (tile.height / -8).toByte()))
                    } else {
                        outputStream.write(byteArrayOf(0))
                    }
                }
            }
        }

        return outputStream.toByteArray()
    }
}