package com.github.sakakiaruka.customcrafter.customcrafter.object.Matter;

public enum EnchantStrict {
    INPUT("INPUT"), // dummy TYPE
    NOTSTRICT("NOTSTRICT"),
    ONLYENCHANT("ONLYENCHANT"),
    STRICT("STRICT");

    private String strict;
    private EnchantStrict(String strict){
        this.strict = strict;
    }

    public String toStr(){
        return strict;
    }

}
