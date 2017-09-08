package wikimap.views

/**
 * Created by Basim on 03/08/2017.
 */
class ChangeEvent {
    private val listeners = mutableListOf< () -> Unit >()

    operator fun plusAssign(listener: () -> Unit) {
        listeners += listener
    }

    operator fun minusAssign(listener: () -> Unit) {
        listeners -= listener
    }

    fun fireChange() {
        listeners.forEach { it() }
    }

}