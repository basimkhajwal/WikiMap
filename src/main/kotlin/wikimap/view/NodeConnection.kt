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
            line.startX = parent.node.layoutX + parent.node.prefWidth / 2
            line.startY = parent.node.layoutY + parent.node.prefHeight / 2
            line.endX = child.node.layoutX + child.node.prefWidth / 2
            line.endY = child.node.layoutY + child.node.prefHeight / 2

            val total = Rectangle(clipPane.layoutBounds.width, clipPane.layoutBounds.height)
            clipPane.clip = Shape.subtract(total, Shape.union(parent.rect, child.rect))
        }

        init {
            child.parent = this
            parent.main.buttonPane += clipPane
            refresh()
        }
    }
