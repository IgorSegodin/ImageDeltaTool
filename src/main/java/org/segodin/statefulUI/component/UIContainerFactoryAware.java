package org.segodin.statefulUI.component;

/**
 * To inject factory use {@link org.segodin.statefulUI.UIStatefulContainer#createProxyContainer(Class)}
 * */
public interface UIContainerFactoryAware {

    void setUIContainerFactory(UIContainerFactory factory);
}
