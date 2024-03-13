package symbol;

import java.util.ArrayList;

public class FuncSymbol extends Symbol{
    private ReturnType returnType;
    private int paraNum;
    private ArrayList<FuncParamType> paras;

    public FuncSymbol(String token, SymbolType symbolType, int table, ReturnType returnType, int num, ArrayList<FuncParamType> paras, String reg) {
        super(token, symbolType, table, reg);
        this.returnType = returnType;
        this.paraNum = num;
        this.paras = paras;
    }

    public ReturnType getReturnType() {
        return returnType;
    }

    public boolean isSameReturnType(ReturnType returnType) {
        return this.returnType == returnType;
    }

    public ArrayList<FuncParamType> getParas() {
        return paras;
    }

    public int getParaNum() {
        return paraNum;
    }

    public boolean isSameParas(ArrayList<FuncParamType> paras) {
        if (paraNum != paras.size()) {
            return false;
        }
        for (int i = 0; i < paraNum; i++) {
            if (!this.paras.get(i).equals(paras.get(i))) {
                return false;
            }
        }
        return true;
    }
}
