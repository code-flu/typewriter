package com.codeflu.typewriter;

public class InputContent {
    private int lineNumber;
    private String line;

    public InputContent(int lineNumber, String line) {
        this.lineNumber = lineNumber;
        this.line = line;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(int lineNumber) {
        this.lineNumber = lineNumber;
    }

    public String getLine() {
        return line;
    }

    public void setLine(String line) {
        this.line = line;
    }

    @Override
    public String toString() {
        return "InputContent{" +
                "lineNumber=" + lineNumber +
                ", line='" + line + '\'' +
                '}';
    }
}
