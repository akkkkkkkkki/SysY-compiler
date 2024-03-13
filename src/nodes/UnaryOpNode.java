package nodes;

import parser.NodeType;

import java.util.ArrayList;

public class UnaryOpNode extends Node {
    //  UnaryOp → '+' | '−' | '!'

    public UnaryOpNode(int startNum, int endNum, NodeType nodeType, ArrayList<Node> children) {
        super(startNum, endNum, nodeType, children);
    }
}
