package com.github.sakakiaruka.customcrafter.customcrafter.object.Matter;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;

import java.util.List;

public class Matter {
    private String name;
    private List<Material> candidate;
    private List<EnchantWrap> wrap;
    private int amount;
    private boolean mass;
    public Matter(String name,List<Material> candidate,List<EnchantWrap> wrap,int amount,boolean mass){
        this.name = name;
        this.candidate = candidate;
        this.wrap = wrap;
        this.amount = amount;
        this.mass = mass;
    }

    public Matter(List<Material> materials,int amount){
        this.name = "";
        this.candidate = materials;
        this.wrap = null;
        this.amount = amount;
        this.mass = false;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Material> getCandidate() {
        return candidate;
    }

    public void setCandidate(List<Material> candidate) {
        this.candidate = candidate;
    }

    public void addCandidate(List<Material> additional){
        this.candidate.addAll(additional);
    }

    public List<EnchantWrap> getWrap() {
        return wrap;
    }

    public void setWarp(List<EnchantWrap> warp) {
        this.wrap = warp;
    }

    public boolean hasWrap(){
        return wrap != null;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public boolean isMass() {
        return mass;
    }

    public void setMass(boolean mass) {
        this.mass = mass;
    }

    public int getEnchantLevel(Enchantment enchant){
        if(wrap == null)return -1;
        for(EnchantWrap w : wrap){
            if(w.getEnchant().equals(enchant))return w.getLevel();
        }
        return -1;
    }
}
