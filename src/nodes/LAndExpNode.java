package nodes;

import llvm.BasicBlock;
import llvm.Instr;
import llvm.InstrManager;
import llvm.Value;
import llvm.instruction.BranchInstr;
import llvm.instruction.InstrType;
import llvm.type.SimpleType;
import parser.NodeType;

import java.util.ArrayList;

public class LAndExpNode extends Node {
    // 式 LAndExp → EqExp | LAndExp '&&' EqExp

    public LAndExpNode(int startNum, int endNum, NodeType nodeType, ArrayList<Node> children) {
        super(startNum, endNum, nodeType, children);
    }

    public void genBranch(BasicBlock trueBB, BasicBlock falseBB) {
        for (int i = 0; i < children.size(); i++) {
            if (i == children.size() - 1) {
                Value condResult = children.get(i).generate();
                BranchInstr branchInstr = new BranchInstr(InstrType.BR, SimpleType.INT1, condResult, trueBB, falseBB);
            } else if (children.get(i) instanceof EqExpNode) {
                BasicBlock nextBB = new BasicBlock(InstrManager.getInstance().newBB("nextEq"));
                Value condResults = children.get(i).generate();
                BranchInstr branchInstr = new BranchInstr(InstrType.BR, SimpleType.INT1, condResults, nextBB, falseBB);
                InstrManager.getInstance().setCurBB(nextBB); // 要过了再设
            }
        }
    }
}
