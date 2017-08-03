package wikimap.models

/**
 * Created by Basim on 01/08/2017.
 */
data class MindMapModel(
    var root: MindMapNode
)

/**
 * A single element of a MindMap
 */
data class MindMapNode(
    var key: String,
    var x: Int, var y: Int,
    var width: Int, var height: Int,
    val children: MutableList<MindMapNode> = mutableListOf()
)
