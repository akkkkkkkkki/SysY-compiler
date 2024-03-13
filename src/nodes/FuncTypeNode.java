package nodes;

import parser.NodeType;

import java.util.ArrayList;

public class FuncTypeNode extends Node {
    //  FuncType → 'void' | 'int'

    public FuncTypeNode(int startNum, int endNum, NodeType nodeType, ArrayList<Node> children) {
        super(startNum, endNum, nodeType, children);
    }
}
