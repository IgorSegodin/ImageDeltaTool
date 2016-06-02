package org.segodin.imageDeltaTool.component;

import org.segodin.imageDeltaTool.util.ImageUtil;
import org.segodin.statefulUI.action.UIAction;
import org.segodin.statefulUI.action.UIActionDispatcher;
import org.segodin.statefulUI.action.UIActionDispatcherAware;
import org.segodin.statefulUI.action.UIActionImpl;
import org.segodin.statefulUI.state.PropertyInfo;
import org.segodin.statefulUI.state.UIState;
import org.segodin.statefulUI.state.UIStateReducer;
import org.segodin.statefulUI.state.UIStatefulComponent;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Displays image, with resize possibility.
 * */
public class ResizableImagePanel extends JPanel implements UIStateReducer, UIStatefulComponent, UIActionDispatcherAware, MouseWheelListener {

    public static final String ACTION_DISPLAY_IMAGE = "ACTION_DISPLAY_IMAGE";
    public static final String ACTION_ZOOM_IMAGE = "ACTION_ZOOM_IMAGE";
    public static final List<String> supportActions = Arrays.asList(ACTION_DISPLAY_IMAGE, ACTION_ZOOM_IMAGE);

    public static final PropertyInfo<BufferedImage> PROP_DISPLAY_IMAGE = PropertyInfo.create("displayImage", BufferedImage.class);
    public static final PropertyInfo<Double> PROP_IMAGE_ZOOM = PropertyInfo.create("zoomImage", Double.class);

    /**
     * Key events
     * */
    public static final String ZOOM_KEY_DOWN = "ZOOM_KEY_DOWN";
    public static final String ZOOM_KEY_UP = "ZOOM_KEY_UP";

    public static final String HORIZONTAL_SCROLL_KEY_DOWN = "HORIZONTAL_SCROLL_KEY_DOWN";
    public static final String HORIZONTAL_SCROLL_KEY_UP = "HORIZONTAL_SCROLL_KEY_UP";

    /**
     * State-less variables
     * */
    protected int maxDimension = 500;
    private double minZoom = 1;
    private double maxZoom = 10;
    private double zoomStep = 0.5;

    /**
     * Instance variables
     * */
    private UIActionDispatcher dispatcher;
    private JScrollPane scrollPanel;
    private ImagePanel imagePanel;

    /**
     * Global state variables
     * */
    private BufferedImage selectedOriginalImage;
    private double zoom = 1;

    /**
     * Internal component state variables
     * */
    private double scrollPaneSizeFactor;
    private int scrollPaneWidth;
    private int scrollPaneHeight;
    private boolean zoomKeyPressed = false;
    private boolean horizontalScrollKeyPressed = false;

    @Override
    public void initialize() {
        setVisible(false);

        imagePanel = new ImagePanel();
        imagePanel.addMouseWheelListener(this);

        scrollPanel = new JScrollPane(imagePanel);

        add(scrollPanel);

        InputMap inputMap = getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_CONTROL, KeyEvent.CTRL_DOWN_MASK), ZOOM_KEY_DOWN);
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_CONTROL, 0, true), ZOOM_KEY_UP);

        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ALT, KeyEvent.ALT_DOWN_MASK), HORIZONTAL_SCROLL_KEY_DOWN);
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ALT, 0, true), HORIZONTAL_SCROLL_KEY_UP);

        ActionMap actionMap = getActionMap();
        actionMap.put(ZOOM_KEY_DOWN, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                zoomKeyPressed = true;
            }
        });
        actionMap.put(ZOOM_KEY_UP, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                zoomKeyPressed = false;
            }
        });
        actionMap.put(HORIZONTAL_SCROLL_KEY_DOWN, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                horizontalScrollKeyPressed = true;
            }
        });
        actionMap.put(HORIZONTAL_SCROLL_KEY_UP, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                horizontalScrollKeyPressed = false;
            }
        });
    }

    @Override
    public Map<String, Object> reduceState(UIState currentState, UIAction action) {
        return action.getData();
    }

    @Override
    public void stateChanged(UIState state) {
        boolean zoomChanged = false;
        Double newZoom = state.getParameter(PROP_IMAGE_ZOOM);
        if (newZoom != null) {
            zoomChanged = this.zoom != newZoom;
            this.zoom = newZoom;
        }

        BufferedImage newImage = state.getParameter(PROP_DISPLAY_IMAGE);
        if (zoomChanged) {
            resizeAndDrawImage(selectedOriginalImage);
        } else if (newImage != null &&
                (selectedOriginalImage == null || ImageUtil.notEquals(selectedOriginalImage, newImage))) {

            if (!isVisible()) {
                setVisible(true);
                revalidate();
            }

            this.selectedOriginalImage = newImage;
            recalculateAndSetPanelSize(selectedOriginalImage);
            resizeAndDrawImage(selectedOriginalImage);
        }
    }

    protected void recalculateAndSetPanelSize(BufferedImage original) {
        if (original.getWidth() >= original.getHeight()) {
            scrollPaneSizeFactor = maxDimension / ((double)original.getWidth());
        } else {
            scrollPaneSizeFactor = maxDimension / ((double)original.getHeight());
        }

        scrollPaneWidth = (int) (original.getWidth() * scrollPaneSizeFactor);
        scrollPaneHeight = (int) (original.getHeight() * scrollPaneSizeFactor);

        Dimension scrollPaneSize = new Dimension(scrollPaneWidth + 2, scrollPaneHeight + 2);
        scrollPanel.getViewport().setPreferredSize(scrollPaneSize);
        scrollPanel.revalidate();
    }

    protected void resizeAndDrawImage(BufferedImage original) {
        BufferedImage resized = new AffineTransformOp(
                AffineTransform.getScaleInstance(scrollPaneSizeFactor * zoom, scrollPaneSizeFactor * zoom),
                AffineTransformOp.TYPE_BILINEAR)
                .filter(original, null);

        imagePanel.setImage(resized);

        SwingUtilities.getWindowAncestor(this).pack();
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        if (zoomKeyPressed) {
            int wheelRotation = e.getWheelRotation() * -1;
            double newZoom = zoom + zoomStep * wheelRotation;
            if (newZoom < minZoom) {
                newZoom = minZoom;
            } else if (newZoom > maxZoom) {
                newZoom = maxZoom;
            }
            dispatcher.dispatch(createZoomImageAction(newZoom));
        } else {
            JScrollBar scrollBar;
            if (horizontalScrollKeyPressed) {
                scrollBar = scrollPanel.getHorizontalScrollBar();
            } else {
                scrollBar = scrollPanel.getVerticalScrollBar();
            }
            int newScrollValue = scrollBar.getValue() + scrollBar.getBlockIncrement() * e.getScrollAmount() * e.getWheelRotation();
            if (newScrollValue > scrollBar.getMaximum()) {
                newScrollValue = scrollBar.getMaximum();
            }
            if (newScrollValue < 0) {
                newScrollValue = 0;
            }
            scrollBar.setValue(newScrollValue);
        }
    }

    @Override
    public List<String> supportActions() {
        return supportActions;
    }

    @Override
    public void setUIActionDispatcher(UIActionDispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    public static UIAction createDisplayImageAction(BufferedImage image) {
        return new UIActionImpl(ACTION_DISPLAY_IMAGE).p(PROP_DISPLAY_IMAGE, image);
    }

    public static UIAction createZoomImageAction(Double zoom) {
        return new UIActionImpl(ACTION_ZOOM_IMAGE).p(PROP_IMAGE_ZOOM, zoom);
    }

}
