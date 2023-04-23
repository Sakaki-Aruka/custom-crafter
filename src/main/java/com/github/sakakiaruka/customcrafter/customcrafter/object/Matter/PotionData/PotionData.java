package com.github.sakakiaruka.customcrafter.customcrafter.object.Matter.PotionData;

public enum PotionData{
    // powered | extended (time long) | duration (int | tick) -> if isInstant = "-1"
    LEAPING(false,false,180*20),
    STRONG_LEAPING(true,false,90*20),
    LONG_LEAPING(false,true,480*20),
    FIRE_RESISTANCE(false,false,180*20),
    LONG_FIRE_RESISTANCE(false,true,480*20),
    SWIFTNESS(false,false,180*20),
    STRONG_SWIFTNESS(true,false,90*20),
    LONG_SWIFTNESS(false,true,480*20),
    SLOWNESS(false,false,90*20),
    STRONG_SLOWNESS(true,false,20*20),
    LONG_SLOWNESS(false,true,240*20),
    WATER_BREATHING(false,false,180*20),
    LONG_WATER_BREATHING(false,true,480*20),
    HEALING(false,false,-1),
    STRONG_HEALING(true,false,-1),
    HARMING(false,false,-1),
    STRONG_HARMING(true,false,-1),
    POISON(false,false,45*20),
    STRONG_POISON(true,false,432),
    LONG_POISON(false,true,90*20),
    REGENERATION(false,false,45*20),
    STRONG_REGENERATION(true,false,22*20),
    LONG_REGENERATION(false,true,90*20),
    STRENGTH(false,false,180*20),
    STRONG_STRENGTH(true,false,90*20),
    LONG_STRENGTH(false,true,480*20),
    WEAKNESS(false,false,90*20),
    LONG_WEAKNESS(false,true,240*20),
    LUCK(false,false,300*20),
    TURTLE_MASTER(false,false,20*20),
    STRONG_TURTLE_MASTER(true,false,20*20),
    LONG_TURTLE_MASTER(false,true,40*20),
    SLOW_FALLING(false,false,90*20),
    LONG_SLOW_FALLING(false,true,240*20),
    INVISIBILITY(false,false,180*20),
    LONG_INVISIBILITY(false,true,480*20),
    NIGHT_VISION(false,false,180*20),
    LONG_NIGHT_VISION(false,true,480*20);

    private boolean power;
    private boolean extended;
    private int duration;

    private PotionData(boolean power, boolean extended, int duration){
        this.power = power;
        this.extended = extended;
        this.duration = duration;
    }

    public int getDuration(String key){
        for(PotionData pd : values()){
            if(!pd.toString().equalsIgnoreCase(key)) continue;
            return pd.duration;
        }
        return -1;
    }
}
