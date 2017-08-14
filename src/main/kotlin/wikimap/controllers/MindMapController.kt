package wikimap.controllers

import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.collections.ObservableList
import javafx.collections.transformation.FilteredList
import tornadofx.*
import wikimap.models.MindMap
import wikimap.models.MindMapNode
import wikimap.utils.KeyboardHandler

/**
 * Created by Basim on 12/08/2017.
 */
class MindMapController : Controller() {

    private val initMindMap = MindMap(
        MindMapNode("Machine Learning", -3, -2, 6, 4,
            mutableListOf(
                MindMapNode("Deep Learning", -9, 1, 5, 3),
                MindMapNode("Artificial Intelligence", -8, -6, 5, 3),
                MindMapNode("Neural Networks", 6, 4, 5, 4)
            )
        )
    )

    private val modelProperty = SimpleObjectProperty(initMindMap)
    var model: MindMap
        get() = modelProperty.get()
        private set(value) = modelProperty.set(value)

    val gridSpacingProperty = SimpleIntegerProperty(20)
    var gridSpacing by gridSpacingProperty

    val keyboardHandler = KeyboardHandler()

    val mindMapNodes = mutableListOf<MindMapNode>().observable()

    init {

        loadModel(initMindMap)
    }

    fun loadModel(newModel: MindMap) {
        model = newModel

        mindMapNodes.clear()
        createNodeTree(newModel.root)
    }

    private fun createNodeTree(node: MindMapNode) {
        mindMapNodes.add(node)
        node.children.forEach { createNodeTree(it) }
    }
}