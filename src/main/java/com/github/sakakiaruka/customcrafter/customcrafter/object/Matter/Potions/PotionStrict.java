package com.github.sakakiaruka.customcrafter.customcrafter.object.Matter.Potions;

public enum PotionStrict {
    INPUT("INPUT"),
    NOT_STRICT("NOTSTRICT"),
    //ONLY_BOTTLE_TYPE,
    ONLY_DURATION("ONLYDURATION"),
    ONLY_AMPLIFIER("ONLYAMPLIFIER"),
    ONLY_EFFECT("ONLYEFFECT"),
    //BOTTLE_TYPE_DURATION,
    //BOTTLE_TYPE_AMPLIFIER,
    //DURATION_AMPLIFIER, // Does "DURATION_AMPLIFIER" equal "STRICT" ?
    STRICT("STRICT");

    private String type;
    private PotionStrict(String type){
        this.type = type;
    }

    public String toStr(){
        return type;
    }
}
