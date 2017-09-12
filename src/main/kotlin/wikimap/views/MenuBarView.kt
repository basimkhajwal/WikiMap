package wikimap.views

import javafx.beans.property.SimpleObjectProperty
import javafx.stage.FileChooser
import tornadofx.*
import wikimap.models.MindMap
import wikimap.utils.UserSettings
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardOpenOption

/**
 * Created by Basim on 09/08/2017.
 */
class MenuBarView : View() {

    val fileProperty = SimpleObjectProperty<File?>(null)
    var file by fileProperty

    private val fileChooser = FileChooser().apply {
        extensionFilters += FileChooser.ExtensionFilter("Other", "*")
        extensionFilters += FileChooser.ExtensionFilter("Text File", "*.txt")
        extensionFilters += FileChooser.ExtensionFilter("WikiMap File", "*.wmap")

        selectedExtensionFilter = extensionFilters!!.last()
    }

    val main: MainView by param()

    override val root = menubar {
        menu("File") {
            item("Open") {
                action {
                    file = fileChooser.showOpenDialog(currentWindow)
                    if (file != null) loadFromFile()
                }
            }

            item("Save") {
                action {
                    if (file == null) file = fileChooser.showSaveDialog(currentWindow)
                    if (file != null) saveToFile()
                }
            }

            item("Save As") {
                action {
                    file = fileChooser.showSaveDialog(currentWindow)
                    if (file != null) saveToFile(true)
                }
            }
        }
    }

    private fun loadFromFile() {
        val fileContents = String(Files.readAllBytes(file!!.toPath()))
        val model = MindMap.deserialize(fileContents)

        UserSettings.updateRecentFile(file!!.absolutePath)
        main.loadModel(model)
    }

    private fun saveToFile(isSaveAs: Boolean = false) {

        val path = file!!.toPath()

        if (Files.exists(path)) {
            if (isSaveAs) {
                confirm("Overwrite File?", content="Saving will overwrite file ${file!!.name}") {
                    saveToFile()
                }
            }
            Files.delete(path)
        }

        val fileData = main.mindMap.serialize().toByteArray()
        UserSettings.updateRecentFile(file!!.absolutePath)
        Files.write(path, fileData, StandardOpenOption.CREATE_NEW)
    }
}