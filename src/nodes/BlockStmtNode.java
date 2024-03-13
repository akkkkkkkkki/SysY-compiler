package nodes;

import parser.NodeType;

import java.util.ArrayList;

public class BlockStmtNode extends StmtNode{
    public BlockStmtNode(int startNum, int endNum, NodeType nodeType, ArrayList<Node> children) {
        super(startNum, endNum, nodeType, children);
    }
}
