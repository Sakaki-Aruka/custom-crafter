package com.github.sakakiaruka.customcrafter.customcrafter.object;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;

import static com.github.sakakiaruka.customcrafter.customcrafter.SettingsLoad.LINE_SEPARATOR;

public class AmorphousVirtualContainer {
    private List<Material> candidate;
    private List<NamespacedKey> keys;
    private List<PersistentDataType> types;
    private List<Object> values;
    private List<String> tags;
    private int amount;

    public AmorphousVirtualContainer(List<Material> candidate, List<NamespacedKey> keys, List<PersistentDataType> types, List<Object> values, List<String> tags, int amount) {
        this.candidate = candidate;
        this.keys = keys;
        this.types = types;
        this.values = values;
        this.tags = tags;
        this.amount = amount;
    }

    public List<Material> getCandidate() {
        return candidate;
    }

    public void setCandidate(List<Material> candidate) {
        this.candidate = candidate;
    }

    public List<NamespacedKey> getKeys() {
        return keys;
    }

    public void setKeys(List<NamespacedKey> keys) {
        this.keys = keys;
    }

    public List<PersistentDataType> getTypes() {
        return types;
    }

    public void setTypes(List<PersistentDataType> types) {
        this.types = types;
    }

    public List<Object> getValues() {
        return values;
    }

    public void setValues(List<Object> values) {
        this.values = values;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public String info() {
        StringBuilder builder = new StringBuilder();
        builder.append("candidate: " + LINE_SEPARATOR);
        for (Material m : candidate) {
            builder.append("  -> " + m.name() + LINE_SEPARATOR);
        }
        builder.append("keys: " + LINE_SEPARATOR);
        for (NamespacedKey key : keys) {
            builder.append("  -> " + key.getKey() + LINE_SEPARATOR);
        }
        builder.append("types: " + LINE_SEPARATOR);
        for (PersistentDataType type : types) {
            builder.append("  -> " + type.toString() + LINE_SEPARATOR);
        }
        builder.append("tags: "+ LINE_SEPARATOR);
        for (String t : tags) {
            builder.append("  -> "+t+ LINE_SEPARATOR);
        }
        return builder.toString();
    }
}
