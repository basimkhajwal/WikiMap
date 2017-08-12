package wikimap.view

import javafx.scene.canvas.Canvas
import javafx.scene.layout.Pane
import javafx.scene.paint.Color
import tornadofx.*
import wikimap.utils.UpdateEvent

/**
 * Created by Basim on 04/08/2017.
 */
class GridView : View() {

    val canvas = Canvas(800.0, 600.0)
    val spacing = 20

    override val root = Pane(canvas)

    init {
        subscribe<UpdateEvent> {
            refresh()
        }
    }

    private fun refresh() {

        val width = root.parent.layoutBounds.width
        val height = root.parent.layoutBounds.height

        root.resize(width, height)
        canvas.resize(width, height)
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
        return Pair(x*spacing + canvas.width/2, y*spacing+ canvas.height/2)
    }

    fun toGridCoords(x: Double, y: Double): Pair<Double, Double> {
        return Pair((x - canvas.width/2) / spacing, (y - canvas.height/2) / spacing)
    }
}