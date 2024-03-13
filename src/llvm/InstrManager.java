package llvm;

import helper.Helper;
import llvm.instruction.InstrType;
import llvm.instruction.JumpInstr;
import llvm.instruction.ReturnInstr;
import symbol.SymbolManager;
import symbol.SymbolTable;

import java.util.HashMap;
import java.util.Stack;

public class InstrManager {
    private static final InstrManager MANAGER = new InstrManager();
    private int globalVarId;
    private int funcId;
    private int bbId;
    private int instrId;
    private SymbolTable curTable;
    private HashMap<String, SymbolTable> tableMap;
    private HashMap<String, Integer> regSize;
    private Function curFunc;
    private Module curModule;
    private BasicBlock curBB;
    private Stack<BasicBlock> curFollows;
    private Stack<BasicBlock> curIncre;
    private boolean hasRet;


    public InstrManager() {
        this.globalVarId = 0;
        this.bbId = 0;
        this.instrId = 0;
        this.funcId = 0;
        this.tableMap = SymbolManager.getManager().getTables();

        this.regSize = new HashMap<>();
        setRegSize();
        curFunc = null;
        curBB = null;
        curModule = new Module();

        curIncre = new Stack<>();
        curFollows = new Stack<>();
    }

    public static InstrManager getInstance() {
        return MANAGER;
    }

    public void setCurFunc(Function function) {
        curFunc = function;
        hasRet = false;
    }

    public void setCurBB(BasicBlock basicBlock) {
        curBB = basicBlock;
    }

    public void setHasRet() {
        hasRet = true;
    }

    public void enterLoop(BasicBlock follow, BasicBlock check) {
        curFollows.push(follow);
        curIncre.push(check);
    }

    public void leaveLoop() {
        curFollows.pop();
        curIncre.pop();
    }

    public Function getCurFunc() {
        return curFunc;
    }

    public BasicBlock getCurBB() {
        return curBB;
    }

    public BasicBlock getCurFollow() {
        return curFollows.peek();
    }

    public BasicBlock getCurIncre() {
        return curIncre.peek();
    }

    public boolean isHasRet() {
        return hasRet;
    }

    public void insertGlobalVar(GlobalVar globalVar) {
        curModule.addGlobalVar(globalVar);
    }

    public void insertFunction(Function function) {
        curModule.addFunction(function);
    }

    public void insertBasicBlock(BasicBlock basicBlock) {
        curFunc.addBB(basicBlock);
    }

    public Module getCurModule() {
        return curModule;
    }

    public void insertInstr(Instr instr) { // 将指令保存在当前func下 >> bb下
        curBB.addInstr(instr);
    }

    private void setRegSize() {
        for (String key : tableMap.keySet()) {
            regSize.put(key, 0);
        }
    }

    public String newReg() { // 返回当前table作用域下的新的寄存器编号
        SymbolTable table = SymbolManager.getManager().getCurTable();
        int size = 0;
        if (regSize.get(String.valueOf(table.getId())) == null) {
            size = 1;
            regSize.put(String.valueOf(table.getId()), 1);
        } else {
            size = regSize.get(String.valueOf(table.getId())) + 1;
            regSize.put(String.valueOf(table.getId()), regSize.get(String.valueOf(table.getId())) + 1);
        }
        return "%tmp" + table.getId() + "_" + size;
    }

    public String newReg(int placeHolder) { // 返回当前table作用域下的新的寄存器编号
        SymbolTable table = SymbolManager.getManager().getCurTable();
        int size = table.size();
        regSize.put(String.valueOf(table.getId()), size + 1);
        return "%var" + table.getId() + "_" + size;
    }

    public String newBB(String tip) {
        SymbolTable table = SymbolManager.getManager().getCurTable();
        bbId++;
        return tip + bbId;
    }

    public void printInstr() {
        Helper.printInstr(curModule.toString());
    }
}
