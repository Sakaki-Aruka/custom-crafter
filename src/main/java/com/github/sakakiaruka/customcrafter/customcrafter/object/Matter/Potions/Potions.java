package com.github.sakakiaruka.customcrafter.customcrafter.object.Matter.Potions;

import com.github.sakakiaruka.customcrafter.customcrafter.interfaces.Matters;
import com.github.sakakiaruka.customcrafter.customcrafter.object.Matter.EnchantWrap;
import com.github.sakakiaruka.customcrafter.customcrafter.object.Matter.Matter;
import com.github.sakakiaruka.customcrafter.customcrafter.util.PotionUtil;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Potions extends Matter implements Matters {
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
        PotionUtil util = new PotionUtil();
        if(meta.getBasePotionData().getType().equals(PotionType.TURTLE_MASTER)){
            int duration = util.getDuration("turtle_master",baseData.isUpgraded(),baseData.isExtended(),util.getBottleType(item.getType()));
            int slowLevel = baseData.isUpgraded() ? 6 : 4;
            int resistanceLevel = baseData.isUpgraded() ? 4 : 3;
            PotionEffect slow = new PotionEffect(PotionEffectType.SLOW,duration,slowLevel);
            PotionEffect resistance = new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE,duration,resistanceLevel);
            map.put(slow,strict);
            map.put(resistance,strict);

        } else if(!baseData.getType().equals(PotionType.WATER) && !baseData.getType().equals(PotionType.UNCRAFTABLE)){
            int duration = util.getDuration(baseData.getType().getEffectType().getName(), baseData.isUpgraded(), baseData.isExtended(),util.getBottleType(item.getType()));
            int level = baseData.isUpgraded() ? baseData.getType().getMaxLevel() : 1;
            PotionEffect effect = new PotionEffect(baseData.getType().getEffectType(),duration,level);
            map.put(effect,strict);
        } else {
            for(PotionEffect effect : meta.getCustomEffects()){
                map.put(effect,strict);
            }
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
        builder.append("=== potion info ===\n\n");
        builder.append(String.format("bottle type match : %b -> only '%s'\n",bottleTypeMatch,super.getCandidate().get(0).name()));
        for(Map.Entry<PotionEffect, PotionStrict> entry : data.entrySet()){
            PotionEffect effect = entry.getKey();
            builder.append(String.format("effect : %s | duration : %d | amplifier : %d\n",effect.getType(),effect.getDuration(),effect.getAmplifier()));
            builder.append(String.format("potion strict : %s\n\n",entry.getValue()));
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

    public String getName(){
        return super.getName();
    }

    public void setName(String name){
        super.setName(name);
    }

    public List<Material> getCandidate(){
        return super.getCandidate();
    }

    public void setCandidate(List<Material> candidate){
        super.setCandidate(candidate);
    }

    public void addCandidate(List<Material> additional){
        super.addCandidate(additional);
    }

    public List<EnchantWrap> getWrap(){
        return super.getWrap();
    }

    public void setWrap(List<EnchantWrap> wrap){
        super.setWrap(wrap);
    }

    public boolean hasWrap(){
        return super.hasWrap();
    }

    public void addWrap(EnchantWrap in){
        super.addWrap(in);
    }

    public void addAllWrap(List<EnchantWrap> in){
        super.addAllWrap(in);
    }

    public int getAmount(){
        return super.getAmount();
    }

    public void setAmount(int amount){
        super.setAmount(amount);
    }

    public boolean isMass(){
        return super.isMass();
    }

    public void setMass(boolean mass){
        super.setMass(mass);
    }

    public int getEnchantLevel(Enchantment enchantment){
        return super.getEnchantLevel(enchantment);
    }

    public String getAllWrapInfo(){
        return super.getAllWrapInfo();
    }

    public boolean contains(Enchantment enchantment){
        return super.contains(enchantment);
    }

    public String info(){
        return super.info()+"\n"+PotionInfo();
    }
}