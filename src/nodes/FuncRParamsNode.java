package nodes;

import llvm.Value;
import parser.NodeType;
import symbol.FuncParamType;
import symbol.SymbolManager;

import java.util.ArrayList;

public class FuncRParamsNode extends Node {
    //  FuncRParams → Exp { ',' Exp }

    public FuncRParamsNode(int startNum, int endNum, NodeType nodeType, ArrayList<Node> children) {
        super(startNum, endNum, nodeType, children);
    }

    @Override
    public void handleError() {  // 需要返回一个参数类型列表，然后和当前函数的参数类型列表匹配。
        SymbolManager symbolManager = SymbolManager.getManager();
        for (Node node: children) {
            node.handleError();
        }

        symbolManager.setRParasNum((children.size() + 1) / 2);
    }

    public ArrayList<FuncParamType> getRParas() { // for handler，get的是para的type
        ArrayList<FuncParamType> ret = new ArrayList<>();
        for (Node node: children) {
            FuncParamType tmp = node.eval();
            if (tmp != null) ret.add(tmp);
        }
        return ret;
    }

    public ArrayList<Value> getRParasValue() {
        ArrayList<Value> values = new ArrayList<>();
        for (Node node: children) {
            if (node instanceof ExpNode) {
                values.add(node.generate());
            }
        }
        return values;
    }
}