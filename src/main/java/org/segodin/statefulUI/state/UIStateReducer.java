package org.segodin.statefulUI.state;

import org.segodin.statefulUI.action.UIAction;

import java.util.List;
import java.util.Map;

/**
 * Component specifies what type of actions it can reduce. And when actions happens component should return updated state
 * */
public interface UIStateReducer {

    /**
     * Supported action ids
     * */
    List<String> supportActions();

    /**
     * Should return updated with action data state
     * @return keys, which should be changed in global state. Rewrites only existing keys.
     * */
    Map<String, Object> reduceState(UIState currentState, UIAction action);
}
