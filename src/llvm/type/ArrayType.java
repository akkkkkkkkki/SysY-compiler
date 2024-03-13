package llvm.type;

public class ArrayType extends Type {
    private Type elementType;
    private int length;

    public ArrayType(Type elementType, int length) {
        this.elementType = elementType;
        this.length = length;
    }

    public boolean isTwoDim() {
        return elementType instanceof ArrayType;
    }

    public Type getElementType() {
        return elementType;
    }

    public int getDim() {
        return length;
    }

    @Override
    public String toString() {
        return "[" +
                length +
                " x " +
                elementType +
                "]";
    }
}