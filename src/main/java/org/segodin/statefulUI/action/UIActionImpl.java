package org.segodin.statefulUI.action;

import org.segodin.statefulUI.state.PropertyInfo;

import java.util.HashMap;
import java.util.Map;

public class UIActionImpl implements UIAction {

    private String id;

    private Map<String, Object> data;

    public UIActionImpl(String id) {
        this.id = id;
    }

    public UIActionImpl(String id, Map<String, Object> data) {
        this.id = id;
        this.data = data;
    }

    /**
     * @deprecated no type info, use {@link #p(PropertyInfo, Object)}
     * */
    @Deprecated
    public UIActionImpl p(String key, Object value) {
        if (data == null) {
            data = new HashMap<>();
        }
        data.put(key, value);
        return this;
    }

    public <T> UIActionImpl p(PropertyInfo<T> propertyInfo, T value) {
        if (data == null) {
            data = new HashMap<>();
        }
        data.put(propertyInfo.getId(), value);
        return this;
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public Map<String, Object> getData() {
        return data;
    }
}
