package com.mark.utils

import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream


@Throws(IOException::class)
fun decompressGzipToBytes(source: File): ByteArray? {
    val output = ByteArrayOutputStream()
    GZIPInputStream(
        FileInputStream(source)
    ).use { gis ->

        // copy GZIPInputStream to ByteArrayOutputStream
        val buffer = ByteArray(1024)
        var len: Int
        while (gis.read(buffer).also { len = it } > 0) {
            output.write(buffer, 0, len)
        }
    }
    return output.toByteArray()
}

fun gzip(output : File,uncompressedData: ByteArray) {
    try {
        ByteArrayOutputStream(uncompressedData.size).use { bos ->
            GZIPOutputStream(output.outputStream()).use { gzipOS ->
                gzipOS.write(uncompressedData)
                gzipOS.close()
            }
        }
    } catch (e: IOException) {
        e.printStackTrace()
    }
}