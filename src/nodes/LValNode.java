package nodes;

import error.ErrorType;
import helper.Helper;
import llvm.*;
import llvm.instruction.AllocaInstr;
import llvm.instruction.GetEleInstr;
import llvm.instruction.InstrType;
import llvm.instruction.LoadInstr;
import llvm.type.ArrayType;
import llvm.type.PointerType;
import llvm.type.SimpleType;
import llvm.type.Type;
import symbol.*;
import parser.NodeType;

import java.util.ArrayList;

public class LValNode extends ExpNode {
    // LVal → Ident {'[' Exp ']'}

    public LValNode(int startNum, int endNum, NodeType nodeType, ArrayList<Node> children) {
        super(startNum, endNum, nodeType, children);
    }

    @Override
    public int calcu() { // 查符号表
        String token = ((TerminalNode) children.get(0)).getValue();
        Symbol symbol = SymbolManager.getManager().getSymbol(token);
        int x, y;
        switch (children.size()) {
            case 1: //普通变量
                return ((SimpleSymbol) symbol).getInitialVal();
            case 3, 4: //一维数组 a[2], a[2
                y = children.get(2).calcu();
                return ((ArraySymbol) symbol).getVal(0, y);
            case 5, 6, 7: //二维数组 a[2[3, a[2][3
                x = children.get(2).calcu();
                y = children.get(5).calcu();
                return ((ArraySymbol) symbol).getVal(x, y);
            default:
                return 0; // error
        }
    }

    public boolean isExistedConst() {
        String token = ((TerminalNode) children.get(0)).getValue();
        Symbol symbol = SymbolManager.getManager().getSymbol(token);

        return symbol != null && symbol.isConst();
    }

    @Override
    public void handleError() {
        for (Node node : children) {
            node.handleError();
        }

        SymbolManager symbolManager = SymbolManager.getManager();
        String token = ((TerminalNode) children.get(0)).getValue();
        if (symbolManager.getSymbol(token) == null) {
            Helper.storeError(this.getStartNum(), ErrorType.c);
        }
    }

    public FuncParamType eval() {
        SymbolManager symbolManager = SymbolManager.getManager();
        String token = ((TerminalNode) children.get(0)).getValue();
        Symbol symbol = symbolManager.getSymbol(token);

        if (children.size() == 1) {
            if (symbol instanceof SimpleSymbol) {
                return new FuncParamType(0);
            } else if (symbol instanceof ArraySymbol) {
                if (((ArraySymbol) symbol).isTwoDim()) {
                    return new FuncParamType(2, ((ArraySymbol) symbol).getD2());
                } else {
                    return new FuncParamType(1);
                }
            } else { // 其实是error
                return null;
            }
        } else if (children.size() == 4 || children.size() == 3) { // 考虑a[2这种情况
            if (symbol instanceof ArraySymbol) {
                if (((ArraySymbol) symbol).isTwoDim()) {
                    return new FuncParamType(1); // 二维数组取维数
                } else {
                    return new FuncParamType(0); // 一维数组取值
                }
            }
            return null; // error
        } else { // 只能是二维数组 && 取数
            if (symbol instanceof ArraySymbol) {
                if (((ArraySymbol) symbol).isTwoDim()) {
                    return new FuncParamType(0); // int
                }
            }
            return null; // error
        }
    }

    @Override
    public Value generate() {
        String token = ((TerminalNode) children.get(0)).getValue();
        Symbol symbol = SymbolManager.getManager().getSymbol(token);  // 只能取定义在其之前的值

        Value x, y; // offset
        if (symbol instanceof SimpleSymbol simpleSymbol) {
            if (simpleSymbol.isConst()) {
                return new Constant(simpleSymbol.getInitialVal());
            } else {
                String regName = InstrManager.getInstance().newReg();
                String pointer = symbol.getReg();
                return new LoadInstr(InstrType.LOAD, SimpleType.INT32, regName, pointer);
            }
        } else {
            ArraySymbol arraySymbol = (ArraySymbol) symbol;
            ArrayList<Value> offsets = new ArrayList<>();

            if (symbol.getValue() instanceof GlobalVar) {
                if (children.size() == 1) { // 这种只可能出现在函数调用中
                    Type gepType;
                    offsets.add(Constant.zero);
                    offsets.add(Constant.zero);
                    if (arraySymbol.isTwoDim()) {
                        gepType = new PointerType(new ArrayType(SimpleType.INT32, arraySymbol.getD2()));
                    } else {
                        gepType = new PointerType(SimpleType.INT32);
                    }
                    return new GetEleInstr(InstrType.GEP, gepType, InstrManager.getInstance().newReg(), symbol.getReg(), symbol.getValue().getType(), offsets);
                } else if (children.size() == 4) {
                    y = children.get(2).generate();
                    Type gepType; // 这个type现在是结果地址的reference type，toString的时候用的是addr的type和+*
                    offsets.add(Constant.zero);
                    offsets.add(y);
                    if (arraySymbol.isTwoDim()) {
                        offsets.add(Constant.zero);
                        gepType = new PointerType(SimpleType.INT32);
                        return new GetEleInstr(InstrType.GEP, gepType, InstrManager.getInstance().newReg(), symbol.getReg(), symbol.getValue().getType(), offsets);
                    } else {
                        GetEleInstr getEleInstr = new GetEleInstr(InstrType.GEP, SimpleType.INT32, InstrManager.getInstance().newReg(), symbol.getReg(), symbol.getValue().getType(), offsets);
                        return new LoadInstr(InstrType.LOAD, SimpleType.INT32, InstrManager.getInstance().newReg(), getEleInstr.getName());
                    }
                } else {
                    x = children.get(2).generate();
                    y = children.get(5).generate();
                    offsets.add(Constant.zero);
                    offsets.add(x);
                    offsets.add(y);
                    GetEleInstr getEleInstr = new GetEleInstr(InstrType.GEP, SimpleType.INT32, InstrManager.getInstance().newReg(), symbol.getReg(), symbol.getValue().getType(), offsets);
                    return new LoadInstr(InstrType.LOAD, SimpleType.INT32, InstrManager.getInstance().newReg(), getEleInstr.getName());
                }
            } else { // 局部变量或者是函数参数 他们的value都是alloca
                Type targetType = ((PointerType) symbol.getValue().getType()).getTarget();

                if (targetType instanceof ArrayType) { //局部变量
                    if (children.size() == 1) { // 取数组本身
                        offsets.add(Constant.zero);
                        offsets.add(Constant.zero);
                        Type gepType;
                        if (arraySymbol.isTwoDim()) {
                            gepType = new PointerType(new ArrayType(SimpleType.INT32, arraySymbol.getD2()));
                        } else {
                            gepType = new PointerType(SimpleType.INT32);
                        }
                        return new GetEleInstr(InstrType.GEP, gepType, InstrManager.getInstance().newReg(), symbol.getReg(), targetType,offsets);
                    } else if (children.size() == 4) {
                        y = children.get(2).generate();
                        offsets.add(Constant.zero);
                        if (((ArrayType) targetType).isTwoDim()) {
                            offsets.add(y);
                            offsets.add(Constant.zero); // bug****
                            Type gepType = new PointerType(SimpleType.INT32);
                            return new GetEleInstr(InstrType.GEP, gepType, InstrManager.getInstance().newReg(), symbol.getReg(), targetType, offsets);
                        } else {
                            offsets.add(y);
                            GetEleInstr getEleInstr = new GetEleInstr(InstrType.GEP, SimpleType.INT32, InstrManager.getInstance().newReg(), symbol.getReg(), targetType, offsets);
                            return new LoadInstr(InstrType.LOAD, SimpleType.INT32, InstrManager.getInstance().newReg(), getEleInstr.getName());
                        }
                    } else {
                        x = children.get(2).generate();
                        y = children.get(5).generate();
                        offsets.add(Constant.zero);
                        offsets.add(x);
                        offsets.add(y);
                        GetEleInstr getEleInstr = new GetEleInstr(InstrType.GEP, SimpleType.INT32, InstrManager.getInstance().newReg(), symbol.getReg(), targetType,offsets);
                        return new LoadInstr(InstrType.LOAD, SimpleType.INT32, InstrManager.getInstance().newReg(), getEleInstr.getName());
                    }
                } else { // 函数参数
                    LoadInstr loadInstr = new LoadInstr(InstrType.LOAD, targetType, InstrManager.getInstance().newReg(), symbol.getReg());
                    Type reference = ((PointerType) targetType).getTarget();
                    if (children.size() == 1) { // 如果传进来的是a[]i32* 或者b[][2] [2 x i32]*， 使用a或者b
                        return loadInstr;
                    } else if (children.size() == 4) {
                        offsets.add(children.get(2).generate());
                        if (arraySymbol.isTwoDim()) {
                            offsets.add(Constant.zero);// bug todo??
                        }
                        if (reference instanceof SimpleType) {
                            GetEleInstr getEleInstr = new GetEleInstr(InstrType.GEP, SimpleType.INT32, InstrManager.getInstance().newReg(), loadInstr.getName(), reference, offsets);
                            return new LoadInstr(InstrType.LOAD, SimpleType.INT32, InstrManager.getInstance().newReg(), getEleInstr.getName());
                        } else {
                            Type gepType = new PointerType(SimpleType.INT32);
                            return new GetEleInstr(InstrType.GEP, gepType, InstrManager.getInstance().newReg(), loadInstr.getName(), reference, offsets);
                        }
                    } else {
                        offsets.add(children.get(2).generate());
                        offsets.add(children.get(5).generate());
                        GetEleInstr getEleInstr = new GetEleInstr(InstrType.GEP, SimpleType.INT32, InstrManager.getInstance().newReg(), loadInstr.getName(), reference, offsets);
                        return new LoadInstr(InstrType.LOAD, SimpleType.INT32, InstrManager.getInstance().newReg(), getEleInstr.getName());
                    }
                }
            }
        }
    }

    public String getLVal() {
        String token = ((TerminalNode) children.get(0)).getValue();
        Symbol symbol = SymbolManager.getManager().getSymbol(token);

        ArrayList<Value> offsets = new ArrayList<>();
        if (symbol instanceof SimpleSymbol) {
            return symbol.getValue().getName();
        } else {
            Type baseType;
            if (symbol.getValue() instanceof GlobalVar) {
                baseType = symbol.getValue().getType();
            } else {
                baseType = ((PointerType) symbol.getValue().getType()).getTarget();
            }
            Value baseAddr = symbol.getValue();

            if (baseType instanceof ArrayType) {
                offsets.add(Constant.zero);
            } else {
                baseAddr = new LoadInstr(InstrType.LOAD, baseType, InstrManager.getInstance().newReg(), symbol.getReg());
                baseType = ((PointerType) baseType).getTarget();
            }

            if (children.size() == 4) { // 只可能一维数组
                offsets.add(children.get(2).generate());
                Instr getEleInstr = new GetEleInstr(InstrType.GEP, SimpleType.INT32,
                        InstrManager.getInstance().newReg(), baseAddr.getName(), baseType, offsets);
                return getEleInstr.getName();
            } else { // 二维数组
                offsets.add(children.get(2).generate());
                offsets.add(children.get(5).generate());
                Instr getEleInstr = new GetEleInstr(InstrType.GEP, SimpleType.INT32,
                        InstrManager.getInstance().newReg(), baseAddr.getName(), baseType, offsets);
                return getEleInstr.getName();
            }
        }
    }
}

