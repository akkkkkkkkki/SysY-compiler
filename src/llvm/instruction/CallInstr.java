package llvm.instruction;

import llvm.Instr;
import llvm.Value;
import llvm.type.SimpleType;
import llvm.type.Type;

import java.util.ArrayList;

public class CallInstr extends Instr {
    // `<result> =  call  [ret attrs]  <ty> <fnptrval>(<function args>)`
    private String funcName;
    private ArrayList<Value> params;

    public CallInstr(InstrType instrType, Type type, String name, String funcName, ArrayList<Value> params) {
        super(instrType, type, name); // type 是 return type
        this.funcName = funcName;
        this.params = params;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("(");
        if (params != null && !params.isEmpty()) {
            sb.append(params.get(0).getType()).append(" ").append(params.get(0).getName());
            for (int i = 1; i < params.size(); i++) {
                sb.append(", ").append(params.get(i).getType()).append(" ").append(params.get(i).getName());
            }
        }
        sb.append(")\n");

        if (getType() == SimpleType.INT32) {
            return getName() + " = call i32 @" + funcName + sb;
        } else {
            return "call void @" + funcName + sb;
        }
    }
}
