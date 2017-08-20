package wikimap.models

import javafx.beans.property.SimpleObjectProperty
import tornadofx.*

/**
 * Created by Basim on 01/08/2017.
 */
class MindMap(root: MindMapNode) {

    val rootProperty = SimpleObjectProperty(root)
    var root by rootProperty

    fun serialize(): String {
        return root.serialize()
    }

    companion object {

        fun deserialize(str: String): MindMap {
            return MindMap(MindMapNode.deserialize(str))
        }
    }
}

