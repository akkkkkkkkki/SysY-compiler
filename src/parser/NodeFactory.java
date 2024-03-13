package parser;

import helper.Helper;
import lexer.Entry;
import nodes.*;

import java.util.ArrayList;

public class NodeFactory {
    public static Node createNode(int start, int end, NodeType nodeType, ArrayList<Node> children) {
        Helper.printParser("<" + nodeType.toString() + ">\n");
        return switch (nodeType) {
            case CompUnit -> new CompUnitNode(start, end, nodeType, children);
            case Decl -> new DeclNode(start, end, nodeType, children);
            case ConstDecl -> new ConstDeclNode(start, end, nodeType, children);
            case ConstDef -> new ConstDefNode(start, end, nodeType, children);
            case ConstInitVal -> new ConstInitValNode(start, end, nodeType, children);
            case VarDecl -> new VarDeclNode(start, end, nodeType, children);
            case VarDef -> new VarDefNode(start, end, nodeType, children);
            case InitVal -> new InitValNode(start, end, nodeType, children);
            case FuncDef -> new FuncDefNode(start, end, nodeType, children);
            case MainFuncDef -> new MainFuncDefNode(start, end, nodeType, children);
            case FuncType -> new FuncTypeNode(start, end, nodeType, children);
            case FuncFParams -> new FuncFParamsNode(start, end, nodeType, children);
            case FuncFParam -> new FuncFParamNode(start, end, nodeType, children);
            case Block -> new BlockNode(start, end, nodeType, children);
            case BlockItem -> new BlockItemNode(start, end, nodeType, children);
            case Stmt -> new StmtNode(start, end, nodeType, children);
            case ForInit -> new ForInitNode(start, end, nodeType, children);
            case Exp -> new ExpNode(start, end, nodeType, children);
            case Cond -> new CondNode(start, end, nodeType, children);
            case LVal -> new LValNode(start, end, nodeType, children);
            case PrimaryExp -> new PrimaryExpNode(start, end, nodeType, children);
            case Number -> new NumberNode(start, end, nodeType, children);
            case UnaryExp -> new UnaryExpNode(start, end, nodeType, children);
            case UnaryOp -> new UnaryOpNode(start, end, nodeType, children);
            case FuncRParams -> new FuncRParamsNode(start, end, nodeType, children);
            case MulExp -> new MulExpNode(start, end, nodeType, children);
            case AddExp -> new AddExpNode(start, end, nodeType, children);
            case RelExp -> new RelExpNode(start, end, nodeType, children);
            case EqExp -> new EqExpNode(start, end, nodeType, children);
            case LAndExp -> new LAndExpNode(start, end, nodeType, children);
            case LOrExp -> new LOrExpNode(start, end, nodeType, children);
            case ConstExp -> new ConstExpNode(start, end, nodeType, children);
            case AssignStmt -> new AssignStmtNode(start, end, nodeType, children);
            case ExpStmt -> new ExpStmtNode(start, end, nodeType, children);
            case BlockStmt -> new BlockStmtNode(start, end, nodeType, children);
            case IfStmt -> new IfStmtNode(start, end, nodeType, children);
            case ForStmt -> new ForStmtNode(start, end, nodeType, children);
            case BreakStmt -> new BreakStmtNode(start, end, nodeType, children);
            case ContinueStmt -> new ContinueStmtNode(start, end, nodeType, children);
            case ReturnStmt -> new ReturnStmtNode(start, end, nodeType, children);
            case GetintStmt -> new GetintStmtNode(start, end, nodeType, children);
            case PrintfStmt -> new PrintfStmtNode(start, end, nodeType, children);
            default -> null;
        };
    }

    public static Node createNode(Entry entry) {
        return new TerminalNode(entry.getLineNum(), entry.getLineNum(), NodeType.Terminal, null, entry.getValue(), entry.getCategory());
    }

    public static Node createNode(Entry entry, NodeType nodeType) {
        return new TerminalNode(entry.getLineNum(), entry.getLineNum(), NodeType.FormatString, null, entry.getValue(), entry.getCategory());
    }
}
