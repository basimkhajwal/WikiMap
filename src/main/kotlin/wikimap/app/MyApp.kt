package wikimap.app

import javafx.application.Application
import tornadofx.App
import wikimap.views.MainView

class MyApp: App(MainView::class)

fun main(args: Array<String>) {
    Application.launch(MyApp::class.java, *args)
}