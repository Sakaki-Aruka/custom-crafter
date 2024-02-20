package com.github.sakakiaruka.customcrafter.customcrafter.object.Result;

import org.bukkit.enchantments.Enchantment;

import java.util.Map;

public class Result {

    private String name;
    private Map<Enchantment,Integer> enchantsInfo;
    private int amount;
    private String nameOrRegex;
    private int matchPoint;

    public Result(String name,Map<Enchantment,Integer> enchantsInfo,int amount,String nameOrRegex,int matchPoint){
        this.name = name;
        this.enchantsInfo = enchantsInfo;
        this.amount = amount;
        this.nameOrRegex = nameOrRegex;
        this.matchPoint = matchPoint;
    }

    public Result() {
    }

    public Result(String name) {
        // for pass-through
        this.name = name;
        this.enchantsInfo = null;
        this.amount = -1;
        this.nameOrRegex = "";
        this.matchPoint = Integer.MIN_VALUE;
    }

    public String getName() {
        return name;
    }

    public Result setName(String name) {
        this.name = name;
        return this;
    }

    public Map<Enchantment, Integer> getEnchantsInfo() {
        return enchantsInfo;
    }

    public Result setEnchantsInfo(Map<Enchantment, Integer> enchantsInfo) {
        this.enchantsInfo = enchantsInfo;
        return this;
    }

    public int getAmount() {
        return amount;
    }

    public Result setAmount(int amount) {
        this.amount = amount;
        return this;
    }


    public String getNameOrRegex() {
        return nameOrRegex;
    }

    public Result setNameOrRegex(String nameOrRegex) {
        this.nameOrRegex = nameOrRegex;
        return this;
    }

    public int getMatchPoint() {
        return matchPoint;
    }

    public Result setMatchPoint(int matchPoint) {
        this.matchPoint = matchPoint;
        return this;
    }
}
