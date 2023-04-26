package com.github.sakakiaruka.customcrafter.customcrafter.object.Matter.PotionData;

public enum PotionDuration {

    JUMP(180*20,90*20,480*20),
    FIRE_RESISTANCE(180*20,-1,480*20),
    SPEED(180*20,90*20,480*20),
    SLOW(90*20,20*20,240*20),
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
    NIGHT_VISION(180*20,-1,480*20),

    // lingering potion
    LINGERING_NIGHT_VISION(45*20,-1,120*20),
    LINGERING_INVISIBILITY(45*20,-1,120*20),
    LINGERING_JUMP(45*20,22*20,120*20),
    LINGERING_FIRE_RESISTANCE(45*20,-1,120*20),
    LINGERING_SPEED(45*20,22*20,120*20),
    LINGERING_SLOW(22*20,5*20,60*20),
    LINGERING_TURTLE_MASTER(5*20,5*20,10*20),
    LINGERING_WATER_BREATHING(45*20,-1,120*20),
    LINGERING_HEALING(-1,-1,-1),
    LINGERING_HARMING(-1,-1,-1),
    LINGERING_POISON(11*20,5*20,22*20),
    LINGERING_REGENERATION(11*20,5*20,22*20),
    LINGERING_STRENGTH(45*20,22*20,120*20),
    LINGERING_WEAKNESS(22*20,-1,60*20),
    LINGERING_LUCK(75*20,-1,-1),
    LINGERING_SLOW_FALLING(22*20,-1,60*20);


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
