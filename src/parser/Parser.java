package parser;

import helper.Helper;
import lexer.Category;
import lexer.Lexer;
import nodes.Node;
import error.ErrorType;

import java.util.ArrayList;

public class Parser {
    private Node root;
    private EntryTable entryTable;

    public Parser(Lexer lexer) {
        this.entryTable = new EntryTable(lexer.getTable());
    }

    //  CompUnit → {Decl} {FuncDef} MainFuncDef
    public Node parseCompUnit() {
        ArrayList<Node> children = new ArrayList<>();
        int startNum = entryTable.getCurLine();

        while (true) {
            if (entryTable.check(NodeType.MainFuncDef)) {
                // main函数
                 children.add(parseMainFuncDef());
                 break;
            } else if (entryTable.check(NodeType.FuncDef)) {
                // FuncDef
                children.add(parseFuncDef());
            } else if (entryTable.check(NodeType.Decl)) {
                // Decl
                children.add(parseDecl());
            } else {
                break;
            }
        }

        int endNum = entryTable.getCurLine(1);
        return NodeFactory.createNode(startNum, endNum, NodeType.CompUnit, children);
    }

    //  MainFuncDef → 'int' 'main' '(' ')' Block
    private Node parseMainFuncDef() {
        ArrayList<Node> children = new ArrayList<>();
        int startNum = entryTable.getCurLine();

        for (int i = 0; i < 3; i++) {
            children.add(parseTerminal());
        }
        if (entryTable.check(")")) { // error j 缺少)
            children.add(parseTerminal());
        } else {
            Helper.storeError(entryTable.getCurLine(1), ErrorType.j);
        }
        children.add(parseBlock());

        int endNum = entryTable.getCurLine(1);
        return NodeFactory.createNode(startNum, endNum, NodeType.MainFuncDef, children);
    }

    // FuncDef → FuncType Ident '(' [FuncFParams] ')' Block
    private Node parseFuncDef() {
        ArrayList<Node> children = new ArrayList<>();
        int startNum = entryTable.getCurLine();

        // FuncType Ident '('
        children.add(parseFuncType());
        for (int i = 0; i < 2; i++) {
            children.add(parseTerminal());
        }
        if (entryTable.check(Category.INTTK)) {  //  将判断)改成了判断int
            children.add(parseFuncFParams());
        }
        if (entryTable.check(")")) { //  error j 缺少右小括号
            // ')'
            children.add(parseTerminal());
        } else {
            Helper.storeError(entryTable.getCurLine(1), ErrorType.j);
        }

        // Block
        children.add(parseBlock());

        int endNum = entryTable.getCurLine(1);
        return NodeFactory.createNode(startNum, endNum, NodeType.FuncDef, children);
    }

    private Node parseFuncType() {
        ArrayList<Node> children = new ArrayList<>();
        int startNum = entryTable.getCurLine();

        children.add(parseTerminal());

        int endNum = entryTable.getCurLine(1);
        return NodeFactory.createNode(startNum, endNum, NodeType.FuncType, children);
    }

    //  Block → '{' { BlockItem } '}'
    private Node parseBlock() {
        ArrayList<Node> children = new ArrayList<>();
        int startNum = entryTable.getCurLine();

        children.add(parseTerminal());
        while (true) {
            if (!entryTable.check("}")) {
                children.add(parseBlockItem());
            } else {
                break;
            }
        }
        children.add(parseTerminal());

        int endNum = entryTable.getCurLine(1);
        return NodeFactory.createNode(startNum, endNum, NodeType.Block, children);
    }

    //  BlockItem → Decl | Stmt
    private Node parseBlockItem() {
        ArrayList<Node> children = new ArrayList<>();
        int startNum = entryTable.getCurLine();

        if (entryTable.check(NodeType.Decl)) {
            children.add(parseDecl());
        } else {
            children.add(parseStmt());
        }

        int endNum = entryTable.getCurLine(1);
        return NodeFactory.createNode(startNum, endNum, NodeType.BlockItem, children);
    }

    private Node parseIfStmt() {
        ArrayList<Node> children = new ArrayList<>();
        int startNum = entryTable.getCurLine();

        for (int i = 0; i < 2; i++) {
            children.add(parseTerminal());
        }
        children.add(parseCond());
        if (entryTable.check(")")) { // error j )
            children.add(parseTerminal());
        } else {
            Helper.storeError(entryTable.getCurLine(1), ErrorType.j);
        }
        children.add(parseStmt());
        if (entryTable.check("else")) {
            children.add(parseTerminal());
            children.add(parseStmt());
        }

        int endNum = entryTable.getCurLine(1);
        return NodeFactory.createNode(startNum, endNum, NodeType.IfStmt, children);
    }

    private Node parseForStmt() {
        ArrayList<Node> children = new ArrayList<>();
        int startNum = entryTable.getCurLine();

        for (int i = 0; i < 2; i++) {
            children.add(parseTerminal());
        }
        if (!entryTable.check(";")) { // 有第一个分句
            children.add(parseForInit());
        }
        children.add(parseTerminal());
        if (!entryTable.check(";")) {
            children.add(parseCond());
        }
        children.add(parseTerminal());
        if (!entryTable.check(")")) {
            children.add(parseForInit());
        }
        children.add(parseTerminal());
        children.add(parseStmt());

        int endNum = entryTable.getCurLine(1);
        return NodeFactory.createNode(startNum, endNum, NodeType.ForStmt, children);
    }

    private Node parseBreakStmt() {
        ArrayList<Node> children = new ArrayList<>();
        int startNum = entryTable.getCurLine();

        children.add(parseTerminal());
        if (entryTable.check(";")) { // error i ;
            children.add(parseTerminal());
        } else {
            Helper.storeError(entryTable.getCurLine(1), ErrorType.i);
        }

        int endNum = entryTable.getCurLine(1);
        return NodeFactory.createNode(startNum, endNum, NodeType.BreakStmt, children);
    }

    private Node parseContinueStmt() {
        ArrayList<Node> children = new ArrayList<>();
        int startNum = entryTable.getCurLine();

        children.add(parseTerminal());
        if (entryTable.check(";")) { // error i ;
            children.add(parseTerminal());
        } else {
            Helper.storeError(entryTable.getCurLine(1), ErrorType.i);
        }

        int endNum = entryTable.getCurLine(1);
        return NodeFactory.createNode(startNum, endNum, NodeType.ContinueStmt, children);
    }

    private Node parseReturnStmt() {
        ArrayList<Node> children = new ArrayList<>();
        int startNum = entryTable.getCurLine();

        children.add(parseTerminal()); // return
        if (entryTable.check(NodeType.Exp)) {
            children.add(parseExp());
        }
        if (entryTable.check(";")) { // error i ;
            children.add(parseTerminal());
        } else {
            Helper.storeError(entryTable.getCurLine(1), ErrorType.i);
        }

        int endNum = entryTable.getCurLine(1);
        return NodeFactory.createNode(startNum, endNum, NodeType.ReturnStmt, children);
    }

    private Node parsePrintfStmt() {
        ArrayList<Node> children = new ArrayList<>();
        int startNum = entryTable.getCurLine();

        for (int i = 0; i < 2; i++) {
            children.add(parseTerminal());
        }
        children.add(parseFormatString());
        while (true) {
            if (entryTable.check(",")) {
                children.add(parseTerminal());
                children.add(parseExp());
            } else {
                break;
            }
        }
        if (entryTable.check(")")) { // error j )
            children.add(parseTerminal());
        } else {
            Helper.storeError(entryTable.getCurLine(1), ErrorType.j);
        }
        if (entryTable.check(";")) { // error i ;
            children.add(parseTerminal());
        } else {
            Helper.storeError(entryTable.getCurLine(1), ErrorType.i);
        }

        int endNum = entryTable.getCurLine(1);
        return NodeFactory.createNode(startNum, endNum, NodeType.PrintfStmt, children);
    }

    private Node parseBlockStmt() {
        ArrayList<Node> children = new ArrayList<>();
        int startNum = entryTable.getCurLine();

        // Block
        children.add(parseBlock());

        int endNum = entryTable.getCurLine(1);
        return NodeFactory.createNode(startNum, endNum, NodeType.BlockStmt, children);
    }

    // Stmt → LVal '=' Exp ';' // 每种类型的语句都要覆盖
    //| [Exp] ';' //有无Exp两种情况
    //| Block
    //| 'if' '(' Cond ')' Stmt [ 'else' Stmt ] // 1.有else 2.无else
    //| 'for' '(' [ForStmt] ';' [Cond] ';' [forStmt] ')' Stmt
    //| 'break' ';' | 'continue' ';'
    //| 'return' [Exp] ';' // 1.有Exp 2.无Exp
    //| LVal '=' 'getint''('')'';'
    //| 'printf''('FormatString{','Exp}')'';' // 1.有Exp 2.无Exp
    private Node parseStmt() {
        if (entryTable.check("if")) {
            return parseIfStmt();
        } else if (entryTable.check("for")) {
            return parseForStmt();
        } else if (entryTable.check("break")) {
            return parseBreakStmt();
        } else if (entryTable.check("continue")) {
            return parseContinueStmt();
        } else if (entryTable.check("return")) {
            return parseReturnStmt();
        } else if (entryTable.check("printf")) {
            return parsePrintfStmt();
        } else if (entryTable.check("{")) {
            return parseBlockStmt();
        } else if (entryTable.check(";")) { // 空语句块
            ArrayList<Node> children = new ArrayList<>();
            int startNum = entryTable.getCurLine();
            children.add(parseTerminal());
            int endNum = entryTable.getCurLine(1);
            return NodeFactory.createNode(startNum, endNum, NodeType.Stmt, children); // todo
        } else {
            ArrayList<Node> children = new ArrayList<>();
            int startNum = entryTable.getCurLine();

            // assign || getint || Exp
            entryTable.readOnly();
            parseExp();
            if (entryTable.check("=")) {
                entryTable.fetch();
                if (entryTable.check("getint")) {
                    //getint
                    entryTable.back2fetch();
                    children.add(parseLVal());
                    for (int i = 0; i < 3; i++) {
                        children.add(parseTerminal());
                    }
                    if (entryTable.check(")")) { // error j )
                        children.add(parseTerminal());
                    } else {
                        Helper.storeError(entryTable.getCurLine(1), ErrorType.j);
                    }
                    if (entryTable.check(";")) { // error i ;
                        children.add(parseTerminal());
                    } else {
                        Helper.storeError(entryTable.getCurLine(1), ErrorType.i);
                    }
                    int endNum = entryTable.getCurLine(1);
                    return NodeFactory.createNode(startNum, endNum, NodeType.GetintStmt, children);
                } else {
                    // assign
                    entryTable.back2fetch();
                    children.add(parseLVal());
                    children.add(parseTerminal());
                    children.add(parseExp());
                    if (entryTable.check(";")) { // error i ;
                        children.add(parseTerminal());
                    } else {
                        Helper.storeError(entryTable.getCurLine(1), ErrorType.i);
                    }
                    int endNum = entryTable.getCurLine(1);
                    return NodeFactory.createNode(startNum, endNum, NodeType.AssignStmt, children);
                }
            } else {
                // Exp  Q 如果是只有空语句块的分号，而分号又缺失了，>>}  aaa，其实不用管这个错误、、
                entryTable.back2fetch();
                if (entryTable.check(NodeType.Exp)) {
                    children.add(parseExp());
                }
                if (entryTable.check(";")) { // error i ;
                    children.add(parseTerminal());
                } else {
                    Helper.storeError(entryTable.getCurLine(1), ErrorType.i);
                }
                int endNum = entryTable.getCurLine(1);
                return NodeFactory.createNode(startNum, endNum, NodeType.ExpStmt, children);
            }
        }
    }

    // Cond → LOrExp
    private Node parseCond() {
        ArrayList<Node> children = new ArrayList<>();
        int startNum = entryTable.getCurLine();

        children.add(parseLOrExp());

        int endNum = entryTable.getCurLine(1);
        return NodeFactory.createNode(startNum, endNum, NodeType.Cond, children);
    }

    // LOrExp → LAndExp | LOrExp '||' LAndExp
    private Node parseLOrExp() {
        ArrayList<Node> children = new ArrayList<>();
        int startNum = entryTable.getCurLine();

        children.add(parseLAndExp());
        while (true) {
            if (entryTable.check("||")) {
                Helper.printParser("<LOrExp>\n");
                children.add(parseTerminal());
                children.add(parseLAndExp());
            } else {
                break;
            }
        }

        int endNum = entryTable.getCurLine(1);
        return NodeFactory.createNode(startNum, endNum, NodeType.LOrExp, children);
    }

    // EqExp | LAndExp '&&' EqExp
    private Node parseLAndExp() {
        ArrayList<Node> children = new ArrayList<>();
        int startNum = entryTable.getCurLine();

        children.add(parseEqExp());
        while (true) {
            if (entryTable.check("&&")) {
                Helper.printParser("<LAndExp>\n");
                children.add(parseTerminal());
                children.add(parseEqExp());
            } else {
                break;
            }
        }

        int endNum = entryTable.getCurLine(1);
        return NodeFactory.createNode(startNum, endNum, NodeType.LAndExp, children);
    }

    // RelExp | EqExp ('==' | '!=') RelExp
    private Node parseEqExp() {
        ArrayList<Node> children = new ArrayList<>();
        int startNum = entryTable.getCurLine();

        children.add(parseRelExp());
        while (true) {
            if (entryTable.check("!=") || entryTable.check("==")) {
                Helper.printParser("<EqExp>\n");
                children.add(parseTerminal());
                children.add(parseRelExp());
            } else {
                break;
            }
        }

        int endNum = entryTable.getCurLine(1);
        return NodeFactory.createNode(startNum, endNum, NodeType.EqExp, children);
    }

    //  RelExp → AddExp | RelExp ('<' | '>' | '<=' | '>=') AddExp
    private Node parseRelExp() {
        ArrayList<Node> children = new ArrayList<>();
        int startNum = entryTable.getCurLine();

        children.add(parseAddExp());
        while (true) {
            if (entryTable.check("<=") || entryTable.check(">=") || entryTable.check("<") || entryTable.check(">")) {
                // todo 打补丁
                Helper.printParser("<RelExp>\n");
                children.add(parseTerminal());
                children.add(parseAddExp());
            } else {
                break;
            }
        }

        int endNum = entryTable.getCurLine(1);
        return NodeFactory.createNode(startNum, endNum, NodeType.RelExp, children);
    }

    //  ForStmt → LVal '=' Exp
    private Node parseForInit() {
        ArrayList<Node> children = new ArrayList<>();
        int startNum = entryTable.getCurLine();

        children.add(parseLVal());
        children.add(parseTerminal());
        children.add(parseExp());

        int endNum = entryTable.getCurLine(1);
        return NodeFactory.createNode(startNum, endNum, NodeType.ForInit, children);
    }

    private Node parseFormatString() {
        return NodeFactory.createNode(entryTable.fetch(), NodeType.FormatString); // 修改，return一个format string了
    }

    // FuncFParams → FuncFParam { ',' FuncFParam }
    private Node parseFuncFParams() {
        ArrayList<Node> children = new ArrayList<>();
        int startNum = entryTable.getCurLine();

        children.add(parseFuncFParam());
        while (true) {
            if (entryTable.check(",")) {
                children.add(parseTerminal());
                children.add(parseFuncFParam());
            } else {
                break;
            }
        }

        int endNum = entryTable.getCurLine(1);
        return NodeFactory.createNode(startNum, endNum, NodeType.FuncFParams, children);
    }

    // FuncFParam → BType Ident ['[' ']' { '[' ConstExp ']' }]
    private Node parseFuncFParam() {
        ArrayList<Node> children = new ArrayList<>();
        int startNum = entryTable.getCurLine();

        for (int i = 0; i < 2; i++) {
            children.add(parseTerminal());
        }
        if (entryTable.check("[")) {
            children.add(parseTerminal());
            if (entryTable.check("]")) { // error k ]
                children.add(parseTerminal());
            } else {
                Helper.storeError(entryTable.getCurLine(1), ErrorType.k);
            }
            while (true) {
                if (entryTable.check("[")) {
                    children.add(parseTerminal());
                    children.add(parseConstExp());
                    if (entryTable.check("]")) { // error k ]
                        children.add(parseTerminal());
                    } else {
                        Helper.storeError(entryTable.getCurLine(1), ErrorType.k);
                    }
                } else {
                    break;
                }
            }
        }

        int endNum = entryTable.getCurLine(1);
        return NodeFactory.createNode(startNum, endNum, NodeType.FuncFParam, children);
    }

    //  Decl → ConstDecl | VarDecl
    private Node parseDecl() {
        ArrayList<Node> children = new ArrayList<>();
        int startNum = entryTable.getCurLine();

        if (entryTable.check(NodeType.ConstDecl)) {
            children.add(parseConstDecl());
        } else {
            children.add(parseVarDecl());
        }

        int endNum = children.get(0).getEndNum();
        return NodeFactory.createNode(startNum, endNum, NodeType.Decl, children);
    }

    // VarDecl → BType VarDef { ',' VarDef } ';'
    private Node parseVarDecl() {
        ArrayList<Node> children = new ArrayList<>();
        int startNum = entryTable.getCurLine();

        children.add(parseTerminal());
        children.add(parseVarDef());
        while ((true)) {
            if (entryTable.check(",")) {
                children.add(parseTerminal());
                children.add(parseVarDef());
            } else {
                break;
            }
        }
        if (entryTable.check(";")) { // error i ;
            children.add(parseTerminal());
        } else {
            Helper.storeError(entryTable.getCurLine(1), ErrorType.i);
        }

        int endNum = entryTable.getCurLine(1);
        return NodeFactory.createNode(startNum, endNum, NodeType.VarDecl, children);
    }

    // VarDef → Ident { '[' ConstExp ']' } // 包含普通变量、一维数组、二维数组定义
    //| Ident { '[' ConstExp ']' } '=' InitVal
    private Node parseVarDef() {
        ArrayList<Node> children = new ArrayList<>();
        int startNum = entryTable.getCurLine();

        children.add(parseTerminal());
        while (true) {
            if (entryTable.check("[")) {
                children.add(parseTerminal());
                children.add(parseConstExp());
                if (entryTable.check("]")) { // error k 缺少右中括号
                    children.add(parseTerminal());
                } else {
                    Helper.storeError(entryTable.getCurLine(1), ErrorType.k);
                }
            } else {
                break;
            }
        }
        if (entryTable.check("=")) {
            children.add(parseTerminal());
            children.add(parseInitVal());
        }

        int endNum = entryTable.getCurLine(1);
        return NodeFactory.createNode(startNum, endNum, NodeType.VarDef, children);
    }

    // InitVal → Exp | '{' [ InitVal { ',' InitVal } ] '}'
    private Node parseInitVal() {
        ArrayList<Node> children = new ArrayList<>();
        int startNum = entryTable.getCurLine();

        if (!entryTable.check("{")) {
            children.add(parseExp());
        } else {
            children.add(parseTerminal());
            if (!entryTable.check("}")) {
                children.add(parseInitVal());
                while (true) {
                    if (entryTable.check(",")) {
                        children.add(parseTerminal());
                        children.add(parseInitVal());
                    } else {
                        break;
                    }
                }
            }
            children.add(parseTerminal());
        }

        int endNum = entryTable.getCurLine(1);
        return NodeFactory.createNode(startNum, endNum, NodeType.InitVal, children);
    }

    // ConstDecl → 'const' BType ConstDef { ',' ConstDef } ';'
    private Node parseConstDecl() {
        ArrayList<Node> children = new ArrayList<>();
        int startNum = entryTable.getCurLine();

        for (int i = 0; i < 2; i++) {
            children.add(parseTerminal());
        }
        children.add(parseConstDef());
        while (true) {
            if (entryTable.check(",")) {
                children.add(parseTerminal());
                children.add(parseConstDef());
            } else {
                break;
            }
        }
        if (entryTable.check(";")) { //error i 缺少分号
            children.add(parseTerminal());
        } else { //此时应该是指向分号的下一个单词，所以应该打印前一个位置的行号
            Helper.storeError(entryTable.getCurLine(1), ErrorType.i);
        }

        int endNum = entryTable.getCurLine(1);
        return NodeFactory.createNode(startNum, endNum, NodeType.ConstDecl, children);
    }

    //  ConstDef → Ident { '[' ConstExp ']' } '=' ConstInitVal
    private Node parseConstDef() {
        ArrayList<Node> children = new ArrayList<>();
        int startNum = entryTable.getCurLine();

        children.add(parseTerminal()); // 这是常量的token
        while (true) {
            if (entryTable.check("[")) {
                children.add(parseTerminal());
                children.add(parseConstExp());
                if (entryTable.check("]")) { // error k ] 缺少右中括号
                    children.add(parseTerminal());
                } else {
                    Helper.storeError(entryTable.getCurLine(1), ErrorType.k);
                }
            } else {
                break;
            }
        }
        // '='
        children.add(parseTerminal());
        children.add(parseConstInitVal());

        int endNum = entryTable.getCurLine(1);
        return NodeFactory.createNode(startNum, endNum, NodeType.ConstDef, children);
    }

    //  ConstInitVal → ConstExp
    private Node parseConstInitVal() {
        ArrayList<Node> children = new ArrayList<>();
        int startNum = entryTable.getCurLine();

        if (!entryTable.check("{")) {
            children.add(parseConstExp());
        } else {
            children.add(parseTerminal());
            if (!entryTable.check("}")) {
                children.add(parseConstInitVal());
                while (true) {
                    if (entryTable.check(",")) {
                        children.add(parseTerminal());
                        children.add(parseConstInitVal());
                    } else {
                        break;
                    }
                }
            }
            children.add(parseTerminal());
        }

        int endNum = entryTable.getCurLine(1);
        return NodeFactory.createNode(startNum, endNum, NodeType.ConstInitVal,children);
    }

    // ConstExp → AddExp
    private Node parseConstExp() {
        ArrayList<Node> children = new ArrayList<>();
        int startNum = entryTable.getCurLine();

        children.add(parseAddExp());

        int endNum = children.get(0).getEndNum();
        return NodeFactory.createNode(startNum, endNum, NodeType.ConstExp, children);
    }

    // AddExp → MulExp | AddExp ('+' | '−') MulExp
    private Node parseAddExp() {
        ArrayList<Node> children = new ArrayList<>();
        int startNum = entryTable.getCurLine();

        children.add(parseMulExp());
        while (true) {
            if (entryTable.check("+") || entryTable.check("-")) {
                // todo 打补丁
                Helper.printParser("<AddExp>\n");
                children.add(parseTerminal());
                children.add(parseMulExp());
            } else {
                break;
            }
        }

        int endNum = entryTable.getCurLine(1);
        return NodeFactory.createNode(startNum, endNum, NodeType.AddExp, children);
    }

    // MulExp → UnaryExp | MulExp ('*' | '/' | '%') UnaryExp
    private Node parseMulExp() {
        ArrayList<Node> children = new ArrayList<>();
        int startNum = entryTable.getCurLine();

        children.add(parseUnaryExp());
        while (true) {
            if (entryTable.check("*") || entryTable.check("/") || entryTable.check("%")) {
                Helper.printParser("<MulExp>\n");
                children.add(parseTerminal());
                children.add(parseUnaryExp());
            } else {
                break;
            }
        }

        int endNum = entryTable.getCurLine(1);
        return NodeFactory.createNode(startNum, endNum, NodeType.MulExp, children);
    }

    // UnaryExp → PrimaryExp | Ident '(' [FuncRParams] ')' | UnaryOp UnaryExp
    private Node parseUnaryExp() {
        ArrayList<Node> children = new ArrayList<>();
        int startNum = entryTable.getCurLine();

        if (entryTable.check(NodeType.PrimaryExp)) {
            children.add(parsePrimaryExp());
        } else if (entryTable.check(NodeType.UnaryOp)) {
            children.add(parseUnaryOp());
            children.add(parseUnaryExp());
        } else {
            for (int i = 0; i < 2; i++) {
                children.add(parseTerminal());
            }
            if (entryTable.check(NodeType.Exp)) { // 修改，原来是!")"
                children.add(parseFuncRParams());
            }
            if (entryTable.check(")")) { // error j )
                children.add(parseTerminal());
            } else {
                Helper.storeError(entryTable.getCurLine(1), ErrorType.j);
            }
        }

        int endNum = entryTable.getCurLine(1);
        return NodeFactory.createNode(startNum, endNum, NodeType.UnaryExp, children);
    }

    // PrimaryExp → '(' Exp ')' | LVal | Number
    private Node parsePrimaryExp() {
        ArrayList<Node> children = new ArrayList<>();
        int startNum = entryTable.getCurLine();

        if (entryTable.check("(")) {
            children.add(parseTerminal());
            children.add(parseExp());
            children.add(parseTerminal());
        } else if (entryTable.check(NodeType.Number)) {
            children.add(parseNumber());
        } else {
            children.add(parseLVal());
        }

        int endNum = entryTable.getCurLine(1);
        return NodeFactory.createNode(startNum, endNum, NodeType.PrimaryExp, children);
    }

    //  Exp → AddExp
    private Node parseExp() {
        ArrayList<Node> children = new ArrayList<>();
        int startNum = entryTable.getCurLine();

        children.add(parseAddExp());

        int endNum = entryTable.getCurLine(1);
        return NodeFactory.createNode(startNum, endNum, NodeType.Exp, children);
    }

    // Number → IntConst
    private Node parseNumber() {
        ArrayList<Node> children = new ArrayList<>();
        int startNum = entryTable.getCurLine();

        children.add(parseTerminal());

        int endNum = entryTable.getCurLine(1);
        return NodeFactory.createNode(startNum, endNum, NodeType.Number, children);
    }

    // LVal → Ident {'[' Exp ']'}
    private Node parseLVal() {
        ArrayList<Node> children = new ArrayList<>();
        int startNum = entryTable.getCurLine();

        children.add(NodeFactory.createNode(entryTable.fetch()));
        while (true) {
            if (entryTable.check("[")) {
                children.add(parseTerminal());
                children.add(parseExp());
                if (entryTable.check("]")) { // error k ]
                    children.add(parseTerminal());
                } else{
                    Helper.storeError(entryTable.getCurLine(1), ErrorType.k);
                }
            } else {
                break;
            }
        }

        int endNum = entryTable.getCurLine(1);
        return NodeFactory.createNode(startNum, endNum, NodeType.LVal, children);
    }

    // UnaryOp → '+' | '−' | '!'
    private Node parseUnaryOp() {
        ArrayList<Node> children = new ArrayList<>();
        int startNum = entryTable.getCurLine();

        children.add(parseTerminal());

        int endNum = entryTable.getCurLine(1);
        return NodeFactory.createNode(startNum, endNum, NodeType.UnaryOp, children);
    }

    // FuncRParams → Exp { ',' Exp }
    private Node parseFuncRParams() {
        ArrayList<Node> children = new ArrayList<>();
        int startNum = entryTable.getCurLine();

        children.add(parseExp());
        while (true) {
            if (entryTable.check(",")) {
                children.add(parseTerminal());
                children.add(parseExp());
            } else {
                break;
            }
        }

        int endNum = entryTable.getCurLine(1);
        return NodeFactory.createNode(startNum, endNum, NodeType.FuncRParams, children);
    }

    private Node parseTerminal() {
        return NodeFactory.createNode(entryTable.fetch());
    }
}
