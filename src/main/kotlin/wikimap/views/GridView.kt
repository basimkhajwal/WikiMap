package wikimap.views

import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.scene.canvas.Canvas
import javafx.scene.layout.Pane
import javafx.scene.paint.Color
import tornadofx.*

/**
 * Created by Basim on 04/08/2017.
 */
class GridView : View() {

    val gridCenterProperty = SimpleObjectProperty(Pair(0.5, 0.5))
    var gridCenter by gridCenterProperty

    val spacingProperty = SimpleIntegerProperty(20)
    var spacing by spacingProperty

    private val canvas = Canvas(800.0, 600.0)

    override val root = Pane(canvas)

    init {
        spacingProperty.onChange { draw() }
        gridCenterProperty.onChange { draw() }
        root.layoutBoundsProperty().onChange { draw() }
    }

    private fun draw() {
        val width = root.layoutBounds.width
        val height = root.layoutBounds.height
        canvas.width = width
        canvas.height = height

        val g2d = canvas.graphicsContext2D

        g2d.clearRect(0.0, 0.0, width, height)
        g2d.stroke = Color.LIGHTGRAY
        g2d.lineWidth = 0.5

        val px = width * gridCenter.first
        val py = height * gridCenter.second

        val xRange = (-px/spacing).toInt()..((width-px).toInt() / spacing)
        val yRange = (-py/spacing).toInt()..((height-py).toInt() / spacing)

        for (x in xRange) g2d.strokeLine(x*spacing + px, 0.0, x*spacing + px, height)
        for (y in yRange) g2d.strokeLine(0.0, y*spacing + py, width, y*spacing + py)
    }

    fun fromGridCoords(x: Double, y: Double): Pair<Double, Double> {
        return Pair(x*spacing + gridCenter.first*canvas.width, y*spacing + gridCenter.second*canvas.height)
    }

    fun toGridCoords(x: Double, y: Double): Pair<Double, Double> {
        return Pair((x - gridCenter.first*canvas.width) / spacing, (y - gridCenter.second*canvas.height) / spacing)
    }
}