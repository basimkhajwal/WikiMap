package wikimap.view;

import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.control.Label
import javafx.scene.layout.Pane
import javafx.scene.layout.StackPane
import javafx.scene.paint.Color
import javafx.scene.shape.Line
import javafx.scene.shape.Rectangle
import javafx.scene.shape.Shape
import javafx.scene.text.FontWeight
import javafx.scene.text.TextAlignment
import wikimap.models.MindMapNode
import tornadofx.*

class NodeViewModel(val main: MainView, val model: MindMapNode, var parent: NodeConnection? = null) {

    class NodeConnection(val parent: NodeViewModel, val child: NodeViewModel) {
        val line = Line(0.0,0.0,0.0,0.0)
        val clipPane = Pane(line)

        fun refresh() {
            line.startX = parent.node.layoutX + parent.node.prefWidth / 2
            line.startY = parent.node.layoutY + parent.node.prefHeight / 2
            line.endX = child.node.layoutX + child.node.prefWidth / 2
            line.endY = child.node.layoutY + child.node.prefHeight / 2

            val total = Rectangle(clipPane.layoutBounds.width, clipPane.layoutBounds.height)
            val mask = Shape.subtract(total, Shape.union(parent.rect, child.rect))
            clipPane.clip = mask
        }

        init {
            child.parent = this
            parent.main.buttonPane += clipPane
            refresh()
        }
    }

    val rect: Rectangle =
        Rectangle(main.gridSpacing * model.width.toDouble(),
                  main.gridSpacing * model.height.toDouble()).apply {
            arcWidth = main.gridSpacing.toDouble()
            arcHeight = main.gridSpacing.toDouble()
            fill = Color(Math.random(), Math.random(), Math.random(), 0.7)
        }

    val label: Label = Label(model.key).apply {
        style {
            textFill = Color.WHITE
            fontWeight = FontWeight.BOLD
            alignment = Pos.CENTER
            textAlignment = TextAlignment.CENTER
        }
    }

    val node = StackPane(rect, label)
    val spacing = main.gridSpacing
    val children: MutableList<NodeConnection> =
            model.children.map{ NodeConnection(this, NodeViewModel(main, it)) }.toMutableList()

    fun fromGridCoords(x: Double, y: Double): Pair<Double, Double> {
        return Pair(x*spacing + main.canvas.width/2, y*spacing + main.canvas.height/2)
    }

    fun toGridCoords(x: Double, y: Double): Pair<Double, Double> {
        return Pair((x - main.canvas.width/2) / spacing, (y - main.canvas.height/2) / spacing)
    }

    fun setup() {
        val (x, y) = fromGridCoords(model.x.toDouble(), model.y.toDouble())
        rect.width = model.width.toDouble() * spacing
        rect.height = model.height.toDouble() * spacing
        node.relocate(x, y)
        node.setPrefSize(rect.width, rect.height)

        parent?.refresh()
        children.forEach { it.refresh() }
        node.toFront()
    }

    fun setupAll() {
        setup()
        children.forEach {
            it.child.setupAll()
            it.refresh()
        }
    }

    val resizeListener = object : DragResizeMod.OnDragResizeEventListener {
        override fun onResize(n: Node?, x: Double, y: Double, h: Double, w: Double) {
            val (nx, ny) = toGridCoords(x, y)

            if (nx != model.x.toDouble()) {
                val cx = Math.round(nx).toInt()
                model.width = (model.x + model.width) - cx
                model.x = cx
            } else {
                model.width = Math.round(w / spacing).toInt()
            }

            if (ny != model.y.toDouble()) {
                val cy = Math.round(ny).toInt()
                model.height = (model.y + model.height) - cy
                model.y = cy
            } else {
                model.height = Math.round(h / spacing).toInt()
            }

            setup()
        }

        override fun onDrag(n: Node?, x: Double, y: Double, h: Double, w: Double) {
            val (nx, ny) = toGridCoords(x, y)
            model.x = Math.round(nx).toInt()
            model.y = Math.round(ny).toInt()
            setup()
        }
    }

    init {
        setup()
        DragResizeMod.makeResizable(node, resizeListener)
        main.buttonPane += node
    }
}
