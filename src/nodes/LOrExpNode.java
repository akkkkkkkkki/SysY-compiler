package nodes;

import llvm.BasicBlock;
import llvm.InstrManager;
import llvm.instruction.BranchInstr;
import llvm.instruction.InstrType;
import llvm.type.SimpleType;
import parser.NodeType;

import java.util.ArrayList;

public class LOrExpNode extends Node {
    //  LOrExp → LAndExp | LOrExp '||' LAndExp

    public LOrExpNode(int startNum, int endNum, NodeType nodeType, ArrayList<Node> children) {
        super(startNum, endNum, nodeType, children);
    }

    public void genBranch(BasicBlock trueBB, BasicBlock falseBB) {
        for (int i = 0; i < children.size(); i++) {
            if (i == children.size() - 1) { // 如果是最后一个，false该跳转false
                ((LAndExpNode) children.get(i)).genBranch(trueBB, falseBB);
                break;
            } else if (children.get(i) instanceof LAndExpNode) {
                // 如果在中间，是false应该是下一个
                BasicBlock nextBlock = new BasicBlock(InstrManager.getInstance().newBB("nextAnd"));
                ((LAndExpNode) children.get(i)).genBranch(trueBB, nextBlock);
                InstrManager.getInstance().setCurBB(nextBlock);
            }
        }
    }
}
