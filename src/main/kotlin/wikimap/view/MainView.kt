package wikimap.view

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
    val nodeView = NodeView(this, mindMap.root)

    override val root = StackPane(gridView, nodeView)

    private fun refresh() {
        onChange.fireChange()
    }

    init {
        root.isFocusTraversable = true
        root.setOnMousePressed { root.requestFocus() }

        currentWindow?.widthProperty()?.onChange { refresh() }
        currentWindow?.heightProperty()?.onChange { refresh() }
        refresh()
    }
}
