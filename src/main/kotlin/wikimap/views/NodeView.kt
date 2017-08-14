package wikimap.views

import javafx.beans.binding.Bindings
import javafx.event.EventHandler
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.control.*
import javafx.scene.layout.*
import javafx.scene.paint.Color
import javafx.scene.shape.Rectangle
import javafx.scene.text.FontWeight
import javafx.scene.text.TextAlignment
import tornadofx.*
import wikimap.controllers.MindMapController
import wikimap.utils.DragResizeMod
import wikimap.utils.UpdateEvent
import wikimap.viewmodels.MindMapNodeModel

class NodeView : Fragment() {

    val controller: MindMapController by inject()
    val gridView: GridView by inject()

    val model: MindMapNodeModel by param()

    var rect: Rectangle by singleAssign()
    var label: Label by singleAssign()
    var textArea: TextArea by singleAssign()

    val selectedBorder = Border(BorderStroke(
        Color.BLUE, BorderStrokeStyle.SOLID,
        CornerRadii.EMPTY, BorderWidths(1.0)
    ))

    private val resizeListener = object : DragResizeMod.OnDragResizeEventListener {
        override fun onResize(n: Node?, x: Double, y: Double, h: Double, w: Double) {
            val (nx, ny) = gridView.toGridCoords(x, y)
            val cx = Math.round(nx).toInt()
            val cy = Math.round(ny).toInt()

            if (nx != model.x.value) {
                model.width.value = (model.x + model.width) - cx
            } else {
                model.width.value = Math.round(w / controller.gridSpacing).toInt()
            }

            if (ny != model.y.toDouble()) {
                model.height.value = (model.y + model.height) - cy
            } else {
                model.height.value = Math.round(h / controller.gridSpacing).toInt()
            }

            model.x.value = cx
            model.y.value = cy

            if (model.width.value > 3) model.width.value = 3
            if (model.height.value > 3) model.height.value = 3

            refresh()
        }

        override fun onDrag(n: Node?, x: Double, y: Double, h: Double, w: Double) {
            val (nx, ny) = gridView.toGridCoords(x, y)
            model.x.value = Math.round(nx).toInt()
            model.y.value = Math.round(ny).toInt()
            refresh()
        }
    }

    /*
    constructor(main: MainView, model: MindMapNode, suggestionParent: NodeView) {

        val totalOffsetX = model.x - suggestionParent.model.x
        val totalOffsetY = model.y - suggestionParent.model.y

        val offsetX = totalOffsetX +
                if (totalOffsetX < 0) model.width else -suggestionParent.model.width

        val offsetY = totalOffsetY +
                if (totalOffsetY < 0) model.height else -suggestionParent.model.height

        rect.fill = Color(0.0, 0.0, 0.0, 0.2)

        onHover {
            if (it) {
                rect.fill = Color(0.2, 0.2, 0.2, 0.2)
            } else {
                rect.fill = Color(0.0, 0.0, 0.0, 0.2)
            }
        }

        onDoubleClick { main.includeSuggestion(suggestionParent, this) }

        suggestionParent.onChange += {
            model.x = suggestionParent.model.x + offsetX
            model.y = suggestionParent.model.y + offsetY

            if (offsetX < 0) model.x -= model.width
            else             model.x += suggestionParent.model.width

            if (offsetY < 0) model.y -= model.height
            else             model.y += suggestionParent.model.height

            refresh()
        }
    }*/

    override val root = stackpane {

        rectangle {
            widthProperty().bind(controller.gridSpacingProperty.multiply(model.width))
            heightProperty().bind(controller.gridSpacingProperty.multiply(model.height))

            arcWidthProperty().bind(controller.gridSpacingProperty)
            arcHeightProperty().bind(controller.gridSpacingProperty)

            fill = Color(Math.random(), Math.random(), Math.random(), 0.7)
            rect = this
        }

        label(model.key) {
            style {
                textFill = Color.WHITE
                fontWeight = FontWeight.BOLD
                alignment = Pos.CENTER
                textAlignment = TextAlignment.CENTER
                wrapText = true
            }

            onDoubleClick {
                model.isEditing.value = true
            }

            visibleWhen { model.isEditing.not() }
            label = this
        }

        textarea(model.key) {
            style {
                textFill = Color.WHITE
                fontWeight = FontWeight.BOLD
                wrapText = true
                backgroundColor = multi(Color.TRANSPARENT)
            }
            paddingTop = 10
            paddingBottom = 10

            visibleWhen { model.isEditing }

            whenVisible { requestFocus() }

            focusedProperty().onChange {
                if (!it) {
                    model.isEditing.value = false
                }
            }

            val scrollPane = children[0] as ScrollPane
            scrollPane.vbarPolicy = ScrollPane.ScrollBarPolicy.NEVER

            getAllChildren(this).forEach {
                it.style {
                    backgroundColor = multi(Color(0.0,0.0,0.0,0.05))
                }

                // Prevent blurry text
                it.isCache = false
            }

            textArea = this
        }

        prefWidthProperty().bind(rect.widthProperty())
        prefHeightProperty().bind(rect.heightProperty())

        borderProperty().bind(
            model.isSelected.objectBinding {
                if (it ?: false) selectedBorder else Border.EMPTY
            }
        )

        if (!isSuggestion) {
            onMouseClicked = EventHandler {
                main.selectNodes(this)
            }
        }

        DragResizeMod.makeResizable(this, resizeListener)
    }

    init {
        subscribe<UpdateEvent> { refresh() }
        refresh()
    }

    fun getCenterX(): Double = gridView.fromGridCoords(model.x + model.width/2.0, 0.0).first
    fun getCenterY(): Double = gridView.fromGridCoords(0.0, model.y + model.height/2.0).second

    private fun refresh() {
        val (x, y) = gridView.fromGridCoords(model.x.toDouble(), model.y.toDouble())

        root.resizeRelocate(x, y, rect.width, rect.height)
        root.toFront()
    }

    private fun getAllChildren(node: Node): List<Node> {
        val children = node.getChildList()?.toList() ?: listOf()
        return children + children.flatMap { getAllChildren(it) }.toList()
    }
}
