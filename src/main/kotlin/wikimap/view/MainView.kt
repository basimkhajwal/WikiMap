package wikimap.view

import com.sun.prism.Image
import javafx.geometry.Bounds
import javafx.geometry.Pos
import javafx.geometry.Rectangle2D
import javafx.scene.canvas.Canvas
import javafx.scene.canvas.GraphicsContext
import javafx.scene.control.Label
import javafx.scene.layout.Pane
import javafx.scene.layout.StackPane
import javafx.scene.layout.VBox
import javafx.scene.paint.*
import javafx.scene.shape.Rectangle
import javafx.scene.text.FontWeight
import javafx.scene.text.TextAlignment
import org.w3c.dom.css.Rect
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

    private fun fromGridCoords(x: Double, y: Double): Pair<Double, Double> {
        return Pair(x*gridSpacing + canvas.width/2, y*gridSpacing + canvas.height/2)
    }

    private fun toGridCoords(x: Double, y: Double): Pair<Double, Double> {
        return Pair((x - canvas.width/2) / gridSpacing, (y - canvas.height/2) / gridSpacing)
    }

    /* Fixed for now, but can be changed later */
    val mindMap = MindMapModel(
        MindMapNode("Machine\n Learning", -3, -2, 6, 4, mutableListOf(
            MindMapNode("Deep\nLearning", -9, 2, 5, 3, mutableListOf()),
            MindMapNode("Artificial\nIntelligence", -8, -6, 5, 3, mutableListOf()),
            MindMapNode("Neural\nNetworks", 6, 4, 5, 4, mutableListOf())
        ))
    )

    val buttonPane = Pane()

    override val root = stackpane {
        this += canvas
        this += buttonPane
    }

    class NodeViewModel(main: MainView, model: MindMapNode){
        private val rect: Rectangle = {
            val (x, y) = main.fromGridCoords(model.x.toDouble(), model.y.toDouble())
            val w = main.gridSpacing * model.width.toDouble()
            val h = main.gridSpacing * model.height.toDouble()
            Rectangle(x, y, w, h).apply {
                arcWidth = main.gridSpacing.toDouble()
                arcHeight = main.gridSpacing.toDouble()
                fill = Color(Math.random(), Math.random(), Math.random(), 0.7)
            }
        }()
        private val label: Label = Label(model.key).apply {
            textFill = Color.WHITE
            alignment = Pos.CENTER
            textAlignment = TextAlignment.CENTER
            style {
                fontWeight = FontWeight.BOLD
            }
        }
        val children: MutableList<NodeViewModel> =
            model.children.map{ NodeViewModel(main, it) }.toMutableList()

        init {
            main.buttonPane.apply {
                add(rect)
                stackpane {
                    relocate(rect.x, rect.y)
                    setPrefSize(rect.width, rect.height)
                    this += label
                }
            }
        }

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

    init {
        drawGrid()
        NodeViewModel(this, mindMap.root)
        this.currentWindow?.widthProperty()?.onChange {
            canvas.width = it
            drawGrid()
        }
        this.currentWindow?.heightProperty()?.onChange {
            canvas.height = it
            drawGrid()
        }
        var test = wikimap.app.WebCrawler("en.wikipedia.org")
    }

}