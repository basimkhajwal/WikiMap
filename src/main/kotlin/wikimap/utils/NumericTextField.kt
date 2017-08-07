package wikimap.utils

import javafx.scene.control.TextField
import javafx.scene.input.KeyEvent

/**
 * Created by Basim on 07/08/2017.
 */
class NumericTextField(val forceInt: Boolean = true) : TextField(if (forceInt) "0" else "0.0"){

    init {
        textProperty().addListener { _, oldValue, newValue ->
            if (newValue.isEmpty()) {
                text = if (forceInt) "0" else "0.0"
            } else if (!isValid(newValue)) {
                text = oldValue
            }
        }
    }

    private fun isValid(str: String): Boolean {
        if (forceInt) return str.toIntOrNull() != null
        return str.toDoubleOrNull() != null
    }
}