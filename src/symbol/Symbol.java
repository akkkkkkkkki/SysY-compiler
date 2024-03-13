package symbol;

import llvm.Value;

public class Symbol {
    private String token;
    private SymbolType symbolType; // 常量，变量还是函数
    private int table;
    private String reg; // 寄存器的名字
    private Value value;

    public Symbol(String token, SymbolType symbolType, int table, String reg) {
        this.token = token;
        this.symbolType = symbolType;
        this.table = table;
        this.reg = reg;
    }

    public String getToken() {
        return token;
    }

    public String getReg() {
        return reg;
    }

    public boolean isConst() {
        return symbolType == SymbolType.CONST;
    }

    public void setValue(Value value) {
        this.value = value;
    }

    public Value getValue() {
        return value;
    }

    public void setReg(String newReg) {
        this.reg = newReg; // 给函数参数用的
    }
}
