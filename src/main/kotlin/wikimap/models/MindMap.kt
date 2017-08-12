package wikimap.models

/**
 * Created by Basim on 01/08/2017.
 */
data class MindMap(
    var root: MindMapNode
) {

    fun serialize(): String {
        return root.serialize()
    }

    companion object {

        fun deserialize(str: String): MindMap {
            return MindMap(MindMapNode.deserialize(str))
        }
    }
}

