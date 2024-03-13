package llvm.instruction;

import llvm.BasicBlock;
import llvm.Instr;
import llvm.Value;
import llvm.type.Type;

public class BranchInstr extends Instr {
    // `br i1 <cond>, label <iftrue>, label <iffalse>`

    private Value condResult;
    private BasicBlock trueTarget;
    private BasicBlock falseTarget;

    public BranchInstr(InstrType instrType, Type type, Value condResult, BasicBlock trueTarget, BasicBlock falseTarget) {
        super(instrType, type, "branch");
        this.condResult = condResult;
        this.trueTarget = trueTarget;
        this.falseTarget = falseTarget;
    }

    @Override
    public String toString() {
        return "br i1 " + condResult.getName() +
                ", label %" + trueTarget.getName() +
                ", label %" + falseTarget.getName() + "\n";
    }
}
