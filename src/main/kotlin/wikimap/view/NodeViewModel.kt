package wikimap.view

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
import wikimap.app.BasicSuggestionProvider

class NodeViewModel(val main: MainView, val model: MindMapNode, var parent: NodeConnection? = null) {

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
            wrapText = true
        }
    }

    val node = StackPane(rect, label)
    val spacing = main.gridSpacing
    val children: MutableList<NodeConnection> =
            model.children.map{ NodeConnection(this, NodeViewModel(main, it)) }.toMutableList()

    var suggestions: MutableList<NodeConnection>? = null

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
        if (suggestions != null) updateSuggestions()
        node.toFront()
    }

    fun setupAll() {
        setup()
        children.forEach {
            it.child.setupAll()
            it.refresh()
        }
    }

    fun show() {
        setup()
        node.show()
        children.forEach {
            it.child.show()
            it.show()
        }
        parent?.show()
    }

    fun hide() {
        children.forEach {
            it.child.hide()
            it.hide()
        }
        parent?.hide()
        node.hide()
    }

    fun close() {
        node.removeFromParent()
        children.forEach {
            it.child.close()
            it.close()
        }
        parent?.close()
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

    fun updateSuggestions() {
        if (suggestions != null) {
            if (suggestions?.size ?: 0 < 1) return
            suggestions?.get(0)?.child?.model?.x = model.x
            suggestions?.get(0)?.child?.model?.y = model.y - 6
            suggestions?.get(0)?.child?.setup()
            if (suggestions?.size ?: 0 < 2) return
            suggestions?.get(1)?.child?.model?.x = model.x + model.width + 2
            suggestions?.get(1)?.child?.model?.y = model.y
            suggestions?.get(1)?.child?.setup()
            if (suggestions?.size ?: 0 < 3) return
            suggestions?.get(2)?.child?.model?.x = model.x - 6 - 2
            suggestions?.get(2)?.child?.model?.y = model.y
            suggestions?.get(2)?.child?.setup()
            return
        }

        try {
            val terms = BasicSuggestionProvider().getSuggestions(model.key)

            val nodes = mutableListOf<MindMapNode>()
            if (terms.size >= 1) nodes += MindMapNode(terms[0], model.x, model.y - 6, 6, 4)
            if (terms.size >= 2) nodes += MindMapNode(terms[1], model.x + model.width + 2, model.y, 6, 4)
            if (terms.size >= 3) nodes += MindMapNode(terms[2], model.x - 6 - 2, model.y, 6, 4)

            suggestions = nodes.map { node -> NodeConnection(this, NodeViewModel(main, node)) }.toMutableList()
            suggestions?.map { it.child.setup(); it.refresh() }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    init {
        DragResizeMod.makeResizable(node, resizeListener)
        main.buttonPane += node
        show()

        node.onHover { isHover ->
            if (model.isLeaf()) {
                if (isHover) {
                    if (suggestions == null) updateSuggestions()

                    suggestions?.forEach {
                        it.child.show()
                        it.show()
                    }
                } else {
                    suggestions?.forEach {
                        it.child.hide()
                        it.hide()
                    }
                }
            }
        }
    }
}
