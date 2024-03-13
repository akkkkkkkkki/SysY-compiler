package llvm.type;

public class FixedType extends Type { // 对于其他val类型，例如module，basic block
    public static FixedType FUNCTION = new FixedType();
    public static FixedType MODULE = new FixedType();
    public static FixedType BB = new FixedType();
}
