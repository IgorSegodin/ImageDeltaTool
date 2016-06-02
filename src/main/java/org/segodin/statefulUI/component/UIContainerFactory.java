package org.segodin.statefulUI.component;

import java.awt.Component;
import java.awt.Container;

/**
 * Main injection tool.
 * */
public interface UIContainerFactory {

    /**
     * @param containerClass Class, NOT interface. Instance of given type will be instantiated, and all it's UI dependencies will be set.
     * */
    <T extends Container> T createProxyContainer(Class<T> containerClass);

    <T extends Component> T initializeComponent(T component);
}
