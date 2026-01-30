package com.codeflu.typewriter;

/**
 * Represents a single line of text that can be reordered for prioritized typing.
 * This class is used in the table view for drag-and-drop reordering functionality.
 */
public class ReorderLine {

    private int lineNumber;
    private String lineText;

    /**
     * Constructs a ReorderLine with the specified line number and text.
     *
     * @param lineNumber the line number (1-based index)
     * @param lineText   the content of the line
     */
    public ReorderLine(int lineNumber, String lineText) {
        this.lineNumber = lineNumber;
        this.lineText = lineText;
    }

    /**
     * Gets the text content of this line.
     *
     * @return the line text
     */
    public String getLineText() {
        return lineText;
    }

    /**
     * Gets the line number (priority).
     *
     * @return the line number
     */
    public int getLineNumber() {
        return lineNumber;
    }

    /**
     * Sets the text content of this line.
     *
     * @param lineText the new line text
     */
    public void setLineText(String lineText) {
        this.lineText = lineText;
    }

    /**
     * Sets the line number (priority).
     *
     * @param lineNumber the new line number
     */
    public void setLineNumber(int lineNumber) {
        this.lineNumber = lineNumber;
    }
}
