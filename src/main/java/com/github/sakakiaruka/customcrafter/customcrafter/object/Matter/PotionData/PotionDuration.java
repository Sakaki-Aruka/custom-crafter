package com.github.sakakiaruka.customcrafter.customcrafter.object.Matter.PotionData;

public enum PotionDuration {

    LEAPING(180*20,90*20,480*20),
    FIRE_RESISTANCE(180*20,-1,480*20),
    SWIFTNESS(180*20,90*20,480*20),
    SLOWNESS(90*20,20*20,240*20),
    WATER_BREATHING(180*20,-1,480*20),
    HEALING(-1,-1,-1),
    HARMING(-1,-1,-1),
    POISON(45*20,432,90*20),
    REGENERATION(45*20,22*20,90*20),
    STRENGTH(180*20,90*20,480*20),
    WEAKNESS(90*20,-1,240*20),
    LUCK(300*20,-1,-1),
    TURTLE_MASTER(20*20,20*20,40*20),
    SLOW_FALLING(90*20,-1,240*20),
    INVISIBILITY(180*20,-1,480*20),
    NIGHT_VISION(180*20,-1,480*20);

    private int upgraded;
    private int extended;
    private int normal;

    private PotionDuration(int normal,int upgraded, int extended){
        this.upgraded = upgraded;
        this.extended = extended;
        this.normal = normal;
    }

    public int getUpgraded(){
        return upgraded;
    }

    public int getExtended(){
        return extended;
    }

    public int getNormal(){
        return normal;
    }
}
