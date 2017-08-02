package wikimap.view;

import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.control.Label
import javafx.scene.layout.Pane
import javafx.scene.layout.StackPane
import javafx.scene.paint.Color
import javafx.scene.shape.Circle
import javafx.scene.shape.Line
import javafx.scene.shape.Rectangle
import javafx.scene.text.FontWeight
import javafx.scene.text.TextAlignment
import wikimap.models.MindMapNode
import tornadofx.*

class NodeViewModel(val main: MainView, val model: MindMapNode, var parent: NodeConnection? = null) {

    class NodeConnection(val parent: NodeViewModel, val child: NodeViewModel) {

        private val a = parent
        private val b = child

        val line = Line(0.0,0.0,0.0,0.0).apply {
            strokeWidth = 2.0
        }

        fun refresh() {
            line.startX = a.node.layoutX + a.node.prefWidth / 2
            line.startY = a.node.layoutY + a.node.prefHeight / 2
            line.endX = b.node.layoutX + b.node.prefWidth / 2
            line.endY = b.node.layoutY + b.node.prefHeight / 2
        }

        init {
            child.parent = this
            refresh()
            //line.clip = Circle(line.startX, line.startY, 100.0)
            val clipPane = Pane().apply {
                style {
                    fillWidth = true
                    fillHeight = true
                }
                this += line

            }
            parent.main.buttonPane += clipPane
        }
    }

    private val rect: Rectangle =
        Rectangle(main.gridSpacing * model.width.toDouble(),
                  main.gridSpacing * model.height.toDouble()).apply {
            arcWidth = main.gridSpacing.toDouble()
            arcHeight = main.gridSpacing.toDouble()
            fill = Color(Math.random(), Math.random(), Math.random(), 1.0)
        }

    private val label: Label = Label(model.key).apply {
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
            val cx = Math.round(nx).toInt()
            val cy = Math.round(ny).toInt()

            if (cx != model.x || cy != model.y) {
                model.width = (model.x + model.width) - cx
                model.height = (model.y + model.height) - cy
                model.x = cx
                model.y = cy
            } else {
                model.width = Math.round(w / spacing).toInt()
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
