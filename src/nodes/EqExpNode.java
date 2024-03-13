package nodes;

import lexer.Category;
import llvm.Constant;
import llvm.Instr;
import llvm.InstrManager;
import llvm.Value;
import llvm.instruction.CmpOp;
import llvm.instruction.IcmpInstr;
import llvm.instruction.InstrType;
import llvm.instruction.ZextInstr;
import llvm.type.SimpleType;
import parser.NodeType;

import java.util.ArrayList;

public class EqExpNode extends Node {
    // EqExp → RelExp | EqExp ('==' | '!=') RelExp

    public EqExpNode(int startNum, int endNum, NodeType nodeType, ArrayList<Node> children) {
        super(startNum, endNum, nodeType, children);
    }

    public Value generate() {
        Value value = children.get(0).generate();
        if (children.size() == 1) {
            if (value.getType() == SimpleType.INT32) {
                value = new IcmpInstr(InstrType.ICMP, InstrManager.getInstance().newReg(), CmpOp.NE, value, Constant.zero);
            }
        } else {
            for (int i = 1; i < children.size(); i += 2) {
                if (value.getType() != SimpleType.INT32) {
                    value = new ZextInstr(InstrType.ZEXT, SimpleType.INT32, InstrManager.getInstance().newReg(), value);
                }
                Value other = children.get(i + 1).generate();
                if (other.getType() != SimpleType.INT32) { // 如果另一个不是i32的话，要比较需要都提升到i32
                    other = new ZextInstr(InstrType.ZEXT, SimpleType.INT32, InstrManager.getInstance().newReg(), other);
                }

                if (((TerminalNode) children.get(i)).getCategory() == Category.EQL) {
                    value = new IcmpInstr(InstrType.ICMP, InstrManager.getInstance().newReg(), CmpOp.EQ, value, other);
                } else {
                    value = new IcmpInstr(InstrType.ICMP, InstrManager.getInstance().newReg(), CmpOp.NE, value, other);
                }
            }
        }
        return value;
    }
}
