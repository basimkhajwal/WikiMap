package wikimap.view

import javafx.beans.binding.Bindings
import javafx.beans.property.SimpleStringProperty
import javafx.beans.property.StringProperty
import javafx.beans.value.ObservableStringValue
import javafx.event.EventHandler
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.control.*
import javafx.scene.layout.Background
import javafx.scene.layout.StackPane
import javafx.scene.paint.Color
import javafx.scene.shape.Rectangle
import javafx.scene.text.FontSmoothingType
import javafx.scene.text.FontWeight
import javafx.scene.text.TextAlignment
import wikimap.models.MindMapNode
import tornadofx.*

class NodeViewModel(val main: MainView, val model: MindMapNode) {

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

    val node = StackPane(rect, label, textArea)
    val spacing = main.gridSpacing
    val children = model.children.map{ NodeConnection(this, NodeViewModel(main, it)) }.toMutableList()

    fun fromGridCoords(x: Double, y: Double): Pair<Double, Double> {
        return Pair(x*spacing + main.canvas.width/2, y*spacing + main.canvas.height/2)
    }

    fun toGridCoords(x: Double, y: Double): Pair<Double, Double> {
        return Pair((x - main.canvas.width/2) / spacing, (y - main.canvas.height/2) / spacing)
    }

    fun getCenterX(): Double = fromGridCoords(model.x + model.width/2.0, 0.0).first
    fun getCenterY(): Double = fromGridCoords(0.0, model.y + model.height/2.0).second

    fun refresh() {
        val (x, y) = fromGridCoords(model.x.toDouble(), model.y.toDouble())
        rect.width = model.width.toDouble() * spacing
        rect.height = model.height.toDouble() * spacing
        node.relocate(x, y)
        node.setPrefSize(rect.width, rect.height)

        onChange.fireChange()
        node.toFront()
    }

    fun createChild(dist: Double = 3.0, angle: Double = Math.random()*2*Math.PI, width:Int=6, height:Int=4, key:String="test") {

        val centerDist = dist + (maxOf(width, height) + maxOf(node.width, node.height)) / Math.sqrt(2.0)

        val centerX = getCenterX() + centerDist * Math.cos(angle)
        val centerY = getCenterY() + centerDist * Math.sin(angle)
        var (gridX, gridY) = toGridCoords(centerX, centerY)
        gridX = if (centerX < getCenterX()) Math.floor(gridX) else Math.ceil(gridX)
        gridY = if (centerY < getCenterX()) Math.floor(gridY) else Math.ceil(gridY)

        val childModel = MindMapNode(key, gridX.toInt() - width/2, gridY.toInt() - height/2, width, height)
        val childNode = NodeViewModel(main, childModel)

        model.children += childModel
        children += NodeConnection(this, childNode)
        refresh()
    }

    fun getAllChildren(node: Node): List<Node> {
        val children = node.getChildList()?.toList() ?: listOf()
        return children + children.flatMap { getAllChildren(it) }.toList()
    }

    fun showTextArea() {
        label.isVisible = false
        textArea.isVisible = true

        (textArea.childrenUnmodifiable[0] as ScrollPane).vbarPolicy = ScrollPane.ScrollBarPolicy.NEVER
        for (child in getAllChildren(textArea)) {
            child.style {
                backgroundColor = multi(Color(0.0,0.0,0.0,0.1))
                alignment = Pos.CENTER
                textAlignment = TextAlignment.CENTER
            }

            // Prevent blurry text
            child.isCache = false
        }

        textArea.requestFocus()
    }

    fun hideTextArea() {
        label.isVisible = true
        textArea.isVisible = false
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
            refresh()
        }

        override fun onDrag(n: Node?, x: Double, y: Double, h: Double, w: Double) {
            val (nx, ny) = toGridCoords(x, y)
            model.x = Math.round(nx).toInt()
            model.y = Math.round(ny).toInt()
            refresh()
        }
    }

    init {
        DragResizeMod.makeResizable(node, resizeListener)
        main.buttonPane += node
        main.onChange += this::refresh

        keyText.onChange { if (it != null) model.key = it }

        label.onDoubleClick { showTextArea() }
        textArea.focusedProperty().onChange { if (!it) hideTextArea() }

        rect.onDoubleClick {  println("CLICKED!") }

        refresh()
    }
}
