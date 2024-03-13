package nodes;

import parser.NodeType;

import java.util.ArrayList;

public class DeclNode extends Node {
    // Decl → ConstDecl | VarDecl

    public DeclNode(int startNum, int endNum, NodeType nodeType, ArrayList<Node> children) {
        super(startNum, endNum, nodeType, children);
    }
}
