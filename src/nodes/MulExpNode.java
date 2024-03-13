package nodes;

import lexer.Category;
import llvm.InstrManager;
import llvm.Value;
import llvm.instruction.AluInstr;
import llvm.instruction.AluOp;
import llvm.instruction.InstrType;
import parser.NodeType;
import symbol.FuncParamType;

import java.util.ArrayList;

public class MulExpNode extends ExpNode {
    // MulExp → UnaryExp | MulExp ('*' | '/' | '%') UnaryExp

    public MulExpNode(int startNum, int endNum, NodeType nodeType, ArrayList<Node> children) {
        super(startNum, endNum, nodeType, children);
    }

    @Override
    public int calcu() {
        int val = children.get(0).calcu();
        for (int i = 1; i < children.size(); i += 2) {
            if (((TerminalNode) children.get(i)).getCategory() == Category.MULT) {
                val *= children.get(i + 1).calcu();
            } else if (((TerminalNode) children.get(i)).getCategory() == Category.DIV) {
                val /= children.get(i + 1).calcu();
            } else {
                val %= children.get(i + 1).calcu();
            }
        }
        return val;
    }

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
    public Value generate() {
        Value op1 = children.get(0).generate();
        Value op2;
        Value ret = op1;

        for (int i = 1; i < children.size(); i += 2) {
            if (children.get(i) instanceof TerminalNode && ((TerminalNode) children.get(i)).getCategory() == Category.MULT) {
                op2 = children.get(i + 1).generate();
                String regName = InstrManager.getInstance().newReg();
                ret = new AluInstr(InstrType.ALU, AluOp.MUL, regName, op1, op2);
                op1 = ret;
            } else if (children.get(i) instanceof TerminalNode && ((TerminalNode) children.get(i)).getCategory() == Category.DIV) {
                op2 = children.get(i + 1).generate();
                String regName = InstrManager.getInstance().newReg();
                ret = new AluInstr(InstrType.ALU, AluOp.SDIV, regName, op1, op2);
                op1 = ret;
            } else {
                op2 = children.get(i + 1).generate();
                String regName = InstrManager.getInstance().newReg();
                ret = new AluInstr(InstrType.ALU, AluOp.SREM, regName, op1, op2);
                op1 = ret;
            }
        }
        return ret;
    }

}
