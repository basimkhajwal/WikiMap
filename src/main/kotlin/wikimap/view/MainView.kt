package wikimap.view

import javafx.geometry.Pos
import javafx.geometry.Rectangle2D
import javafx.scene.canvas.Canvas
import javafx.scene.canvas.GraphicsContext
import javafx.scene.layout.VBox
import javafx.scene.paint.*
import javafx.scene.shape.Rectangle
import javafx.scene.text.TextAlignment
import tornadofx.*
import wikimap.models.MindMapModel
import wikimap.models.MindMapNode

/**
 * The main view of the application
 */
class MainView : View("WikiMap") {

    val canvas = Canvas(800.0, 600.0)
    val graphics: GraphicsContext = canvas.graphicsContext2D

    val gridSpacing: Int = 20

    /* Fixed for now, but can be changed later */
    val mindMap = MindMapModel(
        MindMapNode("Test", -3, -2, 6, 4, mutableListOf())
    )

    override val root = stackpane { this += canvas }

    private fun drawGrid() {

        val hw = canvas.width/2
        val hh = canvas.height/2
        val xRange = Math.floor(canvas.width / gridSpacing ).toInt() + 1
        val yRange = Math.floor(canvas.height / gridSpacing).toInt() + 1

        graphics.clearRect(0.0, 0.0, canvas.width, canvas.height)
        graphics.stroke = Color.LIGHTGRAY
        graphics.lineWidth = 0.5

        for (x in (-xRange/2)..(xRange/2)) {
            graphics.strokeLine(x*gridSpacing + hw, 0.0, x*gridSpacing + hw, canvas.height)
        }
        for (y in (-yRange/2)..(yRange/2)) {
            graphics.strokeLine(0.0, y*gridSpacing + hh, canvas.width, y*gridSpacing + hh)
        }
    }

    init {
        drawGrid()
        this.currentWindow?.widthProperty()?.onChange {
            canvas.width = it
            drawGrid()
        }
        this.currentWindow?.heightProperty()?.onChange {
            canvas.height = it
            drawGrid()
        }
    }

}