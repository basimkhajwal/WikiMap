package wikimap.view

import javafx.scene.canvas.Canvas
import javafx.scene.canvas.GraphicsContext
import javafx.scene.layout.Pane
import javafx.scene.paint.*
import tornadofx.*
import wikimap.models.MindMapModel
import wikimap.models.MindMapNode

/**
 * The main view of the application
 */
class MainView : View("WikiMap") {

    val onChange = ChangeEvent()

    val canvas = Canvas(800.0,700.0)
    val graphics: GraphicsContext = canvas.graphicsContext2D

    val gridSpacing: Int = 20

    /* Fixed for now, but can be changed later */
    val mindMap = MindMapModel(
        MindMapNode("Machine Learning", -3, -2, 6, 4,
            mutableListOf(
                MindMapNode("Deep Learning", -9, 1, 5, 3),
                MindMapNode("Artificial Intelligence", -8, -6, 5, 3),
                MindMapNode("Neural Networks", 6, 4, 5, 4)
            )
        )
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
        onChange.fireChange()
    }

    init {
        currentWindow?.widthProperty()?.onChange { refresh() }
        currentWindow?.heightProperty()?.onChange { refresh() }
    }
}
