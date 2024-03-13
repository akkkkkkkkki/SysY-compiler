package nodes;

import error.ErrorType;
import helper.Helper;
import llvm.InstrManager;
import llvm.instruction.PutchInstr;
import llvm.Value;
import llvm.instruction.InstrType;
import llvm.instruction.PutintInstr;
import llvm.type.SimpleType;
import parser.NodeType;

import java.util.ArrayList;

public class PrintfStmtNode extends StmtNode {
    // 'printf''('FormatString{,Exp}')'';'
    public PrintfStmtNode(int startNum, int endNum, NodeType nodeType, ArrayList<Node> children) {
        super(startNum, endNum, nodeType, children);
    }

    @Override
    public void handleError() {
        for (Node node : children) {
            node.handleError();
        }

        String fstring = ((TerminalNode) children.get(2)).getValue();
        int num = 0;
        int index = fstring.indexOf("%d");
        while (index != -1) {
            num++;
            index = fstring.indexOf("%d", index + 1);
        }

        if (num * 2 != children.size() - 5) { // 逗号的个数应该是和exp个数相同的
            Helper.storeError(this.getStartNum(), ErrorType.l);
        }
    }

    @Override
    public Value generate() {
        ArrayList<Value> vars =new ArrayList<>();
        for (Node node: children) {
            if (node instanceof ExpNode) {
                vars.add(node.generate());
            }
        }
        int index = 0;
        String string = ((TerminalNode) children.get(2)).getValue();
        for (int i = 0; i < string.length(); i++) {
            char ch = string.charAt(i);
            if (ch == '\\') {
                i++;
                String regName = InstrManager.getInstance().newReg();
                PutchInstr putchInstr = new PutchInstr(InstrType.IO, SimpleType.INT32, regName, '\n');
                continue;
            }
            if (ch != '%') {
                String regName = InstrManager.getInstance().newReg();
                PutchInstr putchInstr = new PutchInstr(InstrType.IO, SimpleType.INT32, regName, ch);
            } else {
                if (i + 1 < string.length() && string.charAt(i + 1) == 'd') {
                    i++;
                    String regName = InstrManager.getInstance().newReg();
                    PutintInstr putintInstr = new PutintInstr(InstrType.IO, SimpleType.INT32, regName, vars.get(index));
                    index++;
                }
            }
        }
        return null;
    }
}
