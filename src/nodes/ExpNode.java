package nodes;

import llvm.Value;
import parser.NodeType;
import symbol.FuncParamType;

import java.util.ArrayList;

public class ExpNode extends Node {
    // Exp → AddExp

    public ExpNode(int startNum, int endNum, NodeType nodeType, ArrayList<Node> children) {
        super(startNum, endNum, nodeType, children);
    }

    @Override
    public int calcu() {
        return children.get(0).calcu();
    }

    @Override
    public FuncParamType eval() {
        return children.get(0).eval();
    }

    public Value generate() {
        return children.get(0).generate();
    }
}
