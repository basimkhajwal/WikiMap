package wikimap.view

import javafx.scene.control.MenuBar
import javafx.stage.FileChooser
import tornadofx.*
import wikimap.models.MindMap
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardOpenOption

/**
 * Created by Basim on 09/08/2017.
 */
class MenuBarView(val main: MainView) : MenuBar() {

    var file: File? = null

    init {
        menu("File") {
            item("Open") {
                action {
                    file = FileChooser().showOpenDialog(main.currentWindow)
                    loadFromFile()
                }
            }

            item("Save") {
                action {
                    if (file == null) {
                        file = FileChooser().showSaveDialog(main.currentWindow)
                    }
                    saveToFile()
                }
            }

            item("Save As") {
                action {
                    file = FileChooser().showSaveDialog(main.currentWindow)
                    saveToFile()
                }
            }
        }
    }

    fun loadFromFile() {
        val fileContents = String(Files.readAllBytes(file!!.toPath()))
        val model = MindMap.deserialize(fileContents)

        main.loadModel(model)
    }

    fun saveToFile() {
        val fileData = main.mindMap.serialize().toByteArray()
        Files.write(file!!.toPath(), fileData, StandardOpenOption.CREATE_NEW)
    }
}