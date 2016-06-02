package org.segodin.statefulUI.action;

import java.util.Map;

/**
 * Action, which should reduce (change) state.
 * Should not be used for User interaction with UI.
 * This action represents already happened changes which should be represented on UI.
 * */
public interface UIAction {

    /**
     * Unique identifier. Should be unique inside single state holder
     * */
    String getId();

    /**
     * Parameters of action
     * */
    Map<String, Object> getData();
}
