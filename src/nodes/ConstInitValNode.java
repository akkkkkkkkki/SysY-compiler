package nodes;

import llvm.Constant;
import llvm.Value;
import parser.NodeType;

import java.util.ArrayList;

public class ConstInitValNode extends Node {
    // ConstInitVal → ConstExp | '{' [ ConstInitVal { ',' ConstInitVal } ] '}'

    public ConstInitValNode(int startNum, int endNum, NodeType nodeType, ArrayList<Node> children) {
        super(startNum, endNum, nodeType, children);
    }

    @Override
    public int calcu() {
        return children.get(0).calcu();
    }

    public ArrayList<Integer> calcu(ArrayList<Integer> vals, int d1) { // 一维数组，下面每个val节点的孩子都只有一个exp节点
        for (Node node: children) {
            if (node instanceof ConstInitValNode) {
                vals.add(node.calcu());
            }
        }
        return vals;
    }

    public ArrayList<Integer> calcu(ArrayList<Integer> vals, int d1, int d2) { //二维数组，每个val节点都是一个一维数组初值
        for (Node node: children) {
            if (node instanceof ConstInitValNode) {
                ((ConstInitValNode) node).calcu(vals, d2);
            }
        }
        return vals;
    }

    public Value getSingleValue() { // 给简单变量用, 可以直接const
        return new Constant(children.get(0).calcu());
        // todo 等把lval写好看看能不能 return children.get(0).generate();
//        return children.get(0).generate();
//        这样不太好，因为用到lavl 的eval，但是eval必须考虑offset，当不是const时必须eval offset
    }
}
