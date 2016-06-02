package org.segodin.statefulUI.action;

/**
 * To inject dispatcher use {@link org.segodin.statefulUI.component.UIContainerFactory#createProxyContainer(Class)}
 * */
public interface UIActionDispatcherAware {

    void setUIActionDispatcher(UIActionDispatcher dispatcher);
}
