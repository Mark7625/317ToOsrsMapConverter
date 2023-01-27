package com.mark.map

import com.mark.Application
import com.mark.GZIPUtils
import com.mark.utils.decompressGzipToBytes
import com.mark.utils.gzip
import java.io.File
import java.nio.file.Files


class MapConverter(
    val regionID : Int,
    val terrainFile : File
) {

    private var tileFlags: Array<Array<ByteArray>>
    private var tileHeights: Array<Array<IntArray>>

    private val mapTileWidth: Int = 2 * 52
    private val mapTileHeight: Int = 2 * 52

    lateinit var region : Region

    init {
        tileFlags = Array(4) { Array(mapTileWidth) { ByteArray(mapTileHeight) }}
        tileHeights = Array(4) { Array(mapTileWidth + 1) { IntArray(mapTileHeight + 1) }}
        region = Region(tileFlags, tileHeights);
    }

    fun loadMap() {
        val x: Int = regionID shr 8 shl 6
        val z: Int = regionID and 0xFF shl 6

        val terrainData: ByteArray = GZIPUtils.unzip(Files.readAllBytes(terrainFile.toPath()))!!
        if(terrainData == null) {
            System.out.println("NO BYTES")
            return;
        }
        for (z1 in 0..3) {
            for (x1 in 0..63) {
                for (y1 in 0..63) {
                    region.tileFlags[z1][x1][y1] = 0
                }
            }
        }
        region.unpackTiles(terrainData, 0, 0, x, z)
    }


}

fun main() {
    Application.init()
    val allLandscapes = emptyList<Int>().toMutableList()
    Application.regionToFilesNEW.forEach { t, u ->
        allLandscapes.add(u.second)
    }


    File("C:\\Users\\Home\\Desktop\\dfd\\").listFiles().filter { allLandscapes.contains(it.name.replace(".gz","").toInt())}.forEach {
        val landScapeID = it.nameWithoutExtension.toInt()
        val regionID = Application.regionToFilesNEW.filterValues { it.second == landScapeID }.entries.first().key
        val mapConverter = MapConverter(regionID,it)

        mapConverter.loadMap()

        val bytes = GZIPUtils.gzipBytes(mapConverter.region.save_terrain_block());

        Files.write(File("C:\\Users\\Home\\Desktop\\testing\\",it.name).toPath(), bytes);

    }
}