package llvm.instruction;

import llvm.Instr;
import llvm.Value;
import llvm.type.Type;

public class StoreInstr extends Instr {
    // `store  <ty> <value>, <ty>* <pointer>`
    private Value src; //值所在的寄存器

    public StoreInstr(InstrType instrType, Type type, String name, Value src) {
        super(instrType, type, name); // 这里面的regname是target
        this.src = src;
    }

    public String toString() {
        return "store " + getType() +
                " " + src.getName() + ", " + getType() +
                " * " + getName() + '\n';
    }
}
