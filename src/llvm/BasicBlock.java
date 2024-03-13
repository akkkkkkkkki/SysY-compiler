package llvm;

import llvm.type.FixedType;

import java.util.ArrayList;

public class BasicBlock extends Value {
    private Function function;
    private ArrayList<Instr> instrs;

    public BasicBlock(String name) {
        super(name, FixedType.BB);
        this.function = InstrManager.getInstance().getCurFunc();
        this.instrs = new ArrayList<>();
        InstrManager.getInstance().insertBasicBlock(this);
    }

    public void addInstr(Instr instr) {
        instrs.add(instr);
    }

    public boolean isEmpty() {
        return instrs.isEmpty();
    }

    public String toString1() {
        StringBuilder sb = new StringBuilder();
        for (Instr instr: instrs) {
            sb.append("\t").append(instr);
        }
        return sb.toString();
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getName()).append(":\n");
        for (Instr instr: instrs) {
            sb.append("\t").append(instr);
        }
        return sb.toString();
    }

}
