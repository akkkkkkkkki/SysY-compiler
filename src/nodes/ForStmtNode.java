package nodes;

import llvm.BasicBlock;
import llvm.Constant;
import llvm.InstrManager;
import llvm.Value;
import llvm.instruction.InstrType;
import llvm.instruction.JumpInstr;
import llvm.type.FixedType;
import llvm.type.SimpleType;
import parser.NodeType;
import symbol.SymbolManager;

import java.util.ArrayList;

public class ForStmtNode extends StmtNode {
    // 'for' '('[ForInit] ';' [Cond] ';' [ForStmt] ')' Stmt

    public ForStmtNode(int startNum, int endNum, NodeType nodeType, ArrayList<Node> children) {
        super(startNum, endNum, nodeType, children);
    }

    @Override
    public void handleError() {
        SymbolManager symbolManager = SymbolManager.getManager(); // bug: 错误处理，有的时候缺失了一些节点

        // 前size - 1个不是循环体
        int num = children.size() - 1;
        for (int i = 0; i < num; i++) {
            children.get(i).handleError();
        }
        symbolManager.enterLoop();
        children.get(num).handleError();
        symbolManager.leaveLoop();
    }

    @Override
    public Value generate() {
        BasicBlock init = new BasicBlock(InstrManager.getInstance().newBB("init"));
        BasicBlock check = new BasicBlock(InstrManager.getInstance().newBB("check"));
        BasicBlock body = new BasicBlock(InstrManager.getInstance().newBB("loop"));
        BasicBlock incre = new BasicBlock(InstrManager.getInstance().newBB("incre"));
        BasicBlock follow = new BasicBlock(InstrManager.getInstance().newBB("follow"));

        JumpInstr jumpInstr = new JumpInstr(InstrType.BR, init);
        InstrManager.getInstance().setCurBB(init);
        int checkPoint = 2;
        if (children.get(checkPoint) instanceof ForInitNode) { // 有初始化表达式
            children.get(checkPoint).generate();
            checkPoint += 2;
        } else {
            checkPoint += 1;
        }
        JumpInstr jumpInstr1 = new JumpInstr(InstrType.BR, check);

        InstrManager.getInstance().setCurBB(check); // set 之前肯定是有跳转
        if (children.get(checkPoint) instanceof CondNode) {
            ((CondNode) children.get(checkPoint)).genBranch(body, follow);
            checkPoint += 2;
        } else {
            checkPoint += 1;
            JumpInstr jumpInstr2 = new JumpInstr(InstrType.BR, body);
        }

        InstrManager.getInstance().enterLoop(follow, incre);
        InstrManager.getInstance().setCurBB(body);
        children.get(children.size() - 1).generate();
        JumpInstr jumpInstr2 = new JumpInstr(InstrType.BR, incre); // bug continue是挑转到increment
        // increment要有单独的bb

        InstrManager.getInstance().setCurBB(incre);
        if (children.get(checkPoint) instanceof ForInitNode) {
            children.get(checkPoint).generate();
        }
        JumpInstr jumpInstr3 = new JumpInstr(InstrType.BR, check);

        InstrManager.getInstance().leaveLoop();
        InstrManager.getInstance().setCurBB(follow);

        return null;
    }
}
