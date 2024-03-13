package nodes;

import error.ErrorType;
import helper.Helper;
import llvm.*;
import llvm.instruction.AllocaInstr;
import llvm.instruction.GetEleInstr;
import llvm.instruction.InstrType;
import llvm.instruction.StoreInstr;
import llvm.type.ArrayType;
import llvm.type.Type;
import llvm.type.SimpleType;
import parser.NodeType;
import symbol.*;

import java.util.ArrayList;

public class VarDefNode extends Node {
    // VarDef → Ident { '[' ConstExp ']' } | Ident { '[' ConstExp ']' } '=' InitVal
    private Symbol symbol;
    private boolean isGlobal;
    private boolean assign;

    public Symbol getSymbol() {
        return symbol;
    }

    public VarDefNode(int startNum, int endNum, NodeType nodeType, ArrayList<Node> children) {
        super(startNum, endNum, nodeType, children);
    }

    public Symbol createSymbol(int tableId, boolean isGlobal) {
        this.isGlobal = isGlobal;
        String token = ((TerminalNode) children.get(0)).getValue(); // ident的string
        ArrayList<Integer> dims = new ArrayList<>();
        ArrayList<Integer> vals = new ArrayList<>();
        this.assign = false;
        for (Node node : children) {
            if (node instanceof ConstExpNode) {
                dims.add(node.calcu());
            } else if (node instanceof InitValNode) {
                assign = true;
                if (isGlobal) {
                    if (dims.isEmpty()) { // 非数组变量，初值只有一个const exp表达式
                        vals.add(node.calcu());
                    } else if (dims.size() == 1) { //一维数组
                        vals = ((InitValNode) node).calcu(vals, dims.get(0));
                    } else {
                        vals = ((InitValNode) node).calcu(vals, dims.get(0), dims.get(1));
                    }
                }
            }
        }
        // 对于非const且未初始化的变量没有必要存入val，因为到时候也是从地址里面去找
//        if (isGlobal && !assign) { // 如果是全局变量>>置0
//            switch (dims.size()) {
//                case 0:
//                    vals.add(0);
//                    break;
//                case 1:
//                    for (int i = 0; i < dims.get(0); i++) {
//                        vals.add(0);
//                    }
//                    break;
//                case 2:
//                    for (int i = 0; i < dims.get(0) * dims.get(1); i++) {
//                        vals.add(0);
//                    }
//                    break;
//            }
//        }
        if (isGlobal && !assign && dims.isEmpty()) {
            vals.add(0);
        }
        String reg;
        if (isGlobal) {
            reg = SymbolManager.getManager().getReg(token);
        } else {
            reg = SymbolManager.getManager().getReg();
        }
        symbol = switch (dims.size()) {
            case 0 -> new SimpleSymbol(token, SymbolType.VAR, tableId, vals, reg);
            case 1 -> new ArraySymbol(token, SymbolType.VAR, tableId, dims.get(0), vals, reg);
            case 2 -> new ArraySymbol(token, SymbolType.VAR, tableId, dims.get(0), dims.get(1), vals, reg);
            default -> null;
        };
        return symbol;
    }

    @Override
    public void handleError() {
        SymbolManager symbolManager = SymbolManager.getManager();
        Symbol symbol = createSymbol(symbolManager.getCurId(), symbolManager.isGlobal());

        for (Node node : children) {
            node.handleError();
        }
        if (!symbolManager.insertSymbol(symbol)) { // error b 变量重定义
            Helper.storeError(children.get(0).getStartNum(), ErrorType.b);
        }
    }

    @Override
    public Value generate() {
        SymbolManager.getManager().insertSymbol(symbol);

        if (isGlobal) { // 全局变量
            if (symbol instanceof SimpleSymbol) {
                Type llvmType = SimpleType.INT32;
                GlobalVar value = new GlobalVar(symbol.getReg(), llvmType, false, ((SimpleSymbol) symbol).getInitialVal());
                // 我突然意识到这个代码奏效是因为全局变量assign的时候使用的要么是constant要么是initial值，没有任何值改变的操作
                symbol.setValue(value);
            } else {
                Type llvmType;
                if (((ArraySymbol) symbol).isTwoDim()) {
                    Type elementType = new ArrayType(SimpleType.INT32, ((ArraySymbol) symbol).getD2());
                    llvmType = new ArrayType(elementType, ((ArraySymbol) symbol).getD1());
                } else {
                    llvmType = new ArrayType(SimpleType.INT32, ((ArraySymbol) symbol).getD1());
                }

                GlobalVar value;
                if (assign) {
                    value = new GlobalVar(symbol.getReg(), llvmType, false, ((ArraySymbol) symbol).getInitialVal());
                    // 所以这个应该是可以的？
                } else {
                    value = new GlobalVar(symbol.getReg(), llvmType, false, null);
                }
                symbol.setValue(value);
            }
        } else { // 局部变量
            if (symbol instanceof SimpleSymbol) {
//                String regName = InstrManager.getInstance().newReg();
                String regName = symbol.getReg();
                Instr allocaInstr = new AllocaInstr(InstrType.ALLOCA, SimpleType.INT32, regName);
                symbol.setValue(allocaInstr); // 先生成分配栈上空间的指令

                if (assign) { // 计算初值
                    InitValNode initValNode = (InitValNode) children.get(children.size() - 1);
                    Value initial = initValNode.getSingleValue();
                    StoreInstr storeInstr = new StoreInstr(InstrType.STORE, SimpleType.INT32, regName, initial);
                }
            } else { // 局部数组 需要parse数组exp了
                String regName = symbol.getReg();
                Type llvmType;
                if (((ArraySymbol) symbol).isTwoDim()) {
                    Type elementType = new ArrayType(SimpleType.INT32, ((ArraySymbol) symbol).getD2());
                    llvmType = new ArrayType(elementType, ((ArraySymbol) symbol).getD1());
                } else {
                    llvmType = new ArrayType(SimpleType.INT32, ((ArraySymbol) symbol).getD1());
                }

                Instr allocaInstr = new AllocaInstr(InstrType.ALLOCA, llvmType, regName);
                symbol.setValue(allocaInstr);

                if (assign) {
                    InitValNode initValNode = (InitValNode) children.get(children.size() - 1);
                    ArrayList<Value> vals = initValNode.getArrayValue();
                    for (int i = 0; i < vals.size(); i++) { // todo 好像可以一个个往下推，目前是每次都从base开始
                        // 先计算出对应位置的地址赋给寄存器
                        Instr getEleInstr;
                        ArrayList<Value> offsets = new ArrayList<>();
                        if (((ArraySymbol) symbol).isTwoDim()) {
                            int dim = ((ArraySymbol) symbol).getD2();
                            offsets.add(Constant.zero);
                            offsets.add(new Constant(i / dim));
                            offsets.add(new Constant(i % dim));
                            getEleInstr = new GetEleInstr(InstrType.GEP, SimpleType.INT32, InstrManager.getInstance().newReg(), symbol.getReg(), llvmType, offsets);
                        } else {
                            offsets.add(Constant.zero);
                            offsets.add(new Constant(i));
                            getEleInstr = new GetEleInstr(InstrType.GEP, SimpleType.INT32, InstrManager.getInstance().newReg(), symbol.getReg(), llvmType, offsets);
                        }
                        StoreInstr storeInstr = new StoreInstr(InstrType.STORE, SimpleType.INT32,
                                getEleInstr.getName(),
                                vals.get(i));
                    }
                }
            }
        }
        return null;
    }
}