package com.github.sakakiaruka.customcrafter.customcrafter.object.Matter.Potions;

import com.github.sakakiaruka.customcrafter.customcrafter.object.Matter.Matter;
import com.github.sakakiaruka.customcrafter.customcrafter.util.PotionUtil;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;

import java.util.HashMap;
import java.util.Map;

public class Potions extends Matter{
    private Map<PotionEffect,PotionStrict> data;
    private PotionBottleType bottle;
    private boolean bottleTypeMatch;
    public Potions(Matter matter,Map<PotionEffect,PotionStrict> data,PotionBottleType bottle,boolean bottleTypeMatch){
        super(matter);
        this.data = data;
        this.bottle = bottle;
        this.bottleTypeMatch = bottleTypeMatch;
    }

    public Potions(ItemStack item, PotionStrict strict){
        super(item);
        Map<PotionEffect, PotionStrict> map = new HashMap<>();
        PotionMeta meta = (PotionMeta)item.getItemMeta();

        if(meta.hasCustomEffects()){
            for(PotionEffect effect : meta.getCustomEffects()){
                map.put(effect,strict);
            }
        }
        PotionData baseData = meta.getBasePotionData();
        if(!baseData.getType().equals(PotionType.WATER)){
            int duration = new PotionUtil().getDuration(baseData.getType().getEffectType().getName(), baseData.isUpgraded(), baseData.isExtended());
            int level = baseData.isUpgraded() ? baseData.getType().getMaxLevel() : 1;
            PotionEffect effect = new PotionEffect(baseData.getType().getEffectType(),duration,level);
            map.put(effect,strict);
        }
        data = map;
        bottle = new PotionUtil().getBottleType(item.getType());

        // default is "true"
        bottleTypeMatch = true;
    }

    public ItemStack prescribe(){
        ItemStack result = new ItemStack(bottle.getRelated());
        PotionMeta meta = (PotionMeta) result.getItemMeta();

        PotionData baseData = new PotionData(PotionType.WATER);
        meta.setBasePotionData(baseData);
        for(Map.Entry<PotionEffect,PotionStrict> entry : data.entrySet()){
            meta.addCustomEffect(entry.getKey(),true);
        }
        result.setItemMeta(meta);
        return result;
    }

    public String PotionInfo(){
        StringBuilder builder = new StringBuilder();
        builder.append("=== potion info ===\n");
        for(Map.Entry<PotionEffect, PotionStrict> entry : data.entrySet()){
            PotionEffect effect = entry.getKey();
            builder.append(String.format("effect : %s | duration : %d | amplifier : %d\n",effect.getType(),effect.getDuration(),effect.getAmplifier()));
            builder.append(String.format("potion strict : %s\n",entry.getValue()));
            builder.append(String.format("bottle type match : %b\n",bottleTypeMatch));
        }
        builder.append("=== potion info END ===\n");
        return builder.toString();
    }

    public boolean hasPotionEffect(PotionEffectType effectType){
        for(Map.Entry<PotionEffect,PotionStrict> entry : data.entrySet()){
            if(entry.getKey().getType().equals(effectType)) return true;
        }
        return false;
    }

    public Map<PotionEffect, PotionStrict> getData() {
        return data;
    }

    public PotionBottleType getBottle() {
        return bottle;
    }

    public boolean isBottleTypeMatch() {
        return bottleTypeMatch;
    }

    public int getDuration(PotionEffectType effectType){
        for(Map.Entry<PotionEffect,PotionStrict> entry : data.entrySet()){
            if(entry.getKey().getType().equals(effectType)) return entry.getKey().getDuration();
        }

        return -2;
    }

    public int getAmplifier(PotionEffectType effectType){
        for(Map.Entry<PotionEffect,PotionStrict> entry : data.entrySet()){
            if(entry.getKey().getType().equals(effectType)) return entry.getKey().getAmplifier();
        }

        return -1;
    }
}
