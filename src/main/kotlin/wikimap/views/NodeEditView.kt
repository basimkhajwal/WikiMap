package wikimap.views

import javafx.beans.property.IntegerProperty
import javafx.scene.layout.StackPane
import javafx.scene.text.TextAlignment
import tornadofx.*
import wikimap.utils.NumericTextField
import wikimap.view.MainView
import wikimap.view.NodeView

/**
 * Created by Basim on 04/08/2017.
 */
class NodeEditView(private val main: MainView) : StackPane() {

    private val suggestionsList = mutableListOf<String>().observable()
    private var oldKey = ""

    private val items = main.selectedNodes

    private val root = titledpane("Edit Node") {
        textAlignment = TextAlignment.CENTER
        maxWidth = 300.0
        isCollapsible = false

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
        this += root
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
            suggestionsList.clear()
            suggestionsList.addAll(main.suggestionProvider.getSuggestions(key))
        }
    }
}