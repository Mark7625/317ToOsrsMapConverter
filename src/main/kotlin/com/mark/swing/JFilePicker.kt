package com.mark.swing

import java.awt.FlowLayout
import java.awt.event.ActionEvent
import javax.swing.*


class JFilePicker(textFieldLabel: String, buttonLabel: String) : JPanel() {

    private val label: JLabel
    private val textField: JTextField
    private val button: JButton
    val fileChooser: JFileChooser = JFileChooser()
    private var mode = 0

    init {
        layout = FlowLayout(FlowLayout.CENTER, 5, 5)

        // creates the GUI
        label = JLabel(textFieldLabel)
        textField = JTextField(30)
        button = JButton(buttonLabel)
        button.addActionListener { evt -> buttonActionPerformed(evt) }
        add(label)
        add(textField)
        add(button)
    }

    private fun buttonActionPerformed(evt: ActionEvent) {
        if (mode == MODE_OPEN) {
            if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                textField.text = fileChooser.selectedFile.absolutePath
            }
        } else if (mode == MODE_SAVE) {
            if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                textField.text = fileChooser.selectedFile.absolutePath
            }
        }
    }



    fun setMode(mode: Int) {
        this.mode = mode
    }

    val selectedFilePath: String
        get() = textField.text

    companion object {
        const val MODE_OPEN = 1
        const val MODE_SAVE = 2
    }
}