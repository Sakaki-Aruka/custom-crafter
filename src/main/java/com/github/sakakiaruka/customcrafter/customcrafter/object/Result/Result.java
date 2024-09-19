package com.github.sakakiaruka.customcrafter.customcrafter.object.Result;

import org.bukkit.enchantments.Enchantment;

import java.util.Map;
import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Result)) return false;
        Result result = (Result) o;
        return amount == result.amount && matchPoint == result.matchPoint && Objects.equals(name, result.name) && Objects.equals(enchantsInfo, result.enchantsInfo) && Objects.equals(nameOrRegex, result.nameOrRegex);
    }

    @Override
    public int hashCode() {
        int hash = 17;
        hash = hash * 31 + name.hashCode();
        hash = hash * 31 + enchantsInfo.hashCode();
        hash = hash * 31 + Integer.hashCode(amount);
        hash = hash * 31 + nameOrRegex.hashCode();
        hash = hash * 31 + Integer.hashCode(matchPoint);
        return hash;
    }
}
