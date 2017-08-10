package wikimap.models

import java.io.Serializable
import java.util.*

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
) {

    private fun quoteJoin(vararg xs: String) = xs.map { "\"" + it + "\"" } .joinToString("\n")

    fun serialize(): String {
        return quoteJoin(
            key, x.toString(), y.toString(), width.toString(), height.toString(),
            *children.map { it.serialize() }.toTypedArray()
        )
    }

    companion object {

        fun deserialize(str: String): MindMapNode {
            val tokenizer = StringTokenizer(str)
            val key = tokenizer.nextToken()
            val x = tokenizer.nextToken().toInt()
            val y = tokenizer.nextToken().toInt()
            val width = tokenizer.nextToken().toInt()
            val height = tokenizer.nextToken().toInt()

            val children = mutableListOf<MindMapNode>()
            while (tokenizer.hasMoreTokens()) {
                children += deserialize(tokenizer.nextToken())
            }

            return MindMapNode(key, x, y, width, height, children)
        }
    }

}
