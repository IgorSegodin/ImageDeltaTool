package org.segodin.imageDeltaTool;

import org.segodin.imageDeltaTool.component.AppPanel;
import org.segodin.statefulUI.UIStatefulContainer;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import java.awt.Dimension;

public class App {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("App");
            frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            frame.setMinimumSize(new Dimension(100, 100));

            UIStatefulContainer statefulContainer = new UIStatefulContainer();

            frame.add(statefulContainer.createProxyContainer(AppPanel.class));

            frame.pack();
            frame.setVisible(true);
        });
    }

}
