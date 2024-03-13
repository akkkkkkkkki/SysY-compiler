package llvm.instruction;


import llvm.Instr;
import llvm.Value;
import llvm.type.SimpleType;

public class AluInstr extends Instr {
    // `<result> = op <ty> <op1>, <op2>`
    private AluOp op;
    private Value op1;
    private Value op2;

    public AluInstr(InstrType instrType, AluOp op, String resultReg, Value op1, Value op2) {
        super(instrType, SimpleType.INT32, resultReg);
        this.op = op;
        this.op1 = op1;
        this.op2 = op2;
    }

    public String toString() {
        return getName() + " = " + op.toString().toLowerCase() + " i32 " + op1.getName()  + ", " + op2.getName() + "\n";
    }
}
