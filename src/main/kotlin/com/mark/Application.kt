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

import com.github.weisj.darklaf.LafManager
import com.github.weisj.darklaf.theme.DarculaTheme
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.*
import java.lang.reflect.Type
import java.net.URISyntaxException
import java.nio.charset.StandardCharsets
import javax.swing.SwingUtilities


data class Logging(
    val oldIds: Pair<Int,Int>,
    val newIds: Pair<Int,Int>
)

data class MapDetails(
    val land317 : Int,
    val map317 : Int,
    val landOSRS : String,
    val mapOSRS : String,
)

object Application {


    val logs = emptyMap<Int,Logging>().toMutableMap()

    var beforeShort : MutableMap<Int,MapDetails> = emptyMap<Int,MapDetails>().toMutableMap()
    var afterShort : MutableMap<Int,MapDetails> = emptyMap<Int,MapDetails>().toMutableMap()

    fun init() {

        val mapType: Type = object : TypeToken<Map<Int?, MapDetails?>?>() {}.type

        beforeShort = Gson().fromJson(resourceToString("beforeShort.json"), mapType)
        afterShort = Gson().fromJson(resourceToString("afterShort.json"), mapType)

        val testRegion = 12342

        println("Loaded Map Indexes [Edge (Old: ${beforeShort[testRegion]}), (new ${afterShort[testRegion]})]")

    }

    @Throws(IOException::class, URISyntaxException::class)
    private fun resourceToString(filePath: String): String {
        val stream: InputStream = this.javaClass.classLoader.getResourceAsStream(filePath)!!
        val streamReader = InputStreamReader(stream, StandardCharsets.UTF_8)
        return streamReader.readText()
    }
    
    fun locateGzFiles(dir : File) = dir.walkBottomUp().toList().filter { it.extension == "gz" }
    fun locateGzFiles1(dir : File) = dir.walkBottomUp().toList().filter { it.extension == "gz" || it.extension == "dat" }



}

fun main() {
    SwingUtilities.invokeLater {
        LafManager.install()
        LafManager.install(DarculaTheme())

        ApplicationGUI().isVisible = true
    }
}