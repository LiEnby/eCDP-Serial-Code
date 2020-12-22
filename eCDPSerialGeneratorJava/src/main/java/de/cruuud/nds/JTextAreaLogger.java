package de.cruuud.nds;

import javax.swing.*;

public class JTextAreaLogger implements ILogger {
    private JTextArea loggingPane;

    public JTextAreaLogger(JTextArea loggingPane) {
        this.loggingPane = loggingPane;
    }

    @Override
    public void logString(String data) {
        this.loggingPane.append(data + "\n");
    }
}
