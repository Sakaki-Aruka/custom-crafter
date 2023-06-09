package com.github.sakakiaruka.customcrafter.customcrafter.object.Matter.Potions;

public enum PotionStrict {
    INPUT("INPUT"),
    NOT_STRICT("NOT_STRICT"),
    //ONLY_BOTTLE_TYPE,
    ONLY_DURATION("ONLY_DURATION"),
    ONLY_AMPLIFIER("ONLY_AMPLIFIER"),
    ONLY_EFFECT("ONLY_EFFECT"),
    STRICT("STRICT");

    private String type;
    private PotionStrict(String type){
        this.type = type;
    }

    public String toStr(){
        return type;
    }
}
