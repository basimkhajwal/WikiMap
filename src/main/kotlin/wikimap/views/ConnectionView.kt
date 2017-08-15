package wikimap.views

import javafx.scene.layout.Pane
import javafx.scene.shape.Line
import javafx.scene.shape.Rectangle
import javafx.scene.shape.Shape
import tornadofx.*
import wikimap.controllers.MindMapController
import wikimap.models.MindMapNode

class ConnectionView : Fragment() {

    private val line = Line(0.0,0.0,0.0,0.0)

    val parent: NodeView by param()
    val child: NodeView by param()

    override val root = Pane(line)

    init {
        root.layoutBoundsProperty().onChange { refresh() }

        parent.root.layoutBoundsProperty().onChange { refresh() }
        child.root.layoutBoundsProperty().onChange { refresh() }

        refresh()
    }

    private fun refresh() {
        val (px, py) = parent.getCenter()
        val (cx, cy) = child.getCenter()
        line.startX = px
        line.startY = py
        line.endX = cx
        line.endY = cy

        val total = Rectangle(root.layoutBounds.width, root.layoutBounds.height)
        root.clip = Shape.subtract(total, Shape.union(
                Rectangle(parent.root.layoutX, parent.root.layoutY, parent.rect.width, parent.rect.height),
                Rectangle(child.root.layoutX, child.root.layoutY, child.rect.width, child.rect.height)
        ))
        root.toBack()
    }
}
