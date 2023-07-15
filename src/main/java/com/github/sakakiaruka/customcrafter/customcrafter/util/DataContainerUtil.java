package com.github.sakakiaruka.customcrafter.customcrafter.util;

import com.github.sakakiaruka.customcrafter.customcrafter.object.ContainerWrapper;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;
import java.util.Map;

public class DataContainerUtil{
    public PersistentDataType getDataType(String input) {
        if (input.equalsIgnoreCase("BOOLEAN")) return PersistentDataType.BYTE;
        if (input.equalsIgnoreCase("INTEGER")) return PersistentDataType.INTEGER;
        if (input.equalsIgnoreCase("STRING")) return PersistentDataType.STRING;
        return null;
    }

    public void addAllData(ItemStack in, List<ContainerWrapper> data) {
        ItemMeta meta = in.getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();
        for (ContainerWrapper content : data) {
            if (container.has(content.getKey(),content.getType())) continue;
            container.set(content.getKey(), content.getType(), content.getValue());
        }
    }
}
