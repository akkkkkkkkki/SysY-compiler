package nodes;

import error.ErrorType;
import helper.Helper;
import llvm.*;
import llvm.instruction.*;
import llvm.type.ArrayType;
import llvm.type.SimpleType;
import llvm.type.Type;
import symbol.*;
import parser.NodeType;

import java.util.ArrayList;

public class ConstDefNode extends Node {
    // ConstDef → Ident { '[' ConstExp ']' } '=' ConstInitVal
    private Symbol symbol;
    private boolean isGlobal;
    private boolean assign = false;

    public Symbol getSymbol() {
        return symbol;
    }

    public ConstDefNode(int startNum, int endNum, NodeType nodeType, ArrayList<Node> children) {
        super(startNum, endNum, nodeType, children);
    }

    public Symbol createSymbol(int tableId, boolean isGlobal) { //const 一定有初值
        this.isGlobal = isGlobal;
        String token = ((TerminalNode) children.get(0)).getValue(); // ident的string
        ArrayList<Integer> dims = new ArrayList<>();
        ArrayList<Integer> vals = new ArrayList<>();
        for (Node node : children) {
            if (node instanceof ConstExpNode) {
                dims.add(node.calcu());
            } else if (node instanceof ConstInitValNode) {
                this.assign = true;
                if (dims.isEmpty()) { // 非数组变量，初值只有一个const exp表达式
                    vals.add(node.calcu());
                } else if (dims.size() == 1) { //一维数组
                    vals = ((ConstInitValNode) node).calcu(vals, dims.get(0));
                } else {
                    vals = ((ConstInitValNode) node).calcu(vals, dims.get(0), dims.get(1));
                }
            }
        }
        String reg;
        if (isGlobal) {
            reg = SymbolManager.getManager().getReg(token);
        } else {
            reg = SymbolManager.getManager().getReg();
        }
        symbol = switch (dims.size()) {
            case 0 -> new SimpleSymbol(token, SymbolType.CONST, tableId, vals, reg);
            case 1 -> new ArraySymbol(token, SymbolType.CONST, tableId, dims.get(0), vals, reg);
            case 2 -> new ArraySymbol(token, SymbolType.CONST, tableId, dims.get(0), dims.get(1), vals, reg);
            default -> null;
        };
//        Type llvmType = switch (dims.size()) {
//            case 0 -> SimpleType.INT32;
//            case 1 -> new ArrayType(SimpleType.INT32, dims.get(0));
//            case 2 -> new ArrayType(SimpleType.INT32, dims.get(0) * dims.get(1));
//            default -> null;
//        };
//        if (isGlobal) {
//            GlobalVar value = new GlobalVar(symbol.getReg(), llvmType, true);
//            InstrManager.getInstance().insertGlobalVar(value);
//        } else {
//            String regName = InstrManager.getInstance().newReg();
//            Instr allocaInstr = new AllocaInstr(InstrType.ALLOCA, regName);
//            symbol.setValue(allocaInstr);
//        }
        return symbol;
    }

    @Override
    public void handleError() {
        SymbolManager symbolManager = SymbolManager.getManager();
        Symbol symbol = createSymbol(symbolManager.getCurId(), symbolManager.isGlobal());

        for (Node node : children) { // 先递归检查子节点的错误
            node.handleError();
        }
        if (!symbolManager.insertSymbol(symbol)) { // error b 名字重定义
            Helper.storeError(children.get(0).getStartNum(), ErrorType.b); // 选用了ident所在的行数
        }
    }

    @Override
    public Value generate() {
        SymbolManager symbolManager = SymbolManager.getManager();
        symbolManager.insertSymbol(symbol);

        if (isGlobal) { // 全局const
            if (symbol instanceof SimpleSymbol) {
                Type llvmType = SimpleType.INT32;
                GlobalVar value = new GlobalVar(symbol.getReg(), llvmType, true, ((SimpleSymbol) symbol).getInitialVal());
                symbol.setValue(value);
            } else {
                if (((ArraySymbol) symbol).isTwoDim()) { // 二维数组
                    Type elementType = new ArrayType(SimpleType.INT32, ((ArraySymbol) symbol).getD2());
                    Type llvmType = new ArrayType(elementType, ((ArraySymbol) symbol).getD1());
                    GlobalVar value = new GlobalVar(symbol.getReg(), llvmType, true, ((ArraySymbol) symbol).getInitialVal());
                    symbol.setValue(value);
                } else {
                    Type llvmType = new ArrayType(SimpleType.INT32, ((ArraySymbol) symbol).getD1());
                    GlobalVar value = new GlobalVar(symbol.getReg(), llvmType, true, ((ArraySymbol) symbol).getInitialVal());
                    symbol.setValue(value);
                }
            }
        } else { // 局部const 仍然有初值
            if (symbol instanceof SimpleSymbol) {
//                String regName = InstrManager.getInstance().newReg();
                String regName = symbol.getReg(); // 在代码生成二改的**
                Instr allocaInstr = new AllocaInstr(InstrType.ALLOCA, SimpleType.INT32, regName);
                symbol.setValue(allocaInstr); // 先生成分配栈上空间的指令

                if (assign) { // 计算初值  其实const一定是有初值的
                    ConstInitValNode constInitValNode = (ConstInitValNode) children.get(children.size() - 1);
                    Value initial = constInitValNode.getSingleValue();
                    StoreInstr storeInstr = new StoreInstr(InstrType.STORE, SimpleType.INT32, regName, initial);
                }
            } else {
                String regName = symbol.getReg();
                Type llvmType;
                if (((ArraySymbol) symbol).isTwoDim()) { // 二维数组
                    Type elementType = new ArrayType(SimpleType.INT32, ((ArraySymbol) symbol).getD2());
                    llvmType = new ArrayType(elementType, ((ArraySymbol) symbol).getD1());
                } else { // 一维数组
                    llvmType = new ArrayType(SimpleType.INT32, ((ArraySymbol) symbol).getD1());
                }
                Instr allocaInstr = new AllocaInstr(InstrType.ALLOCA, llvmType, regName); // 这个地方得是数组类型不是元素类型啊
                symbol.setValue(allocaInstr);

                if (assign) {
                    for (int i = 0; i < ((ArraySymbol) symbol).getInitialVal().size(); i++) {
                        Instr getEleInstr;
                        ArrayList<Value> offsets = new ArrayList<>();
                        if (((ArraySymbol) symbol).isTwoDim()) {
                            int dim = ((ArraySymbol) symbol).getD2();
                            offsets.add(Constant.zero);
                            offsets.add(new Constant(i / dim));
                            offsets.add(new Constant((i % dim)));
                            getEleInstr = new GetEleInstr(InstrType.GEP, SimpleType.INT32, InstrManager.getInstance().newReg(), symbol.getReg(), llvmType, offsets);
                        } else {
                            offsets.add(Constant.zero);
                            offsets.add(new Constant(i));
                            getEleInstr = new GetEleInstr(InstrType.GEP, SimpleType.INT32, InstrManager.getInstance().newReg(), symbol.getReg(), llvmType, offsets);
                        }
                        Instr storeInstr = new StoreInstr(InstrType.STORE, SimpleType.INT32,
                                getEleInstr.getName(),
                                new Constant(((ArraySymbol) symbol).getInitialVal().get(i)));
                    }
                }
            }
        }
        return null;
    }
}
