package nodes;

import error.ErrorType;
import helper.Helper;
import llvm.Value;
import llvm.instruction.InstrType;
import llvm.instruction.StoreInstr;
import llvm.type.SimpleType;
import parser.NodeType;

import java.util.ArrayList;

public class ForInitNode extends Node {
    // ForStmt → LVal '=' Exp 最开始是这样

    public ForInitNode(int startNum, int endNum, NodeType nodeType, ArrayList<Node> children) {
        super(startNum, endNum, nodeType, children);
    }

    @Override
    public void handleError() { // h 不能改变常量的值
        for (Node node: children) {
            node.handleError();
        }
        if (((LValNode) children.get(0)).isExistedConst()) {  // 怎么感觉应该先看是否定义啊 todo
            Helper.storeError(children.get(0).getStartNum(), ErrorType.h);
        }
    }

    public Value generate() {
        Value rvalue = children.get(2).generate();
        String target = ((LValNode) children.get(0)).getLVal();
        StoreInstr storeInstr = new StoreInstr(InstrType.STORE, SimpleType.INT32, target, rvalue);
        return null;
    }
}
