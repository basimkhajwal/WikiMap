package wikimap.viewmodels

import javafx.beans.property.BooleanProperty
import tornadofx.*
import wikimap.models.MindMapNode

/**
 * Created by Basim on 12/08/2017.
 */
class MindMapNodeModel : ItemViewModel<MindMapNode>() {
    val key = bind(MindMapNode::keyProperty)
    val x = bind(MindMapNode::xProperty)
    val y = bind(MindMapNode::yProperty)
    val width = bind(MindMapNode::widthProperty)
    val height = bind(MindMapNode::heightProperty)
    val children = bind(MindMapNode::children)

    val isSuggestion = bind(MindMapNode::suggestionProperty) as BooleanProperty
    val isSelected = bind(MindMapNode::selectedProperty) as BooleanProperty
    val isEditing = bind(MindMapNode::editingProperty) as BooleanProperty
}
