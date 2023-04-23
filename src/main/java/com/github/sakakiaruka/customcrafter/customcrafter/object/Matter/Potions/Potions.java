package com.github.sakakiaruka.customcrafter.customcrafter.object.Matter.Potions;

import com.github.sakakiaruka.customcrafter.customcrafter.object.Matter.Matter;
import com.github.sakakiaruka.customcrafter.customcrafter.object.Matter.PotionData.PotionDuration;
import com.github.sakakiaruka.customcrafter.customcrafter.util.PotionUtil;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffect;

import java.util.HashMap;
import java.util.Map;

public class Potions extends Matter{
    private Map<PotionEffect,PotionStrict> data;
    public Potions(Matter matter,Map<PotionEffect,PotionStrict> data){
        super(matter);
        this.data = data;
    }

    public Potions(ItemStack item){
        super(item);
        Map<PotionEffect, PotionStrict> map = new HashMap<>();
        PotionMeta meta = (PotionMeta)item.getItemMeta();
        if(((PotionMeta)item.getItemMeta()).hasCustomEffects()){
            PotionStrict strict = PotionStrict.INPUT;
            for(PotionEffect effect : meta.getCustomEffects()){
                map.put(effect,strict);
            }
        }else{
            PotionData baseData = meta.getBasePotionData();
            int duration = new PotionUtil().getDuration(baseData.getType().getEffectType().getName(), baseData.isUpgraded(), baseData.isExtended());
            int level = baseData.isUpgraded() ? baseData.getType().getMaxLevel() : 1;
            PotionEffect effect = new PotionEffect(baseData.getType().getEffectType(),duration,level);
            map.put(effect,PotionStrict.INPUT);
        }
    }
}
