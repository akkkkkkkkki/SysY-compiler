package nodes;

import llvm.Value;
import parser.NodeType;

import java.util.ArrayList;

public class InitValNode extends Node {
    //  InitVal → Exp | '{' [ InitVal { ',' InitVal } ] '}'
    private int dim;

    public InitValNode(int startNum, int endNum, NodeType nodeType, ArrayList<Node> children) {
        super(startNum, endNum, nodeType, children);
    }

    @Override
    public int calcu() {
        return children.get(0).calcu();
    }

    public ArrayList<Integer> calcu(ArrayList<Integer> vals, int d1) { // 一维数组，下面每个val节点的孩子都只有一个exp节点
        for (Node node: children) {
            if (node instanceof InitValNode) {
                vals.add(node.calcu());
            }
        }
        return vals;
    }

    public ArrayList<Integer> calcu(ArrayList<Integer> vals, int d1, int d2) { //二维数组，每个val节点都是一个一维数组初值
        for (Node node: children) {
            if (node instanceof InitValNode) {
                ((InitValNode) node).calcu(vals, d2);
            }
        }
        return vals;
    }

    public Value getSingleValue() { // 给简单变量用
        return children.get(0).generate();
    }

    public ArrayList<Value> getArrayValue() { // 给数组用 把二维数组全部parse成一维数组
        ArrayList<Value> ret = new ArrayList<>();
        for (Node node: children) {
            if (node instanceof InitValNode) {
                ret.addAll(((InitValNode) node).getArrayValue());
            } else if (node instanceof ExpNode) {
                ret.add(node.generate());
            }
        }
        return ret;
    }
}
