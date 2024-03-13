package symbol;

import llvm.InstrManager;

import java.util.ArrayList;
import java.util.HashMap;

public class SymbolTable {
    private int id;
    private int fatherId; //结构里面的table嵌套
    private int nextId; // 树结构中的下一个兄弟节点
    private HashMap<String, Symbol> symbols;
    private ArrayList<SymbolTable> children;
    private ArrayList<Symbol> sortedSymbols;

    public SymbolTable(int id, int fatherId) {
        this.id = id;
        this.fatherId = fatherId;
        this.symbols = new HashMap<>();
        this.children = new ArrayList<>();
        this.nextId = -1;
        this.sortedSymbols = new ArrayList<>();
    }

    public int getId() {
        return id;
    }

    public int getFatherId() {
        return fatherId;
    }

    public int getNextId() {
        return nextId;
    }

    public int size() {
        return symbols.size();
    }

    public void setNextId(int nextId) {
        this.nextId = nextId;
    }

    public void addChildren(SymbolTable symbolTable) {
        children.add(symbolTable);
    }

    public boolean addSymbol(Symbol symbol) { // 只需要在当前scope内没有定义就行
        if (symbols.get(symbol.getToken()) != null) {
            return false;
        }
        symbols.put(symbol.getToken(), symbol);
        sortedSymbols.add(symbol);
        return true;
    }

    public Symbol getSymbol(String token) {
        return symbols.get(token);
    }

    public String getReg(String token) {
        return symbols.get(token).getReg();
    }

    public void check(int depth) { // 检查树形结构 和 reg分配用的
        for (int i = 0; i < depth; i++) {
            System.out.print("-");
        }
        System.out.println("enter" + id);
        for (String key: symbols.keySet()) {
            System.out.print(symbols.get(key).getToken() + "-" + symbols.get(key).getReg() + " ");
        }
        for (SymbolTable symbolTable: children) {
            symbolTable.check(depth + 1);
        }
        for (int i = 0; i < depth; i++) {
            System.out.print("-");
        }
        System.out.println("leave " + id);
    }
}
