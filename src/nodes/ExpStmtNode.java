package nodes;

import parser.NodeType;

import java.util.ArrayList;

public class ExpStmtNode extends StmtNode {
    public ExpStmtNode(int startNum, int endNum, NodeType nodeType, ArrayList<Node> children) {
        super(startNum, endNum, nodeType, children);
    }
}
