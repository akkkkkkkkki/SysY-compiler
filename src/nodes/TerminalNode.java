package nodes;

import lexer.Category;
import parser.NodeType;
import symbol.FuncParamType;

import java.util.ArrayList;

public class TerminalNode extends Node {
    private String value;
    private Category category;

    public TerminalNode(int startNum, int endNum, NodeType nodeType, ArrayList<Node> children, String value, Category category) {
        super(startNum, endNum, nodeType, children);
        this.value = value;
        this.category = category;
    }

    public String getValue() {
        return value;
    }

    public Category getCategory() {
        return category;
    }

}
