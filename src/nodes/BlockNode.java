package nodes;

import error.ErrorType;
import helper.Helper;
import llvm.InstrManager;
import llvm.Value;
import parser.NodeType;
import symbol.ReturnType;
import symbol.Symbol;
import symbol.SymbolManager;

import java.util.ArrayList;

public class BlockNode extends Node {
    // Block → '{' { BlockItem } '}'


    public BlockNode(int startNum, int endNum, NodeType nodeType, ArrayList<Node> children) {
        super(startNum, endNum, nodeType, children);
    }

    public void handleError(int placeHolder) { // 给函数体用的
        for (Node node: children) {
            node.handleError();
        }
        if (children.size() == 2) { // 空函数，只有{}
            if (SymbolManager.getManager().checkReturnType(ReturnType.INT)) {
                Helper.storeError(this.getEndNum(), ErrorType.g);
                return;
            }
        }
        int num = children.size() - 2;
        if (SymbolManager.getManager().checkReturnType(ReturnType.INT)
                && !(children.get(num).getChildren().get(0) instanceof ReturnStmtNode
                && ((ReturnStmtNode) children.get(num).getChildren().get(0)).isReturnVal())) { // 当前函数的返回值类型在manager内，此时可以check是否符合return
            Helper.storeError(this.getEndNum(), ErrorType.g);
        }
    }

    @Override
    public void handleError() { // 给普通block用的
        SymbolManager symbolManager = SymbolManager.getManager();
        // 新建符号表, 为该block内部符号的符号表
        symbolManager.createTable();
        for (Node node: children) {
            node.handleError();
        }
        symbolManager.leaveBlock();
    }

    @Override
    public Value generate() {
//        InstrManager.getInstance().nextTable();
        SymbolManager.getManager().createTable();
        super.generate();
//        InstrManager.getInstance().nextTable(); // 退出的时候再调用一次
        SymbolManager.getManager().leaveBlock();
        return null;
    }

    public Value generate(int placeholder) {
        super.generate();
        return null;
    }
}
