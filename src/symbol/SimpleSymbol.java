package symbol;

import llvm.Value;

import java.util.ArrayList;

public class SimpleSymbol extends Symbol{
    private int initialVal;

    public SimpleSymbol(String token, SymbolType symbolType, int table, ArrayList<Integer> vals, String reg) {
        super(token, symbolType, table, reg);
        if (vals == null || vals.isEmpty()) {
            initialVal = -1; // 如果是没有初始化的局部变量，值置-1
        } else {
            initialVal = vals.get(0);
        }
    }

    public int getInitialVal() {
        return initialVal;
    }
}
