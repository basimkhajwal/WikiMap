package wikimap.view;

import javafx.scene.layout.Pane
import javafx.scene.shape.Line
import javafx.scene.shape.Rectangle
import javafx.scene.shape.Shape
import tornadofx.*

class NodeConnection(val parent: NodeViewModel, val child: NodeViewModel) {
        val line = Line(0.0,0.0,0.0,0.0)
        val clipPane = Pane(line)

        fun refresh() {
            line.startX = parent.getCenterX()
            line.startY = parent.getCenterY()
            line.endX = child.getCenterX()
            line.endY = child.getCenterY()

            val total = Rectangle(clipPane.layoutBounds.width, clipPane.layoutBounds.height)
            clipPane.clip = Shape.subtract(total, Shape.union(parent.rect, child.rect))
            clipPane.toBack()
        }

        init {
            refresh()
            parent.main.buttonPane += clipPane

            parent.onChange += this::refresh
            child.onChange += this::refresh
            clipPane.layoutBoundsProperty().onChange { refresh() }
        }
    }
