package com.github.sakakiaruka.customcrafter.customcrafter.util;

import com.github.sakakiaruka.customcrafter.customcrafter.object.Matter.Matter;
import com.github.sakakiaruka.customcrafter.customcrafter.object.Matter.PotionData.PotionDuration;
import com.github.sakakiaruka.customcrafter.customcrafter.object.Matter.Potions.PotionBottleType;
import com.github.sakakiaruka.customcrafter.customcrafter.object.Matter.Potions.PotionStrict;
import com.github.sakakiaruka.customcrafter.customcrafter.object.Matter.Potions.Potions;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class PotionUtil {
    // this string key is "PotionEffectType" 's name
    public int getDuration(String key,boolean upgraded,boolean extended,PotionBottleType bottleType){
        if(bottleType.equals(PotionBottleType.LINGERING)){
            StringBuilder builder = new StringBuilder("LINGERING_");
            builder.append(key);
            key = builder.toString();
        }
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

    public boolean isPotion(Material material){
        for(PotionBottleType type : PotionBottleType.values()){
            if(type.getRelated().equals(material)) return true;
        }
        return false;
    }

    public List<String> getPotionEffectTypeStringList(){
        List<String> list = new ArrayList<>();
        for(PotionEffectType e : PotionEffectType.values()){
            list.add(e.getName());
        }
        return list;
    }

    public List<String> getPotionStrictStringList(){
        List<String> list = new ArrayList<>();
        for(PotionStrict strict : PotionStrict.values()){
            if(strict.toStr().equalsIgnoreCase("INPUT")) continue;
            list.add(strict.toStr());
        }
        return list;
    }

    public Potions water_bottle(){
        ItemStack item = new ItemStack(Material.POTION);
        PotionMeta meta = (PotionMeta) item.getItemMeta();
        PotionType type = PotionType.WATER;
        PotionData data = new PotionData(type);
        meta.setBasePotionData(data);
        item.setItemMeta(meta);
        Potions potion = new Potions(item,PotionStrict.STRICT);
        //potion.setName("water_bottle");
        return potion;
    }

    public ItemStack water_bottle_ItemStack(){
        ItemStack item = new ItemStack(Material.POTION);
        PotionMeta meta = (PotionMeta) item.getItemMeta();
        PotionType type = PotionType.WATER;
        PotionData data = new PotionData(type);
        meta.setBasePotionData(data);
        item.setItemMeta(meta);
        return item;
    }

    public boolean isWaterBottle(Matter matter){
        Potions potion;
        try{
            potion = (Potions) matter;
        }catch (Exception e){
            return false;
        }
        if(!potion.getCandidate().get(0).equals(Material.POTION)) return false;
        if(potion.hasAnyCustomEffect()) return false;
        return true;
    }


    public boolean isSamePotion(Potions recipe, Potions input){
        Potions r = recipe;
        Potions i = input;
        if(r.isBottleTypeMatch()){
            if(!r.getBottle().equals(i.getBottle())) return false;
        }

        if(!recipe.hasAnyCustomEffect()){
            if(input.hasAnyCustomEffect()) return false;
            if(!recipe.isBottleTypeMatch()) return true;
            if(!recipe.getBottle().equals(input.getBottle())) return false;
            return true;
        }
//
//        //debug
//        System.out.println(String.format("r : %s\ni : %s",r.PotionInfo(),i.PotionInfo()));
//
//
//        if(r.getName().equals("water_bottle")){
//            if(i.hasAnyCustomEffect()) return false;
//        }

        for(Map.Entry<PotionEffect, PotionStrict> entry : r.getData().entrySet()){

            PotionEffectType effectType = entry.getKey().getType();

            if(entry.getValue().equals(PotionStrict.NOT_STRICT)){
                continue;
            }

            if(!i.hasPotionEffect(entry.getKey().getType())) return false;

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
