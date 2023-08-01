package com.github.sakakiaruka.customcrafter.customcrafter.object.Result;

import com.github.sakakiaruka.customcrafter.customcrafter.object.ContainerWrapper;
import com.github.sakakiaruka.customcrafter.customcrafter.util.DataContainerUtil;
import com.github.sakakiaruka.customcrafter.customcrafter.util.PotionUtil;
import org.bukkit.Color;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class Result {
    private String name;
    private Map<Enchantment,Integer> enchantsInfo;
    private int amount;
    private Map<MetadataType, List<String>> metadata;
    private String nameOrRegex;
    private int matchPoint;
    private List<ContainerWrapper> dataContainer;

    public Result(String name,Map<Enchantment,Integer> enchantsInfo,int amount,Map<MetadataType,List<String>> metadata,String nameOrRegex,int matchPoint, List<ContainerWrapper> dataContainer){
        this.name = name;
        this.enchantsInfo = enchantsInfo;
        this.amount = amount;
        this.metadata = metadata;
        this.nameOrRegex = nameOrRegex;
        this.matchPoint = matchPoint;
        this.dataContainer = dataContainer;
    }

    public List<ContainerWrapper> getDataContainer() {
        return dataContainer;
    }

    public void setDataContainer(List<ContainerWrapper> dataContainer) {
        this.dataContainer = dataContainer;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<Enchantment, Integer> getEnchantsInfo() {
        return enchantsInfo;
    }

    public void setEnchantsInfo(Map<Enchantment, Integer> enchantsInfo) {
        this.enchantsInfo = enchantsInfo;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public Map<MetadataType, List<String>> getMetadata() {
        return metadata;
    }

    public void setMetadataValue(Map<MetadataType, List<String>> metadata) {
        this.metadata = metadata;
    }

    public String getNameOrRegex() {
        return nameOrRegex;
    }

    public void setNameOrRegex(String nameOrRegex) {
        this.nameOrRegex = nameOrRegex;
    }

    public int getMatchPoint() {
        return matchPoint;
    }

    public void setMatchPoint(int matchPoint) {
        this.matchPoint = matchPoint;
    }

    public void setMetaData(ItemStack item){
        if(metadata==null || metadata.isEmpty()) return;
        ItemMeta meta = item.getItemMeta();

        new DataContainerUtil().addAllData(item, this.dataContainer);

        for(Map.Entry<MetadataType,List<String>> entry : metadata.entrySet()){
            /*
            * kind of metadata -> lore, displayName, enchantment, itemFlag, unbreakable, customModelData, PotionData, PotionColor
            *
            * lore -> split with ","
            * displayName -> used directly itemName
            * enchantment -> "enchantment, level". These are separated with ",".
            * itemFlag -> "flagName, bool". These are separated with ",".
            * unbreakable -> "bool"
            * customModelData -> "modelNumber"
            * potionData -> "PotionEffectType, duration, amplifier(level)". These are separated with ",".
            * potionColor -> "red, green, blue" (RGB)
            *
             */

            MetadataType type = entry.getKey();
            List<String> content = entry.getValue();

            if(type.equals(MetadataType.LORE)) meta.setLore(content);
            if(type.equals(MetadataType.DISPLAYNAME)) meta.setDisplayName(content.get(0));
            if(type.equals(MetadataType.ENCHANTMENT)) {
                for(String s : content){
                    List<String> enchants = Arrays.asList(s.split(","));
                    Enchantment enchant = Enchantment.getByName(enchants.get(0).toUpperCase());
                    int level = Integer.valueOf(enchants.get(1));
                    meta.addEnchant(enchant,level,true);
                }
            }

            if(type.equals(MetadataType.ITEMFLAG)) content.forEach(s->meta.addItemFlags(ItemFlag.valueOf(s.toUpperCase())));
            if(type.equals(MetadataType.UNBREAKABLE)) meta.setUnbreakable(Boolean.valueOf(content.get(0)));
            if(type.equals(MetadataType.CUSTOMMODELDATA)) meta.setCustomModelData(Integer.valueOf(content.get(0)));
            if(type.equals(MetadataType.POTIONDATA)) {
                if(!new PotionUtil().isPotion(item.getType())) return;
                for(String s : content){
                    List<String> potionData = Arrays.asList(s.split(","));
                    PotionEffectType effectType = PotionEffectType.getByName(potionData.get(0).toUpperCase());
                    int duration = Integer.valueOf(potionData.get(1)) < 1 ? 1 : Integer.valueOf(potionData.get(1));
                    int amplifier = Integer.valueOf(potionData.get(2)) < 1 ? 1 : Integer.valueOf(potionData.get(2));
                    PotionEffect effect = new PotionEffect(effectType,duration,amplifier);
                    PotionMeta potionMeta = (PotionMeta)  meta;
                    potionMeta.addCustomEffect(effect,true);
                }
            }
            if(type.equals(MetadataType.POTIONCOLOR)) {
                for(String s : content){
                    List<String> colors = Arrays.asList(s.split(","));
                    int r = Integer.valueOf(colors.get(0));
                    int g = Integer.valueOf(colors.get(1));
                    int b = Integer.valueOf(colors.get(2));

                    PotionMeta potionMeta = (PotionMeta)meta;
                    potionMeta.setColor(Color.fromRGB(r,g,b));
                }
            }
            item.setItemMeta(meta);
        }
    }
}
