package com.github.sakakiaruka.customcrafter.customcrafter.util;

import com.github.sakakiaruka.customcrafter.customcrafter.CustomCrafter;
import com.github.sakakiaruka.customcrafter.customcrafter.object.Matter.Matter;
import com.github.sakakiaruka.customcrafter.customcrafter.object.Matter.PotionData.PotionDuration;
import com.github.sakakiaruka.customcrafter.customcrafter.object.Matter.Potions.PotionBottleType;
import com.github.sakakiaruka.customcrafter.customcrafter.object.Matter.Potions.PotionStrict;
import com.github.sakakiaruka.customcrafter.customcrafter.object.Matter.Potions.Potions;
import com.github.sakakiaruka.customcrafter.customcrafter.object.Recipe.Recipe;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.*;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import static com.github.sakakiaruka.customcrafter.customcrafter.CustomCrafter.getInstance;
import static com.github.sakakiaruka.customcrafter.customcrafter.SettingsLoad.bar;
import static com.github.sakakiaruka.customcrafter.customcrafter.SettingsLoad.nl;

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
        if(recipe.isBottleTypeMatch()){
            if(!recipe.getBottle().equals(input.getBottle())) return false;
        }

        if(!recipe.hasAnyCustomEffect()){
            if(input.hasAnyCustomEffect()) return false;
            if(!recipe.isBottleTypeMatch()) return true;
            if(!recipe.getBottle().equals(input.getBottle())) return false;
            return true;
        }

        for(Map.Entry<PotionEffect, PotionStrict> entry : recipe.getData().entrySet()){

            PotionEffectType effectType = entry.getKey().getType();

            if(entry.getValue().equals(PotionStrict.NOT_STRICT)){
                continue;
            }

            if(!input.hasPotionEffect(entry.getKey().getType())) return false;

            if(entry.getValue().equals(PotionStrict.ONLY_EFFECT)){
                if(!input.hasPotionEffect(effectType)) return false;
            }

            if(entry.getValue().equals(PotionStrict.ONLY_DURATION)){
                if(entry.getKey().getDuration() != input.getDuration(effectType)) return false;
            }

            if(entry.getValue().equals(PotionStrict.ONLY_AMPLIFIER)){
                if(entry.getKey().getAmplifier() != input.getAmplifier(effectType)) return false;
            }

            if(entry.getValue().equals(PotionStrict.STRICT)){
                if(entry.getKey().getAmplifier() != input.getAmplifier(effectType)) return false;
                if(entry.getKey().getDuration() != input.getDuration(effectType)) return false;
            }

        }

        return true;
    }


    private List<String> getPDNames() {
        List<String> list = new ArrayList<>();
        for(PotionDuration pd : PotionDuration.values()) {
            list.add(pd.getPDName());
        }
        return list;
    }

    public boolean getPotionsCongruence(Recipe r, Recipe i) {
        List<Potions> recipes = getPotions(r);
        List<Potions> inputs = getPotions(i);
        if(recipes.isEmpty()) return true;
        if(recipes.size() > inputs.size()) return false;

        for(Potions potion : recipes) {
            for(Potions drug : inputs) {
                if(isSamePotion(potion, drug)) break;
            }
            return false;
        }
        return true;
    }

    private List<Potions> getPotions(Recipe recipe) {
        List<Potions> list = new ArrayList<>();
        for(Matter matter : recipe.getContentsNoAir()) {
            if(!matter.getClass().equals(Potions.class)) continue;
            list.add((Potions) matter);
        }
        return list;
    }


    private void makeDefaultPotionFiles(String basePath, boolean mass, boolean upgraded, boolean extended, String strict) {
        final String NORMAL = String.format("%s/normal/",basePath);
        final String SPLASH = String.format("%s/splash/",basePath);
        final String LINGERING = String.format("%s/lingering/",basePath);
        Map<String,String> paths = new HashMap<>();
        paths.put("NORMAL",NORMAL);
        paths.put("SPLASH",SPLASH);
        paths.put("LINGERING",LINGERING);
        final int AMOUNT = 1;

        for(PotionEffectType type : PotionEffectType.values()) {
            if(!getPDNames().contains(type.getName())) continue;
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

                try{
                    Files.createDirectories(Paths.get(path.getValue()));
                }catch (Exception e){
                    e.printStackTrace();
                }

                File file = new File(path.getValue() + name + ".yml");
                try{
                    file.createNewFile();
                }catch (Exception e){
                    e.printStackTrace();
                    continue;
                }

                List<String> content = new ArrayList<>();
                content.add("name: "+name + nl);
                content.add("amount: "+AMOUNT + nl);
                content.add("mass: "+mass + nl);
                content.add("candidate: ["+bottle+"]" + nl);

                int duration = getDuration(type.getName(),upgraded,extended, getBottleType(Material.valueOf(bottle.toUpperCase())));
                content.add("potion: " + nl);
                content.add(String.format("  - %s,%d,%d,%s%s",type.getName(),duration,upgraded ? 1 : 0,strict,nl));

                writerWrapper(content, file);

            }
        }
    }

    public void makeDefaultPotionFilesWrapper() {
        final String BASE_PATH = CustomCrafter.getInstance().getConfig().getStringList("matters").get(0) + "/default/potion";

        new BukkitRunnable(){
            @Override
            public void run() {

                for(String strict : getPotionStrictStringList()) {
                    for(int i=0;i<8;i++){
                        List<Boolean> list = new ArrayList<>();
                        binary(i,list);
                        if(list.size() < 3) list.addAll(Collections.nCopies(3 -list.size(),false));
                        makeDefaultPotionFiles(BASE_PATH,list.get(0),list.get(1),list.get(2),strict);
                    }
                }

                Bukkit.getLogger().info(bar);
                Bukkit.getLogger().info(String.format("[Custom Crafter] Finished creating default potion files."));
                Bukkit.getLogger().info(bar);

            }
        }.runTaskAsynchronously(getInstance());


    }

    private void binary(int num, List<Boolean> list){
        if(num == 0 || num == 1) {
            list.add(num == 0 ? false : true);
            return;
        }
        list.add(num % 2 == 0 ? false : true);
        binary(num / 2,list);
    }

    private boolean writerWrapper(List<String> list, File file) {
        FileWriter writer;
        try{
            writer = new FileWriter(file);
        }catch (IOException e){
            e.printStackTrace();
            return false;
        }

        for(String str : list) {
            try {
                writer.write(str);
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }

        try{
            writer.close();
        } catch (IOException e){
            e.printStackTrace();
            return false;
        }
        return true;
    }

}
