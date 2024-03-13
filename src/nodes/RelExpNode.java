package nodes;

import lexer.Category;
import llvm.InstrManager;
import llvm.Value;
import llvm.instruction.CmpOp;
import llvm.instruction.IcmpInstr;
import llvm.instruction.InstrType;
import llvm.instruction.ZextInstr;
import llvm.type.SimpleType;
import parser.NodeType;

import javax.swing.*;
import java.util.ArrayList;

public class RelExpNode extends Node {
    // RelExp → AddExp | RelExp ('<' | '>' | '<=' | '>=') AddExp

    public RelExpNode(int startNum, int endNum, NodeType nodeType, ArrayList<Node> children) {
        super(startNum, endNum, nodeType, children);
    }

    @Override
    public Value generate() {
        if (children.size() == 1) {
            return children.get(0).generate();
        } else {
            Value value = children.get(0).generate();
            for (int i = 1; i < children.size(); i += 2) {
                Value other = children.get(i + 1).generate(); // 都是addexp，肯定是i32
                if (value.getType() != SimpleType.INT32) { // 可能是前一个比较结果，那就是i1，需要提升
                    value = new ZextInstr(InstrType.ZEXT, SimpleType.INT32, InstrManager.getInstance().newReg(), value);
                }

                if (((TerminalNode) children.get(i)).getCategory() == Category.GRE) { // >
                    value = new IcmpInstr(InstrType.ICMP, InstrManager.getInstance().newReg(), CmpOp.SGT, value, other);
                } else if (((TerminalNode) children.get(i)).getCategory() == Category.GEQ) { // >=
                    value = new IcmpInstr(InstrType.ICMP, InstrManager.getInstance().newReg(), CmpOp.SGE, value, other);
                } else if (((TerminalNode) children.get(i)).getCategory() == Category.LSS) { // <
                    value = new IcmpInstr(InstrType.ICMP, InstrManager.getInstance().newReg(), CmpOp.SLT, value, other);
                } else { // <=
                    value = new IcmpInstr(InstrType.ICMP, InstrManager.getInstance().newReg(), CmpOp.SLE, value, other);
                }
            }
            return value;
        }
    }
}
