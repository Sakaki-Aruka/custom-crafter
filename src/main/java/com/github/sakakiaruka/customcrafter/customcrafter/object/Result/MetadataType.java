package com.github.sakakiaruka.customcrafter.customcrafter.object.Result;

public enum MetadataType {
    LORE("LORE"),
    DISPLAYNAME("DISPLAYNAME"),
    ENCHANTMENT("ENCHANTMENT"),
    ITEMFLAG("ITEMFLAG"),
    UNBREAKABLE("UNBREAKABLE"),
    CUSTOMMODELDATA("CUSTOMMODELDATA"),
    POTIONDATA("POTIONDATA"),
    POTIONCOLOR("POTIONCOLOR");

    private String type;
    private MetadataType(String type){
        this.type = type;
    }

    public String toStr(){
        return type;
    }
}
