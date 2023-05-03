package com.github.sakakiaruka.customcrafter.customcrafter.object.Matter.Potions;

public enum PotionStrict {
    INPUT("INPUT"),
    NOT_STRICT("NOT_STRICT"),
    //ONLY_BOTTLE_TYPE,
    ONLY_DURATION("ONLY_DURATION"),
    ONLY_AMPLIFIER("ONLY_AMPLIFIER"),
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
