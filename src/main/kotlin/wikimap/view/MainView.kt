package wikimap.view

import javafx.scene.control.SplitPane
import javafx.scene.layout.Pane
import javafx.scene.layout.StackPane
import tornadofx.*
import wikimap.models.MindMapModel
import wikimap.models.MindMapNode

/**
 * The main view of the application
 */
class MainView : View("WikiMap") {

    val onChange = ChangeEvent()

    val gridSpacing: Int = 20

    /* Fixed for now, but can be changed later */
    val mindMap = MindMapModel(
        MindMapNode("Machine Learning", -3, -2, 6, 4,
            mutableListOf(
                MindMapNode("Deep Learning", -9, 1, 5, 3),
                MindMapNode("Artificial Intelligence", -8, -6, 5, 3),
                MindMapNode("Neural Networks", 6, 4, 5, 4)
            )
        )
    )

    val gridView = GridView(this)
    val nodePane = Pane()

    override val root = SplitPane(StackPane(gridView, nodePane), SelectionView())

    private fun refresh() {
        onChange.fireChange()
    }

    init {

        NodeView(this, mindMap.root)

        root.dividers.forEach { it.positionProperty().onChange { refresh() } }

        root.isFocusTraversable = true
        root.setOnMousePressed { root.requestFocus() }

        currentWindow?.widthProperty()?.onChange { refresh() }
        currentWindow?.heightProperty()?.onChange { refresh() }
        refresh()
    }
}
