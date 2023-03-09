package com.github.sakakiaruka.customcrafter.customcrafter.objects;

import com.github.sakakiaruka.customcrafter.customcrafter.interfaces.Materials;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;


public class RegexRecipeMaterial extends ItemStack implements Materials {
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

    public void initializeMatched(){
        Pattern p = Pattern.compile(this.pattern);
        //debug
    }

    public List<Material> getCandidate(){
        return matched;
    }
}
