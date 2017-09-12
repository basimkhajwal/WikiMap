package wikimap.views

import javafx.application.Platform
import javafx.event.EventHandler
import javafx.scene.control.SplitPane
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.scene.input.MouseButton
import javafx.scene.layout.BorderPane
import javafx.scene.layout.Pane
import javafx.scene.layout.StackPane
import javafx.scene.paint.Color
import javafx.scene.shape.Rectangle
import javafx.util.Duration
import tornadofx.*
import wikimap.models.MindMap
import wikimap.models.MindMapNode
import wikimap.utils.KeyboardHandler
import wikimap.suggestion.SuggestionsCache
import wikimap.suggestion.BasicSuggestionProvider

/**
 * The main view of the application
 */
class MainView : View("WikiMap") {

    val onChange = ChangeEvent()
    val suggestionProvider = SuggestionsCache(BasicSuggestionProvider())

    var mindMap = MindMap(
        MindMapNode("Machine Learning", -3, -2, 6, 4,
            mutableListOf(
                MindMapNode("Deep Learning", -9, 1, 5, 3),
                MindMapNode("Artificial Intelligence", -8, -6, 5, 3),
                MindMapNode("Neural Networks", 6, 4, 5, 4)
            )
        )
    )

    val nodePane = Pane()

    val nodes = mutableListOf<NodeView>()
    val nodeConnections = mutableListOf<ConnectionView>()
    val suggestionNodes = mutableListOf<NodeView>()
    val selectedNodes = mutableListOf<NodeView>().observable()

    private val keyboardHandler = KeyboardHandler()

    private var clickX = 0.0
    private var clickY = 0.0

    private var mousePointerX = 0.0
    private var mousePointerY = 0.0

    private var oldCenter = Pair(0.0, 0.0)

    private val rectangleSelect = Rectangle().apply {
        fill = Color(0.0,0.05,0.1,0.05)
        stroke = Color.DARKGRAY
        isVisible = false
    }


    val gridView: GridView by inject()
    val menuBarView: MenuBarView = find(mapOf(MenuBarView::main to this))
    val nodeEditView: NodeEditView = find(mapOf(NodeEditView::main to this))

    val mindMapView = StackPane(gridView.root, nodePane)
    val splitPane = SplitPane(mindMapView, nodeEditView.root)

    override val root = BorderPane(splitPane, menuBarView.root, null, null, null)

    init {
        splitPane.addEventFilter(KeyEvent.ANY, keyboardHandler)
        splitPane.dividers.forEach { it.positionProperty().onChange { refresh() } }

        titleProperty.bind(
            menuBarView.fileProperty.stringBinding {
                if (it == null) "WikiMap - Untitled" else "WikiMap - ${it.absolutePath}"
            }
        )

        nodePane += rectangleSelect

        mindMapView.onKeyTyped = EventHandler { event ->

            if (event.character == " " && selectedNodes.size == 1) {
                val selectedNode = selectedNodes.first().model
                val (mouseX, mouseY) = gridView.toGridCoords(mousePointerX, mousePointerY)

                val newNode = MindMapNode("...", mouseX.toInt() - 3, mouseY.toInt() - 2, 6, 4)
                createChild(selectedNode, newNode, false)
            }

            if (event.character == "x") {

                val removeNodes = selectedNodes.filter { isLeaf(it.model) && !isRoot(it.model) }
                selectNodes()

                nodeConnections
                    .filter { removeNodes.contains(it.parent) || removeNodes.contains(it.child) }
                    .forEach { it.removeFromParent() }


                removeNodes.forEach { node ->
                    node.removeFromParent()
                    removeSuggestions(node)
                    nodes.forEach { it.model.children.remove(node.model) }
                }
                nodes.removeAll(removeNodes)

                refresh()
            }
        }

        mindMapView.onMousePressed = EventHandler { event ->
            clickX = event.x
            clickY = event.y

            if (event.button == MouseButton.SECONDARY) {
                oldCenter = gridView.gridCenter
            } else {
                selectNodes()
                mindMapView.requestFocus()
                rectangleSelect.x = event.x
                rectangleSelect.y = event.y
                rectangleSelect.toFront()
            }
        }

        mindMapView.onMouseDragged = EventHandler { event ->
            if (event.button == MouseButton.SECONDARY) {
                val dx = (event.x - clickX) / gridView.root.width
                val dy = (event.y - clickY) / gridView.root.height
                gridView.gridCenter = Pair(oldCenter.first + dx, oldCenter.second + dy)
                refresh()
            } else {
                rectangleSelect.isVisible = true
                rectangleSelect.x = minOf(event.x, clickX)
                rectangleSelect.y = minOf(event.y, clickY)
                rectangleSelect.width = maxOf(event.x, clickX) - rectangleSelect.x
                rectangleSelect.height = maxOf(event.y, clickY) - rectangleSelect.y
            }
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

        mindMapView.onMouseMoved = EventHandler { event ->
            mousePointerX = event.x
            mousePointerY = event.y
        }

        mindMapView.isFocusTraversable = true
        loadModel(mindMap)
    }

    override fun onDock() {
        super.onDock()

        currentStage?.isResizable = true
        currentWindow?.widthProperty()?.onChange { refresh() }
        currentWindow?.heightProperty()?.onChange { refresh() }

        refresh()
        runLater(Duration(200.0)) { refresh() } // dirty fix
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

    fun addSuggestionToSelection(suggestion: String) {
        assert(selectedNodes.size == 1)
        createChild(selectedNodes.first().model, key = suggestion)
    }

    fun showSuggestions(parent: NodeView) {
        runAsync {
            suggestionProvider.getSuggestions(parent.model.key)
        } ui { suggestions ->

            // Make sure node still exists and is selected
            if (nodes.contains(parent) && selectedNodes.contains(parent)) {
                if (suggestions.isNotEmpty()) createChild(parent.model, key = suggestions[0], angle = 0.0, isSuggestion = true)
                if (suggestions.size > 1) createChild(parent.model, key = suggestions[1], angle = 120.0, isSuggestion = true)
                if (suggestions.size > 2) createChild(parent.model, key = suggestions[2], angle = 240.0, isSuggestion = true)
            }
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

    private fun isLeaf(model: MindMapNode): Boolean = model.children.isEmpty()

    private fun isRoot(model: MindMapNode): Boolean = nodes.all { !it.model.children.contains(model) }

    private fun findNode(model: MindMapNode): NodeView? = nodes.find { it.model == model }

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
