package nodes;

import parser.NodeType;
import symbol.Symbol;
import symbol.SymbolManager;

import java.util.ArrayList;

public class StmtNode extends Node {
    // Stmt → LVal '=' Exp ';' // 每种类型的语句都要覆盖
    //| [Exp] ';' //有无Exp两种情况
    //| Block
    //| 'if' '(' Cond ')' Stmt [ 'else' Stmt ] // 1.有else 2.无else
    //| 'for' '(' [ForStmt] ';' [Cond] ';' [forStmt] ')' Stmt
    //| 'break' ';' | 'continue' ';'
    //| 'return' [Exp] ';' // 1.有Exp 2.无Exp
    //| LVal '=' 'getint''('')'';'
    //| 'printf''('FormatString{','Exp}')'';' // 1.有Exp 2.无Exp

    public StmtNode(int startNum, int endNum, NodeType nodeType, ArrayList<Node> children) {
        super(startNum, endNum, nodeType, children);
    }
}
