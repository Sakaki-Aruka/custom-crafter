package com.github.sakakiaruka.customcrafter.customcrafter.util;

import com.github.sakakiaruka.customcrafter.customcrafter.CustomCrafter;
import com.github.sakakiaruka.customcrafter.customcrafter.object.Matter.PotionData.PotionDuration;
import com.github.sakakiaruka.customcrafter.customcrafter.object.Matter.Potions.PotionBottleType;
import com.github.sakakiaruka.customcrafter.customcrafter.object.Matter.Potions.PotionStrict;
import com.github.sakakiaruka.customcrafter.customcrafter.object.Matter.Potions.Potions;
import com.github.sakakiaruka.customcrafter.customcrafter.object.Recipe.Coordinate;
import com.github.sakakiaruka.customcrafter.customcrafter.object.Recipe.Recipe;
import com.github.sakakiaruka.customcrafter.customcrafter.search.Search;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;
import org.bukkit.potion.PotionData;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.github.sakakiaruka.customcrafter.customcrafter.CustomCrafter.getInstance;
import static com.github.sakakiaruka.customcrafter.customcrafter.SettingsLoad.BAR;
import static com.github.sakakiaruka.customcrafter.customcrafter.SettingsLoad.LINE_SEPARATOR;

public class PotionUtil {
    // this string key is "PotionEffectType" 's name
    public static int getDuration(String key,boolean upgraded,boolean extended,PotionBottleType bottleType){
        if(bottleType.equals(PotionBottleType.LINGERING)){
            key = "LINGERING_" + key;
        }
        for(PotionDuration pd : PotionDuration.values()){
            if(!pd.toString().equalsIgnoreCase(key)) continue;
            if(upgraded) return pd.getUpgraded();
            if(extended) return pd.getExtended();
            return pd.getNormal();
        }
        return -1;
    }

    public static PotionBottleType getBottleType(Material material){
        for(PotionBottleType type : PotionBottleType.values()){
            if(type.getRelated().equals(material)) return type;
        }
        return null;
    }

    public static boolean isPotion(Material material){
        for(PotionBottleType type : PotionBottleType.values()){
            if(type.getRelated().equals(material)) return true;
        }
        return false;
    }

    public static List<String> getPotionStrictStringList(){
        List<String> list = new ArrayList<>();
        for(PotionStrict strict : PotionStrict.values()){
            if(strict.toStr().equalsIgnoreCase("INPUT")) continue;
            list.add(strict.toStr());
        }
        return list;
    }

    public static Potions water_bottle(){
        ItemStack item = new ItemStack(Material.POTION);
        PotionMeta meta = (PotionMeta) item.getItemMeta();
        PotionType type = PotionType.WATER;
        PotionData data = new PotionData(type);
        meta.setBasePotionData(data);
        item.setItemMeta(meta);
        //potion.setName("water_bottle");
        return new Potions(item,PotionStrict.STRICT);
    }

    public static ItemStack water_bottle_ItemStack(){
        ItemStack item = new ItemStack(Material.POTION);
        PotionMeta meta = (PotionMeta) item.getItemMeta();
        PotionType type = PotionType.WATER;
        PotionData data = new PotionData(type);
        meta.setBasePotionData(data);
        item.setItemMeta(meta);
        return item;
    }


    public static boolean isSamePotion(Potions recipe, Potions input){
        if(recipe.isBottleTypeMatch()){
            if(!recipe.getBottle().equals(input.getBottle())) return false;
        }

        if(!recipe.hasAnyCustomEffect()){
            if(input.hasAnyCustomEffect()) return false;
            if(!recipe.isBottleTypeMatch()) return true;
            return recipe.getBottle().equals(input.getBottle());
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


    private static List<String> getPDNames() {
        List<String> list = new ArrayList<>();
        for(PotionDuration pd : PotionDuration.values()) {
            list.add(pd.getPDName());
        }
        return list;
    }



    private static void makeDefaultPotionFiles(String basePath, boolean mass, boolean upgraded, boolean extended, String strict) {
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
                content.add("name: "+name + LINE_SEPARATOR);
                content.add("amount: "+AMOUNT + LINE_SEPARATOR);
                content.add("mass: "+mass + LINE_SEPARATOR);
                content.add("candidate: ["+bottle+"]" + LINE_SEPARATOR);

                int duration = getDuration(type.getName(),upgraded,extended, getBottleType(Material.valueOf(bottle.toUpperCase())));
                content.add("potion: " + LINE_SEPARATOR);
                content.add(String.format("  - %s,%d,%d,%s%s",type.getName(),duration,upgraded ? 1 : 0,strict, LINE_SEPARATOR));

                writerWrapper(content, file);

            }
        }
    }

    public static void makeDefaultPotionFilesWrapper() {
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

                CustomCrafter.getInstance().getLogger().info(BAR);
                CustomCrafter.getInstance().getLogger().info("[Custom Crafter] Finished creating default potion files.");
                CustomCrafter.getInstance().getLogger().info(BAR);

            }
        }.runTaskAsynchronously(getInstance());


    }

    private static void binary(int num, List<Boolean> list){
        if(num == 0 || num == 1) {
            list.add(num != 0);
            return;
        }
        list.add(num % 2 != 0);
        binary(num / 2,list);
    }

    private static boolean writerWrapper(List<String> list, File file) {
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

    public static Map<Coordinate, List<Coordinate>> amorphous(Recipe recipe, Recipe input) {
        // returns candidate of correct pattern
        Map<Coordinate, List<Coordinate>> result = new HashMap<>();
        //
        List<Coordinate> r = recipe.getPotionCoordinateList();
        List<Coordinate> i = input.getPotionCoordinateList();

        if (r.isEmpty()) return Search.AMORPHOUS_NON_REQUIRED_ANCHOR; // no required potion elements
        if (r.size() > i.size()) return Search.AMORPHOUS_NULL_ANCHOR; // failed to match

        for (Coordinate a : r) {
            for (Coordinate b : i) {
                Potions source = (Potions) recipe.getMatterFromCoordinate(a);
                Potions target = (Potions) input.getMatterFromCoordinate(b);

                if (!isSamePotion(source, target)) continue;
                if (!result.containsKey(a)) result.put(a, new ArrayList<>());
                result.get(a).add(b);
            }
        }

        return result.isEmpty() ? Search.AMORPHOUS_NULL_ANCHOR : result;
    }

}
