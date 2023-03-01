package com.github.sakakiaruka.cutomcrafter.customcrafter.objects;

import org.bukkit.inventory.ItemStack;


public class RegexRecipeMaterial extends ItemStack {
    private String regex;
    private RegexRecipeMaterilEnum enumType;
    private int quantity;

    public RegexRecipeMaterial(String regex,RegexRecipeMaterilEnum enumType,int quantity){
        this.regex = regex;
        this.enumType = enumType;
        this.quantity = quantity;
    }

    public RegexRecipeMaterial(){}

    public String getRegex() {
        return regex;
    }

    public void setRegex(String regex) {
        this.regex = regex;
    }

    public RegexRecipeMaterilEnum getEnumType() {
        return enumType;
    }

    public void setEnumType(RegexRecipeMaterilEnum enumType) {
        this.enumType = enumType;
    }

    public int getAmount() {
        return quantity;
    }

    public void setAmount(int quantity) {
        this.quantity = quantity;
    }

}
