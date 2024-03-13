package nodes;

import parser.NodeType;

import java.util.ArrayList;

public class VarDeclNode extends Node {
    // VarDecl → BType VarDef { ',' VarDef } ';'

    public VarDeclNode(int startNum, int endNum, NodeType nodeType, ArrayList<Node> children) {
        super(startNum, endNum, nodeType, children);
    }
}
