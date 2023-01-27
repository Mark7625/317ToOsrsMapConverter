package com.mark.utils

class Buffer(val payload: ByteArray) {

    var position = 0

    fun readUByte(): Int {
        return payload[position++].toInt() and 0xff
    }

    fun readUShort(): Int {
        position += 2
        return (payload[position - 2].toInt() and 0xff shl 8) + (payload[position - 1].toInt() and 0xff)
    }

    fun writeByte(i: Int) {
        payload[position++] = i.toByte()
    }

    fun writeShort(i: Int) {
        payload[position++] = (i shr 8).toByte()
        payload[position++] = i.toByte()
    }
}