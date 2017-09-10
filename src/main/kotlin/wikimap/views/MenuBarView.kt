package wikimap.views

import javafx.stage.FileChooser
import tornadofx.*
import wikimap.models.MindMap
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardOpenOption

/**
 * Created by Basim on 09/08/2017.
 */
class MenuBarView : View() {

    private var file: File? = null

    val main: MainView by param()

    override val root = menubar {
        menu("File") {
            item("Open") {
                action {
                    file = FileChooser().showOpenDialog(currentWindow)
                    if (file != null) loadFromFile()
                }
            }

            item("Save") {
                action {
                    if (file == null) file = FileChooser().showSaveDialog(currentWindow)
                    if (file != null) saveToFile()
                }
            }

            item("Save As") {
                action {
                    file = FileChooser().showSaveDialog(currentWindow)
                    if (file != null) saveToFile()
                }
            }
        }
    }

    private fun loadFromFile() {
        val fileContents = String(Files.readAllBytes(file!!.toPath()))
        val model = MindMap.deserialize(fileContents)

        main.loadModel(model)
    }

    private fun saveToFile() {
        val fileData = main.mindMap.serialize().toByteArray()
        Files.write(file!!.toPath(), fileData, StandardOpenOption.CREATE_NEW)
    }
}