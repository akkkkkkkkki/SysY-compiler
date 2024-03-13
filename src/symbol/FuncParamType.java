package symbol;

import static java.lang.Math.max;

public class FuncParamType {
    private int type; //0 simple 1 一维  2 二维 3 void 函数
    private int dim;

    public FuncParamType(int type, int dim) { // a[][2]
        this.type = type;
        this.dim = dim;
    }

    public FuncParamType(int type) { // a  a[]
        this.type = type;
        this.dim = -1;
    }

    public int getType() {
        return type;
    }

    public int getDim() {
        return dim;
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof FuncParamType) {
            return type == ((FuncParamType) other).getType()
                    && dim == ((FuncParamType) other).getDim();
        }
        return false;
    }

    public FuncParamType merge(FuncParamType other) { // 这种类型之间做运算
        if (other == null) {
            return this;
        }
        int type = max(getType(), other.getType());
        int dim = max(getDim(), other.getDim());
        return new FuncParamType(type, dim);
    }
}
