package lexer;

public class Entry {
    private int lineNum;
    private Category category;
    private String value;

    public Entry(int lineNum, Category category, String value) {
        this.lineNum = lineNum;
        this.category = category;
        this.value = value;
    }

    public Category getCategory() {
        return category;
    }

    public int getLineNum() {
        return lineNum;
    }

    public String getValue() {
        return value;
    }

    public String toString() {
        return category + " " + value + "\n";
    }
}
