package com.mark

import com.mark.swing.JFilePicker
import java.awt.Dimension
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.Insets
import java.beans.PropertyChangeEvent
import java.beans.PropertyChangeListener
import javax.swing.*

class ConvertTab(beforeShort: Boolean) : JPanel(), PropertyChangeListener {

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

        // set up components
        inputPicker.setMode(JFilePicker.MODE_SAVE)
        inputPicker.fileChooser.fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
        outputPicker.setMode(JFilePicker.MODE_SAVE)
        outputPicker.fileChooser.fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
        fieldFileSize.isEditable = false

        progressBar.preferredSize = Dimension(200, 30)
        progressBar.isStringPainted = true

        buttonStart.addActionListener { ApplicationGUI.startConverting(beforeShort,this,progressBar,outputPicker,inputPicker,buttonStart) }

        val constraints = GridBagConstraints()
        constraints.anchor = GridBagConstraints.WEST
        constraints.insets = Insets(5, 5, 5, 5)

        layout = GridBagLayout()

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