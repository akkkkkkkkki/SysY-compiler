package nodes;

import lexer.Category;
import llvm.Value;
import parser.NodeType;
import symbol.FuncParamType;

import java.util.ArrayList;

public class PrimaryExpNode extends ExpNode {
    // PrimaryExp → '(' Exp ')' | LVal | Number

    public PrimaryExpNode(int startNum, int endNum, NodeType nodeType, ArrayList<Node> children) {
        super(startNum, endNum, nodeType, children);
    }

    @Override
    public int calcu() {
        if (children.get(0) instanceof TerminalNode) { // ( exp )
            return children.get(1).calcu();
        } else if (children.get(0) instanceof LValNode) {
            return children.get(0).calcu();
        } else if (children.get(0) instanceof NumberNode) { //只有一个terminal，所以直接递归calcu
            return children.get(0).calcu();
        } else {
            return 0; // error
        }
    }

    public FuncParamType eval() {
        if (children.get(0) instanceof NumberNode) {
            return new FuncParamType(0);
        } else {
            FuncParamType ret = children.get(0).eval();
            if (children.size() == 1) {
                return ret;
            }
            return children.get(1).eval();
        }
    }

    public Value generate() {
        if (children.get(0) instanceof TerminalNode) { // ( exp )
            return children.get(1).generate();
        } else {
            return children.get(0).generate(); // num 和 lval（做eval的lval
        }
    }
}
