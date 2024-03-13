package nodes;

import llvm.Constant;
import llvm.Value;
import parser.NodeType;

import java.util.ArrayList;

public class NumberNode extends ExpNode {
    //  Number → IntConst

    public NumberNode(int startNum, int endNum, NodeType nodeType, ArrayList<Node> children) {
        super(startNum, endNum, nodeType, children);
    }

    @Override
    public int calcu() {
        return Integer.parseInt(((TerminalNode) children.get(0)).getValue());
    }

    @Override
    public Value generate() {
        return new Constant(Integer.parseInt(((TerminalNode) children.get(0)).getValue()));
    }

}
