package nodes;

import llvm.InstrManager;
import llvm.Value;
import parser.NodeType;

import java.util.ArrayList;

public class BlockItemNode extends Node {
    //  BlockItem → Decl | Stmt

    public BlockItemNode(int startNum, int endNum, NodeType nodeType, ArrayList<Node> children) {
        super(startNum, endNum, nodeType, children);
    }
}
