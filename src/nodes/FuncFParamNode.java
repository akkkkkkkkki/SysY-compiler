package nodes;

import error.ErrorType;
import helper.Helper;
import llvm.InstrManager;
import llvm.Param;
import llvm.Value;
import llvm.instruction.AllocaInstr;
import llvm.instruction.InstrType;
import llvm.instruction.StoreInstr;
import llvm.type.ArrayType;
import llvm.type.PointerType;
import llvm.type.SimpleType;
import llvm.type.Type;
import parser.NodeType;
import symbol.*;

import java.util.ArrayList;

public class FuncFParamNode extends Node {
    // FuncFParam → BType Ident ['[' ']' { '[' ConstExp ']' }]
    private Symbol symbol;
    private FuncParamType funcParamType;

    public Symbol getSymbol() {
        return symbol;
    }

    public FuncFParamNode(int startNum, int endNum, NodeType nodeType, ArrayList<Node> children) {
        super(startNum, endNum, nodeType, children);
    }

    public FuncParamType calcu(int placeHolder) {
        if (children.size() == 2) {
            return new FuncParamType(0);
        }
        for (Node node: children) {
            if (node instanceof ConstExpNode) { // 二维数组形参
                return new FuncParamType(2, node.calcu());
            }
        }
        return new FuncParamType(1); //一维数组
    }

    public Symbol createSymbol(int tableId) {
        String token = ((TerminalNode) children.get(1)).getValue(); // ident的string

        String reg = SymbolManager.getManager().getReg();
        symbol =  switch (children.size()) {
            case 2 -> new SimpleSymbol(token, SymbolType.VAR, tableId, null, reg); // 形参的val用-1占位
            case 3, 4 -> new ArraySymbol(token, SymbolType.VAR, tableId, 0, null, reg); // a[]
            case 5, 6, 7 -> {
                int dim = -1;
                for (Node node: children) {
                    if (node instanceof ConstExpNode) {
                        dim = node.calcu();
                        break;
                    }
                }
                yield new ArraySymbol(token, SymbolType.VAR, tableId, 0, dim, null, reg); // a[][2]
            }
            default -> null;
        };
        funcParamType = switch (children.size()) {
            case 2 -> new FuncParamType(0);
            case 3, 4 -> new FuncParamType(1);
            case 5, 6, 7 -> {
                int dim = -1;
                for (Node node: children) {
                    if (node instanceof ConstExpNode) {
                        dim = node.calcu();
                        break;
                    }
                }
                yield new FuncParamType(2, dim);
            }
            default -> null;
        };
        return symbol;
    }

    @Override
    public void handleError() { //查询符号表并插入
        SymbolManager symbolManager = SymbolManager.getManager();
        Symbol symbol = createSymbol(symbolManager.getCurId());

        for (Node node: children) {
            node.handleError();
        }

        if (!symbolManager.insertSymbol(symbol)) { // error b 函数形参重复声明
            Helper.storeError(children.get(1).getStartNum(), ErrorType.b);
        }
    }

    @Override
    public Value generate() {
        SymbolManager.getManager().insertSymbol(symbol);

        Type llvmType = null;
        switch (funcParamType.getType()) {
            case 0:
                llvmType = SimpleType.INT32;
                break;
            case 1:
                llvmType = new PointerType(SimpleType.INT32);
                break;
            case 2:
                llvmType = new PointerType(new ArrayType(SimpleType.INT32, funcParamType.getDim()));
                break;
            default:
        }
        String regName = symbol.getReg();
        Param param = new Param(regName, llvmType, InstrManager.getInstance().getCurFunc());
        // 新开栈空间store
        String tmpReg = InstrManager.getInstance().newReg(); // 已经是新的符号表了
        AllocaInstr allocaInstr = new AllocaInstr(InstrType.ALLOCA, llvmType, tmpReg);
        symbol.setValue(allocaInstr);
        symbol.setReg(tmpReg);
        StoreInstr storeInstr = new StoreInstr(InstrType.STORE, llvmType, tmpReg, param);

        super.generate();
        return null;
    }
}
