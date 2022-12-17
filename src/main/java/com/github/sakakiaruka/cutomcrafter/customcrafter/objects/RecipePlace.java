package com.github.sakakiaruka.cutomcrafter.customcrafter.objects;

import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RecipePlace {
    int x;
    int y;
    ItemStack item;
    public RecipePlace(int x,int y,ItemStack item){
        this.x = x;
        this.y = y;
        this.item = item;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public ItemStack getItem() {
        return item;
    }

    public void setItem(ItemStack item) {
        this.item = item;
    }


}
