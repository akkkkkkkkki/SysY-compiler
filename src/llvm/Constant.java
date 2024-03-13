package llvm;

import llvm.type.SimpleType;
import llvm.type.Type;

public class Constant extends Value {
    private int value;
    public static Constant zero = new Constant(0);

    public Constant(int value) {
        super(String.valueOf(value), SimpleType.INT32);
        this.value = value;
    }

    public String toString() {
        return "i32 " + value;
    }
}
