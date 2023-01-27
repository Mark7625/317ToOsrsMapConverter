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

import com.mark.map.MapConverter
import com.mark.swing.JFilePicker
import com.mark.utils.GZIPUtils
import java.awt.Dimension
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.Insets
import java.beans.PropertyChangeEvent
import java.beans.PropertyChangeListener
import java.io.File
import javax.swing.*

class ApplicationGUI : JFrame("Map ID Converter (Mark7625)"), PropertyChangeListener {

    private val inputPicker: JFilePicker = JFilePicker(
        "Input Location: ",
        "Browse..."
    )

    private val outputPicker: JFilePicker = JFilePicker(
        "Output Location: ",
        "Browse..."
    )

    private val buttonStart = JButton("Start")
    private val fieldFileSize = JTextField(15)
    private val progressBar = JProgressBar(0, 100)

    init {
        // set up layout
        layout = GridBagLayout()
        val constraints = GridBagConstraints()
        constraints.anchor = GridBagConstraints.WEST
        constraints.insets = Insets(5, 5, 5, 5)

        // set up components
        inputPicker.setMode(JFilePicker.MODE_SAVE)
        inputPicker.fileChooser.fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
        outputPicker.setMode(JFilePicker.MODE_SAVE)
        outputPicker.fileChooser.fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
        fieldFileSize.isEditable = false
        buttonStart.addActionListener { startConverting() }
        progressBar.preferredSize = Dimension(200, 30)
        progressBar.isStringPainted = true
        isResizable = false

        // add components to the frame

        constraints.gridx = 0
        constraints.gridy = 0
        add(inputPicker, constraints)

        constraints.gridx = 0
        constraints.fill = GridBagConstraints.HORIZONTAL
        constraints.gridy = 1
        add(outputPicker, constraints)

        constraints.gridx = 0
        constraints.gridy = 2
        add(buttonStart, constraints)

        constraints.gridx = 0
        constraints.gridy = 3
        add(progressBar, constraints)


        Application.init()
        pack()
        setLocationRelativeTo(null)
        defaultCloseOperation = EXIT_ON_CLOSE
    }

    /**
     * handle click event of the Download button
     */
    private fun startConverting() {

        if(outputPicker.selectedFilePath.isEmpty() || outputPicker.selectedFilePath.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Input or Output has not been defined.", "Error", JOptionPane.WARNING_MESSAGE)
            return
        }

        progressBar.value = 0
        buttonStart.isEnabled = false
        buttonStart.text = "Looking for Files..."

        val gzFiles = Application.locateGzFiles(File(inputPicker.selectedFilePath))!!

        val output = File(outputPicker.selectedFilePath)
        val outputGZLoc = File(outputPicker.selectedFilePath + "/gz/")
        if(!outputGZLoc.exists()) {
            outputGZLoc.mkdirs()
        }

        progressBar.maximum = gzFiles.size
        buttonStart.text = "Converting..."

        val landscapeList = emptyList<Int>().toMutableList()
        Application.mapIdsNew.forEach { _, u ->
            landscapeList.add(u.first)
        }


        gzFiles.forEachIndexed { index, file ->
            val oldID = file.nameWithoutExtension.toInt()
            val newID = Application.newMapIds[oldID]
            val isLandscape = landscapeList.contains(newID)
            if(isLandscape) {
                val regionID = Application.mapIdsNew.filterValues { it.first == newID }.entries.first().key
                val mapConverter = MapConverter(regionID,file)
                mapConverter.loadMap()
                val bytes = GZIPUtils.gzipBytes(mapConverter.region.saveTerrainBlock());
                File(outputGZLoc,"$newID.gz").writeBytes(bytes)
                println("Found Landscape: $newID")
            } else {
                file.copyTo(File(outputGZLoc,"$newID.gz"),true)
            }

            progressBar.string = "$index / ${gzFiles.size}"
            progressBar.value = index

            val regionID = Application.mapIdsOLD.filterValues { it.second == oldID || it.first == oldID }.entries.first().key

            if(!Application.logs.containsKey(regionID)) {
                Application.logs[regionID] = Logging(Application.mapIdsOLD[regionID]!!,Application.mapIdsNew[regionID]!!)
            }

        }
        buttonStart.text = "Making Logs..."
        val outputLog = StringBuilder()

        Application.logs.forEach { (region, data) ->
            outputLog.append("Region: $region : [${data.oldIds.first}, ${data.oldIds.second}] to [${data.newIds.first}, ${data.newIds.second}]${System.lineSeparator()}")
        }

        File(output,"log.txt").writeText(outputLog.toString())

        buttonStart.text = "Finished..."

        buttonStart.isEnabled = true
        buttonStart.text = "Start"

    }


    /**
     * Update the progress bar's state whenever the progress of download
     * changes.
     */
    override fun propertyChange(evt: PropertyChangeEvent) {
        if ("progress" === evt.propertyName) {
            val progress = evt.newValue as Int
            progressBar.value = progress
        }
    }
}