package nodes;

import parser.NodeType;

import java.util.ArrayList;

public class ConstDeclNode extends Node {
    // ConstDecl → 'const' BType ConstDef { ',' ConstDef } ';'

    public ConstDeclNode(int startNum, int endNum, NodeType nodeType, ArrayList<Node> children) {
        super(startNum, endNum, nodeType, children);
    }
}
