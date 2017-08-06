package wikimap.view

import javafx.event.EventHandler
import javafx.scene.control.SplitPane
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.scene.layout.Pane
import javafx.scene.layout.StackPane
import javafx.scene.paint.Color
import javafx.scene.shape.Rectangle
import tornadofx.*
import wikimap.models.MindMapModel
import wikimap.models.MindMapNode
import wikimap.utils.KeyboardHandler

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

    val nodes = mutableListOf<NodeView>()
    val nodeConnections = mutableListOf<ConnectionView>()

    val selectedNodes = mutableListOf<NodeView>().observable()

    val keyboardHandler = KeyboardHandler()

    var clickX = 0.0
    var clickY = 0.0
    val rectangleSelect = Rectangle().apply {
        fill = Color(0.0,0.05,0.1,0.05)
        stroke = Color.DARKGRAY
        isVisible = false
    }
    var isRectangleSelect = false

    val mindMapView = StackPane(gridView, nodePane)
    override val root = SplitPane(mindMapView, SelectionView())

    init {
        createNodeTree(mindMap.root)

        root.addEventFilter(KeyEvent.ANY, keyboardHandler)
        root.dividers.forEach { it.positionProperty().onChange { refresh() } }

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

        mindMapView.onMouseReleased = EventHandler { event ->
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
        refresh()
    }

    fun selectNodes(vararg ns: NodeView) {
        if (!keyboardHandler.isKeyDown(KeyCode.SHIFT)) {
            for (node in selectedNodes) node.isSelected = false
            selectedNodes.clear()
        }

        for (node in ns) {
            node.isSelected = true
            selectedNodes += node
        }
    }

    private fun refresh() {
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

    private fun createChild(parent: MindMapNode, dist: Double = 3.0, angle: Double = Math.random()*2*Math.PI, width:Int=6, height:Int=4, key:String="test") {

        val parentNode = findNode(parent)!!
        val centerDist = dist + (maxOf(width, height) + maxOf(parentNode.width, parentNode.height)) / Math.sqrt(2.0)

        val centerX = parentNode.getCenterX() + centerDist * Math.cos(angle)
        val centerY = parentNode.getCenterY() + centerDist * Math.sin(angle)
        var (gridX, gridY) = gridView.toGridCoords(centerX, centerY)
        gridX = if (Math.cos(angle) < 0) Math.floor(gridX) else Math.ceil(gridX)
        gridY = if (Math.sin(angle) < 0) Math.floor(gridY) else Math.ceil(gridY)

        val childModel = MindMapNode(key, gridX.toInt() - width/2, gridY.toInt() - height/2, width, height)
        val childNode = NodeView(this, childModel)
        val conn = ConnectionView(parentNode, childNode)

        parent.children += childModel
        nodes += childNode
        nodeConnections += conn

        refresh()
    }
}
