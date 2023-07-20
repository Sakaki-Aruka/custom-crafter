package com.github.sakakiaruka.customcrafter.customcrafter.object;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;

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
}
