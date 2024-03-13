package nodes;

import llvm.Value;
import parser.NodeType;

import java.util.ArrayList;

public class CompUnitNode extends Node {
    //  CompUnit → {Decl} {FuncDef} MainFuncDef
    public CompUnitNode(int start, int end, NodeType nodeType, ArrayList<Node> children) {
        super(start, end, nodeType, children);
    }

    @Override
    public void handleError() {
        //已经建立了最外层的符号表
        for (Node node: children) {
            node.handleError();
        }
    }
}
