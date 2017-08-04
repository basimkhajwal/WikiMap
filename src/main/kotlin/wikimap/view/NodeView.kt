package wikimap.view

import javafx.beans.property.SimpleStringProperty
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.control.*
import javafx.scene.layout.StackPane
import javafx.scene.paint.Color
import javafx.scene.shape.Rectangle
import javafx.scene.text.FontWeight
import javafx.scene.text.TextAlignment
import wikimap.models.MindMapNode
import tornadofx.*

class NodeView(val main: MainView, val model: MindMapNode): StackPane() {

    val rect: Rectangle =
        Rectangle(main.gridSpacing * model.width.toDouble(),
                  main.gridSpacing * model.height.toDouble()).apply {
            arcWidth = main.gridSpacing.toDouble()
            arcHeight = main.gridSpacing.toDouble()
            fill = Color(Math.random(), Math.random(), Math.random(), 0.7)
        }

    val keyText = SimpleStringProperty(model.key)
    val label: Label = Label(model.key).apply {
        style {
            textFill = Color.WHITE
            fontWeight = FontWeight.BOLD
            alignment = Pos.CENTER
            textAlignment = TextAlignment.CENTER
            wrapText = true
        }
        textProperty().bind(keyText)
    }
    val textArea: TextArea = TextArea(model.key).apply {
        style {
            textFill = Color.WHITE
            fontWeight = FontWeight.BOLD
            wrapText = true
            backgroundColor = multi(Color.TRANSPARENT)
        }
        paddingTop = 10
        paddingBottom = 10

        textProperty().bindBidirectional(keyText)
        isVisible = false
    }

    val onChange = ChangeEvent()

    val spacing = main.gridSpacing
    val grid = main.gridView
    val children = model.children.map{ ConnectionView(this, NodeView(main, it)) }.toMutableList()

    fun getCenterX(): Double = grid.fromGridCoords(model.x + model.width/2.0, 0.0).first
    fun getCenterY(): Double = grid.fromGridCoords(0.0, model.y + model.height/2.0).second

    private fun refresh() {
        val (x, y) = grid.fromGridCoords(model.x.toDouble(), model.y.toDouble())
        rect.width = model.width.toDouble() * spacing
        rect.height = model.height.toDouble() * spacing
        relocate(x, y)
        setPrefSize(rect.width, rect.height)

        onChange.fireChange()
        toFront()
    }

    fun createChild(dist: Double = 3.0, angle: Double = Math.random()*2*Math.PI, width:Int=6, height:Int=4, key:String="test") {

        val centerDist = dist + (maxOf(width, height) + maxOf(this.width, this.height)) / Math.sqrt(2.0)

        val centerX = getCenterX() + centerDist * Math.cos(angle)
        val centerY = getCenterY() + centerDist * Math.sin(angle)
        var (gridX, gridY) = grid.toGridCoords(centerX, centerY)
        gridX = if (centerX < getCenterX()) Math.floor(gridX) else Math.ceil(gridX)
        gridY = if (centerY < getCenterX()) Math.floor(gridY) else Math.ceil(gridY)

        val childModel = MindMapNode(key, gridX.toInt() - width/2, gridY.toInt() - height/2, width, height)
        val childNode = NodeView(main, childModel)

        model.children += childModel
        children += ConnectionView(this, childNode)
        refresh()
    }

    fun getAllChildren(node: Node): List<Node> {
        val children = node.getChildList()?.toList() ?: listOf()
        return children + children.flatMap { getAllChildren(it) }.toList()
    }

    private fun showTextArea() {
        label.isVisible = false
        textArea.isVisible = true

        val scrollPane = textArea.childrenUnmodifiable[0] as ScrollPane
        scrollPane.vbarPolicy = ScrollPane.ScrollBarPolicy.NEVER

        for (child in getAllChildren(textArea)) {
            child.style {
                backgroundColor = multi(Color(0.0,0.0,0.0,0.05))
                alignment = Pos.CENTER
                textAlignment = TextAlignment.CENTER
            }

            // Prevent blurry text
            child.isCache = false
        }

        textArea.requestFocus()
    }

    private fun hideTextArea() {
        label.isVisible = true
        textArea.isVisible = false
    }

    private val resizeListener = object : DragResizeMod.OnDragResizeEventListener {
        override fun onResize(n: Node?, x: Double, y: Double, h: Double, w: Double) {
            val (nx, ny) = grid.toGridCoords(x, y)

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
            refresh()
        }

        override fun onDrag(n: Node?, x: Double, y: Double, h: Double, w: Double) {
            val (nx, ny) = grid.toGridCoords(x, y)
            model.x = Math.round(nx).toInt()
            model.y = Math.round(ny).toInt()
            refresh()
        }
    }

    init {
        DragResizeMod.makeResizable(this, resizeListener)

        this += rect
        this += label
        this += textArea

        keyText.onChange { if (it != null) model.key = it }

        label.onDoubleClick { showTextArea() }
        textArea.focusedProperty().onChange { if (!it) hideTextArea() }

        rect.onDoubleClick { /* TODO: Add selection code */ }

        main.onChange += this::refresh
        refresh()
    }
}
