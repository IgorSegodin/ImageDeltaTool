package org.segodin.statefulUI.action;

/**
 * Notifies State container about happened action
 * */
public interface UIActionDispatcher {

    /**
     * Dispatch implementation should be synchronous
     * */
    void dispatch(UIAction action);
}
