package org.segodin.imageDeltaTool.component.button;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

public abstract class FileSelectButton extends JButton implements ActionListener {

    protected JFileChooser fileChooser;

    public FileSelectButton(String text, JFileChooser fileChooser) {
        super(text);
        this.fileChooser = fileChooser;

        addActionListener(this);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        int openStatus = fileChooser.showOpenDialog(null);
        switch (openStatus) {
            case JFileChooser.APPROVE_OPTION: {
                File file = fileChooser.getSelectedFile();
                onFileSelected(file, fileChooser);
                break;
            }
            case JFileChooser.CANCEL_OPTION: {
                onCancel(fileChooser);
                break;
            }
            case JFileChooser.ERROR_OPTION: {
                onError(fileChooser);
                break;
            }
            default: {
                throw new IllegalStateException("Unexpected dialog status: " + openStatus);
            }
        }
    }

    protected abstract void onFileSelected(File file, JFileChooser fileChooser);

    protected void onCancel(JFileChooser fileChooser) {}

    protected void onError(JFileChooser fileChooser) {}
}
