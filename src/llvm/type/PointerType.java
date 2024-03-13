package llvm.type;

public class PointerType extends Type { // 函数数组传参
    private Type target;

    public PointerType(Type target) {
        this.target = target;
    }

    public Type getTarget() {
        return target;
    }

    @Override
    public String toString() {
        return target + "*";
    }
}
