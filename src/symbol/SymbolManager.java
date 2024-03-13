package symbol;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Stack;

public class SymbolManager {
    private static final SymbolManager MANAGER = new SymbolManager(); // 单例模式
    private int curId; // 当前所在的符号表id
    private Stack<SymbolTable> tableStack; // 当前所在的 table链(执行
    private HashMap<String, SymbolTable> tables; // 所有解析的table, 对于函数，可以用id，也可以用函数名
    private int loop; // 记录循环的层数
    private boolean isGlobal;
    private int maxId;
    private int lastPop; //  感觉没用
    private ReturnType curType;
    private int rParasNum;
    private boolean isRParas;
    private SymbolTable rootTable;
    private ArrayList<String> nameList;

    public SymbolManager() {
        this.curId = 0; // 一开始 id为 0的时候，是全局变量
        this.tableStack = new Stack<>();
        this.tables = new HashMap<>();
        this.loop = 0;
        this.maxId = 1;
        this.lastPop = 0;
        this.isGlobal = true;
        this.curType = ReturnType.VOID;
        this.isRParas = false;

        SymbolTable symbolTable = new SymbolTable(0, -1);
        symbolTable.setNextId(0);
        rootTable = symbolTable;
        tableStack.push(symbolTable);
        tables.put("0", symbolTable); // 全局部分的符号表
        this.nameList = new ArrayList<>();
        nameList.add("0");
    }

    public static SymbolManager getManager() {
        return MANAGER;
    }

    public void reset() {
        this.curId = 0; // 一开始 id为 0的时候，是全局变量
        this.tableStack.clear();
        this.tables.clear();
//        this.loop = 0;
        this.maxId = 1;
        this.lastPop = 0;
        this.isGlobal = true;
        this.curType = ReturnType.VOID;
        this.isRParas = false;

        SymbolTable symbolTable = new SymbolTable(0, -1);
        symbolTable.setNextId(0);
        rootTable = symbolTable;
        tableStack.push(symbolTable);
        tables.put("0", symbolTable); // 全局部分的符号表
//        this.nameList = new ArrayList<>();
//        nameList.add("0");
    }

    public int getCurId() {
        return curId;
    }

    public boolean isGlobal() {
        return isGlobal;
    }

    public void enterMain() {
        isGlobal = false;
        curType = ReturnType.INT;
    }

    public SymbolTable getRootTable() {
        return rootTable;
    }

    public HashMap<String, SymbolTable> getTables() {
        return tables;
    }

    public ArrayList<String> getNameList() {
        return nameList;
    }

    public SymbolTable createTable(String name) { // 对函数 既可以用函数名，又可以用id
        SymbolTable newTable = new SymbolTable(maxId, curId);
        tables.get(String.valueOf(lastPop)).setNextId(curId);
        tableStack.peek().addChildren(newTable);
        tableStack.push(newTable);
        tables.put(String.valueOf(maxId), newTable);
        tables.put(name, newTable);
        curId = maxId;
        maxId++;
        nameList.add(String.valueOf(curId));
        return newTable;
    }

    public void createTable() { // 对无名的{}
        SymbolTable newTable = new SymbolTable(maxId, curId);
        tables.get(String.valueOf(lastPop)).setNextId(curId);
        tableStack.peek().addChildren(newTable);
        tableStack.push(newTable);
        tables.put(String.valueOf(maxId), newTable);
        curId = maxId;
        maxId++;
        nameList.add(String.valueOf(curId));
    }

    public void leaveBlock() {
        lastPop = tableStack.pop().getId();
        curId = tableStack.peek().getId();
        nameList.add(String.valueOf(curId));
    }

    public void enterLoop() {
        loop++;
    }

    public void leaveLoop() {
        loop--;
    }

    public boolean isLooping() {
        return loop != 0;
    }

    public void setReturnType(ReturnType returnType) { // 当前block内是否存在有返回值的return语句
        this.curType = returnType;
    }

    public boolean checkReturnType(ReturnType returnType) { // 这里的type是按照函数声明的时候来的，到时候检查对应return语句
        return this.curType == returnType;
    }

    public void setRParasNum(int num) {
        this.rParasNum = num;
    }

    public void enterFParas() {
        this.isGlobal = false;
    }

    public void enterRParas() {
        this.isRParas = true;
    }

    public void leaveRParas() {
        this.isRParas = false;
    }

    public boolean insertSymbol(Symbol symbol) {
        if (tableStack.peek().addSymbol(symbol)) {
            return true;
        } else {
            return false;
        }
    }

    public Symbol getSymbol(String token) {
        Symbol symbol = null;
        Stack<SymbolTable> tmp = (Stack<SymbolTable>) tableStack.clone();

        while (!tmp.isEmpty()) {
            symbol = tmp.pop().getSymbol(token);
            if (symbol != null) {
                return symbol;
            }
        }
        return symbol;
    }

    public String getReg() {
        SymbolTable symbolTable = tableStack.peek();
        return "%var" + symbolTable.getId() + "_" + symbolTable.size() ;
    }

    public String getReg(String token) {
        return "@" + token;
    }

    public SymbolTable getCurTable() {
        return tableStack.peek();
    }
}
