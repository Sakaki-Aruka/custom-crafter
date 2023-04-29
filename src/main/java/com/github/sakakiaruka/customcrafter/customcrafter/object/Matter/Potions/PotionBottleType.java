package com.github.sakakiaruka.customcrafter.customcrafter.object.Matter.Potions;

import org.bukkit.Material;

public enum PotionBottleType {
    NORMAL(Material.POTION),
    LINGERING(Material.LINGERING_POTION),
    SPLASH(Material.SPLASH_POTION);

    private Material related;
    private PotionBottleType(Material related){
        this.related = related;
    }

    public Material getRelated(){
        return related;
    }
}
