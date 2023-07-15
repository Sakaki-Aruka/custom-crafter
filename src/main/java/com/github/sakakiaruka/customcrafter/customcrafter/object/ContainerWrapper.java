package com.github.sakakiaruka.customcrafter.customcrafter.object;

import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataType;

import static com.github.sakakiaruka.customcrafter.customcrafter.SettingsLoad.nl;

public class ContainerWrapper {
    private NamespacedKey key;
    private PersistentDataType type;
    private Object value;
    public ContainerWrapper(NamespacedKey key, PersistentDataType type, Object value) {
        this.key = key;
        this.type = type;
        this.value = value;
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

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public String info() {
        StringBuilder builder = new StringBuilder();
        builder.append("key: "+key.toString()+nl);
        builder.append("type: "+type.getPrimitiveType().getSimpleName()+nl);
        builder.append("content:"+nl);
        builder.append("  -> class: "+value.getClass().getSimpleName()+nl);
        builder.append("  -> value: "+value.toString()+nl);
        return builder.toString();
    }
}
