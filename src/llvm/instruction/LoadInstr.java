package llvm.instruction;

import llvm.Instr;
import llvm.Value;
import llvm.type.SimpleType;
import llvm.type.Type;

public class LoadInstr extends Instr {
    // `<result> = load  <ty>, <ty>* <pointer>`  先默认ty是int32了
    private String pointer; // pointer

    public LoadInstr(InstrType instrType, Type type, String result, String pointer) {
        super(instrType, type, result);
        this.pointer = pointer;
    }

    public String toString() {
        return getName() + " = load " + getType() + ", " + getType() + " * " + pointer + "\n";
    }

}
