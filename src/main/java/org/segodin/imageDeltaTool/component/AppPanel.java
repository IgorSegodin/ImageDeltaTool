package org.segodin.imageDeltaTool.component;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.imageio.ImageIO;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.segodin.imageDeltaTool.component.button.FileSelectButton;
import org.segodin.imageDeltaTool.service.ImageDiffService;
import org.segodin.imageDeltaTool.service.ImageDiffServiceImpl;
import org.segodin.imageDeltaTool.service.data.BufferedImageData;
import org.segodin.imageDeltaTool.util.ImageUtil;
import org.segodin.statefulUI.action.UIAction;
import org.segodin.statefulUI.action.UIActionDispatcher;
import org.segodin.statefulUI.action.UIActionDispatcherAware;
import org.segodin.statefulUI.action.UIActionImpl;
import org.segodin.statefulUI.component.UIComponent;
import org.segodin.statefulUI.component.UIContainerFactory;
import org.segodin.statefulUI.component.UIContainerFactoryAware;
import org.segodin.statefulUI.state.UIState;
import org.segodin.statefulUI.state.UIStateReducer;
import org.segodin.statefulUI.state.UIStatefulComponent;

/**
 * Main app panel.
 * */
public class AppPanel extends JPanel implements UIComponent, UIContainerFactoryAware, UIActionDispatcherAware, UIStateReducer, UIStatefulComponent {

    public static final String ACTION_SELECT_IMAGE = "ACTION_SELECT_IMAGE";
    public static final List<String> supportActions = Collections.singletonList(ACTION_SELECT_IMAGE);

    public static final String PROP_ORIGINAL_IMAGE = "originalImage";
    public static final String PROP_ORIGINAL_IMAGE_LABEL = "originalImageLabel";

    public static final String PROP_COMPARE_IMAGE = "compareImage";
    public static final String PROP_COMPARE_IMAGE_LABEL = "compareImageLabel";

    private UIContainerFactory factory;
    private UIActionDispatcher dispatcher;

    private BufferedImage originalImage;
    private JLabel originalImageLabel;

    private BufferedImage compareImage;
    private JLabel compareImageLabel;

    private JButton displayDifferenceBtn;
    private JButton saveDisplayedBtn;

    private UIState localState;


    @Override
    public List<String> supportActions() {
        return supportActions;
    }

    @Override
    public void initialize() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        ImageDiffService imageDiffService = new ImageDiffServiceImpl();

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.addChoosableFileFilter(new ImageFilter());

        // Original image

        JPanel originalImagePanel = factory.createProxyContainer(JPanel.class);

        FileSelectButton originalImageSelectBtn = new FileSelectButton("Select original image", fileChooser) {
            @Override
            protected void onFileSelected(File file, JFileChooser fileChooser) {
                try {
                    BufferedImage newFile = ImageIO.read(file);
                    if (newFile == null) {
                        throw new Exception("Can not open file: " + file.getPath());
                    }
                    if (originalImage == null || ImageUtil.notEquals(originalImage, newFile)) {
                        dispatcher.dispatch(createActionSelectImage(
                                PROP_ORIGINAL_IMAGE, newFile,
                                PROP_ORIGINAL_IMAGE_LABEL, file.getPath()
                        ));
                        SwingUtilities.getWindowAncestor(this).pack();
                    }
                } catch (Exception e) {
                    dispatcher.dispatch(LogPanel.createLogAction(e.getMessage()));
                }
            }
        };
        JButton originalImageDisplayBtn = new JButton("Display");
        originalImageDisplayBtn.addActionListener(e -> {
            dispatcher.dispatch(ResizableImagePanel.createDisplayImageAction(originalImage));
        });
        originalImageLabel = new JLabel();
        originalImagePanel.add(originalImageSelectBtn);
        originalImagePanel.add(originalImageDisplayBtn);
        originalImagePanel.add(originalImageLabel);

        // Compare image

        JPanel compareImagePanel = factory.createProxyContainer(JPanel.class);

        FileSelectButton compareImageSelectBtn = new FileSelectButton("Select compare image", fileChooser) {
            @Override
            protected void onFileSelected(File file, JFileChooser fileChooser) {
                try {
                    BufferedImage newFile = ImageIO.read(file);
                    if (newFile == null) {
                        throw new Exception("Can not open file: " + file.getPath());
                    }
                    if (compareImage == null || ImageUtil.notEquals(compareImage, newFile)) {
                        dispatcher.dispatch(createActionSelectImage(
                                PROP_COMPARE_IMAGE, ImageIO.read(file),
                                PROP_COMPARE_IMAGE_LABEL, file.getPath()
                        ));
                        SwingUtilities.getWindowAncestor(this).pack();
                    }
                } catch (Exception e) {
                    dispatcher.dispatch(LogPanel.createLogAction(e.getMessage()));
                }
            }
        };
        JButton compareImageDisplayBtn = new JButton("Display");
        compareImageDisplayBtn.addActionListener(e -> {
            dispatcher.dispatch(ResizableImagePanel.createDisplayImageAction(compareImage));
        });
        compareImageLabel = new JLabel();
        compareImagePanel.add(compareImageSelectBtn);
        compareImagePanel.add(compareImageDisplayBtn);
        compareImagePanel.add(compareImageLabel);

        // Diff panel

        // show diff btn
        JPanel diffImagePanel = factory.createProxyContainer(JPanel.class);
        displayDifferenceBtn = new JButton("Display difference");
        displayDifferenceBtn.setEnabled(false);
        displayDifferenceBtn.addActionListener(event -> {
            if (ImageUtil.notEquals(originalImage, compareImage)) {
                try {
                    BufferedImage currentDisplayedImage = localState.getParameter(ResizableImagePanel.PROP_DISPLAY_IMAGE);

                    BufferedImage diff;
                    /**
                     * Need highlight difference on current displayed image
                     * */
                    if (currentDisplayedImage != null && currentDisplayedImage == originalImage) {
                        diff = imageDiffService.getHighlightedImage(new BufferedImageData(compareImage), new BufferedImageData(originalImage));
                    } else {
                        diff = imageDiffService.getHighlightedImage(new BufferedImageData(originalImage), new BufferedImageData(compareImage));
                    }

                    dispatcher.dispatch(ResizableImagePanel.createDisplayImageAction(diff));
                } catch (Exception e) {
                    dispatcher.dispatch(LogPanel.createLogAction(e.getMessage()));
                }
            } else {
                dispatcher.dispatch(LogPanel.createLogAction("Images are equal."));
            }
        });
        diffImagePanel.add(displayDifferenceBtn);

        // Save displayed image btn
        saveDisplayedBtn = new JButton("Save displayed");
        saveDisplayedBtn.setEnabled(false);
        saveDisplayedBtn.addActionListener(e -> {
            int dialogStatus = fileChooser.showSaveDialog(null);
            switch (dialogStatus) {
                case JFileChooser.APPROVE_OPTION: {
                    File selectedFile = fileChooser.getSelectedFile();
                    try {
                        ImageIO.write(localState.getParameter(ResizableImagePanel.PROP_DISPLAY_IMAGE), ImageUtil.getImageFormatName(selectedFile), selectedFile);
                        dispatcher.dispatch(LogPanel.createLogAction("Image saved: " + selectedFile.getPath()));
                    } catch (IOException e1) {
                        dispatcher.dispatch(LogPanel.createLogAction("Error during file saving: " + e1.getMessage()));
                    }
                    break;
                }
                case JFileChooser.ERROR_OPTION: {
                    dispatcher.dispatch(LogPanel.createLogAction("Error in save dialog."));
                }
            }
        });
        diffImagePanel.add(saveDisplayedBtn);

        // Add panels

        add(originalImagePanel);
        add(compareImagePanel);
        add(factory.createProxyContainer(ResizableImagePanel.class));
        add(diffImagePanel);
        add(factory.createProxyContainer(LogPanel.class));
    }

    @Override
    public void setUIContainerFactory(UIContainerFactory factory) {
        this.factory = factory;
    }

    @Override
    public void setUIActionDispatcher(UIActionDispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    @Override
    public Map<String, Object> reduceState(UIState currentState, UIAction action) {
        return action.getData();
    }

    @Override
    public void stateChanged(UIState state) {
        this.originalImage = (BufferedImage) state.getParameter(PROP_ORIGINAL_IMAGE);
        this.originalImageLabel.setText((String) state.getParameter(PROP_ORIGINAL_IMAGE_LABEL));

        this.compareImage = (BufferedImage) state.getParameter(PROP_COMPARE_IMAGE);
        this.compareImageLabel.setText((String) state.getParameter(PROP_COMPARE_IMAGE_LABEL));

        if (this.originalImage != null && this.compareImage != null) {
            displayDifferenceBtn.setEnabled(true);
        } else {
            displayDifferenceBtn.setEnabled(false);
        }

        state.getParameter(ResizableImagePanel.PROP_DISPLAY_IMAGE);

        if (state.getParameter(ResizableImagePanel.PROP_DISPLAY_IMAGE) != null) {
            this.saveDisplayedBtn.setEnabled(true);
        } else {
            this.saveDisplayedBtn.setEnabled(false);
        }

        this.localState = state;
    }

    public static UIAction createActionSelectImage(String imageKey, BufferedImage image, String labelKey, String label) {
        return new UIActionImpl(ACTION_SELECT_IMAGE).p(imageKey, image).p(labelKey, label);
    }
}
