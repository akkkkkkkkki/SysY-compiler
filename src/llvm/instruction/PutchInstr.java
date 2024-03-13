package llvm.instruction;

import llvm.Instr;
import llvm.instruction.InstrType;
import llvm.type.Type;

public class PutchInstr extends Instr {
    // call void @putch(i32 104)
    private char target;

    public PutchInstr(InstrType instrType, Type type, String name, char target) {
        super(instrType, type, name);
        this.target = target;
    }

    public String toString() {
        return "call void @putch(i32 " + (int)target + ")\n";
    }
}
