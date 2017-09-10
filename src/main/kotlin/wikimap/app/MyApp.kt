package wikimap.app

import javafx.stage.Stage
import tornadofx.App
import wikimap.views.MainView

class MyApp: App(MainView::class)

fun main(args: Array<String>) {
    MyApp().start(Stage())
}