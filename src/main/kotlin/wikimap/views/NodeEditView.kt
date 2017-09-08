package wikimap.views

import javafx.beans.property.IntegerProperty
import javafx.beans.property.ObjectProperty
import javafx.scene.control.Label
import javafx.scene.layout.Priority
import javafx.scene.paint.Color
import javafx.scene.text.TextAlignment
import tornadofx.*
import wikimap.models.MindMapNode
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
                field("X") { this += NumericTextField().multiFieldEdit { it.xProperty } }
                field("Y") { this += NumericTextField().multiFieldEdit { it.yProperty } }
                field("Width") { this += NumericTextField().multiFieldEdit { it.widthProperty } }
                field("Height") { this += NumericTextField().multiFieldEdit { it.heightProperty } }
            }

            fieldset("Background") {
                hbox(5) {
                    field("Red") { this += colourEdit({ it.red }, { c, x -> Color(x, c.green, c.blue, c.opacity)}, { it.backgroundColourProperty }) }
                    field("Green") { this += colourEdit({ it.green }, { c, x -> Color(c.red, x, c.blue, c.opacity)}, { it.backgroundColourProperty }) }
                    field("Blue") { this += colourEdit({ it.blue }, { c, x -> Color(c.red, c.green, x, c.opacity)}, { it.backgroundColourProperty }) }
                }
            }

            fieldset("Text") {
                hbox(5) {
                    field("Red") { this += colourEdit({ it.red }, { c, x -> Color(x, c.green, c.blue, c.opacity)}, { it.textColourProperty }) }
                    field("Green") { this += colourEdit({ it.green }, { c, x -> Color(c.red, x, c.blue, c.opacity)}, { it.textColourProperty }) }
                    field("Blue") { this += colourEdit({ it.blue }, { c, x -> Color(c.red, c.green, x, c.opacity)}, { it.textColourProperty }) }
                }
                field("Size") { this += NumericTextField { it >= 0 } .multiFieldEdit { it.fontSizeProperty }}
            }

            fieldset("Node Suggestions") {
                listview(suggestionsList) {
                    cellFormat { suggestion ->
                        graphic = Label(suggestion)
                        onDoubleClick {
                            main.addSuggestionToSelection(suggestion)
                        }
                    }
                }
            }
        }
    }

    init {
        main.onChange += this::refresh
        refresh()
    }

    private fun <T, V> List<T>.sameBy(p: (T) -> V): Boolean = distinctBy(p).size <= 1

    private fun colourEdit(c: (Color) -> Double, d: (Color, Double) -> Color, f: (MindMapNode) -> ObjectProperty<Color>): NumericTextField {

        val textField = NumericTextField { it in 0..255 }

        return textField.multiFieldEdit({ (c(f(it).value) * 256).toInt() }, { node, x -> f(node).value = d(f(node).value, x / 256.0) })
    }

    private fun NumericTextField.multiFieldEdit(field: (MindMapNode) -> IntegerProperty): NumericTextField {
        return multiFieldEdit({ field(it).value }, { node, x -> field(node).value = x })
    }

    private fun NumericTextField.multiFieldEdit(fieldGet: (MindMapNode) -> Int, fieldSet: (MindMapNode, Int) -> Unit): NumericTextField {

        fun update() {

            if (items.isEmpty()) {
                isDisable = true
                clear()

            } else {
                isDisable = false

                if (items.sameBy { fieldGet(it.model) }) {
                    inSync = false
                    value = fieldGet(items.first().model)
                } else {
                    clear()
                }
            }
        }

        items.onChange {
            while (it.next()) {
                if (it.wasAdded()) it.addedSubList.forEach { it.onChange += ::update }
                if (it.wasRemoved()) it.removed.forEach { it.onChange -= ::update }
            }
            update()
        }

        main.onChange += ::update

        valueProperty.onChange {
            if (!isDisable) {
                items.forEach { fieldSet(it.model, value) }
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