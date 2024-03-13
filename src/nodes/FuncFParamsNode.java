package nodes;

import parser.NodeType;
import symbol.FuncParamType;

import java.util.ArrayList;

public class FuncFParamsNode extends Node {
    // FuncFParams → FuncFParam { ',' FuncFParam }

    public FuncFParamsNode(int startNum, int endNum, NodeType nodeType, ArrayList<Node> children) {
        super(startNum, endNum, nodeType, children);
    }

    public ArrayList<FuncParamType> calcu(int placeHolder) {
        ArrayList<FuncParamType> ret = new ArrayList<>();
        for (Node node: children) {
            if (node instanceof FuncFParamNode) {
                ret.add(((FuncFParamNode) node).calcu(1));
            }
        }
        return ret;
    }
}
