package wikimap.app

import javafx.application.Application
import tornadofx.App
import wikimap.opening.OpeningView

class MyApp: App(OpeningView::class)

fun main(args: Array<String>) {
    Application.launch(MyApp::class.java, *args)
}