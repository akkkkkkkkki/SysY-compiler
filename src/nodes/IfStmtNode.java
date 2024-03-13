package nodes;

import llvm.BasicBlock;
import llvm.InstrManager;
import llvm.Value;
import llvm.instruction.BranchInstr;
import llvm.instruction.InstrType;
import llvm.instruction.JumpInstr;
import llvm.type.SimpleType;
import parser.NodeType;

import java.util.ArrayList;

public class IfStmtNode extends StmtNode{
    // 'if' '(' Cond ')' Stmt [ 'else' Stmt ]
    public IfStmtNode(int startNum, int endNum, NodeType nodeType, ArrayList<Node> children) {
        super(startNum, endNum, nodeType, children);
    }

    @Override
    public Value generate() {
//        Value condResult = children.get(2).generate(); // Cond  这太神奇了，这个地方有输出错误的东西
        if (children.size() > 5) { // 有else
            BasicBlock stmt1 = new BasicBlock(InstrManager.getInstance().newBB("then"));
            BasicBlock stmt2 = new BasicBlock(InstrManager.getInstance().newBB("else"));
            BasicBlock follow = new BasicBlock(InstrManager.getInstance().newBB("follow"));
//            BranchInstr branchInstr = new BranchInstr(InstrType.BR, SimpleType.INT1, condResult, stmt1, stmt2);
            ((CondNode) children.get(2)).genBranch(stmt1, stmt2);
            InstrManager.getInstance().setCurBB(stmt1);
            children.get(4).generate();
            JumpInstr jumpInstr = new JumpInstr(InstrType.BR, follow);
            InstrManager.getInstance().setCurBB(stmt2);
            children.get(6).generate();
            JumpInstr jumpInstr1 = new JumpInstr(InstrType.BR, follow);
            InstrManager.getInstance().setCurBB(follow);
        } else {
            BasicBlock stmt1 = new BasicBlock(InstrManager.getInstance().newBB("then"));
            BasicBlock follow = new BasicBlock(InstrManager.getInstance().newBB("follow"));
//            BranchInstr branchInstr = new BranchInstr(InstrType.BR, SimpleType.INT1, condResult, stmt1, follow);
            ((CondNode) children.get(2)).genBranch(stmt1, follow);
            InstrManager.getInstance().setCurBB(stmt1);
            children.get(4).generate();
            JumpInstr jumpInstr = new JumpInstr(InstrType.BR, follow);
            InstrManager.getInstance().setCurBB(follow);
        }
        return null;
    }
}
