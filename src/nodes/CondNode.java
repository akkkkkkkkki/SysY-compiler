package nodes;

import llvm.BasicBlock;
import llvm.Value;
import parser.NodeType;

import java.util.ArrayList;

public class CondNode extends Node {
    //  Cond → LOrExp

    public CondNode(int startNum, int endNum, NodeType nodeType, ArrayList<Node> children) {
        super(startNum, endNum, nodeType, children);
    }

    public void genBranch(BasicBlock trueBB, BasicBlock falseBB) {
        ((LOrExpNode) children.get(0)).genBranch(trueBB, falseBB);
    }
}
