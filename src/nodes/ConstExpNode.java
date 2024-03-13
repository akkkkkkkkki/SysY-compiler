package nodes;

import lexer.Category;
import parser.NodeType;

import java.util.ArrayList;

public class ConstExpNode extends ExpNode { // 使用的ident是常量，所以去符号表里面找即可
    // ConstExp → AddExp

    public ConstExpNode(int startNum, int endNum, NodeType nodeType, ArrayList<Node> children) {
        super(startNum, endNum, nodeType, children);
    }

    @Override
    public int calcu() {
        return children.get(0).calcu(); //只有一个children节点：addExpr
    }
}
