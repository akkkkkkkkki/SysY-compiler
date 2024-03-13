package nodes;

import error.ErrorType;
import helper.Helper;
import llvm.Constant;
import llvm.InstrManager;
import llvm.Value;
import llvm.instruction.InstrType;
import llvm.instruction.LoadInstr;
import llvm.instruction.ReturnInstr;
import llvm.type.PointerType;
import llvm.type.SimpleType;
import parser.NodeType;
import symbol.ReturnType;
import symbol.SymbolManager;

import java.util.ArrayList;

public class ReturnStmtNode extends StmtNode {
    // 'return' [Exp] ';' // 1.有Exp 2.无Exp
    private boolean isReturnVal;  // 是否是要return int的返回语句 (在初始化的时候就准备好值

    public ReturnStmtNode(int startNum, int endNum, NodeType nodeType, ArrayList<Node> children) {
        super(startNum, endNum, nodeType, children);
        this.isReturnVal = false;
        for (Node node: children) {
            if (node instanceof ExpNode) {
                isReturnVal = true;
                SymbolManager.getManager().setReturnType(ReturnType.INT);
                break;
            }
        }
    }

    public boolean isReturnVal() {
        return isReturnVal;
    }

    @Override
    public void handleError() { // 已经进到return 语句了，
        SymbolManager symbolManager = SymbolManager.getManager();
        for (Node node: children) {
            node.handleError();
        }
        if (isReturnVal && symbolManager.checkReturnType(ReturnType.VOID)) { // 只能判断是否是无返回值的函数返回了某个值
            Helper.storeError(children.get(0).getStartNum(), ErrorType.f);
        } else {
            if (isReturnVal) {
                symbolManager.setReturnType(ReturnType.INT);
            }
        }

    }

    @Override
    public Value generate() {
        if (isReturnVal) {
            Value retVal = ((ExpNode) children.get(1)).generate();
            String regName = InstrManager.getInstance().newReg();
            ReturnInstr returnInstr = new ReturnInstr(InstrType.RET, SimpleType.INT32, retVal.getName(), InstrManager.getInstance().getCurFunc());
        } else {
            ReturnInstr returnInstr = new ReturnInstr(InstrType.RET, SimpleType.VOID, "", InstrManager.getInstance().getCurFunc());
        }
        InstrManager.getInstance().setHasRet();
        return null;
    }
}
