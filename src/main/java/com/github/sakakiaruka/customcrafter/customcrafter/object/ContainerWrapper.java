package com.github.sakakiaruka.customcrafter.customcrafter.object;

import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataType;

import static com.github.sakakiaruka.customcrafter.customcrafter.SettingsLoad.LINE_SEPARATOR;

public class ContainerWrapper {
    private int order; //need
    private String tag; //need
    private NamespacedKey key; //need
    private PersistentDataType type;  //need
    private String value; //not need
    public ContainerWrapper(NamespacedKey key, PersistentDataType type, String value, int order, String tag) {
        this.key = key;
        this.type = type;
        this.value = value;
        this.order = order;
        this.tag = tag;
    }

    public Class getValueType() {
        return value.getClass();
    }

    public NamespacedKey getKey() {
        return key;
    }

    public void setKey(NamespacedKey key) {
        this.key = key;
    }

    public PersistentDataType getType() {
        return type;
    }

    public void setType(PersistentDataType type) {
        this.type = type;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getValue() {
        return value;
    }

    public boolean hasKey() {
        return key != null;
    }

    public boolean hasType() {
        return type != null;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String info() {
        StringBuilder builder = new StringBuilder();
        builder.append("key: ").append(key.toString()).append(LINE_SEPARATOR);
        builder.append("type: "+type != null ? type.getPrimitiveType().getSimpleName() : "null" + LINE_SEPARATOR);
        builder.append("tag: ").append(tag).append(LINE_SEPARATOR);
        builder.append("order: ").append(order).append(LINE_SEPARATOR);
        builder.append("content:").append(LINE_SEPARATOR);
        builder.append("  -> class: ").append(value.getClass().getSimpleName()).append(LINE_SEPARATOR);
        builder.append("  -> value: ").append(value).append(LINE_SEPARATOR);
        return builder.toString();
    }
}
