package org.segodin.statefulUI.state;

public class PropertyInfo<TYPE> {

    private String id;
    private Class<TYPE> type;

    private PropertyInfo(String id, Class<TYPE> type) {
        this.id = id;
        this.type = type;
    }

    public static <T> PropertyInfo<T> create(String Id, Class<T> type) {
        return new PropertyInfo<>(Id, type);
    }

    public String getId() {
        return id;
    }

    public Class<TYPE> getType() {
        return type;
    }
}
