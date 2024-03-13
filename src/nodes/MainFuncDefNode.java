package nodes;

import llvm.BasicBlock;
import llvm.Function;
import llvm.InstrManager;
import llvm.Value;
import llvm.type.FixedType;
import llvm.type.SimpleType;
import parser.NodeType;
import symbol.*;

import java.util.ArrayList;

public class MainFuncDefNode extends Node {
    // MainFuncDef → 'int' 'main' '(' ')' Block
    private FuncSymbol symbol;

    public Symbol getSymbol() {
        return symbol;
    }

    public MainFuncDefNode(int startNum, int endNum, NodeType nodeType, ArrayList<Node> children) {
        super(startNum, endNum, nodeType, children);
    }

    private Symbol createSymbol(int tableId) {
        symbol = new FuncSymbol("main", SymbolType.FUNC, tableId, ReturnType.INT, 0, null, SymbolManager.getManager().getReg("main"));
        return symbol;
    }

    @Override
    public void handleError() {
        SymbolManager symbolManager = SymbolManager.getManager();
        Symbol symbol = createSymbol(symbolManager.getCurId());

        symbolManager.insertSymbol(symbol);
        symbolManager.createTable(((TerminalNode) children.get(1)).getValue()); //该函数的符号表
        symbolManager.enterMain(); // 在建立函数符号表的时候将返回值的类型放进去了，应该维护一个当前是否有返回值语句的变量
        for (Node node: children) {
            if (node instanceof BlockNode) {
                ((BlockNode) node).handleError(1); // 这个时候自己就不建符号表了
            } else {
                node.handleError();
            }
        }
        symbolManager.leaveBlock(); // 和create table要成对出现
    }

    @Override
    public Value generate() {
        SymbolManager.getManager().insertSymbol(symbol);
        SymbolManager.getManager().createTable(((TerminalNode) children.get(1)).getValue()); //该函数的符号表

        Function function = new Function("main", SimpleType.INT32);
        InstrManager.getInstance().setCurFunc(function);
        symbol.setValue(function);
        BasicBlock basicBlock = new BasicBlock(InstrManager.getInstance().newBB("func"));
        InstrManager.getInstance().setCurBB(basicBlock);
//        InstrManager.getInstance().nextTable(); 此时的符号表还是全局的，但是解析参数的时候并不需要符号表，等到参数解析完再

        for (Node node: children) {
            if (node instanceof BlockNode) {
                ((BlockNode) node).generate(1); // 这个时候自己就不建符号表了
            } else {
                node.generate(); // 对每个形参节点，都已经插入到符号表中
            }
        }

        SymbolManager.getManager().leaveBlock(); // 和create table要成对出现
        return null;
    }
}
