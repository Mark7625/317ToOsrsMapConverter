package com.mark.utils

import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

object GZIPUtils {
    fun gzipBytes(input: ByteArray) = ByteArrayOutputStream().use { bout ->
        GzipCompressorOutputStream(bout).use {
            it.write(input, 0, input.size)
            it.close()
            bout.toByteArray()
        }
    }?: error("Gzip was Null")

    fun unzip(input: ByteArray) = ByteArrayInputStream(input).use { bin ->
        GzipCompressorInputStream(bin).use {
            val out = ByteArrayOutputStream()
            val buffer = ByteArray(2048)
            var n = 0
            while (-1 != it.read(buffer).also { n = it }) {
                out.write(buffer, 0, n)
            }
            bin.close()
            it.close()
            out.toByteArray()
        }
    }?: error("Unzip was Null")

}