package wikimap.utils

import javafx.beans.property.SimpleIntegerProperty
import javafx.scene.control.TextField
import javafx.scene.input.KeyEvent
import tornadofx.*

/**
 * Created by Basim on 07/08/2017.
 */
class NumericTextField : TextField(""){

    val valueProperty = SimpleIntegerProperty()
    var value by valueProperty

    private var inSync = false

    init {

        promptText = "..."

        addEventFilter(KeyEvent.KEY_TYPED, { inSync = false })

        textProperty().addListener { _, _, newValue ->
            if (!inSync && isValid(newValue)) {
                inSync = true
                value = newValue.toIntOrNull() ?: 0
            }
        }

        valueProperty.onChange {
            if (!inSync || value != text.toIntOrNull() ?: 0) {
                inSync = true
                text = value.toString()
            }
        }
    }

    private fun isValid(str: String): Boolean {
        return str.isEmpty() || str.toIntOrNull() != null
    }
}