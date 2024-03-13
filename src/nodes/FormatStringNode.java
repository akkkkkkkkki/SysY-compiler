package nodes;

import lexer.Category;
import parser.NodeType;

import java.util.ArrayList;

public class FormatStringNode extends Node {
    private String value;
    private Category category;

    public FormatStringNode(int startNum, int endNum, NodeType nodeType, ArrayList<Node> children) {
        super(startNum, endNum, nodeType, children);
    }

    public String getValue() {
        return value;
    }

    public Category getCategory() {
        return category;
    }
}
