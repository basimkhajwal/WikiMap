package wikimap.models

import javafx.beans.property.*
import javafx.scene.paint.Color
import tornadofx.*

class MindMapNode(
    key: String, x: Int, y: Int, width: Int, height: Int,
    children: MutableList<MindMapNode> = mutableListOf(),
    backgroundColour: Color = Color(Math.random(), Math.random(), Math.random(), 0.7),
    textColour: Color = Color.WHITE,
    fontSize: Int = 12
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

    val textColourProperty = SimpleObjectProperty<Color>(textColour)
    var textColour by textColourProperty

    val fontSizeProperty = SimpleIntegerProperty(fontSize)
    var fontSize by fontSizeProperty

    val backgroundColourProperty = SimpleObjectProperty<Color>(backgroundColour)
    var backgroundColour by backgroundColourProperty

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
