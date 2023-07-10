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
import java.awt.*
import java.awt.event.KeyEvent
import java.io.File
import javax.swing.*


class ApplicationGUI : JFrame("Map ID Converter (Mark7625)") {

    init {

        val tabbedPane = JTabbedPane()

        val leftPanel = JPanel()

        tabbedPane.addTab(
            "Map Converter (To Old Format)", null, ConvertTab(true),
            "Converts Maps to old byte format"
        )
        tabbedPane.setMnemonicAt(0, KeyEvent.VK_1)

        tabbedPane.addTab(
            "Map Converter (To New Format)", null, ConvertTab(false),
            "Converts to new short format"
        )
        tabbedPane.setMnemonicAt(1, KeyEvent.VK_2)

        tabbedPane.addTab(
            "317 map name format to OSRS", null, NameConvertTab(),
            "Converts to osrs naming (\"317 naming (34/35)\",\"OSRS(\\\"l34_35\\\"/)\")"
        )
        tabbedPane.setMnemonicAt(2, KeyEvent.VK_3)


        tabbedPane.selectedIndex = 0

        leftPanel.add(tabbedPane)


        isResizable = false

        val rightPanel = JPanel()

        rightPanel.layout = BoxLayout(rightPanel, BoxLayout.Y_AXIS)

        nameSaving.alignmentX = Component.CENTER_ALIGNMENT
        format.alignmentX = Component.CENTER_ALIGNMENT

        rightPanel.add(nameSaving)
        rightPanel.add(format)

        val splitPane = JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, rightPanel)
        add(splitPane)

        Application.init()
        pack()
        setLocationRelativeTo(null)
        defaultCloseOperation = EXIT_ON_CLOSE
    }


    companion object {

        var format: JComboBox<String> = JComboBox<String>(listOf("gz","dat").toTypedArray())
        var nameSaving: JComboBox<String> = JComboBox<String>(listOf("317 naming (34/35)","OSRS(\"l34_35\"/)").toTypedArray())

        /**
         * handle click event of the Download button
         */
        fun startConverting(beforeShort : Boolean, panel : JPanel, progressBar : JProgressBar, outputPicker : JFilePicker, inputPicker : JFilePicker, buttonStart: JButton) {

            if(outputPicker.selectedFilePath.isEmpty() || outputPicker.selectedFilePath.isEmpty()) {
                JOptionPane.showMessageDialog(panel, "Input or Output has not been defined.", "Error", JOptionPane.WARNING_MESSAGE)
                return
            }

            progressBar.value = 0
            buttonStart.isEnabled = false
            buttonStart.text = "Looking for Files..."

            val gzFiles = Application.locateGzFiles(File(inputPicker.selectedFilePath))

            val outputGZLoc = File(outputPicker.selectedFilePath + "/gz/")
            if(!outputGZLoc.exists()) {
                outputGZLoc.mkdirs()
            }

            progressBar.maximum = gzFiles.size

            buttonStart.text = if (!beforeShort) "Converting to post208" else "Converting to pre208"

            gzFiles.forEachIndexed { index, file ->
                val fileID = file.nameWithoutExtension

                val regionID: Int
                val generatedMapData : MapDetails

                if (beforeShort) {
                    regionID = Application.afterShort.filter { (it.value.land317 == fileID.toInt()) || (it.value.map317 == fileID.toInt())  }.keys.first()
                    generatedMapData = Application.beforeShort[regionID]!!
                } else {
                    regionID = Application.beforeShort.filter { (it.value.land317 == fileID.toInt()) || (it.value.map317 == fileID.toInt())  }.keys.first()
                    generatedMapData = Application.afterShort[regionID]!!
                }

                val isLandScape = if (beforeShort) Application.beforeShort.filterValues { it.land317 == fileID.toInt() }.count() == 1 else  Application.afterShort.filterValues { it.land317 == fileID.toInt() }.count() == 1

                val mapFileID = if (isLandScape) generatedMapData.land317.toString() else generatedMapData.map317.toString()


                val saveName = getSaveName(mapFileID,regionID,isLandScape)

                if(isLandScape) {

                    val mapConverter = MapConverter(regionID,file)
                    val shouldGZip = format.selectedIndex == 0
                    val bytes : ByteArray = if (!beforeShort) {
                        mapConverter.loadMapByte()
                        GZIPUtils.gzipBytes(mapConverter.region.saveTerrainBlockShort(), shouldGZip)
                    } else {
                        mapConverter.loadMapShort()
                        GZIPUtils.gzipBytes(mapConverter.region.saveTerrainBlockByte(), shouldGZip)
                    }
                    File(outputGZLoc, saveName).writeBytes(bytes)
                } else {
                    file.copyTo(File(outputGZLoc,saveName),true)
                }

                progressBar.string = "$index / ${gzFiles.size}"
                progressBar.value = index

            }

            progressBar.value = gzFiles.size + 1
            progressBar.string = "Finished"
            buttonStart.isEnabled = true
            buttonStart.text = "Start"

        }

        fun getSaveName(mapID: String,regionID : Int, landscape : Boolean) : String {
            val formatAppend = format.selectedItem!!.toString()

            if (nameSaving.selectedIndex == 0) {
                return "${mapID}.${formatAppend}"
            }
            val x = regionID shr 8 shl 6
            val y = regionID and 255 shl 6
            return "${if (landscape) "l" else "m"}${x}_${y}.${formatAppend}"
        }


    }


}