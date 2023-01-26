package com.mark.map

import com.mark.Application
import com.mark.utils.decompressGzipToBytes
import com.mark.utils.gzip
import java.io.File


fun main() {
    Application.init()
    val allLandscapes = emptyList<Int>().toMutableList()
    Application.regionToFilesNEW.forEach { t, u ->
        allLandscapes.add(u.second)
    }

    val mapConverter = RegionLoader()

    File("C:\\Users\\Home\\Desktop\\dfd\\").listFiles().filter { allLandscapes.contains(it.name.replace(".gz","").toInt())}.forEach {
        val landScapeID = it.nameWithoutExtension.toInt()
        val regionID = Application.regionToFilesNEW.filterValues { it.second == landScapeID }.entries.first().key
        val regionDef = mapConverter.loadRegion(regionID,decompressGzipToBytes(it)!!)
        if(regionDef != null) {
            gzip(File("C:\\Users\\Home\\Desktop\\testing\\",it.name),mapConverter.writeRegion(regionDef!!))
        }
    }
}