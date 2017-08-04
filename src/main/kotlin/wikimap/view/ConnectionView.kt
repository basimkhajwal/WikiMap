package wikimap.view;

import javafx.scene.layout.Pane
import javafx.scene.shape.Line
import javafx.scene.shape.Rectangle
import javafx.scene.shape.Shape
import tornadofx.*

class ConnectionView(val parent: NodeView, val child: NodeView): Pane() {

        private val line = Line(0.0,0.0,0.0,0.0)

        private fun refresh() {
            line.startX = parent.getCenterX()
            line.startY = parent.getCenterY()
            line.endX = child.getCenterX()
            line.endY = child.getCenterY()

            val total = Rectangle(layoutBounds.width, layoutBounds.height)
            clip = Shape.subtract(total, Shape.union(parent.rect, child.rect))
            toBack()
        }

        init {
            layoutBoundsProperty().onChange { refresh() }

            refresh()
            parent.onChange += this::refresh
            child.onChange += this::refresh
        }
    }
