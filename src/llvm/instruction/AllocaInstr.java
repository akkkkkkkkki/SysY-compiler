package llvm.instruction;

import llvm.Instr;
import llvm.type.PointerType;
import llvm.type.SimpleType;
import llvm.type.Type;

public class AllocaInstr extends Instr {
    // `<result> = alloca <type>`
    private Type targetType;

    public AllocaInstr(InstrType instrType, Type targetType, String name) {
        super(instrType, new PointerType(targetType), name);
        this.targetType = targetType;
    }

    public String toString() {
        return getName() + " = alloca " + targetType + "\n";
    }
}
