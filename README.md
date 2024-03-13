SysY-compiler
---

### 总体结构

本编译器采用Java语言编写，将SysY语言翻译成llvm中间代码，分为词法分析、语法分析、错误处理、代码生成四个部分。

### 文件组织和接口

项目结构如下所示

```
├─src
│  ├─error       	# 错误类别    
│  ├─helper			# 输入输出和配置的工具类
│  ├─lexer          # lexer和词法分析的表项			
│  ├─llvm			# LLVM IR定义
│  │  ├─instruction
│  │  ├─type
│  │  └─classes
│  ├─nodes           # AST的各类节点
│  ├─parser          # parser和创建节点相关的类
│  └─Compliler.java  # 入口程序
```

`Compiler.java`部分代码如下所示：

```java
public static void main(String[] args) throws IOException {
    String input = "";
    Helper.init();
    try {
        Path filePath = Paths.get("testfile.txt");
        input = Files.readString(filePath);
    } catch (IOException e) {
        e.printStackTrace();
    }
    // 词法分析
    lexer.getInput(input);
    while(lexer.next());
    // 语法树生成
    Node root = parser.parseCompUnit();
    // 错误处理
    root.handleError();
    Helper.printError();
    // 如果没有错误进行代码生成
    SymbolManager.getManager().reset();
    if (!Helper.error) {
        root.generate();
        InstrManager.getInstance().printInstr();
    }

    Helper.closeStream();
}
```

大体流程如下：

- Lexer类解析源程序，将源程序按照词法解析成一个个`Entry`，并封装放入`EntryTable`中（管理token的类）。

- Parser类根据上一步生成的`EntryTable`，根据文法生成抽象语法树，并返回根节点`root`。

- 在parse过程中检查语法错误，而语义错误从root根节点开始，自顶向下递归调用每个节点的`handleError`方法进行检查（在此过程中生成符号表，由`SymBolManager`统一管理）。

- 从root开始，自顶向下递归调用每个节点的generate方法生成llvm，由`InstrManager`统一管理。

### 词法分析

> 单词类别码

| 单词名称     | 类别码     | 单词名称 | 类别码   | 单词名称 | 类别码 | 单词名称 | 类别码  |
| ------------ | ---------- | -------- | -------- | -------- | ------ | -------- | ------- |
| Ident        | IDENFR     | !        | NOT      | *        | MULT   | =        | ASSIGN  |
| IntConst     | INTCON     | &&       | AND      | /        | DIV    | ;        | SEMICN  |
| FormatString | STRCON     | \|\|     | OR       | %        | MOD    | ,        | COMMA   |
| main         | MAINTK     | for      | FORTK    | <        | LSS    | (        | LPARENT |
| const        | CONSTTK    | getint   | GETINTTK | <=       | LEQ    | )        | RPARENT |
| int          | INTTK      | printf   | PRINTFTK | >        | GRE    | [        | LBRACK  |
| break        | BREAKTK    | return   | RETURNTK | >=       | GEQ    | ]        | RBRACK  |
| continue     | CONTINUETK | +        | PLUS     | ==       | EQL    | {        | LBRACE  |
| if           | IFTK       | -        | MINU     | !=       | NEQ    | }        | RBRACE  |
| else         | ELSETK     | void     | VOIDTK   |          |        |          |         |

#### 编码前设计

编码前设计与编码设计相同，未进行大规模修改，一开始即考虑到保存行号。在编码的过程中有少量bug：对\r的处理

词法分析主要由`Lexer`类实现，该类的几个主要属性如下：

- input：输入文件字符串
- pos：当前解析位置字符串指针
- curToken：当前解析单词
- lineNum：当前行号

最终生成单词输入流保存在`EntryTable`中，每个`Entry`的属性包括：

```java
public class Entry {
    private int lineNum;
    private Category category; // 类别码
    private String value;
}
```

首先将输入文件读到input字符串里面，然后调用`next()`函数不断的挨个读取字符，根据读到字符先后判断注释、操作符、数字、单词、换行符以及多余字符`\r, \t, \space`，在各个if分支中再调用子函数继续读取，将一个个单词包装成`Entry`类加入到`EntryTable`中，读到文件末尾时返回`flase`。大体逻辑如下：

```java
public boolean next() {
    if (pos == input.length()) {
        return false;
    }

    char ch = input.charAt(pos);
    if (qMarks.indexOf(ch) != -1) { // qMarks = "''\"\"";
        curToken = getString(ch);
        Entry string = new Entry(lineNum, Category.STRCON, curToken);
        table.add(string); 
    } else if (operators.indexOf(ch) != -1) { // operators = "+-*/!&|%<=>;,()[]{}";
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
        table.add(reserve(curToken)); // 读取word之后判断是否是保留字
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
} // Lexer.java
```

**注意**

- 对于`\r`的判断，读到`\n`的时候维护当前行号；

- 对于operators有两种情况，`+, -`等单个char的运算符和`==, >=`这种两个char的；

- 在`getAnnotation()`方法中也需要维护`lineNum`；

- `getWord()`判断完之后还需要判断是否是保留字

  ```java
  private Entry reserve(String word) {
      if (categoryMap.get(word) != null) {
          return new Entry(lineNum, categoryMap.get(word), word);
      }
      return new Entry(lineNum, Category.IDENFR, word);
  } //Lexer.java
  ```

### 语法分析

#### 编码设计修改

在开始编码之前，我做了如下设想和工作：

- 重写lexer逻辑，处理成边读input边parse的形式：以为这样能够提升效率，但是后来发现编程难度大，并且难以预测语法成分，最终还是采用一遍词法分析一遍语法分析；
- 封装“向前看”逻辑：最终实现在entrytable中，简化了parse函数的流程，并且增加代码可读性。

最终语法分析的逻辑如下：

语法分析主要由`Parser`类实现，从词法分析得到的`EntryTable`中循环读入单词（`curPos`指向即将读到的单词），递归下降进行解析，然后建立抽象语法树。

`Node`类属性如下：

```java
public class Node {
    protected int startNum;
    protected int endNum;
    protected NodeType type;
    protected ArrayList<Node> children;
}
```

其他节点类都继承`Node`。

#### 改写文法

建立语法树时需要按照文法对每个终结符和非终结符建立节点，为了消除左递归和方便后期代码编写，首先对文法进行改写：

- 将`Stmt`的产生式改写为`Stmt -> AssignStmt | ExpStmt | BlockStmt | IfStmt | ForStmt | BreakStmt | ContinueStmt | ReturnStmt | GetIntStmt | PrintfStmt`；
- for循环`'for' '(' [ForStmt] ';' [Cond] ';' [forStmt] ')' Stmt`改写为`'for' '('[ForInit] ';' [Cond] ';' [ForInit] ')' Stmt`；
- 将`AddExp`使用巴克斯范式范式改写为`AddExp -> MulExp {('+' | '-') MulExp}`，`MulExp，RelExp, EqExp, LAndExp, LOrExp`同理；

- 将所有的终结符包装成一个类`TerminalNode`；
- 删除`BType`，将其加入`TerminalNode`类中；

#### 递归下降

按照每条文法进行程序的编写，示例如下：

```java
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
} // Parser.java
```

最后返回节点的时候的时候使用工厂模式生成节点，`createNode()`如下：

```java
public static Node createNode(int start, int end, NodeType nodeType, ArrayList<Node> children) {
    Helper.printParser("<" + nodeType.toString() + ">\n");
    return switch (nodeType) {
        case CompUnit -> new CompUnitNode(start, end, nodeType, children);
        case Decl -> new DeclNode(start, end, nodeType, children);
        //...
        default -> null;
    };
} // NodeFactory.java
```

在解析过程中需要向前看进行语法成分的判断，我将这一逻辑包装到了`EntryTable`不同的`check()`方法中，用于判断是否是相应的语法成分或者字符或者类别码：

```java
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
            return entries.get(curPos).getCategory() == Category.CONSTTK; 
        }
        case ConstInitVal -> {
            return check(NodeType.ConstExp) || entries.get(curPos).getCategory() == Category.LBRACE; //*****第二种情况可能和block相同
        }
        // ...
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
} // EntryTable.java
```

同时，需要注意在`stmt`的判断中，`assign,getint,exp`三种情况有重合，需要解析一个表达式之后才能进行判断，于是在进入这三种情况的时候，首先对`EntryTable`设置成为readonly模式，然后调用`parseExp`，然后再判断是哪种表达式，识别成功之后返回readonly处。逻辑如下:

```java
private Node parseStmt() {
    if (entryTable.check("if")) {
        //...
    } else {
        ArrayList<Node> children = new ArrayList<>();
        int startNum = entryTable.getCurLine();

        // assign || getint || Exp
        entryTable.readOnly();
        parseExp();
        if (entryTable.check("=")) {
            entryTable.fetch();
            if (entryTable.check("getint")) {
                entryTable.back2fetch();
                // ...
                return NodeFactory.createNode(startNum, endNum, NodeType.GetintStmt, children);
            } else { // assign
                entryTable.back2fetch();
                // ...
                return NodeFactory.createNode(startNum, endNum, NodeType.AssignStmt, children);
            }
        } else { // Exp
            entryTable.back2fetch();
            // ...
            return NodeFactory.createNode(startNum, endNum, NodeType.ExpStmt, children);
        }
    }
} // Parser.java
```

最后是输出语法树的注意事项：

- `readonly`时要关闭Helper的输出，`back2fetch`的时候再打开；

- 主要输出逻辑在`createNode`中，所以改写文法导致输出`AddExp`时可能会少输出`<AddExp>`，需要在parse时加补丁：

  ```java
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
  } // Parser.java
  ```

### 错误处理

#### 编码设计修改

在写代码之前，我分析了错误的形式，做了如下工作和设想：

- 首先建立符号表，然后挨个思考如何处理语义错误（见下），再分别实现；

- 建立一个描述形参类型的类，写cmp方法；调用函数的时候将维数和标准类型作比较
  - 这里遇到了困难，由于此时尚未代码生成，我天真的认为把所有的值都计算出来存到符号表中就可以完成维数的匹配
  - 但是函数调用的返回值无法计算，除非重新进入函数在符号表计算一遍然后找到返回值
  - 后来发现错误处理没有数组维数匹配的错误，于是写到一半的calcu函数作罢；
- 在符号表中存值，在数组声明的时候将数组的维数存到符号表中：采用，并最终存initval
- 在符号表中维护变量的层数：最终没有采用，采用栈式符号表不需要维护层数
- 在每个符号表中维护父符号表id和子符号表idlist：也没有采用，栈式符号表是动态的

最终错误处理的逻辑如下：

在错误处理阶段，根据每条文法中可能出现的错误挨个编写程序，将错误分为语法错误和语义错误分别处理：

- 语法错误：在`Parser`解析时完成；
- 语义错误：调用每个节点的`handleError()`方法，自顶向下遍历语法树完成，在这个过程中需要建立符号表

由于分别处理两种错误，不是按照行号先后顺序，则发现错误之后调用`Helper`的`storeError()`方法，错误都处理完之后再排序输出。

#### 语法错误

这一部分相对容易，只需要在解析的时候加入判断逻辑即可，以`BreakStmt`为例:

```java
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
} // Parser.java
```

但还需要改写一部分`EntryTable`的判断逻辑，例如：

对于`UnaryExp → PrimaryExp | Ident '(' [FuncRParams] ')' | UnaryOp UnaryExp`，对`Ident '(' [FuncRParams] ')'`是否有参数的判断是检查下一个entry是否是`)`，而此时会出现缺少`)`的错误，则修改判断为下一个是否是`Exp`：

```java
if (entryTable.check(NodeType.Exp)) { // 修改，原来是!")"
    children.add(parseFuncRParams());
}
if (entryTable.check(")")) { // error j )
    children.add(parseTerminal());
} else {
    Helper.storeError(entryTable.getCurLine(1), ErrorType.j);
} // Parser.java
```

另外对于非法符号：0-31,34-39,127，且单独出现92\： 遍历`Lexer`中的formatstring检查即可

#### 语义错误

需要注意：在语义错误的处理过程中可能会受到语法错误的影响，此时的语法树并不是符合文法定义的。

##### 建立符号表

在此部分需要建立符号表，符号表相关类如下：

```java
public class Symbol {
    private String token;
    private SymbolType symbolType; // 常量，变量还是函数
    private int table;
    private String reg; // 寄存器的名字
    private Value value;
}
```

子类有`ArraySymbol, FuncSymbol, SimpleSymbol`，部分属性如下：

```java
public class SimpleSymbol extends Symbol{
    private int initialVal;
}
public class ArraySymbol extends Symbol { // 数组变量\常量，包括函数形参
    private int d1; //一维
    private int d2;
    private ArrayList<Integer> initialVal; 
}
public class FuncSymbol extends Symbol{
    private ReturnType returnType;
    private int paraNum;
    private ArrayList<FuncParamType> paras;
}
```

使用`SymbolManager`类统一管理符号表，部分属性如下：

```java
public class SymbolManager {
    private static final SymbolManager MANAGER = new SymbolManager(); // 单例模式
    private int curId; // 当前所在的符号表id
    private Stack<SymbolTable> tableStack; // 当前所在的 table链(执行
    private HashMap<String, SymbolTable> tables; // 所有解析的table, 对于函数，可以用id，也可以用函数名
    private int loop; // 记录循环的层数
    private boolean isGlobal;
    private int maxId;
}
```

符号表维护逻辑如下：

- 进入大括号后，新建符号表；遇到declare，在当前符号表新建符号项；

- 离开大括号后符号表弹栈，切换当前符号表到调用它的符号表；
- 函数和block的符号表维护稍有差异，函数需要在定义时建表，将形参也放入符号表中，进入最外层block时不需要建表（使用placeholder区别）

##### 遍历语法树

每种语义错误的处理逻辑如下：

- 名字重定义：遇到def的时候查最顶层符号表

- 未定义的ident：ident使用的时候查整个栈的符号表（lval和函数调用出现）

- 参数个数不匹配：给`FuncSymbol`保存一个` ArrayList<FuncParamType>`，调用函数的时候查符号表

- 参数类型不匹配：同上

- return语句：`SymbolManager`维护一个当前函数的`ReturnType`，遇到return语句或者退出函数的时候判断（包括main函数

- 不能改变常量的值：assign,  getint时查表

- printf不匹配：`printfStmt.handleError()`时保存%d和exp的个数

- 非循环块中使用break和continue：`SymbolManager`维护loop，遇到这俩判断是否在循环中

**注意**

- 函数参数类型匹配新建`FuncParaType`类，并根据匹配规则重写equals方法：

  ```java
  public class FuncParamType {
      private int type; //0 simple 1 一维  2 二维 3 void 函数
      private int dim;
      
      @Override
      public boolean equals(Object other) {
          if (other instanceof FuncParamType) {
              return type == ((FuncParamType) other).getType()
                      && dim == ((FuncParamType) other).getDim();
          }
          return false;
      }
  
      public FuncParamType merge(FuncParamType other) { // 这种类型之间做运算
          if (other == null) {
              return this;
          }
          int type = max(getType(), other.getType());
          int dim = max(getDim(), other.getDim());
          return new FuncParamType(type, dim);
      }
  } // FuncParaType.java
  ```

  在**函数定义**时解析出参数列表并存在symbol中，对每个函数参数有一个`calcu()`方法返回参数类型：

  ```java
  public FuncParamType calcu(int placeHolder) {
      if (children.size() == 2) {
          return new FuncParamType(0);
      }
      for (Node node: children) {
          if (node instanceof ConstExpNode) { // 二维数组形参
              return new FuncParamType(2, node.calcu());
          }
      }
      return new FuncParamType(1); //一维数组
  } // FuncParamNode.java
  ```

  并给Node添加eval方法，在**函数调用**时自顶向下返回该node的类型：

  ```java
  @Override
  public FuncParamType eval() {
      return children.get(0).eval();
  } // e.g. ExpNode.java
  ```

  每种exp重写该方法，返回所有子节点`merge`之后的type，再与符号表中的定义时保存的参数类型作比较判断即可

- return语句不匹配分成两种情况：

  - return语句和声明的函数类型不匹配：遇到return语句时调用`SymbolManager`：

    ```java
    if (isReturnVal && symbolManager.checkReturnType(ReturnType.VOID)) { 
        Helper.storeError(children.get(0).getStartNum(), ErrorType.f);
    } else {
        // ...
    } // ReturnStmtNode.java
    ```

  - 缺少return语句：由于已经在函数定义时将当前函数的返回类型保存在`SymbolManager`中，则对函数体的block`handleError`时判断即可：

    ```java
    public void handleError(int placeHolder) { // 给函数体用的
        for (Node node: children) {
            node.handleError();
        }
        if (children.size() == 2) { // 空函数，只有{}
            if (SymbolManager.getManager().checkReturnType(ReturnType.INT)) {
                Helper.storeError(this.getEndNum(), ErrorType.g);
                return;
            }
        }
        int num = children.size() - 2;
        if (SymbolManager.getManager().checkReturnType(ReturnType.INT)
                && !(children.get(num).getChildren().get(0) instanceof ReturnStmtNode
                && ((ReturnStmtNode) children.get(num).getChildren().get(0)).isReturnVal())) { // 当前函数的返回值类型在manager内，此时可以check是否符合return
            Helper.storeError(this.getEndNum(), ErrorType.g);
        }
    }
    ```

### 代码生成

#### 编码设计修改

在写代码生成之前，我做了以下思考和行动：

- 先把之前遍历语法树生成的symbol存起来（常量、变量、函数、形参
- 把错误处理的符号表存下来，建立成树形符号表以便代码生成使用
- 使用clang工具链生成了几个.ll文件，阅读建立llvm建构
  - instr保存类型和parent block
- 给node编写generate方法生成指令存入一个string的list中
  - 此时对exp的编写使用calcu方法返回exp的值
  - 建立`Val`和`Reg`类，保存值和寄存器，继承`Value`，作为instr的操作数

最终在编码的过程中舍弃了树形符号表，还是采用了边生成边建表的方式，具体原因见下；instr不保存parent block，而由bb直接保存；舍弃val和reg类，建立统一的value类，并且generate方法改返回Value，没有返回值时返回null即可。

#### llvm架构

中间代码采用llvm形式，生成llvm ir的过程就是根据建立的语法树重新生成一个ir结构的代码的过程。分析一段llvm程序发现：

![image-20231220142055355](C:\Users\xqy\AppData\Roaming\Typora\typora-user-images\image-20231220142055355.png)

- 顶层`Module`：Global var，Funcdef
- 每个`Function`: paras, basic block
- `Basic Block`: label， instruction
- instruction：operand（常数或者instruction）

则根据以上的架构，我们可以写出建构llvm的相关类及其属性：`Module, Function, Para, Basic Block, GlobalVar, Instr, Constant`；由”llvm的一切皆Value的原则“，建立`Value`顶层类，让所有llvm相关类继承它。

对于每条llvm指令的每个操作数，都有其label和类型，则`Value`应该具备的属性如下：

```java
public class Value {
    protected String name; 
    private Type type; 
} // Value.java
```

其中`Type`为llvm的类型系统，建立`Type`基类，子类包括`SimpleType, ArrayType, PointerType, FixedType`（最后一项包含Function，Module，BB这些固定类型）。

```java
public class SimpleType extends Type { // 简单变量 int
    public static SimpleType INT32 = new SimpleType(32);
    public static SimpleType INT1 = new SimpleType(1);
    public static SimpleType VOID = new SimpleType(0);
    private int type; // int1 int32 或者 void
}

public class ArrayType extends Type {
    private Type elementType;
    private int length;
}

public class PointerType extends Type { // 函数数组传参
    private Type target;
}

public class FixedType extends Type {
    public static FixedType FUNCTION = new FixedType();
    public static FixedType MODULE = new FixedType();
    public static FixedType BB = new FixedType();
}
```

而对于不同的llvm指令，则对应建构类继承`Instr`，

#### 实现细节

遍历语法树，对Node节点添加`generate()`方法，子类重写该方法，递归下降生成llvm代码。设置返回值为`Value`类型，是因为对于所有的instruction指令操作数都是`Value`，一条add指令的操作数可能是上一条load指令出来的value，示例如下：

```java
@Override
public Value generate() { // 直接返回计算出最终结果的指令
    Value op1 = children.get(0).generate();
    Value op2;
    Value ret = op1;

    for (int i = 1; i < children.size(); i += 2) {
        if (children.get(i) instanceof TerminalNode && ((TerminalNode) children.get(i)).getCategory() == Category.PLUS) {
            op2 = children.get(i + 1).generate();
            String regName = InstrManager.getInstance().newReg();
            ret = new AluInstr(InstrType.ALU, AluOp.ADD, regName, op1, op2);
            op1 = ret;
        } else {
            op2 = children.get(i + 1).generate();
            String regName = InstrManager.getInstance().newReg();
            ret = new AluInstr(InstrType.ALU, AluOp.SUB, regName, op1, op2);
            op1 = ret;
        }
    }
    return ret;
} // AddExpNode.java
```

另外，在Instr的构造函数中调用insertInstr，能够自动将new出来的指令添加到当前bb中。

```java
public Instr(InstrType instrType, Type type, String name) {
    super(name, type);
    this.instrType = instrType;
    InstrManager.getInstance().insertInstr(this); // 在这里实现了把new的指令都放进去
}
```

##### 符号表管理

此时也需要建立符号表，和错误处理的符号表建表逻辑相同；使用`InstrManager`类来管理中间代码生成，对于`Value`的命名如下：

- 源代码显式变量：在建立符号表时将名字存入symbol，生成格式为`%var<tableId>_cnt`；

- 函数：同上，生成格式为`<functionName>`；

- 基本块：生成格式为`<前缀>cnt`，前缀可能为`then, else, loop_begin`等；

- 中间变量：调用`InstrManager`中的`newReg()`方法，生成格式为`%tmp<tableId>_cnt`：

  ```java
  public String newReg() { // 返回当前table作用域下的新的寄存器编号
      SymbolTable table = SymbolManager.getManager().getCurTable();
      int size = 0;
      if (regSize.get(String.valueOf(table.getId())) == null) {
          size = 1;
          regSize.put(String.valueOf(table.getId()), 1);
      } else {
          size = regSize.get(String.valueOf(table.getId())) + 1;
          regSize.put(String.valueOf(table.getId()), regSize.get(String.valueOf(table.getId())) + 1);
      }
      return "%tmp" + table.getId() + "_" + size;
  } 
  public String newBB(String tip) {
      SymbolTable table = SymbolManager.getManager().getCurTable();
      bbId++;
      return tip + bbId;
  } // InstrManager.java
  ```

注意仍然需要栈式符号表边解析边生成，若沿用已生成的符号表，错误在于：

```c
int b = 1;
int main() {
    int a = b, b;
    return 0;
}
```

对于以上代码，在解析`a = b`时应该使用顶层符号表的b，但由于查找当前作用域变量优先，则使用到后一个定义的b，出错。

**注意：**

- 为了方便使用，修改部分节点的`createSymbol()`，将node对应的symbol保存在属性中；
- 对`Symbol`类新增属性`value`，保存该symbol对应的llvm值；
- 对于每个函数变量，在进入函数之后开辟新空间保存，并修改其value为新的`load`指令

##### const优化

由于const必定包含初始值，初始化只包含常量ident，且不会涉及到值修改，则可以将初始值保存到符号表中，优化如下：

- 对于每个constInitval可以使用`Constant`优化，直接递归计算出初值然后保存到符号表中:

  ```java
  for (Node node : children) {
      if (node instanceof ConstExpNode) {
          dims.add(node.calcu());
      } else if (node instanceof ConstInitValNode) {
          this.assign = true;
          if (dims.isEmpty()) { // 非数组变量，初值只有一个const exp表达式
              vals.add(node.calcu());
          } else if (dims.size() == 1) { //一维数组
              vals = ((ConstInitValNode) node).calcu(vals, dims.get(0));
          } else {
              vals = ((ConstInitValNode) node).calcu(vals, dims.get(0), dims.get(1));
          }
      }
  } // ConstDefNode.java
  ```

  使用Node类的`calcu()`函数实现，对exp重写该方法，以`AddExpNode`为例

  ```java
  @Override
  public int calcu() {
      int val = children.get(0).calcu(); //先把第一个mul的值取出来，再一对一对的取op和mul
      for (int i = 1; i < children.size(); i += 2) {
          if (((TerminalNode) children.get(i)).getCategory() == Category.PLUS) {
              val += children.get(i + 1).calcu();
          } else {
              val -= children.get(i + 1).calcu();
          }
      }
      return val;
  } // AddExpNode.java
  ```

  而lval的`calcu()`直接查表：

  ```java
  @Override
  public int calcu() { // 查符号表
      String token = ((TerminalNode) children.get(0)).getValue();
      Symbol symbol = SymbolManager.getManager().getSymbol(token);
      int x, y;
      switch (children.size()) {
          case 1: //普通变量
              return ((SimpleSymbol) symbol).getInitialVal();
          case 3, 4: //一维数组 a[2], a[2
              y = children.get(2).calcu();
              return ((ArraySymbol) symbol).getVal(0, y);
          case 5, 6, 7: //二维数组 a[2[3, a[2][3
              x = children.get(2).calcu();
              y = children.get(5).calcu();
              return ((ArraySymbol) symbol).getVal(x, y);
          default:
              return 0; // error
      }
  } // LValNode.java
  ```

- 对const进行evaluate时直接从符号表中查找值，以`Constant`返回即可：

  ```java
  if (symbol instanceof SimpleSymbol simpleSymbol) {
      if (simpleSymbol.isConst()) {
          return new Constant(simpleSymbol.getInitialVal());
      } else {
          String regName = InstrManager.getInstance().newReg();
          String pointer = symbol.getReg();
          return new LoadInstr(InstrType.LOAD, SimpleType.INT32, regName, pointer);
      }
  } //LValNode.java
  ```

##### 数组

关键指令是`getelementptr`，gep的type是指向元素的reference type；保存一个baseAddr用来写toString。

保留了二维数组的形式，在`LValNode`类中evaluate的逻辑是蛮力if，如下：

```java
@Override
public Value generate() {
    String token = ((TerminalNode) children.get(0)).getValue();
    Symbol symbol = SymbolManager.getManager().getSymbol(token); 
    if (symbol instanceof SimpleSymbol simpleSymbol) {
       // ...
    } else {
        ArraySymbol arraySymbol = (ArraySymbol) symbol;
        ArrayList<Value> offsets = new ArrayList<>();

        if (symbol.getValue() instanceof GlobalVar) { // 全局变量
            if (children.size() == 1) { 
                // 这种只可能出现在函数调用中
            } else if (children.size() == 4) {
                if (arraySymbol.isTwoDim()) {
                    // 二维数组取一维
                } else {
                    // 一维数组取值
                }
            } else {
            	// 二维数组取值    
            }
        } else { // 局部变量或者是函数参数 他们的value都是alloca->PointerType
            Type targetType = ((PointerType) symbol.getValue().getType()).getTarget();

            if (targetType instanceof ArrayType) { //局部变量
                if (children.size() == 1) { 
                    // 取数组本身
                } else if (children.size() == 4) {
                    // ...
                } else {
                    // ...
                }
            } else { // 函数参数
                LoadInstr loadInstr = new LoadInstr(InstrType.LOAD, targetType, InstrManager.getInstance().newReg(), symbol.getReg());
                Type reference = ((PointerType) targetType).getTarget();
                if (children.size() == 1) { 
                    // 如果传进来的是a[]i32* 或者b[][2] [2 x i32]*， 使用a或者b
                } else if (children.size() == 4) {
                    // ...
                } else {
                    // ...
                }
            }
        }
    }
}
```

- global var：llvmType就是数组type
  - 直接从0开始偏移
- 局部变量：value是alloca，type是数组的pointer type
  - 先load，然后和global var一样
- 函数参数：
  - 一维数组：i32*，value对应的是i32**
    - 先load成i32*, 再偏移
  - 二维数组：一维数组指针，value对应的是[2 x i32]**
    - 先load成[2 x i32]*，再偏移

**注意**

- 非const的全局数组无初值要置零

##### 短路求值

```java
public void genBranch(BasicBlock trueBB, BasicBlock falseBB) {}
```

为了实现短路求值，则需要在解析`LAndExp`时将需要跳转的bb编号传下去，随时准备生成branch和jump指令。短路求值的逻辑如下图：

![image-20231220154429443](C:\Users\xqy\AppData\Roaming\Typora\typora-user-images\image-20231220154429443.png)

- LOrExp
  - 解析中间的`LAndExp`时，递归调用`LAndExp`的`genBranch`：传入trueBB；若为false应该进入下一个`LAndExp`，新建BB，将新建的BB作为falseBB传入
  - 解析最后一个：调用`genBranch`：false，true分别跳转即可
- LAndExp
  - 解析中间的EqExp：生成branch指令，若为true继续进入下一个BB（新建BB）；若为false直接跳转falseBB
  - 解析最后一个：生成branch指令，分别跳转
- EqExp，RelExp
  - 递归生成Icmp指令，注意类型提升（若有操作数为i1，则先提升为i32再比较）

##### 循环跳转

这个实现较为容易，将当前循环的自增BB和followBB保存在`InstrManager`中即可。注意使用栈保存，因为可能存在循环嵌套，退出循环时分别pop即可。

### 附录

#### 实验文法

```
编译单元    CompUnit → {Decl} {FuncDef} MainFuncDef  
声明  Decl → ConstDecl | VarDecl
常量声明    ConstDecl → 'const' BType ConstDef { ',' ConstDef } ';' // i
基本类型    BType → 'int'
常数定义    ConstDef → Ident { '[' ConstExp ']' } '=' ConstInitVal  // b k
常量初值    ConstInitVal → ConstExp
    | '{' [ ConstInitVal { ',' ConstInitVal } ] '}' 
变量声明    VarDecl → BType VarDef { ',' VarDef } ';' // i
变量定义    VarDef → Ident { '[' ConstExp ']' } // b
    | Ident { '[' ConstExp ']' } '=' InitVal // k
变量初值    InitVal → Exp | '{' [ InitVal { ',' InitVal } ] '}'
函数定义    FuncDef → FuncType Ident '(' [FuncFParams] ')' Block // b g j
主函数定义   MainFuncDef → 'int' 'main' '(' ')' Block // g j
函数类型    FuncType → 'void' | 'int' 
函数形参表   FuncFParams → FuncFParam { ',' FuncFParam } 
函数形参    FuncFParam → BType Ident ['[' ']' { '[' ConstExp ']' }]  //   b k
语句块     Block → '{' { BlockItem } '}' 
语句块项    BlockItem → Decl | Stmt 
语句  Stmt → LVal '=' Exp ';' | [Exp] ';' | Block // h i
    | 'if' '(' Cond ')' Stmt [ 'else' Stmt ] // j
    | 'for' '('[ForStmt] ';' [Cond] ';' [ForStmt] ')' Stmt
    | 'break' ';' | 'continue' ';' // i m
    | 'return' [Exp] ';' // f i
    | LVal '=' 'getint''('')'';' // h i j
    | 'printf''('FormatString{,Exp}')'';' // i j l
语句 ForStmt → LVal '=' Exp   //h
表达式 Exp → AddExp 注：SysY 表达式是int 型表达式 
条件表达式   Cond → LOrExp 
左值表达式   LVal → Ident {'[' Exp ']'} // c k
基本表达式   PrimaryExp → '(' Exp ')' | LVal | Number 
数值  Number → IntConst 
一元表达式   UnaryExp → PrimaryExp | Ident '(' [FuncRParams] ')' // c d e j
        | UnaryOp UnaryExp 
单目运算符   UnaryOp → '+' | '−' | '!' 注：'!'仅出现在条件表达式中 
函数实参表   FuncRParams → Exp { ',' Exp } 
乘除模表达式  MulExp → UnaryExp | MulExp ('*' | '/' | '%') UnaryExp 
加减表达式   AddExp → MulExp | AddExp ('+' | '−') MulExp 
关系表达式   RelExp → AddExp | RelExp ('<' | '>' | '<=' | '>=') AddExp
相等性表达式  EqExp → RelExp | EqExp ('==' | '!=') RelExp
逻辑与表达式  LAndExp → EqExp | LAndExp '&&' EqExp
逻辑或表达式  LOrExp → LAndExp | LOrExp '||' LAndExp 
常量表达式   ConstExp → AddExp 注：使用的Ident 必须是常量
格式字符串:
<FormatString> → '"'{<Char>}'"' // a
```

#### 参考编译器

本编译器参考两位学长的课设编译器：

hjc-owo：https://github.com/hjc-owo/Compiler

hyggge：https://github.com/Hyggge/Petrichor
