package llvm.instruction;

import llvm.Instr;
import llvm.Value;
import llvm.type.SimpleType;
import llvm.type.Type;

public class ZextInstr extends Instr {
    // `<result> = zext <ty> <value> to <ty2>`
    private Value src;

    public ZextInstr(InstrType instrType, Type type, String name, Value src) {
        super(instrType, type, name);
        this.src = src;
    }

    public String toString() {
        return getName() + " = zext " + src.getType() + " " + src.getName() + " to " + getType() + '\n';
    }
}
