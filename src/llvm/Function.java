package llvm;

import helper.Helper;
import llvm.instruction.InstrType;
import llvm.instruction.ReturnInstr;
import llvm.type.SimpleType;
import llvm.type.Type;

import java.util.ArrayList;

public class Function extends Value {
    private ArrayList<Param> params;
    private ArrayList<BasicBlock> basicBlocks;
    private ArrayList<Instr> instrs;

    public Function(String name, Type type) { // 这里的name是没有加@的
        super(name, type);
        this.params = new ArrayList<>();
        this.basicBlocks = new ArrayList<>();
        this.instrs = new ArrayList<>();
        InstrManager.getInstance().insertFunction(this);
    }

    public void addParam(Param param) {
        params.add(param);
    }

    public void addInstr(Instr instr) {
        instrs.add(instr);
    }

    public void addBB(BasicBlock basicBlock) {
        basicBlocks.add(basicBlock);
    }

    public void mend() {
        for (BasicBlock basicBlock: basicBlocks) {
            if (basicBlock.isEmpty()) {
                ReturnInstr returnInstr = new ReturnInstr(InstrType.RET, getType(), InstrManager.getInstance().newReg(), this);
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("define dso_local ");
        if (getType() == SimpleType.INT32) {
            sb.append("i32");
        } else {
            sb.append("void");
        }
        sb.append(" @").append(getName()).append("(");
        if (!params.isEmpty()) {
            sb.append(params.get(0).toString());
            for (int i = 1; i < params.size(); i++) {
                sb.append(", ");
                sb.append(params.get(i).toString());
            }
        }
        sb.append(") {\n");
        sb.append(basicBlocks.get(0).toString1());
        for (int i = 1; i < basicBlocks.size(); i++) {
            sb.append(basicBlocks.get(i));
        }
        sb.append("}\n");
        return sb.toString();
    }
}
