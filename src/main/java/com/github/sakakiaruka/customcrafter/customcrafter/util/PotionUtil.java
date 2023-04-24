package com.github.sakakiaruka.customcrafter.customcrafter.util;

import com.github.sakakiaruka.customcrafter.customcrafter.object.Matter.Matter;
import com.github.sakakiaruka.customcrafter.customcrafter.object.Matter.PotionData.PotionDuration;
import com.github.sakakiaruka.customcrafter.customcrafter.object.Matter.Potions.PotionBottleType;
import com.github.sakakiaruka.customcrafter.customcrafter.object.Matter.Potions.PotionStrict;
import com.github.sakakiaruka.customcrafter.customcrafter.object.Matter.Potions.Potions;
import org.bukkit.Material;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Map;

public class PotionUtil {
    public int getDuration(String key,boolean upgraded,boolean extended){
        for(PotionDuration pd : PotionDuration.values()){
            if(!pd.toString().equalsIgnoreCase(key)) continue;
            if(upgraded) return pd.getUpgraded();
            if(extended) return pd.getExtended();
            return pd.getNormal();
        }
        return -1;
    }

    public PotionBottleType getBottleType(Material material){
        for(PotionBottleType type : PotionBottleType.values()){
            if(type.getRelated().equals(material)) return type;
        }
        return null;
    }


    public boolean isSamePotion(Matter recipe, Matter input){
        Potions r = (Potions) recipe;
        Potions i = (Potions) input;
        if(r.isBottleTypeMatch()){
            if(!r.getBottle().equals(i.getBottle())) return false;
        }

        for(Map.Entry<PotionEffect, PotionStrict> entry : r.getData().entrySet()){

            PotionEffectType effectType = entry.getKey().getType();

            if(!i.hasPotionEffect(entry.getKey().getType())){
                if(!entry.getValue().equals(PotionStrict.NOT_STRICT)) return false;
            }

            if(entry.getValue().equals(PotionStrict.ONLY_DURATION)){
                if(entry.getKey().getDuration() != i.getDuration(effectType)) return false;
            }

            if(entry.getValue().equals(PotionStrict.ONLY_AMPLIFIER)){
                if(entry.getKey().getAmplifier() != i.getAmplifier(effectType)) return false;
            }

            if(entry.getValue().equals(PotionStrict.STRICT)){
                if(entry.getKey().getAmplifier() != i.getAmplifier(effectType)) return false;
                if(entry.getKey().getDuration() != i.getDuration(effectType)) return false;
            }

        }

        return true;
    }
}
