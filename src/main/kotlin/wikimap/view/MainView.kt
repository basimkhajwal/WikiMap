package wikimap.view

import com.sun.javafx.scene.SceneHelper
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.canvas.Canvas
import javafx.scene.canvas.GraphicsContext
import javafx.scene.control.Label
import javafx.scene.layout.Pane
import javafx.scene.layout.StackPane
import javafx.scene.paint.*
import javafx.scene.shape.Rectangle
import javafx.scene.text.FontWeight
import javafx.scene.text.TextAlignment
import javafx.stage.Screen
import tornadofx.*
import wikimap.models.MindMapModel
import wikimap.models.MindMapNode

/**
 * The main view of the application
 */
class MainView : View("WikiMap") {

    val canvas = Canvas(800.0,700.0)
    val graphics: GraphicsContext = canvas.graphicsContext2D

    val gridSpacing: Int = 20

    /* Fixed for now, but can be changed later */
    val mindMap = MindMapModel(
        MindMapNode("Machine\n Learning", -3, -2, 6, 4, mutableListOf(
            MindMapNode("Deep\nLearning", -9, 1, 5, 3, mutableListOf()),
            MindMapNode("Artificial\nIntelligence", -8, -6, 5, 3, mutableListOf()),
            MindMapNode("Neural\nNetworks", 6, 4, 5, 4, mutableListOf())
        ))
    )
    val buttonPane = Pane()
    val viewModel = NodeViewModel(this, mindMap.root)

    override val root = stackpane {
        this += canvas
        this += buttonPane
    }

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

    private fun refresh() {
        canvas.width = root.scene.width
        canvas.height = root.scene.height
        drawGrid()
        viewModel.setupAll()
    }

    init {
        root.scene.widthProperty().onChange { refresh() }
        root.scene.heightProperty().onChange { refresh() }
    }
}
