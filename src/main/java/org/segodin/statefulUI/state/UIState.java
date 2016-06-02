package org.segodin.statefulUI.state;

/**
 * Global application state.
 * */
public interface UIState {

    /**
     * @deprecated no type info, use {@link #getParameter(PropertyInfo)}
     * */
    @Deprecated
    Object getParameter(String key);

    <T> T getParameter(PropertyInfo<T> info);
}
