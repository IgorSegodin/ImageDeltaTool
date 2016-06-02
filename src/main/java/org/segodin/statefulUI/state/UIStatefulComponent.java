package org.segodin.statefulUI.state;

import org.segodin.statefulUI.component.UIComponent;

/**
 * Component will be notified when global state is changed.
 * Use {@link org.segodin.statefulUI.component.UIContainerFactory#createProxyContainer(Class)} to inject
 * */
public interface UIStatefulComponent extends UIComponent {

    void stateChanged(UIState state);
}
