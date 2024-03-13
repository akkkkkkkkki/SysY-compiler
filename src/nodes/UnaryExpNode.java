package nodes;

import error.ErrorType;
import helper.Helper;
import lexer.Category;
import llvm.*;
import llvm.instruction.*;
import llvm.type.SimpleType;
import llvm.type.Type;
import parser.NodeType;
import symbol.*;

import java.util.ArrayList;

public class UnaryExpNode extends ExpNode {
    // UnaryExp → PrimaryExp | Ident '(' [FuncRParams] ')' | UnaryOp UnaryExp

    public UnaryExpNode(int startNum, int endNum, NodeType nodeType, ArrayList<Node> children) {
        super(startNum, endNum, nodeType, children);
    }

    @Override
    public int calcu() {  // 发现我的calcu和gen的格式差不多
        if (children.get(0) instanceof PrimaryExpNode) {
            return children.get(0).calcu();
        } else if (children.get(0) instanceof UnaryOpNode) {
            TerminalNode node = (TerminalNode) children.get(0).getChildren().get(0);
            if (node.getCategory() == Category.PLUS) {
                return children.get(1).calcu();
            } else if (node.getCategory() == Category.MINU) {
                return -children.get(1).calcu();
            } else if (node.getCategory() == Category.NOT) {
                return children.get(1).calcu() == 0 ? 1 : 0;
            } else {
                return 0; //error
            }
        } else { // 函数调用那种一元表达式，有计算结果的 TODO
            return 0;
        }
    }

    @Override
    public void handleError() {
        if (children.get(0) instanceof TerminalNode) { // 函数调用
            SymbolManager symbolManager = SymbolManager.getManager();
            String token = ((TerminalNode) children.get(0)).getValue();
            Symbol symbol = symbolManager.getSymbol(token);
            if (symbol == null) {
                Helper.storeError(this.getStartNum(), ErrorType.c); // 然后就不用分析下面的东西了
                return;
            }

            symbolManager.enterRParas();
            // error f(
            ArrayList<FuncParamType> rParas;
            if (children.size() < 3 || !(children.get(2) instanceof FuncRParamsNode)) {  // 保护func(这种确实
                 rParas = new ArrayList<>();
            } else {
                rParas = ((FuncRParamsNode) children.get(2)).getRParas();
            }
            if (!checkRParasType(rParas, ((FuncSymbol) symbol).getParas())) { // error e 参数类型
                Helper.storeError(this.getStartNum(), ErrorType.e);
            }
            symbolManager.leaveRParas();

            if (((FuncSymbol) symbol).getParaNum() != rParas.size()) { // error d 参数个数
                Helper.storeError(this.getStartNum(), ErrorType.d);
            }

            for (Node node: children) {
                node.handleError();
            }
        } else {
            for (Node node: children) {
                node.handleError();
            }
        }
    }

    private boolean checkRParasType(ArrayList<FuncParamType> rParas, ArrayList<FuncParamType> fParas) { // 遍历形参，看实参是否相符
        if (fParas == null) {
            return rParas == null || rParas.isEmpty();
        }
        int num = Math.min(fParas.size(), rParas.size());
        for (int i = 0; i < num; i++) {
            if (rParas.get(i) != null) {
                if (!rParas.get(i).equals(fParas.get(i))) {
                    return false;
                }
            } else {
                return false;
            }
        }
        return true;
    }

    public FuncParamType eval() {
        if (children.get(0) instanceof TerminalNode) { // 函数调用
            SymbolManager symbolManager = SymbolManager.getManager();
            String token = ((TerminalNode) children.get(0)).getValue();
            Symbol symbol = symbolManager.getSymbol(token);
            if (((FuncSymbol) symbol).getReturnType() == ReturnType.INT) {
                return new FuncParamType(0);
            } else {
                return new FuncParamType(3); //这种情况是 error
            }
        } else {
//            FuncParamType ret = children.get(0).eval();
//            if (children.size() == 1) {
//                return ret;
//            }
//            for (Node node: children) {
//                if (ret != null) {
//                    ret = ret.merge(node.eval());
//                }
//            }
//            return ret;
            return children.get(children.size() - 1).eval(); //
        }
    }

    @Override
    public Value generate() {
        if (children.get(0) instanceof PrimaryExpNode) {
            return children.get(0).generate();
        } else if (children.get(0) instanceof UnaryOpNode) {
            TerminalNode node = (TerminalNode) children.get(0).getChildren().get(0);
            if (node.getCategory() == Category.PLUS) {
                return children.get(1).generate();
            } else if (node.getCategory() == Category.MINU) {
                Value zero = new Constant(0);
                String regName = InstrManager.getInstance().newReg();
                return new AluInstr(InstrType.ALU, AluOp.SUB, regName, zero, children.get(1).generate());
            } else if (node.getCategory() == Category.NOT) {
                Value zero = new Constant(0);
                String regName = InstrManager.getInstance().newReg();
                Instr cmp = new IcmpInstr(InstrType.ICMP, regName, CmpOp.EQ, children.get(1).generate(), zero);
                // 本来生成的是ne，然后xor一下，再提升一下，但是现在直接和0比较再提升一下就行
                return new ZextInstr(InstrType.ZEXT, SimpleType.INT32, InstrManager.getInstance().newReg(), cmp); // TODO 这里的regname曾经是再生成的，修改为icmp的了？？
            } else {
                return null;
            }
        } else {
            String funcIdent = ((TerminalNode) children.get(0)).getValue();
            FuncSymbol function = (FuncSymbol) SymbolManager.getManager().getSymbol(funcIdent);

            ArrayList<Value> params;
            if (children.get(2) instanceof FuncRParamsNode) {
                params = ((FuncRParamsNode) children.get(2)).getRParasValue();
            } else {
                params = new ArrayList<>();
            }

            Type returnType = (function.getReturnType() == ReturnType.INT)? SimpleType.INT32 : SimpleType.VOID;
            String regName = InstrManager.getInstance().newReg(); // 对void函数是否有改进？ todo
            return new CallInstr(InstrType.CALL, returnType, regName, funcIdent, params);
        }
    }
}
