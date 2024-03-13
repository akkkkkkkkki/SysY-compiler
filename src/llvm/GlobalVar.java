package llvm;

import llvm.type.ArrayType;
import llvm.type.Type;

import java.util.ArrayList;

public class GlobalVar extends Value {
    private boolean isConst;
    private ArrayList<Integer> initailVal;
    private boolean isArray;

    public GlobalVar(String name, Type type, boolean isConst, ArrayList<Integer> initailVal) {
        super(name, type);
        this.isConst = isConst;
        this.initailVal = initailVal;
        this.isArray = true;
        InstrManager.getInstance().insertGlobalVar(this);
    }

    public GlobalVar(String name, Type type, boolean isConst, int initialVal) {
        super(name, type);
        this.isConst = isConst;
        this.initailVal = new ArrayList<>();
        this.initailVal.add(initialVal);
        this.isArray = false;
        InstrManager.getInstance().insertGlobalVar(this);
    }

    @Override
    public String toString() {
        // @a = dso_local constant i32 5
        StringBuilder sb = new StringBuilder();
        sb.append(getName());
        sb.append(" = dso_local ");
        if (isConst) {
            sb.append("constant ");
        } else {
            sb.append("global ");
        }
        sb.append(getType()).append(" ");
        if (!isArray) {
            sb.append(initailVal.get(0));
        } else {
            if (initailVal == null) { // 某个维度是0没有考虑了
                sb.append(" zeroinitializer");
            } else {
                if (((ArrayType) getType()).isTwoDim()) {
                    int dim = ((ArrayType) getType()).getDim();
                    int dim2 = ((ArrayType) ((ArrayType) getType()).getElementType()).getDim();
                    sb.append("[");
                    for (int i = 0; i < dim; i++) {
                        sb.append(((ArrayType) getType()).getElementType());
                        sb.append(" [i32 ").append(initailVal.get(i * dim2));
                        for (int j = 1; j < dim2; j++) {
                            sb.append(", i32 ").append(initailVal.get(i * dim2 + j));
                        }
                        sb.append("]");
                        if (i != dim - 1) {
                            sb.append(", ");
                        }
                    }
                    sb.append("]");
                } else {
                    sb.append("[i32 ").append(initailVal.get(0));
                    for (int i = 1; i < initailVal.size(); i++) {
                        sb.append(", ");
                        sb.append("i32 ").append(initailVal.get(i));
                    }
                    sb.append("]");
                }
            }
        }
        sb.append("\n");
        return sb.toString();
    }
}
