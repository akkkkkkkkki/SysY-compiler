package llvm;

import llvm.type.Type;

public class Value {
    protected String name; // reg的标号 (对于alu，保存计算结果的寄存器
    private Type type; // llvm的type

    public Value(String name, Type type) {
        this.name = name;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public Type getType() {
        return type;
    }
}
