package wikimap.models

data class MindMapNode(
    var key: String,
    var x: Int, var y: Int,
    var width: Int, var height: Int,
    val children: MutableList<MindMapNode> = mutableListOf()
) {

    fun serialize(depth: Int = 0): String {
        return join(getDepthKey(depth),
            key, x.toString(), y.toString(), width.toString(), height.toString(),
            *children.map { it.serialize(depth + 1) }.toTypedArray()
        )
    }

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
