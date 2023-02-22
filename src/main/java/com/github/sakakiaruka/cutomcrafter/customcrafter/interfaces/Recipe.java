package com.github.sakakiaruka.cutomcrafter.customcrafter.interfaces;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public interface Recipe {
    public abstract List<ItemStack> getItemStackListNoAir();
    public abstract Material getLargestMaterial();
    public abstract int getLargestAmount();
    public abstract int getTotalItems();
    public abstract String info();
    public abstract boolean isEmpty();
}
