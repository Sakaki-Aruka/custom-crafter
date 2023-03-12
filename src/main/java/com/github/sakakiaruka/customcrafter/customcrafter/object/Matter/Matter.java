package com.github.sakakiaruka.customcrafter.customcrafter.object.Matter;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;

import java.util.List;

public class Matter {
    private String name;
    private List<Material> candidate;
    private EnchantWrap warp;
    private int amount;
    private boolean mass;
    public Matter(String name,List<Material> candidate,EnchantWrap warp,int amount,boolean mass){
        this.name = name;
        this.candidate = candidate;
        this.warp = warp;
        this.amount = amount;
        this.mass = mass;
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

    public EnchantWrap getWarp() {
        return warp;
    }

    public void setWarp(EnchantWrap warp) {
        this.warp = warp;
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
}
