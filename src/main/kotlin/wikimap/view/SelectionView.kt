package wikimap.view

import javafx.event.EventHandler
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.control.Label
import javafx.scene.control.TitledPane
import javafx.scene.layout.Pane
import javafx.scene.layout.StackPane
import javafx.scene.paint.Color
import tornadofx.*

/**
 * Created by Basim on 04/08/2017.
 */
class SelectionView : Pane() {

    val root = TitledPane("Edit Node", Label("Test"))

    init {
        root.style {
            fillWidth = true
            fillHeight = true
        }
        root.isCollapsible = false
        this += root
    }
}