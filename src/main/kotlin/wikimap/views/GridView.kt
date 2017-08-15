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

    val centerProperty = SimpleObjectProperty(Pair(0.0, 0.0))
    var center by centerProperty

    val spacingProperty = SimpleIntegerProperty(20)
    var spacing by spacingProperty

    private val canvas = Canvas(800.0, 600.0)

    override val root = Pane(canvas)

    init {

        centerProperty.bind(
            root.layoutBoundsProperty().objectBinding { bounds ->
                Pair((bounds?.width ?: 0.0) / 2, (bounds?.height ?: 0.0) / 2)
            }
        )

        spacingProperty.onChange { draw() }
        centerProperty.onChange { draw() }
    }

    private fun draw() {

        val width = root.layoutBounds.width
        val height = root.layoutBounds.height
        canvas.width = width
        canvas.height = height

        val g2d = canvas.graphicsContext2D
        val hw = width/2
        val hh = height/2
        val xRange = Math.floor(width / spacing).toInt() + 1
        val yRange = Math.floor(height / spacing).toInt() + 1

        g2d.clearRect(0.0, 0.0, width, height)
        g2d.stroke = Color.LIGHTGRAY
        g2d.lineWidth = 0.5

        for (x in (-xRange/2)..(xRange/2)) {
            g2d.strokeLine(x*spacing + hw, 0.0, x*spacing + hw, height)
        }
        for (y in (-yRange/2)..(yRange/2)) {
            g2d.strokeLine(0.0, y*spacing + hh, width, y*spacing + hh)
        }
    }

    fun fromGridCoords(x: Double, y: Double): Pair<Double, Double> {
        return Pair(x*spacing + center.first, y*spacing + center.second)
    }

    fun toGridCoords(x: Double, y: Double): Pair<Double, Double> {
        return Pair((x - center.first) / spacing, (y - center.second) / spacing)
    }
}