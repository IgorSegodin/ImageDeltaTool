package org.segodin.statefulUI.state;

import java.util.HashMap;
import java.util.Map;

public class UIStateImpl implements UIState {

    private Map<String, Object> data = new HashMap<>();

    @Override
    public Object getParameter(String key) {
        return data.get(key);
    }

    @Override
    public <T> T getParameter(PropertyInfo<T> info) {
        return (T) data.get(info.getId());
    }

    public void setParameter(String key, Object value) {
        data.put(key, value);
    }
}
