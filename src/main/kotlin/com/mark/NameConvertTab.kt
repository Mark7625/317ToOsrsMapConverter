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

class NameConvertTab() : JPanel(), PropertyChangeListener {

    private val inputPicker: JFilePicker = JFilePicker(
        "Input Location: ",
        "Browse..."
    )

    private val buttonStart = JButton("Start")
    private val fieldFileSize = JTextField(15)
    private val progressBar = JProgressBar(0, 100)

    init {

        // set up components
        inputPicker.setMode(JFilePicker.MODE_SAVE)
        inputPicker.fileChooser.fileSelectionMode = JFileChooser.DIRECTORIES_ONLY

        fieldFileSize.isEditable = false

        progressBar.preferredSize = Dimension(200, 30)
        progressBar.isStringPainted = true

        buttonStart.addActionListener { nameConvert() }

        val constraints = GridBagConstraints()
        constraints.anchor = GridBagConstraints.WEST
        constraints.insets = Insets(5, 5, 5, 5)

        layout = GridBagLayout()

        constraints.gridx = 0
        constraints.gridy = 0
        add(JLabel("Convert from 317 map File ids to osrs [l0_0], [m0_0] format (please make sure the regions ids are after 209)"), constraints)

        constraints.gridx = 0
        constraints.gridy = 1
        add(inputPicker, constraints)

        constraints.gridx = 0
        constraints.gridy = 2
        add(buttonStart, constraints)

        constraints.gridx = 0
        constraints.gridy = 3
        add(progressBar, constraints)
    }

    private fun nameConvert() {

        val gzFiles = Application.locateGzFiles1(File(inputPicker.selectedFilePath))

        progressBar.maximum = gzFiles.size

        gzFiles.forEachIndexed { index, file ->
            val fileID = file.nameWithoutExtension

            val regionID: Int = Application.afterShort.filter { (it.value.land317 == fileID.toInt()) || (it.value.map317 == fileID.toInt())  }.keys.first()
            val generatedMapData = Application.afterShort[regionID]!!

            val isLandScape = Application.afterShort.filterValues { it.land317 == fileID.toInt() }.count() == 1

            val fileType = file.extension

            if (isLandScape) {
                file.renameTo(File(inputPicker.selectedFilePath + "/" + generatedMapData.landOSRS + "." + fileType))
            } else {
                file.renameTo(File(inputPicker.selectedFilePath + "/" + generatedMapData.mapOSRS + "." + fileType))
            }


            buttonStart.text = "Converting to names"

            progressBar.string = "$index / ${gzFiles.size}"
            progressBar.value = index


        }

        progressBar.value = gzFiles.size + 1
        progressBar.string = "Finished"
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