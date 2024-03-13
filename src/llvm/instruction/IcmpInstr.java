package llvm.instruction;

import llvm.Instr;
import llvm.Value;
import llvm.type.SimpleType;
import llvm.type.Type;

public class IcmpInstr extends Instr {
    // `<result> = icmp <cond> <ty> <op1>, <op2>`
    private Value op1;
    private Value op2;
    private CmpOp op; // cond

    public IcmpInstr(InstrType instrType, String name, CmpOp op, Value op1,  Value op2) {
        super(instrType, SimpleType.INT1, name);
        this.op = op;
        this.op1 = op1;
        this.op2 = op2;
    }

    public String toString() {
        return getName() + " = icmp " + op.toString().toLowerCase() + " i32 " + op1.getName() + ", " + op2.getName() + "\n";
    }
}
