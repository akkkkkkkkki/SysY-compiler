package nodes;

import llvm.Value;
import parser.NodeType;
import symbol.FuncParamType;

import java.util.ArrayList;

public class Node {
    protected int startNum;
    protected int endNum;
    protected NodeType type;
    protected ArrayList<Node> children;

    public Node(int startNum, int endNum, NodeType nodeType, ArrayList<Node> children) {
        this.startNum = startNum;
        this.endNum = endNum;
        this.type = nodeType;
        this.children = children;
    }

    public int getStartNum() {
        return startNum;
    }

    public int getEndNum() {
        return endNum;
    }

    public NodeType getType() {
        return type;
    }

    public int calcu() { // 不要了啊啊，我懒得删了
        return 0;
    }

    public FuncParamType eval() {
        return null;
    }

    public ArrayList<Node> getChildren() {
        return children;
    }

    public void handleError() {
        if (children == null) return; // 对于terminal
        for (Node node: children) {
            node.handleError();
        }
    }

    public Value generate() {
        if (children == null) return null;
        for (Node node: children) {
            node.generate();
        }
        return null;
    }
}
