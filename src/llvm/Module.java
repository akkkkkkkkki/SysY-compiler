package llvm;

import helper.Helper;

import java.util.ArrayList;

public class Module extends User {
    private String headers;
    private ArrayList<GlobalVar> globalVars;
    private ArrayList<Function> functions;

    public Module() {
        if (Helper.my) {
            this.headers = Helper.headers;
        } else {
            this.headers = """
                declare i32 @getint()
                declare void @putint(i32)
                declare void @putch(i32)
                declare void @putstr(i8*)\n
                """;
        }
        this.globalVars = new ArrayList<>();
        this.functions = new ArrayList<>();
    }
    public void addGlobalVar(GlobalVar globalVar) {
        globalVars.add(globalVar);
    }

    public void addFunction(Function function) {
        functions.add(function);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(headers);
        for (GlobalVar globalVar: globalVars) {
            sb.append(globalVar.toString());
        }
        sb.append("\n");
        for (Function function: functions) {
            sb.append(function.toString());
        }
        return sb.toString();
    }
}
