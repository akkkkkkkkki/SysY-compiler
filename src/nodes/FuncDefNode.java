package nodes;

import error.ErrorType;
import helper.Helper;
import lexer.Category;
import llvm.BasicBlock;
import llvm.Function;
import llvm.InstrManager;
import llvm.Value;
import llvm.instruction.InstrType;
import llvm.instruction.ReturnInstr;
import llvm.type.FixedType;
import llvm.type.SimpleType;
import llvm.type.Type;
import parser.NodeType;
import symbol.*;

import java.util.ArrayList;

public class FuncDefNode extends Node {
    //  FuncDef → FuncType Ident '(' [FuncFParams] ')' Block
    private FuncSymbol symbol;

    public FuncSymbol getSymbol() {
        return symbol;
    }

    public FuncDefNode(int startNum, int endNum, NodeType nodeType, ArrayList<Node> children) {
        super(startNum, endNum, nodeType, children);
    }

    public Symbol createSymbol(int tableId) {
        String token = ((TerminalNode) children.get(1)).getValue();
        ReturnType returnType;
        ArrayList<FuncParamType> paras;
        if (((TerminalNode) children.get(0).getChildren().get(0)).getCategory() == Category.VOIDTK) {
            returnType = ReturnType.VOID;
        } else {
            returnType = ReturnType.INT;
        }
        if (children.get(3) instanceof FuncFParamsNode) {
            Node node = children.get(3);
            paras = ((FuncFParamsNode) node).calcu(1); // 是函数参数专用的，返回参数列表的calcu
            symbol = new FuncSymbol(token, SymbolType.FUNC, tableId, returnType, paras.size(), paras, SymbolManager.getManager().getReg(token));
            return symbol;
        } else {
            symbol = new FuncSymbol(token, SymbolType.FUNC, tableId, returnType, 0, null, SymbolManager.getManager().getReg(token));
            return symbol;
        }
    }

    @Override
    public void handleError() {
        SymbolManager symbolManager = SymbolManager.getManager();
        Symbol symbol = createSymbol(symbolManager.getCurId());

        if (!symbolManager.insertSymbol(symbol)) { // error b 重定义
            Helper.storeError(children.get(1).getStartNum(), ErrorType.b);
        }
        // 新建符号表，把符号表压到符号表栈，然后把形参放到这个符号表里
        symbolManager.createTable(((TerminalNode) children.get(1)).getValue()); //该函数的符号表
        // 把当前所在的block设置为函数的return type，方便检查return语句
        symbolManager.setReturnType(((FuncSymbol) symbol).getReturnType());
        symbolManager.enterFParas(); // 手动加了，在这之后不会再出现全局变量

        for (Node node: children) {
            if (node instanceof BlockNode) {
                ((BlockNode) node).handleError(1); // 这个时候自己就不建符号表了
            } else {
                node.handleError(); // 对每个形参节点，都已经插入到符号表中
            }
        }

        symbolManager.setReturnType(ReturnType.VOID); // 函数定义只在全局范围内出现，所以直接设置成void即可
        symbolManager.leaveBlock(); // 和create table要成对出现
    }

    @Override
    public Value generate() {
        SymbolManager.getManager().insertSymbol(symbol);
        SymbolManager.getManager().createTable(((TerminalNode) children.get(1)).getValue()); //该函数的符号表

        Type funcType = (symbol.getReturnType() == ReturnType.INT) ? SimpleType.INT32 : SimpleType.VOID;
        Function function = new Function(symbol.getToken(), funcType);
        //进入函数
        InstrManager.getInstance().setCurFunc(function);
        symbol.setValue(function);
        BasicBlock basicBlock = new BasicBlock(InstrManager.getInstance().newBB("func"));
        InstrManager.getInstance().setCurBB(basicBlock);

        //接着解析参数 和 函数体
        for (Node node: children) {
            if (node instanceof BlockNode) {
                ((BlockNode) node).generate(1); // 这个时候自己就不建符号表了
            } else {
                node.generate(); // 对每个形参节点，都已经插入到符号表中
            }
        }

        if (!InstrManager.getInstance().isHasRet()) {
            if (funcType == SimpleType.INT32) {
                ReturnInstr returnInstr = new ReturnInstr(InstrType.RET, SimpleType.INT32, "0", function);
            } else {
                ReturnInstr returnInstr = new ReturnInstr(InstrType.RET, SimpleType.VOID, "", function);
            }
        }

        // 遍历bb查看空块加ret
        function.mend();

        // 关于return语句
        SymbolManager.getManager().leaveBlock(); // 和create table要成对出现

        return null;
    }
}
