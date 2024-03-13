package symbol;

import java.util.ArrayList;

public class ArraySymbol extends Symbol { // 数组变量\常量，包括函数形参
    private int d1; //一维
    private int d2;
    private ArrayList<Integer> initialVal; //用一维数组来存二维数组


    public ArraySymbol(String token, SymbolType symbolType, int table, int d1, ArrayList<Integer> vals, String reg) {
        super(token, symbolType, table, reg);
        this.d1 = d1;
        this.d2 = -1;
        this.initialVal = vals;
    }

    public ArraySymbol(String token, SymbolType symbolType, int table, int d1, int d2, ArrayList<Integer> vals, String reg) {
        super(token, symbolType, table, reg);
        this.d1 = d1;
        this.d2 = d2;
        this.initialVal = vals;
    }

    public boolean isTwoDim() {
        return d2 != -1;
    }

    public int getD2() {
        return d2;
    }

    public int getD1() {
        return d1;
    }

    public int getVal(int x, int y) { // 要不要判断越界
        if (initialVal == null || initialVal.isEmpty()) { // 对全局变量没有初始化的情况
            return 0;
        }
        return initialVal.get(x * d1 + y);
    }

    public ArrayList<Integer> getInitialVal() {
        return initialVal;
    }
}
