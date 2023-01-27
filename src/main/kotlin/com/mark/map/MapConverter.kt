package com.mark.map

import com.mark.utils.GZIPUtils
import java.io.File

class MapConverter(
    private val regionID : Int,
    private val terrainFile : File
) {

    var region : Region = Region()

    fun loadMap() {
        val regionX: Int = regionID shr 8 shl 6
        val regionY: Int = regionID and 0xFF shl 6

        val terrainData: ByteArray = GZIPUtils.unzip(terrainFile.readBytes())

        for (plane in 0..3) {
            for (x in 0..63) {
                for (y in 0..63) {
                    region.tileFlags[plane][x][y] = 0
                }
            }
        }

        region.unpackTiles(terrainData, 0, 0, regionX, regionY)
    }


}