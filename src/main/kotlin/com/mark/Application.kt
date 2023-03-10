/*
 * Copyright (c) 2023, Mark7625 <https://github.com/Mark7625>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.mark

import java.io.DataInputStream
import java.io.File
import javax.swing.SwingUtilities
import javax.swing.UIManager

data class Logging(
    val oldIds: Pair<Int,Int>,
    val newIds: Pair<Int,Int>
)

object Application {


    val logs = emptyMap<Int,Logging>().toMutableMap()


    val mapIdsOLD = emptyMap<Int,Pair<Int,Int>>().toMutableMap()
    val mapIdsNew = emptyMap<Int,Pair<Int,Int>>().toMutableMap()

    val oldMapIds = emptyMap<Int,Pair<Int,Int>>().toMutableMap()
    val newMapIds = emptyMap<Int,Int>().toMutableMap()

    fun init() {
        val oldData = DataInputStream(this.javaClass.classLoader.getResourceAsStream("map_index_old")!!)

        for(index in 0..oldData.readUnsignedShort()) {
            try {
                val area = oldData.readUnsignedShort()
                val objects = oldData.readUnsignedShort()
                val landscape = oldData.readUnsignedShort()
                oldMapIds[area] = Pair(objects, landscape)
                mapIdsOLD[area] = Pair(objects, landscape)
            } catch (e: Exception) {
                println("Error Decoding Map Data")
            }
        }

        val newData = DataInputStream(this.javaClass.classLoader.getResourceAsStream("map_index_new")!!)

        for(index in 0..newData.readUnsignedShort()) {
            try {
                val area = newData.readUnsignedShort()
                val objects = newData.readUnsignedShort()
                val landscape = newData.readUnsignedShort()

                val old = oldMapIds[area]!!
                newMapIds[old.first] = objects
                newMapIds[old.second] = landscape
                mapIdsNew[area] = Pair(objects, landscape)
            }catch (e : Exception) {
                println("Error Decoding Map Data")
            }
        }

        println("Loaded Map Data")
    }
    
    fun locateGzFiles(dir : File) = dir.walkBottomUp().toList().filter { it.extension == "gz" }


}

fun main() {
    try {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())
    } catch (ex: Exception) {
        ex.printStackTrace()
    }
    SwingUtilities.invokeLater { ApplicationGUI().isVisible = true }
}