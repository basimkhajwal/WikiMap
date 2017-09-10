package wikimap.opening

import javafx.scene.Parent
import tornadofx.*
import java.util.prefs.Preferences

/**
 * Created by Basim on 09/09/2017.
 */
class OpeningView : View("WikiMap") {

    override val root: Parent = vbox {

        hbox {

        }

        label("Recent")

        datagrid(listOf<String>()) {

        }
    }

}