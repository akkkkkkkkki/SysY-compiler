package nodes;

import error.ErrorType;
import helper.Helper;
import llvm.Value;
import llvm.instruction.InstrType;
import llvm.instruction.StoreInstr;
import llvm.type.SimpleType;
import parser.NodeType;

import java.util.ArrayList;

public class AssignStmtNode extends StmtNode{
    // LVal '=' Exp ';'
    public AssignStmtNode(int startNum, int endNum, NodeType nodeType, ArrayList<Node> children) {
        super(startNum, endNum, nodeType, children);
    }

    @Override
    public void handleError() { // h 不能改变常量的值
        for (Node node: children) {
            node.handleError();
        }
        if (((LValNode) children.get(0)).isExistedConst()) {
            Helper.storeError(children.get(0).getStartNum(), ErrorType.h);
        }
    }

    @Override
    public Value generate() {
        Value rvalue = children.get(2).generate();
        String target = ((LValNode) children.get(0)).getLVal();
        StoreInstr storeInstr = new StoreInstr(InstrType.STORE, SimpleType.INT32, target, rvalue);
        return null;
    }
}
