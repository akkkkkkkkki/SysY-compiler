package llvm.instruction;

import llvm.Constant;
import llvm.Instr;
import llvm.Value;
import llvm.type.ArrayType;
import llvm.type.Type;
import symbol.ArraySymbol;

import java.util.ArrayList;

public class GetEleInstr extends Instr {
    // `<result> = getelementptr <ty>, * {, [inrange] <ty> <idx>}*`    `<result> = getelementptr inbounds <ty>, <ty>* <ptrval>{, [inrange] <ty> <idx>}*`
    private ArrayList<Value> offsets;
    private String baseAddr;
    private Type baseType;

    public GetEleInstr(InstrType instrType, Type type, String name, String baseAddr, Type baseType, ArrayList<Value> offsets) {
        super(instrType, type, name);
        this.baseAddr = baseAddr;
        this.baseType = baseType;
        this.offsets = offsets;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getName()).append(" = getelementptr ");
        sb.append(baseType).append(", ");
        sb.append(baseType).append("* ").append(baseAddr).append(",");
        for (int i = 0; i < offsets.size(); i++) {
            sb.append(" i32 ").append(offsets.get(i).getName());
            if (i != offsets.size() - 1) {
                sb.append(",");
            }
        }
        sb.append("\n");
        return sb.toString();
    }
}
