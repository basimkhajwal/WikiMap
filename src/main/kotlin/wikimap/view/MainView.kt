package wikimap.view

import javafx.application.Platform
import javafx.event.EventHandler
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.scene.layout.BorderPane
import javafx.scene.layout.Pane
import javafx.scene.layout.StackPane
import javafx.scene.paint.Color
import javafx.scene.shape.Rectangle
import tornadofx.*
import wikimap.models.MindMap
import wikimap.models.MindMapNode
import wikimap.utils.KeyboardHandler
import wikimap.utils.SuggestionsCache
import wikimap.app.BasicSuggestionProvider
import wikimap.utils.UpdateEvent

/**
 * The main view of the application
 */
class MainView : View("WikiMap") {

    val onChange = ChangeEvent()
    val suggestionProvider = SuggestionsCache(BasicSuggestionProvider())

    val gridSpacing: Int = 20

    var mindMap = MindMap(
        MindMapNode("Machine Learning", -3, -2, 6, 4,
            mutableListOf(
                MindMapNode("Deep Learning", -9, 1, 5, 3),
                MindMapNode("Artificial Intelligence", -8, -6, 5, 3),
                MindMapNode("Neural Networks", 6, 4, 5, 4)
            )
        )
    )

    val keyboardHandler = KeyboardHandler()
    val nodePane = Pane()
    val gridView: GridView by inject()

    val nodes = mutableListOf<NodeView>()
    val suggestionNodes = mutableListOf<NodeView>()
    val selectedNodes = mutableListOf<NodeView>().observable()
    val nodeConnections = mutableListOf<ConnectionView>()

    var clickX = 0.0
    var clickY = 0.0
    val rectangleSelect = Rectangle().apply {
        fill = Color(0.0,0.05,0.1,0.05)
        stroke = Color.DARKGRAY
        isVisible = false
    }

    val mindMapView = StackPane(gridView.root, nodePane, NodeEditView(this))

    override val root = BorderPane(mindMapView, MenuBarView(this), null, null, null)

    init {
        root.addEventFilter(KeyEvent.ANY, keyboardHandler)

        nodePane += rectangleSelect

        mindMapView.onMousePressed = EventHandler { event ->
            selectNodes()

            mindMapView.requestFocus()
            clickX = event.x
            clickY = event.y

            rectangleSelect.x = event.x
            rectangleSelect.y = event.y
            rectangleSelect.toFront()
        }

        mindMapView.onMouseDragged = EventHandler { event ->
            rectangleSelect.isVisible = true

            rectangleSelect.x = minOf(event.x, clickX)
            rectangleSelect.y = minOf(event.y, clickY)
            rectangleSelect.width = maxOf(event.x, clickX) - rectangleSelect.x
            rectangleSelect.height = maxOf(event.y, clickY) - rectangleSelect.y
        }

        mindMapView.onMouseReleased = EventHandler {
            if (rectangleSelect.isVisible) {
                rectangleSelect.isVisible = false

                selectNodes(*nodes.filter { node ->
                    rectangleSelect.contains(node.layoutX, node.layoutY) &&
                    rectangleSelect.contains(node.layoutX + node.width, node.layoutY + node.height)
                }.toTypedArray())
            }
        }

        mindMapView.isFocusTraversable = true

        currentWindow?.widthProperty()?.onChange { refresh() }
        currentWindow?.heightProperty()?.onChange { refresh() }

        loadModel(mindMap)
    }

    fun loadModel(model: MindMap) {

        selectedNodes.clear()

        nodes.forEach { it.removeFromParent() }
        nodes.clear()

        suggestionNodes.forEach { it.removeFromParent() }
        suggestionNodes.clear()

        nodeConnections.forEach { it.removeFromParent() }
        nodeConnections.clear()

        mindMap = model
        createNodeTree(mindMap.root)
        refresh()
    }

    fun selectNodes(vararg ns: NodeView) {
        if (selectedNodes.size == 1) {
            removeSuggestions(selectedNodes[0])
        }

        if (!keyboardHandler.isKeyDown(KeyCode.SHIFT) && selectedNodes.isNotEmpty()) {
            for (node in selectedNodes) node.isSelected = false
            selectedNodes.clear()
        }

        for (node in ns) {
            if (!node.isSelected) {
                node.isSelected = true
                selectedNodes += node
            }
        }

        if (selectedNodes.size == 1) {
            Platform.runLater {
                showSuggestions(selectedNodes[0])
            }
        }
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

    fun refresh() {
        fire(UpdateEvent)
        onChange.fireChange()
    }

    private fun createNodeTree(node: MindMapNode): NodeView {
        val nodeView = NodeView(this, node)
        nodes += nodeView
        nodePane += nodeView

        for (child in node.children) {
            val childView = createNodeTree(child)
            val conn = ConnectionView(nodeView, childView)

            nodePane += conn
            nodeConnections += conn
        }

        return nodeView
    }

    private fun findNode(model: MindMapNode): NodeView? {
        return nodes.find { it.model == model }
    }

    private fun createChild(parent: MindMapNode, childModel: MindMapNode, isSuggestion: Boolean): NodeView {
        val parentNode = findNode(parent)!!
        val childNode =
                if (isSuggestion) NodeView(this, childModel, parentNode)
                else NodeView(this, childModel)

        val conn = ConnectionView(parentNode, childNode)
        nodeConnections += conn

        if (isSuggestion) {
            suggestionNodes += childNode
        } else {
            parent.children += childModel
            nodes += childNode
        }

        nodePane += conn
        nodePane += childNode
        refresh()

        return childNode
    }

    private fun createChild(
            parent: MindMapNode, dist: Double = 0.0,
            angle: Double = Math.random()*2*Math.PI,
            width:Int=6, height:Int=4,
            key:String="test", isSuggestion: Boolean = false): NodeView {

        val centerDist = dist + (maxOf(width, height) + maxOf(parent.width, parent.height)) / Math.sqrt(2.0)

        val centerX = parent.x + parent.width/2.0 + centerDist * Math.cos(angle)
        val centerY = parent.y + parent.height/2.0 + centerDist * Math.sin(angle)
        val gridX = if (Math.cos(angle) < 0) Math.floor(centerX).toInt() else Math.ceil(centerX).toInt()
        val gridY = if (Math.sin(angle) < 0) Math.floor(centerY).toInt() else Math.ceil(centerY).toInt()

        val childModel = MindMapNode(key, gridX - width/2, gridY - height/2, width, height)

        return createChild(parent, childModel, isSuggestion)
    }
}
