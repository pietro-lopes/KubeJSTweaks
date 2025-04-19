package dev.uncandango.kubejstweaks.kubejs.schema;

import net.neoforged.bus.api.Event;

import java.util.ArrayList;
import java.util.List;

public class CodecNode {
    public CodecNode parent;
    public List<CodecNode> childs = new ArrayList<>();
    public Event value;

    public CodecNode(Event value) {
        this.value = value;
    }

    public static List<CodecNode> flattenNodes(List<CodecNode> nodes) {
        List<CodecNode> result = new ArrayList<>();
        nodes.forEach(node -> {
            result.add(node);
            result.addAll(flattenNodes(node.childs));
        });
        return result;
    }

    public void setParent(CodecNode parent) {
        this.parent = parent;
    }

    public void setChilds(List<CodecNode> childs) {
        childs.forEach(child -> child.setParent(this));
        this.childs = childs;
    }

    public boolean isSibling(CodecNode node) {
        return parent != null && node.parent != null && parent.hasSameKeyAndDecoder(node.parent);
    }

    public boolean hasSameKeyAndDecoder(CodecNode node) {
        return CodecParsedListener.getName(value).equals(CodecParsedListener.getName(node.value)) && CodecParsedListener.getDecoder(value).equals(CodecParsedListener.getDecoder(node.value)) && CodecParsedListener.getDepth(value) == CodecParsedListener.getDepth(node.value);
    }
}
