package wikimap.views

import javafx.beans.property.IntegerProperty
import javafx.scene.layout.Priority
import javafx.scene.text.TextAlignment
import tornadofx.*
import wikimap.utils.NumericTextField

/**
 * Created by Basim on 04/08/2017.
 */
class NodeEditView : View() {

    private val suggestionsList = mutableListOf<String>().observable()
    private var oldKey = ""

    val main: MainView by param()
    private val items = main.selectedNodes

    override val root = titledpane("Edit Node") {
        textAlignment = TextAlignment.CENTER
        minWidth = 350.0
        maxWidth = 450.0
        isCollapsible = false
        vgrow = Priority.ALWAYS
        maxHeight = Double.MAX_VALUE

        content = form {
            fieldset("Node Properties") {
                field("X") { this += NumericTextField().multiFieldEdit { it.model.xProperty } }
                field("Y") { this += NumericTextField().multiFieldEdit { it.model.yProperty } }
                field("Width") { this += NumericTextField().multiFieldEdit { it.model.widthProperty } }
                field("Height") { this += NumericTextField().multiFieldEdit { it.model.heightProperty } }
            }

            fieldset("Node Suggestions") {
                listview(suggestionsList)
            }
        }
    }

    init {
        main.onChange += this::refresh
        refresh()
    }

    private fun <T, V> List<T>.sameBy(p: (T) -> V): Boolean = distinctBy(p).size == 1

    private fun NumericTextField.multiFieldEdit(field: (NodeView) -> IntegerProperty): NumericTextField {

        fun update() {

            if (items.isEmpty()) {
                isDisable = true
                clear()

            } else {
                isDisable = false

                if (items.sameBy { field(it).value }) {
                    value = field(items.first()).value
                } else {
                    clear()
                }
            }

            items.forEach { it.onChange += ::update }
        }

        items.onChange { update() }

        valueProperty.onChange {
            if (!isDisable) {
                items.forEach { field(it).value = value }
                main.refresh()
            }
        }

        update()
        return this
    }

    private fun refresh() {
        if (items.isEmpty()) {
            suggestionsList.clear()
            oldKey = ""
            return
        }

        val key = items.first().model.key
        if (key != oldKey) {
            oldKey = key

            runAsync {
                main.suggestionProvider.getSuggestions(key)
            } ui {
                suggestionsList.clear()
                suggestionsList.addAll(it)
            }

        }
    }
}