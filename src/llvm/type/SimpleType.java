package llvm.type;

public class SimpleType extends Type { // 简单变量 int
    public static SimpleType INT32 = new SimpleType(32);
    public static SimpleType INT1 = new SimpleType(1);
    public static SimpleType VOID = new SimpleType(0);
    private int type; // int1 int32 或者 void

    public SimpleType(int type) {
        this.type = type;
    }

    public boolean isInt() {
        return type != 0;
    }

    public String toString() {
        return switch (type) {
            case 0 -> "void";
            case 1 -> "i1";
            default -> "i32";
        };
    }
}
