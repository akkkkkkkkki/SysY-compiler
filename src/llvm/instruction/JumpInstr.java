package llvm.instruction;

import llvm.BasicBlock;
import llvm.Instr;
import llvm.type.SimpleType;
import llvm.type.Type;

public class JumpInstr extends Instr {
    // `br label <dest>`
    private BasicBlock target;

    public JumpInstr(InstrType instrType, BasicBlock target) {
        super(instrType, SimpleType.VOID, "jump");
        this.target = target;
    }

    @Override
    public String toString() {
        return "br label %" + target.getName() + "\n";
    }
}
