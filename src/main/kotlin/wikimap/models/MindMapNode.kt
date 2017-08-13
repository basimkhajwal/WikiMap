package wikimap.models

import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleStringProperty
import tornadofx.*

class MindMapNode(
    key: String, x: Int, y: Int, width: Int, height: Int,
    children: MutableList<MindMapNode> = mutableListOf()
) {

    val keyProperty = SimpleStringProperty(key)
    var key by keyProperty

    val xProperty = SimpleIntegerProperty(x)
    var x by xProperty

    val yProperty = SimpleIntegerProperty(y)
    var y by yProperty

    val widthProperty = SimpleIntegerProperty(width)
    var width by widthProperty

    val heightProperty = SimpleIntegerProperty(height)
    var height by heightProperty

    val children = children.observable()

    fun serialize(depth: Int = 0): String {
        return join(getDepthKey(depth),
            key, x.toString(), y.toString(), width.toString(), height.toString(),
            *children.map { it.serialize(depth + 1) }.toTypedArray()
        )
    }

    fun copy() = MindMapNode(key, x, y, width, height, children)

    companion object {

        private fun join(s: String, vararg xs: String) = xs.joinToString(s)

        private fun getDepthKey(depth: Int) = "\n{$$$depth$$}\n"

        fun deserialize(str: String, depth: Int = 0): MindMapNode {
            val tokens = str.split(getDepthKey(depth))

            val key = tokens[0]
            val x = tokens[1].toInt()
            val y = tokens[2].toInt()
            val width = tokens[3].toInt()
            val height = tokens[4].toInt()

            val children =
                tokens.drop(5).map { deserialize(it, depth+1) }.toMutableList()

            return MindMapNode(key, x, y, width, height, children)
        }
    }

}
