package nodes;

import lexer.Category;
import llvm.*;
import llvm.instruction.AluInstr;
import llvm.instruction.AluOp;
import llvm.instruction.InstrType;
import parser.NodeType;
import symbol.FuncParamType;

import java.util.ArrayList;

public class AddExpNode extends ExpNode {
    // AddExp → MulExp | AddExp ('+' | '−') MulExp

    public AddExpNode(int startNum, int endNum, NodeType nodeType, ArrayList<Node> children) {
        super(startNum, endNum, nodeType, children);
    }

    @Override
    public int calcu() {
        int val = children.get(0).calcu(); //先把第一个mul的值取出来，再一对一对的取op和mul
        for (int i = 1; i < children.size(); i += 2) {
            if (((TerminalNode) children.get(i)).getCategory() == Category.PLUS) {
                val += children.get(i + 1).calcu();
            } else {
                val -= children.get(i + 1).calcu();
            }
        }
        return val;
    }

    @Override
    public FuncParamType eval() {
        FuncParamType ret = children.get(0).eval();
        if (children.size() == 1) {
            return ret;
        }
        for (int i = 1; i < children.size(); i++) {
            if (ret != null) {
                ret = ret.merge(children.get(i).eval());
            } else {
                ret = children.get(i).eval();
            }
        }
        return ret;
    }

    @Override
    public Value generate() { // 直接返回计算出最终结果的指令
        Value op1 = children.get(0).generate();
        Value op2;
        Value ret = op1;

        for (int i = 1; i < children.size(); i += 2) {
            if (children.get(i) instanceof TerminalNode && ((TerminalNode) children.get(i)).getCategory() == Category.PLUS) {
                op2 = children.get(i + 1).generate();
                String regName = InstrManager.getInstance().newReg();
                ret = new AluInstr(InstrType.ALU, AluOp.ADD, regName, op1, op2);
                op1 = ret;
            } else {
                op2 = children.get(i + 1).generate();
                String regName = InstrManager.getInstance().newReg();
                ret = new AluInstr(InstrType.ALU, AluOp.SUB, regName, op1, op2);
                op1 = ret;
            }
        }
        return ret;
    }
}
