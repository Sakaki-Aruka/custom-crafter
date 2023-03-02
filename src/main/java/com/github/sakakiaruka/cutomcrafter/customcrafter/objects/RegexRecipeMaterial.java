package com.github.sakakiaruka.cutomcrafter.customcrafter.objects;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;


public class RegexRecipeMaterial extends ItemStack {
    private RegexRecipeMaterialEnum enumType;
    private String pattern;
    private int matchPoint;
    private List<Material> matched;
    private ItemStack provisional;

    public RegexRecipeMaterial(RegexRecipeMaterialEnum enumType,String pattern,int matchPoint){
        this.enumType = enumType;
        this.pattern = pattern;
        this.matchPoint = matchPoint;
        this.matched = new ArrayList<>();
        this.provisional = null;
    }

    public ItemStack getProvisional() {
        return provisional;
    }

    public void setProvisional(ItemStack provisional) {
        this.provisional = provisional;
    }

    public RegexRecipeMaterialEnum getEnumType() {
        return enumType;
    }

    public void setEnumType(RegexRecipeMaterialEnum enumType) {
        this.enumType = enumType;
    }

    public String getPattern() {
        return pattern;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    public List<Material> getMatched() {
        return matched;
    }

    public void setMatched(List<Material> matched) {
        this.matched = matched;
    }

    public int getMatchPoint() {
        return matchPoint;
    }

    public void setMatchPoint(int matchPoint) {
        this.matchPoint = matchPoint;
    }

    public void addMatched(Material matched){
        this.matched.add(matched);
    }
}
