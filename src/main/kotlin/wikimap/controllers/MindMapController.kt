package wikimap.controllers

import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.collections.transformation.FilteredList
import javafx.scene.input.KeyCode
import tornadofx.*
import wikimap.app.BasicSuggestionProvider
import wikimap.models.MindMap
import wikimap.models.MindMapNode
import wikimap.utils.KeyboardHandler
import wikimap.utils.SuggestionsCache
import wikimap.views.NodeView

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

    val gridCenterProperty = SimpleObjectProperty<Pair<Double, Double>>(0.0 to 0.0)
    var gridCenter by gridCenterProperty

    val keyboardHandler = KeyboardHandler()
    val suggestionProvider = SuggestionsCache(BasicSuggestionProvider())

    val mindMapNodes = mutableListOf<MindMapNode>().observable()
    val selectedNodes = FilteredList(mindMapNodes) { it.isSelected }

    init {
        loadModel(initMindMap)
    }

    fun loadModel(newModel: MindMap) {
        model = newModel

        mindMapNodes.clear()
        createNodeTree(newModel.root)
    }

    fun select(vararg nodes: MindMapNode) {
        select(nodes.toList())
    }

    fun select(nodes: Iterable<MindMapNode>) {
        //if (selectedNodes.size == 1) {
        //    removeSuggestions(selectedNodes[0])
        //}

        if (!keyboardHandler.isKeyDown(KeyCode.SHIFT)) {
            selectedNodes.forEach { it.isSelected = false }
        }

        nodes.forEach { it.isSelected = false }

        //if (selectedNodes.size == 1) {
            //Platform.runLater {
                //showSuggestions(selectedNodes[0])
        //    }
        //}
    }

    fun showSuggestions(parent: NodeView) {
        val suggestions = suggestionProvider.getSuggestions(parent.keyText.get())

        if (suggestions.size > 0) {
            createChild(parent.model, key = suggestions[0], angle = 0.0, isSuggestion = true)
        }

        if (suggestions.size > 1) {
            createChild(parent.model, key = suggestions[1], angle = 120.0, isSuggestion = true)
        }

        if (suggestions.size > 2) {
            createChild(parent.model, key = suggestions[2], angle = 240.0, isSuggestion = true)
        }
    }

    fun removeSuggestions(parent: NodeView) {
        val toRemove = nodeConnections.filter { it.parent == parent && suggestionNodes.contains(it.child) }

        nodeConnections.removeAll(toRemove)
        toRemove.forEach {
            it.removeFromParent()
            it.child.removeFromParent()
            suggestionNodes.remove(it.child)
        }
    }

    fun includeSuggestion(parent: NodeView, suggestion: NodeView) {
        val conn = nodeConnections.find { it.child == suggestion } !!
        conn.removeFromParent()
        nodeConnections.remove(conn)

        suggestionNodes.remove(suggestion)
        suggestion.removeFromParent()

        val child = createChild(parent.model, suggestion.model.copy(), false)
        selectNodes(child)
    }

    private fun createNodeTree(node: MindMapNode) {
        mindMapNodes.add(node)
        node.children.forEach { createNodeTree(it) }
    }
}