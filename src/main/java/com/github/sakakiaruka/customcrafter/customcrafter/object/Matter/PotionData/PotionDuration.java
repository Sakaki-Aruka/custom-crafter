package com.github.sakakiaruka.customcrafter.customcrafter.object.Matter.PotionData;

public enum PotionDuration {

    JUMP(180*20,90*20,480*20,"JUMP"),
    FIRE_RESISTANCE(180*20,-1,480*20,"FIRE_RESISTANCE"),
    SPEED(180*20,90*20,480*20,"SPEED"),
    SLOW(90*20,20*20,240*20,"SLOW"),
    WATER_BREATHING(180*20,-1,480*20,"WATER_BREATHING"),
    HEAL(-1,-1,-1,"HEAL"),
    HARM(-1,-1,-1,"HARM"),
    POISON(45*20,432,90*20,"POISON"),
    REGENERATION(45*20,22*20,90*20,"REGENERATION"),
    INCREASE_DAMAGE(180*20,90*20,480*20,"INCREASE_DAMAGE"),
    WEAKNESS(90*20,-1,240*20,"WEAKNESS"),
    LUCK(300*20,-1,-1,"LUCK"),
    TURTLE_MASTER(20*20,20*20,40*20,"TURTLE_MASTER"),
    SLOW_FALLING(90*20,-1,240*20,"SLOW_FALLING"),
    INVISIBILITY(180*20,-1,480*20,"INVISIBILITY"),
    NIGHT_VISION(180*20,-1,480*20,"NIGHT_VISION"),

    // lingering potion
    LINGERING_NIGHT_VISION(45*20,-1,120*20,"LINGERING_NIGHT_VISION"),
    LINGERING_INVISIBILITY(45*20,-1,120*20,"LINGERING_INVISIBILITY"),
    LINGERING_JUMP(45*20,22*20,120*20,"LINGERING_JUMP"),
    LINGERING_FIRE_RESISTANCE(45*20,-1,120*20,"LINGERING_FIRE_RESISTANCE"),
    LINGERING_SPEED(45*20,22*20,120*20,"LINGERING_SPEED"),
    LINGERING_SLOW(22*20,5*20,60*20,"LINGERING_SLOW"),
    LINGERING_TURTLE_MASTER(5*20,5*20,10*20,"LINGERING_TURTLE_MASTER"),
    LINGERING_WATER_BREATHING(45*20,-1,120*20,"LINGERING_WATER_BREATHING"),
    LINGERING_HEAL(-1,-1,-1,"LINGERING_HEAL"),
    LINGERING_HARM(-1,-1,-1,"LINGERING_HARM"),
    LINGERING_POISON(11*20,5*20,22*20,"LINGERING_POISON"),
    LINGERING_REGENERATION(11*20,5*20,22*20,"LINGERING_REGENERATION"),
    LINGERING_INCREASE_DAMAGE(45*20,22*20,120*20,"LINGERING_INCREASE_DAMAGE"),
    LINGERING_WEAKNESS(22*20,-1,60*20,"LINGERING_WEAKNESS"),
    LINGERING_LUCK(75*20,-1,-1,"LINGERING_LUCK"),
    LINGERING_SLOW_FALLING(22*20,-1,60*20,"LINGERING_SLOW_FALLING");


    private int upgraded;
    private int extended;
    private int normal;
    private String name;

    private PotionDuration(int normal,int upgraded, int extended, String name){
        this.upgraded = upgraded;
        this.extended = extended;
        this.normal = normal;
        this.name = name;
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

    public String getPDName() {return name;}
}
