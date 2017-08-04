package wikimap.view

import javafx.scene.canvas.Canvas
import javafx.scene.layout.Pane
import javafx.scene.paint.Color
import tornadofx.*

/**
 * Created by Basim on 04/08/2017.
 */
class GridView(main: MainView) : Pane() {
    private val canvas = Canvas(800.0, 600.0)
    private val g2d = canvas.graphicsContext2D
    private val spacing = main.gridSpacing

    fun refresh() {

        val bounds = parent.layoutBounds
        width = bounds.width
        height = bounds.height

        canvas.resizeRelocate(0.0, 0.0, width, height)
        canvas.width = width
        canvas.height = height

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
        return Pair(x*spacing + width/2, y*spacing + height/2)
    }

    fun toGridCoords(x: Double, y: Double): Pair<Double, Double> {
        return Pair((x - width/2) / spacing, (y - height/2) / spacing)
    }

    init {
        this += canvas
        main.onChange += this::refresh
    }
}