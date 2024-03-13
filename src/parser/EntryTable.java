package parser;

import helper.Helper;
import lexer.Category;
import lexer.Entry;

import java.util.ArrayList;
import java.util.Objects;

public class EntryTable {
    private ArrayList<Entry> entries;
    private int curPos;
    private int readOnly = -1;

    public EntryTable(ArrayList<Entry> entries) {
        this.entries = entries;
        this.curPos = 0;
    }

    public int getCurLine() {
        return entries.get(curPos).getLineNum();
    }

    public int getCurLine(int placeHolder) { // get当前pos的前一个的linenum
        return entries.get(curPos - 1).getLineNum();
    }

    public Entry fetch() {
        if (readOnly == -1) {
            Helper.printLexer(entries.get(curPos).toString());
        }
        return entries.get(curPos++);
    }

    public void readOnly() {
        readOnly = curPos;
        Helper.closePrint();
    }

    public void back2fetch() {
        curPos = readOnly;
        readOnly = -1;
        Helper.openPrint();
    }

    public boolean check(NodeType nodeType) {
        switch (nodeType) {
            case MainFuncDef -> {
                return entries.get(curPos + 1).getCategory() == Category.MAINTK;
            }
            case Decl -> {
                return check(NodeType.ConstDecl) || check(NodeType.VarDecl);
            }
            case FuncDef -> {
                return (entries.get(curPos).getCategory() == Category.VOIDTK || entries.get(curPos).getCategory() == Category.INTTK)
                        && entries.get(curPos + 1).getCategory() == Category.IDENFR
                        && entries.get(curPos + 2).getCategory() == Category.LPARENT;
            }
            case ConstDecl -> {
                return entries.get(curPos).getCategory() == Category.CONSTTK; //Q 这种情况是只判断最极端的情况就行还是要递归判断所有呢？错误处理？
            }
            case ConstDef -> {
                // 这个只在右边出现了一次，所以其实不需要判断？
            }
            case ConstInitVal -> {
                return check(NodeType.ConstExp) || entries.get(curPos).getCategory() == Category.LBRACE; //*****第二种情况可能和block相同
            }
            case VarDecl -> {
                return entries.get(curPos).getCategory() == Category.INTTK
                        && entries.get(curPos + 1).getCategory() == Category.IDENFR;
            }
            case VarDef -> {

            }
            case InitVal -> {

            }
            case FuncType -> {

            }
            case FuncFParams -> {

            }
            case FuncFParam -> {

            }
            case Block -> {
                return entries.get(curPos).getCategory() == Category.LBRACE;
            }
            case BlockItem -> {

            }
            case Stmt -> {

            }
            case ForInit -> {

            }
            case Exp -> {
                return check(NodeType.UnaryExp);
            }
            case Cond -> {

            }
            case LVal -> {

            }
            case PrimaryExp -> {
                return entries.get(curPos).getCategory() == Category.LPARENT
                        || entries.get(curPos).getCategory() == Category.IDENFR
                        && entries.get(curPos + 1).getCategory() != Category.LPARENT
                        || entries.get(curPos).getCategory() == Category.INTCON;
            }
            case Number -> {
                return entries.get(curPos).getCategory() == Category.INTCON;
            }
            case UnaryExp -> {
                return check(NodeType.PrimaryExp)
                        || entries.get(curPos).getCategory() == Category.IDENFR
                        && entries.get(curPos + 1).getCategory() == Category.LPARENT
                        || check(NodeType.UnaryOp);
            }
            case UnaryOp -> {
                return entries.get(curPos).getCategory() == Category.PLUS
                        || entries.get(curPos).getCategory() == Category.MINU
                        || entries.get(curPos).getCategory() == Category.NOT;
            }
            case FuncRParams -> {
                return entries.get(curPos).getCategory() != Category.RPARENT;
            }
            case MulExp -> {

            }
            case AddExp -> {

            }
            case RelExp -> {

            }
            case EqExp -> {

            }
            case LAndExp -> {

            }
            case LOrExp -> {

            }
            case ConstExp -> {

            }
            default -> {
                return false;
            }
        }
        return false;
    }

    public boolean check(String terminal) {
        return Objects.equals(terminal, entries.get(curPos).getValue());
    }

    public boolean check(Category category) {
        return Objects.equals(entries.get(curPos).getCategory(), category);
    }
}
