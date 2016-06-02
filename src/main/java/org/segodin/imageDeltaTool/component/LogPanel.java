package org.segodin.imageDeltaTool.component;

import org.segodin.statefulUI.action.UIAction;
import org.segodin.statefulUI.action.UIActionImpl;
import org.segodin.statefulUI.state.UIState;
import org.segodin.statefulUI.state.UIStateReducer;

import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import java.awt.Insets;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class LogPanel extends JScrollPane implements UIStateReducer {

    public static final String NEW_LINE = "\n";

    public static final String ACTION_LOG = "ACTION_LOG";

    protected List<String> supportActions = Collections.singletonList(ACTION_LOG);

    protected JTextArea log;

    public LogPanel() {
        super(createTextArea(5, 20));
        log = (JTextArea) getViewport().getView();
    }

    @Override
    public List<String> supportActions() {
        return supportActions;
    }

    @Override
    public Map<String, Object> reduceState(UIState currentState, UIAction action) {
        printLn(action.getData().get("message").toString());
        return null;
    }



    protected static JTextArea createTextArea(int rows, int columns) {
        JTextArea textArea = new JTextArea(rows, columns);
        textArea.setMargin(new Insets(5, 5, 5, 5));
        textArea.setEditable(false);
        return textArea;
    }

    public void printLn(String text) {
        log.append(text + NEW_LINE);
        log.setCaretPosition(log.getDocument().getLength());
    }

    public static UIAction createLogAction(String message) {
        return new UIActionImpl(ACTION_LOG).p("message", message);
    }

}
