package llvm;

import llvm.instruction.InstrType;
import llvm.type.Type;

public class Instr extends Value {
//    private BasicBlock basicBlock;
    private InstrType instrType;

    public Instr(InstrType instrType, Type type, String name) {
        super(name, type);
        this.instrType = instrType;
        InstrManager.getInstance().insertInstr(this); // 在这里实现了把new的指令都放进去
    }
}
