package wikimap.views

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
import wikimap.controllers.MindMapController
import wikimap.utils.UpdateEvent

/**
 * The main view of the application
 */
class MainView : View("WikiMap") {

    val onChange = ChangeEvent()

    private val nodePane = Pane()

    var clickX = 0.0
    var clickY = 0.0
    val rectangleSelect = Rectangle().apply {
        fill = Color(0.0,0.05,0.1,0.05)
        stroke = Color.DARKGRAY
        isVisible = false
    }

    val controller: MindMapController by inject()

    val gridView: GridView by inject()
    val menuBarView: MenuBarView by inject()

    val mindMapView = StackPane(gridView.root, nodePane, NodeEditView(this))

    override val root = BorderPane(mindMapView, menuBarView.root, null, null, null)

    init {
        root.addEventFilter(KeyEvent.ANY, controller.keyboardHandler)

        nodePane += rectangleSelect

        mindMapView.onMousePressed = EventHandler { event ->
            controller.select()

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

                val (gx, gy) = gridView.toGridCoords(rectangleSelect.x, rectangleSelect.y)
                val gridRect = Rectangle(gx, gy,
                        rectangleSelect.width * controller.gridSpacing,
                        rectangleSelect.height * controller.gridSpacing)

                val selectedNodes = controller.mindMapNodes.filter { node ->
                    gridRect.contains(node.x.toDouble(), node.y.toDouble()) &&
                    gridRect.contains(node.x.toDouble() + node.width, node.y.toDouble() + node.height)
                }

                controller.select(selectedNodes)
            }
        }

        mindMapView.isFocusTraversable = true

        controller.mindMapNodes.onChange { change ->

            change.addedSubList.forEach {

                val conn = find<ConnectionView>(
                    mapOf("parent" to nodeView, "child" to childView)
                ) //ConnectionView(nodeView, childView)

            }

        }
    }

    private fun createNodeTree(node: MindMapNode): NodeView {
        val nodeView = NodeView(this, node)
        nodes += nodeView
        nodePane += nodeView

        for (child in node.children) {
            val childView = createNodeTree(child)

            nodePane += conn.root
            nodeConnections += conn
        }

        return nodeView
    }

    private fun createChild(parent: MindMapNode, childModel: MindMapNode, isSuggestion: Boolean): NodeView {
        val parentNode = findNode(parent)!!
        val childNode =
                if (isSuggestion) NodeView(this, childModel, parentNode)
                else NodeView(this, childModel)

        val conn = find<ConnectionView>(mapOf("parent" to parentNode, "child" to childNode))
        nodeConnections += conn

        if (isSuggestion) {
            suggestionNodes += childNode
        } else {
            parent.children += childModel
            nodes += childNode
        }

        nodePane += conn.root
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
