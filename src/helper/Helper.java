package helper;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

import error.ErrorType;

public class Helper {
    private static FileWriter writer;
    private static FileWriter errorWriter;
    private static FileWriter instrWriter;
    private static boolean ifLexer = true;
    private static boolean ifParser = true; //不同的作业版本
    private static boolean ifHandler = true;
    private static boolean ifLlvm = true;
    private static boolean printOn = true;  //给监视点用的
    public static boolean my = false;       // 本地调试true，提交false
    public static String headers = "";
    public static boolean error = false;

    private static HashMap<Integer, ArrayList<String>> errorMessage;
    private static HashSet<Integer> errorSet;

    public static void init() throws IOException {
        writer = new FileWriter("output.txt");          // 语法分析部分
        errorWriter = new FileWriter("error.txt");      // 错误处理部分
        instrWriter = new FileWriter("llvm_ir.txt");    // 代码生成
        errorMessage = new HashMap<>();
        errorSet = new HashSet<>();

        if (my) {
            Path filePath = Paths.get("env.txt");
            headers = Files.readString(filePath);
        }
    }

    public static void printParser(String string) {
        if (ifParser) {
            if ("<Decl>\n<BlockItem>\n".contains(string)) {
                return;
            }
            if (printOn) {
                try {
                    writer.write(string);
                } catch (IOException e) {
                    e.printStackTrace();
                }
//                System.out.print(string);
            }
        }
    }

    public static void printLexer(String string) {
        if (ifLexer) {
            if (printOn) {
                try {
                    writer.write(string);
                } catch (IOException e) {
                    e.printStackTrace();
                }
//                System.out.print(string);
            }
        }
    }

    public static void storeError(int lineNum, ErrorType errorType) {
        error = true;
        if (printOn) {
            errorSet.add(lineNum);
            errorMessage.computeIfAbsent(lineNum, k -> new ArrayList<>());
            errorMessage.get(lineNum).add(lineNum + " " + errorType + "\n");
        }
    }

    public static void printError() throws IOException {
        Object[] errorLines = errorSet.toArray();
        Arrays.sort(errorLines);
        for (Object i: errorLines) {
            for (String string: errorMessage.get((Integer) i)) {
                errorWriter.write(string);
//                System.out.print(string);
            }
        }
    }

    public static void openPrint() {
        printOn = true;
    }

    public static void closePrint() {
        printOn = false;
    }

    public static void closeStream() throws IOException {
        writer.close();
        errorWriter.close();
        instrWriter.close();
    }

    public static boolean checkFormatString(String string) {
        for (int i = 0; i < string.length(); i++) {
            char ch = string.charAt(i);
            if (ch <= 31 || 34 <= ch  && ch <= 39 || ch == 127 || ch == '\\') {
                if (ch == '%' && i != string.length() - 1 && string.charAt(i + 1) == 'd') {
                    continue;
                }
                if (ch == '\\' && i != string.length() - 1 && string.charAt(i + 1) == 'n') {
                    continue;
                }
                return false;
            }
        }
        return true;
    }

    public static void printInstr(String string) {
        if (ifLlvm) {
            try {
                instrWriter.write(string);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
