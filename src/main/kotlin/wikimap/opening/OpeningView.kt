package wikimap.opening

import javafx.event.EventHandler
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.Parent
import javafx.scene.control.SelectionMode
import javafx.scene.layout.*
import javafx.scene.paint.Color
import javafx.scene.paint.Paint
import javafx.scene.shape.Rectangle
import javafx.scene.text.FontWeight
import javafx.scene.text.TextAlignment
import tornadofx.*
import wikimap.models.MindMap
import wikimap.models.MindMapNode
import wikimap.utils.UserSettings
import wikimap.views.MainView
import wikimap.views.MenuBarView
import java.io.File
import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.prefs.Preferences

/**
 * Created by Basim on 09/09/2017.
 */
class OpeningView : View("WikiMap") {

    private val recentFiles = UserSettings.getRecentFiles().take(3)

    private val selectedBorder = Border(BorderStroke(
        Color.BLUE, BorderStrokeStyle.SOLID,
        CornerRadii.EMPTY, BorderWidths(1.0)
    ))

    private val normalBorder = Border(BorderStroke(
        Color.TRANSPARENT, BorderStrokeStyle.SOLID,
        CornerRadii.EMPTY, BorderWidths(1.0)
    ))

    private fun makeBox(name: String, action: () -> Unit): StackPane = StackPane().apply {
        rectangle(width = 150, height = 200) {
            style {
                fill = Color.WHITE
            }

            borderProperty().bind(
                hoverProperty().objectBinding {
                    if (it ?: false) selectedBorder else normalBorder
                }
            )
        }

        label(name) {
            style {
                fontSize = 15.px
                alignment = Pos.CENTER
                textAlignment = TextAlignment.CENTER
                wrapText = true
            }
        }

        onMouseClicked = EventHandler { action() }
    }

    override val root: Parent = vbox {

        hbox {

            padding = tornadofx.insets(50)

            style {
                backgroundColor = multi(Color.LIGHTGRAY)
            }

            this += makeBox("New\nDocument") {

                val initModel = MindMap(MindMapNode("...", -3, -2, 6, 4))

                find(MainView::class).loadModel(initModel)
                replaceWith(MainView::class)
            }

            stackpane {
                label("Wiki\nMap") {
                    style {
                        fontSize = 60.px
                        fontWeight = FontWeight.BOLD
                    }
                }

                HBox.setHgrow(this, Priority.ALWAYS)
            }
        }

        label("Recent") {
            padding = tornadofx.insets(20, 0, 0, 20)
            style {
                fontSize = 30.px
                fontWeight = FontWeight.SEMI_BOLD
            }
        }

        if (recentFiles.isEmpty()) {

            stackpane {

                label("No Recent Files") {
                    style {
                        fontSize = 20.px
                        fontWeight = FontWeight.SEMI_BOLD
                    }
                }
                paddingTop = 100
                paddingBottom = 50
                alignment = Pos.CENTER
            }
        } else {

            flowpane {

                padding = tornadofx.insets(50)

                children += recentFiles.map { filePath ->
                    val fileName = filePath.substring(filePath.lastIndexOf(File.separator) + 1)

                    makeBox(fileName) {
                        val model = MindMap.deserialize(
                            Files.readAllBytes(Paths.get(filePath))
                                .toString(Charset.defaultCharset())
                        )
                        find(MainView::class).loadModel(model)
                        find(MenuBarView::class).file = File(filePath)
                        replaceWith(MainView::class)
                    }
                }
            }

        }
    }

    init {
        primaryStage.isResizable = false
        primaryStage.width = 800.0
        primaryStage.height = 700.0
    }
}