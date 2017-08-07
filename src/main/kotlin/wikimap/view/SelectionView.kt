package wikimap.view

import javafx.beans.property.SimpleIntegerProperty
import javafx.geometry.Orientation
import javafx.scene.control.Label
import javafx.scene.control.ListView
import javafx.scene.control.TextField
import javafx.scene.control.TitledPane
import javafx.scene.control.cell.TextFieldListCell
import javafx.scene.layout.GridPane
import javafx.scene.layout.Pane
import javafx.scene.layout.VBox
import tornadofx.*
import wikimap.app.BasicSuggestionProvider
import wikimap.utils.NumericTextField
import wikimap.utils.SuggestionsCache

/**
 * Created by Basim on 04/08/2017.
 */
class SelectionView(val main: MainView) : Pane() {

    val contentPane = VBox()

    val propertiesGrid = GridPane()

    val xProperty = SimpleIntegerProperty()
    val yProperty = SimpleIntegerProperty()
    val widthProperty = SimpleIntegerProperty()
    val heightProperty = SimpleIntegerProperty()

    val suggestionsList = ListView<String>()
    private var oldKey = ""
    val suggestionProvider = SuggestionsCache(BasicSuggestionProvider())

    val root = TitledPane("Edit Node", contentPane)

    init {
        contentPane.label("Node Properties")

        propertiesGrid.add(Label("X:"), 0, 0)
        propertiesGrid.add(Label("Y:"), 0, 1)
        propertiesGrid.add(Label("Width:"), 0, 2)
        propertiesGrid.add(Label("Height:"), 0, 3)

        val xLabel = NumericTextField()
        val yLabel = NumericTextField()
        val widthLabel = NumericTextField()
        val heightLabel = NumericTextField()

        propertiesGrid.add(xLabel, 1, 0)
        propertiesGrid.add(yLabel, 1, 1)
        propertiesGrid.add(widthLabel, 1, 2)
        propertiesGrid.add(heightLabel, 1, 3)

        contentPane += propertiesGrid

        contentPane.label("Node Suggestions")
        suggestionsList.orientation = Orientation.VERTICAL
        suggestionsList.cellFactory = TextFieldListCell.forListView()
        contentPane += suggestionsList


        this.layoutBoundsProperty().onChange {
            root.prefWidth = layoutBounds.width
            root.prefHeight = layoutBounds.height
        }

        root.isCollapsible = false
        this += root

        main.onChange += this::refresh
        main.selectedNodes.onChange { refresh() }
    }

    private fun refresh() {
        if (main.selectedNodes.isEmpty()) {
            if (oldKey != "") {
                suggestionsList.items.clear()
                oldKey = ""
            }
            return
        }

        val key = main.selectedNodes.first().keyText.get()

        if (key != oldKey) {
            suggestionsList.items.clear()
            suggestionsList.items.addAll(suggestionProvider.getSuggestions(key))
            oldKey = key
        }
    }
}