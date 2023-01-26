package com.mark.swing

import java.io.File
import java.util.*
import javax.swing.filechooser.FileFilter


class FileTypeFilter(private val extension: String, private val description: String) : FileFilter() {
    override fun accept(file: File): Boolean {
        return if (file.isDirectory) {
            true
        } else file.name.lowercase(Locale.getDefault()).endsWith(extension)
    }

    override fun getDescription(): String {
        return description + String.format(" (*%s)", extension)
    }
}