package llvm.instruction;

import llvm.Instr;
import llvm.Value;
import llvm.type.Type;

public class PutintInstr extends Instr {
    private Value target;

    public PutintInstr(InstrType instrType, Type type, String name, Value target) {
        super(instrType, type, name);
        this.target = target;
    }

    public String toString() {
        return "call void @putint(i32 " + target.getName() + ")\n";
    }
}
