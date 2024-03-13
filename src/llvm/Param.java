package llvm;

import llvm.type.Type;

public class Param extends Value {
    private Function function;

    public Param(String name, Type type, Function function) {
        super(name, type);
        this.function = function;
        function.addParam(this);
    }

    public void setName(String newName) {
        this.name = newName;
    }

    public String toString() {
        return getType() + " " + getName();
    }
}
