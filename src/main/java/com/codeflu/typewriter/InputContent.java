package com.codeflu.typewriter;

import javafx.beans.property.SimpleBooleanProperty;

public class InputContent {
    private int lineNumber;
    private String preSpeech;
    private String line;
    private String postSpeech;
    private Integer delay=0;
    private SimpleBooleanProperty hasPause;

    public int getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(int lineNumber) {
        this.lineNumber = lineNumber;
    }

    public String getPreSpeech() {
        return preSpeech;
    }

    public void setPreSpeech(String preSpeech) {
        this.preSpeech = preSpeech;
    }

    public String getLine() {
        return line;
    }

    public void setLine(String line) {
        this.line = line;
    }

    public String getPostSpeech() {
        return postSpeech;
    }

    public void setPostSpeech(String postSpeech) {
        this.postSpeech = postSpeech;
    }

    public Integer getDelay() {
        return delay;
    }

    public void setDelay(Integer delay) {
        this.delay = delay;
    }

    public SimpleBooleanProperty getHasPause() {
        return hasPause;
    }

    public void setHasPause(SimpleBooleanProperty hasPause) {
        this.hasPause = hasPause;
    }

    public InputContent(int lineNumber, String preSpeech, String line, String postSpeech, Integer delay, Boolean hasPause) {
        this.lineNumber = lineNumber;
        this.preSpeech = preSpeech;
        this.line = line;
        this.postSpeech = postSpeech;
        this.delay = delay;
        this.hasPause = new SimpleBooleanProperty(hasPause);
    }
}
