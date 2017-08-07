package wikimap.utils

import javafx.event.EventHandler
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent

/**
 * Created by Basim on 06/08/2017.
 */
class KeyboardHandler : EventHandler<KeyEvent> {

    private val keyMap: MutableMap<KeyCode, Boolean> = mutableMapOf()

    fun isKeyDown(key: KeyCode): Boolean = keyMap[key] ?: false

    override fun handle(event: KeyEvent?) {
        if (event == null) return

        if (event.eventType == KeyEvent.KEY_PRESSED) {
            keyMap[event.code] = true

        } else if (event.eventType == KeyEvent.KEY_RELEASED) {
            keyMap[event.code] = false
        }
    }


}