package nodes;

import error.ErrorType;
import helper.Helper;
import llvm.InstrManager;
import llvm.Value;
import llvm.instruction.InstrType;
import llvm.instruction.JumpInstr;
import llvm.type.SimpleType;
import parser.NodeType;
import symbol.SymbolManager;

import java.util.ArrayList;

public class BreakStmtNode extends StmtNode {
    // 'break' ';'

    public BreakStmtNode(int startNum, int endNum, NodeType nodeType, ArrayList<Node> children) {
        super(startNum, endNum, nodeType, children);
    }

    @Override
    public void handleError() { // 这个和continue应该不用检查子节点吧
        SymbolManager symbolManager = SymbolManager.getManager();
        if (!symbolManager.isLooping()) {
            Helper.storeError(this.getStartNum(), ErrorType.m); // 在循环中之外出现了break
        }
    }

    public Value generate() {
        JumpInstr jumpInstr = new JumpInstr(InstrType.BR, InstrManager.getInstance().getCurFollow());
        return null;
    }
}
