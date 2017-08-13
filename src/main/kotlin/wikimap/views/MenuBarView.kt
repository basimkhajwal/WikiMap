package wikimap.views

import javafx.scene.control.MenuBar
import javafx.stage.FileChooser
import tornadofx.*
import wikimap.controllers.MindMapController
import wikimap.models.MindMap
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardOpenOption

/**
 * Created by Basim on 09/08/2017.
 */
class MenuBarView : View() {

    private var file: File? = null

    val controller: MindMapController by inject()

    override val root = MenuBar()

    init {
        with(root) {
            menu("File") {
                item("Open") {
                    action {
                        file = FileChooser().showOpenDialog(currentWindow)
                        loadFromFile()
                    }
                }

                item("Save") {
                    action {
                        if (file == null) {
                            file = FileChooser().showSaveDialog(currentWindow)
                        }
                        saveToFile()
                    }
                }

                item("Save As") {
                    action {
                        file = FileChooser().showSaveDialog(currentWindow)
                        saveToFile()
                    }
                }
            }
        }
    }

    private fun loadFromFile() {
        val fileContents = String(Files.readAllBytes(file!!.toPath()))
        val model = MindMap.deserialize(fileContents)

        controller.loadModel(model)
    }

    private fun saveToFile() {
        val fileData = controller.model.serialize().toByteArray()
        Files.write(file!!.toPath(), fileData, StandardOpenOption.CREATE_NEW)
    }
}