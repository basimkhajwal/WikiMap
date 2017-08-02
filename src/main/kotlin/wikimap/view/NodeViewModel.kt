package wikimap.view;

import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.control.Label
import javafx.scene.layout.StackPane
import javafx.scene.paint.Color
import javafx.scene.shape.Rectangle
import javafx.scene.text.FontWeight
import javafx.scene.text.TextAlignment
import wikimap.models.MindMapNode
import tornadofx.*

class NodeViewModel(val main: MainView, val model: MindMapNode) {

    private val rect: Rectangle =
        Rectangle(main.gridSpacing * model.width.toDouble(),
                  main.gridSpacing * model.height.toDouble()).apply {
            arcWidth = main.gridSpacing.toDouble()
            arcHeight = main.gridSpacing.toDouble()
            fill = Color(Math.random(), Math.random(), Math.random(), 0.7)
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
    val children: MutableList<NodeViewModel> =
            model.children.map{ NodeViewModel(main, it) }.toMutableList()

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
    }

    fun setupAll() {
        setup()
        children.forEach { it.setupAll() }
    }

    val resizeListener = object : DragResizeMod.OnDragResizeEventListener {
        override fun onResize(n: Node?, x: Double, y: Double, h: Double, w: Double) {
            val (nx, ny) = toGridCoords(x, y)
            model.x = Math.round(nx).toInt()
            model.y = Math.round(ny).toInt()
            model.width = w.toInt() / spacing
            model.height = h.toInt() / spacing
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
