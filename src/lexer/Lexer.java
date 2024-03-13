package lexer;

import error.ErrorType;
import helper.Helper;

import java.util.ArrayList;
import java.util.HashMap;

public class Lexer {
    private String input;
    private int pos; //当前字符串位置指针
    private String curToken = ""; //当前解析的单词
    private int lineNum; //当前行号
    private static String operators = "+-*/!&|%<=>;,()[]{}";
    private static String qMarks = "''\"\"";
    private ArrayList<Entry> table;
    private int length;
    private static HashMap<String, Category> categoryMap;

    public Lexer() {
        this.pos = 0;
        this.lineNum = 1;
        this.table = new ArrayList<>();
        this.categoryMap = new HashMap<>();
        this.setUpMap();
    }

    private void setUpMap() {
        categoryMap.put("main", Category.MAINTK);
        categoryMap.put("const", Category.CONSTTK);
        categoryMap.put("int", Category.INTTK);
        categoryMap.put("break", Category.BREAKTK);
        categoryMap.put("continue", Category.CONTINUETK);
        categoryMap.put("if", Category.IFTK);
        categoryMap.put("else", Category.ELSETK);
        categoryMap.put("for", Category.FORTK);
        categoryMap.put("getint", Category.GETINTTK);
        categoryMap.put("printf", Category.PRINTFTK);
        categoryMap.put("return", Category.RETURNTK);
        categoryMap.put("void", Category.VOIDTK);
    }

    public void getInput(String string) {
        this.input = string;
        this.length = string.length();
    }

    public boolean next() {
        if (pos == input.length()) {
            return false;
        }

        char ch = input.charAt(pos);
        if (qMarks.indexOf(ch) != -1) {
            curToken = getString(ch);
            Entry string = new Entry(lineNum, Category.STRCON, curToken);
            table.add(string);
        } else if (operators.indexOf(ch) != -1) {
            if (ch == '/') {
                if (getAnnotation()) {
                    //注释
                    curToken = "";
                } else {
                    Entry div = new Entry(lineNum, Category.DIV, "/");
                    table.add(div);
                }
            } else {
                table.add(getOperator());
            }
        } else if (Character.isDigit(ch)) {
            curToken = getNumber();
            Entry number = new Entry(lineNum, Category.INTCON, curToken); // 设置单词类别
            table.add(number);
        } else if (Character.isLetter(ch) || ch == '_') {
            curToken = getWord();
            table.add(reserve(curToken));
        } else if (ch == '\n') {
            pos++;
            lineNum++;
        } else if (ch == ' ' || ch == '\r' || ch == '\t') {
            ++pos;
            next();
        } else {
            System.out.println("error in next()!");
        }
        return true;
    }

    private String getString(char ch) {
        StringBuilder sb = new StringBuilder();
//        sb.append(ch);
        ++pos;
        while (pos < length && input.charAt(pos) != ch) {
            sb.append(input.charAt(pos));
            ++pos;
        }
//        sb.append(ch);
        ++pos;
        if (!Helper.checkFormatString(sb.toString())) { // error a 非法字符
            Helper.storeError(lineNum, ErrorType.a);
        }

        return sb.toString();
    }

    private boolean getAnnotation() {
        curToken += '/';
        ++pos;
        if (pos < length && input.charAt(pos) == '/') { //单行注释
            curToken += '/';
            ++pos;
            while (pos < length && input.charAt(pos) != '\n') {
                curToken += input.charAt(pos++);
            }
            if (pos < length) { //遇到换行符
                curToken += input.charAt(pos++);
                lineNum++;
            }
            return true;
        } else if (pos < length && input.charAt(pos) == '*') {
            curToken += '*';
            ++pos;
            while (pos < length) {
                if (input.charAt(pos) != '*') {
                    curToken += input.charAt(pos++);
                    if (input.charAt(pos - 1) == '\n') {
                        lineNum++;
                    }
                } else {
                    while (input.charAt(pos) == '*') {
                        curToken += '*';
                        ++pos;
                    }
                    if (input.charAt(pos) == '/') {
                        curToken += '/';
                        ++pos;
                        return true;
                    }
                }
            }
            System.out.println("error in multi annotation!");
            return true;
        } else {
            return false;
        }
    }

    private Entry getOperator() {
        char ch = input.charAt(pos);
        if ("+-*%;,()[]{}".indexOf(ch) != -1) {
            return getSingleOp(ch);
        } else {
            return getMultiOp(ch);
        }
    }

    private String getNumber() {
        StringBuilder sb = new StringBuilder();
        while (pos < length && Character.isDigit(input.charAt(pos))) {
            sb.append(input.charAt(pos));
            ++pos;
        }

        return sb.toString();
    }

    private String getWord() {
        StringBuilder sb = new StringBuilder();
        while (pos < length && (Character.isLetter(input.charAt(pos))
                || input.charAt(pos) == '_'
                || Character.isDigit(input.charAt(pos)))) {
            sb.append(input.charAt(pos));
            ++pos;
        }

        return sb.toString();
    }

    private Entry getSingleOp(char ch) {
        ++pos;
        if (ch == '*') {
            return new Entry(lineNum, Category.MULT, "*");
        } else if (ch == '+') {
            return new Entry(lineNum, Category.PLUS, "+");
        } else if (ch == '-') {
            return new Entry(lineNum, Category.MINU, "-");
        } else if (ch == '%') {
            return new Entry(lineNum, Category.MOD, "%");
        } else if (ch == ';') {
            return new Entry(lineNum, Category.SEMICN, ";");
        } else if (ch == ',') {
            return new Entry(lineNum, Category.COMMA, ",");
        } else if (ch == '(') {
            return new Entry(lineNum, Category.LPARENT, "(");
        } else if (ch == ')') {
            return new Entry(lineNum, Category.RPARENT, ")");
        } else if (ch == '[') {
            return new Entry(lineNum, Category.LBRACK, "[");
        } else if (ch == ']') {
            return new Entry(lineNum, Category.RBRACK, "]");
        } else if (ch == '{') {
            return new Entry(lineNum, Category.LBRACE, "{");
        } else if (ch == '}') {
            return new Entry(lineNum, Category.RBRACE, "}");
        } else {
            System.out.println("error in getSingle()!");
            return null;
        }
    }

    private Entry getMultiOp(char ch) {
        ++pos;
        if (ch == '!') {
            if (pos < length && input.charAt(pos) == '=') {
                ++pos;
                return new Entry(lineNum, Category.NEQ, "!=");
            } else {
                return new Entry(lineNum, Category.NOT, "!");
            }
        } else if (ch == '<') {
            if (pos < length && input.charAt(pos) == '=') {
                ++pos;
                return new Entry(lineNum, Category.LEQ, "<=");
            } else {
                return new Entry(lineNum, Category.LSS, "<");
            }
        } else if (ch == '>') {
            if (pos < length && input.charAt(pos) == '=') {
                ++pos;
                return new Entry(lineNum, Category.GEQ, ">=");
            } else {
                return new Entry(lineNum, Category.GRE, ">");
            }
        } else if (ch == '=') {
            if (pos < length && input.charAt(pos) == '=') {
                ++pos;
                return new Entry(lineNum, Category.EQL, "==");
            } else {
                return new Entry(lineNum, Category.ASSIGN, "=");
            }
        } else if (ch == '&') {
            if (pos < length && input.charAt(pos) == '&') {
                ++pos;
                return new Entry(lineNum, Category.AND, "&&");
            } else {
                System.out.println("error in op: get single &!");
            }
        } else if (ch == '|') {
            if (pos < length && input.charAt(pos) == '|') {
                ++pos;
                return new Entry(lineNum, Category.OR, "||");
            } else {
                System.out.println("error in op: get single |!");
            }
        }
        return null;
    }

    private Entry reserve(String word) {
        if (categoryMap.get(word) != null) {
            return new Entry(lineNum, categoryMap.get(word), word);
        }
        return new Entry(lineNum, Category.IDENFR, word);
    }

    public String getSymbols() {
        StringBuilder ret = new StringBuilder();
        for (Entry entry: table) {
            ret.append(entry.toString());
        }
        return ret.toString();
    }

    public ArrayList<Entry> getTable() {
        return table;
    }
}
