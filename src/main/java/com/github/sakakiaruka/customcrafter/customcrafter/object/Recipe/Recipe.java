package com.github.sakakiaruka.customcrafter.customcrafter.object.Recipe;

import com.github.sakakiaruka.customcrafter.customcrafter.object.Matter.Matter;
import com.github.sakakiaruka.customcrafter.customcrafter.object.Result.Result;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public class Recipe {
    private String name;
    private Map<Coordinate, Matter> coordinate;
    private Map<Material, ItemStack> returnItems;
    private Result result;
    public Recipe(String name,Map<Coordinate,Matter> coordinate,Map<Material,ItemStack> returnItems,Result result){
        this.name = name;
        this.coordinate = coordinate;
        this.returnItems = returnItems;
        this.result = result;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<Coordinate, Matter> getCoordinate() {
        return coordinate;
    }

    public void setCoordinate(Map<Coordinate, Matter> coordinate) {
        this.coordinate = coordinate;
    }

    public Map<Material, ItemStack> getReturnItems() {
        return returnItems;
    }

    public void setReturnItems(Map<Material, ItemStack> returnItems) {
        this.returnItems = returnItems;
    }

    public Result getResult() {
        return result;
    }

    public void setResult(Result result) {
        this.result = result;
    }
}
