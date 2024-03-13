import helper.Helper;
import lexer.Lexer;
import llvm.Instr;
import llvm.InstrManager;
import nodes.Node;
import parser.Parser;
import symbol.SymbolManager;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Compiler {
    private static Lexer lexer = new Lexer();
    private static Parser parser = new Parser(lexer);

    public static void main(String[] args) throws IOException {
        String input = "";
        Helper.init();
        try {
            Path filePath = Paths.get("testfile.txt");
            input = Files.readString(filePath);

        } catch (IOException e) {
            e.printStackTrace();
        }
        lexer.getInput(input);

        while(lexer.next());

        Node root = parser.parseCompUnit();
        root.handleError();

        Helper.printError();
        SymbolManager.getManager().reset();

        if (!Helper.error) {
            root.generate();

            InstrManager.getInstance().printInstr();
        }


        Helper.closeStream();
    }
}
