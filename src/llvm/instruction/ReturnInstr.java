package llvm.instruction;

import llvm.Function;
import llvm.Instr;
import llvm.type.SimpleType;
import llvm.type.Type;

public class ReturnInstr extends Instr {
    // `ret <type> <value>`  ,`ret void`
    private Function function;

    public ReturnInstr(InstrType instrType, Type type, String name, Function function) {
        super(instrType, type, name); // type就是返回值的type
        this.function = function;
    }

    public String toString() {
        if (getType() == SimpleType.INT32) {
            return "ret i32 " + getName() + '\n';
        } else {
            return "ret void\n";
        }
    }
}
