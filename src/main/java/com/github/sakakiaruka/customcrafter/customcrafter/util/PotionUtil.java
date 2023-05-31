package com.github.sakakiaruka.customcrafter.customcrafter.util;

import com.github.sakakiaruka.customcrafter.customcrafter.object.Matter.Matter;
import com.github.sakakiaruka.customcrafter.customcrafter.object.Matter.PotionData.PotionDuration;
import com.github.sakakiaruka.customcrafter.customcrafter.object.Matter.Potions.PotionBottleType;
import com.github.sakakiaruka.customcrafter.customcrafter.object.Matter.Potions.PotionStrict;
import com.github.sakakiaruka.customcrafter.customcrafter.object.Matter.Potions.Potions;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.*;

import java.io.File;
import java.nio.file.Path;
import java.util.*;

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

        for(Map.Entry<PotionEffect, PotionStrict> entry : r.getData().entrySet()){

            PotionEffectType effectType = entry.getKey().getType();

            if(entry.getValue().equals(PotionStrict.NOT_STRICT)){
                continue;
            }

            if(!i.hasPotionEffect(entry.getKey().getType())) return false;

            if(entry.getValue().equals(PotionStrict.ONLY_EFFECT)){
                if(!i.hasPotionEffect(effectType)) return false;
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


    public void makeDefaultPotionFiles(String basePath, boolean mass, boolean upgraded, boolean extended, PotionStrict strict) {
        final String NORMAL = String.format("%s/normal/",basePath);
        final String SPLASH = String.format("%s/splash/",basePath);
        final String LINGERING = String.format("%s/lingering/",basePath);
        Map<String,String> paths = new HashMap<>();
        paths.put("NORMAL",NORMAL);
        paths.put("SPLASH",SPLASH);
        paths.put("LINGERING",LINGERING);
        final String AMOUNT = "1";

        for(PotionEffectType type : PotionEffectType.values()) {
            for(Map.Entry<String,String> path : paths.entrySet()) {
                String key = path.getKey();
                String bottle = key.equals("NORMAL")
                        ? "potion"
                        : key.toLowerCase() + "_potion";

                String name = key.equals("NORMAL")
                        ? type.getName().toLowerCase()
                        : key.toLowerCase() + "_" + type.getName().toLowerCase();

                // example -> extended_upgraded_speed_lingering_potion
                if(upgraded) name = "upgraded_" + name;
                if(extended) name = "extended_" + name;

                File file = new File(path.getValue() + name + ".yml");
                try{
                    file.createNewFile();
                }catch (Exception e){
                    e.printStackTrace();
                    continue;
                }

                Map<String,String> content = new HashMap<>();
                content.put("name",name);
                content.put("amount",AMOUNT);
                content.put("mass",String.valueOf(mass));
                content.put("candidate",String.format("[%s]",bottle));
                int duration = getDuration(type.getName(),upgraded,extended, getBottleType(Material.valueOf(bottle.toUpperCase())));
                content.put("potion",String.format("[\"%s,%d,%d,%s\"]",type.getName(),duration,upgraded ? 1 : 0,strict.toStr()));

                FileConfiguration config = YamlConfiguration.loadConfiguration(file);
                for(Map.Entry<String,String> entry : content.entrySet()) {
                    config.set(entry.getKey(),entry.getValue());
                }
            }
        }
    }

    public void makeDefaultPotionFilesWrapper() {
        final String BASE_PATH = "plugins/Custom_Crafter/default/potion";

        for(PotionStrict strict : PotionStrict.values()) {
            for(List<Boolean> b : getDefaultPotionFilesPattern()){
                makeDefaultPotionFiles(BASE_PATH,b.get(0),b.get(1),b.get(2),strict);
            }
        }
    }

    private Set<List<Boolean>> getDefaultPotionFilesPattern() {
        // make 8 patterns boolean list
        Set<List<Boolean>> set = new HashSet<>();
        for (int i=0;i<8;i++){
            List<String> l = Arrays.asList(String.format("%03d",Integer.toBinaryString(i)).split(""));
            List<Boolean> l2 = new ArrayList<>();
            l.forEach(s->l2.add(Integer.valueOf(s) == 0 ? true : false));
            set.add(l2);
        }
        return set;
    }

}
