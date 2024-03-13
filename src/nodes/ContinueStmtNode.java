package nodes;

import error.ErrorType;
import helper.Helper;
import llvm.InstrManager;
import llvm.Value;
import llvm.instruction.InstrType;
import llvm.instruction.JumpInstr;
import parser.NodeType;
import symbol.SymbolManager;

import java.util.ArrayList;

public class ContinueStmtNode extends StmtNode {
    public ContinueStmtNode(int startNum, int endNum, NodeType nodeType, ArrayList<Node> children) {
        super(startNum, endNum, nodeType, children);
    }

    @Override
    public void handleError() {
        SymbolManager symbolManager = SymbolManager.getManager();
        if (!symbolManager.isLooping()) {
            Helper.storeError(this.getStartNum(), ErrorType.m); // 在循环中之外出现了continue
        }
    }

    public Value generate() {
        JumpInstr jumpInstr = new JumpInstr(InstrType.BR, InstrManager.getInstance().getCurIncre());
        return null;
    }
}
