package com.github.sakakiaruka.customcrafter.customcrafter.object.Result;

import org.bukkit.enchantments.Enchantment;

import java.util.List;
import java.util.Map;

public class Result {
    private String name;
    private Map<Enchantment,Integer> enchantsInfo;
    private int amount;
    private Map<String, List<String>> metadata;
    private String nameOrRegex;
    private int matchPoint;

    public Result(String name,Map<Enchantment,Integer> enchantsInfo,int amount,Map<String,List<String>> metadata,String nameOrRegex,int matchPoint){
        this.name = name;
        this.enchantsInfo = enchantsInfo;
        this.amount = amount;
        this.metadata = metadata;
        this.nameOrRegex = nameOrRegex;
        this.matchPoint = matchPoint;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<Enchantment, Integer> getEnchantsInfo() {
        return enchantsInfo;
    }

    public void setEnchantsInfo(Map<Enchantment, Integer> enchantsInfo) {
        this.enchantsInfo = enchantsInfo;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public Map<String, List<String>> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, List<String>> metadata) {
        this.metadata = metadata;
    }

    public String getNameOrRegex() {
        return nameOrRegex;
    }

    public void setNameOrRegex(String nameOrRegex) {
        this.nameOrRegex = nameOrRegex;
    }

    public int getMatchPoint() {
        return matchPoint;
    }

    public void setMatchPoint(int matchPoint) {
        this.matchPoint = matchPoint;
    }
}
