package com.github.sakakiaruka.customcrafter.customcrafter.util;

import com.github.sakakiaruka.customcrafter.customcrafter.SettingsLoad;
import com.github.sakakiaruka.customcrafter.customcrafter.object.Matter.EnchantStrict;
import com.github.sakakiaruka.customcrafter.customcrafter.object.Result.MetadataType;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import static com.github.sakakiaruka.customcrafter.customcrafter.SettingsLoad.allMaterials;
import static com.github.sakakiaruka.customcrafter.customcrafter.SettingsLoad.matters;

public class DataCheckerUtil {

    private final String bar = String.join("",Collections.nCopies(40,"="));
    private final String nl = System.getProperty("line.separator");

    public void matterCheck(StringBuilder builder,FileConfiguration config){
        int count = 0;
        if(!nameCheck(config,builder)) count++;
        if(!intCheck(config,builder,"amount",64)) count++;
        if(!candidateCheck(config,builder)) count++;
        if(!massCheck(config, builder)) count++;
        if(config.contains("enchant")) if(!enchantCheck(config,builder,true,3)) count++;
        if(config.contains("potion")) if(!potionCheck(config,builder)) count++;
        if(count != 0) System.out.println(builder.toString());
    }

    private boolean nameCheck(FileConfiguration config,StringBuilder builder){
        if(!config.contains("name")) {
            appendLn(builder,"name -> A section is not found.");
            return false;
        }
        String name = config.getString("name");
        if(!name.matches("[a-zA-Z_]+")) {
            appendLn(builder,"name -> This name does not follow the naming rules.");
            return false;
        }
        for(String str : allMaterials){
            if(name.toUpperCase().equals(str)) {
                appendLn(builder,"name -> This name conflicts with a vanilla item id.");
                return false;
            }
        }
        return true;
    }

    private boolean intCheck(FileConfiguration config,StringBuilder builder,String section,int limit){
        if(!config.contains(section)) {
            appendLn(builder,section + " -> A section is not found.");
            return false;
        }
        int amount;
        try{
            amount = Integer.valueOf(config.getString(section));
        }catch (Exception e){
            appendLn(builder,section + " -> The system cannot cast to integer.");
            return false;
        }

        if(amount < 1 || amount > limit) {
            appendLn(builder,section + " -> The amount is not on the correct range. (1 ~ "+limit+")");
            return false;
        }
        return true;
    }

    private boolean massCheck(FileConfiguration config,StringBuilder builder){
        if(!config.contains("mass")) {
            appendLn(builder,"mass -> A section is not found.");
            return false;
        }
        if(!Boolean.valueOf(config.getString("mass"))){
            appendLn(builder,"mass -> This section has an invalid value. (Not bool.)");
        }
        return true;
    }


    private boolean candidateCheck(FileConfiguration config,StringBuilder builder){
        if(!config.contains("candidate")) {
            appendLn(builder,"candidate -> A section is not found.");
            return false;
        }
        List<String> candidates = config.getStringList("candidate");
        if(candidates.isEmpty()) {
            appendLn(builder,"candidate -> The candidate section has no settings.");
            return false;
        }
//        List<String> list = new ArrayList<>();

        if(candidates.get(0).startsWith("R|") && candidates.size() != 1){
            appendLn(builder,"candidate -> Do not write more than one regular expression on this section.");
            return false;
        }

        if(candidates.get(0).startsWith("R|")){
            String pattern = candidates.get(0).replace("R|","");
            if(!isCorrectNameOrRegex(builder,pattern,true,"candidate")) return false;
        }else{
            for(String str : candidates){
                if(!isCorrectNameOrRegex(builder,str.toUpperCase(),false,"candidate")) return false;
            }
        }


        return true;
    }

    private boolean isCorrectNameOrRegex(StringBuilder builder,String pattern,boolean isRegex,String section){
        if(!isRegex){
            if(!allMaterials.contains(pattern.toUpperCase())) {
                appendLn(builder,section+" -> Not found a correct material name.");
                return false;
            }
            return true;
        }

        try{
            List<String> list = new ArrayList<>();
            Pattern p = Pattern.compile(pattern);
            for(String str : allMaterials){
                Matcher matcher = p.matcher(str);
                if(matcher.matches()) list.add(str);
            }
            if(list.isEmpty()) {
                appendLn(builder,section+" -> No item IDs from the pattern.");
                return false;
            }
        }catch (Exception e){
            appendLn(builder,section+" -> Found an invalid regular expression.");
            return false;
        }
        return true;
    }

    private boolean enchantCheck(FileConfiguration config,StringBuilder builder,boolean strict,int splitter){
        if(!config.contains("enchant")) {
            //appendLn(builder,"enchant -> A section is not found.");
            return true;
        }
        List<String> enchants = config.getStringList("enchant");
        if(enchants.isEmpty()) {
            appendLn(builder,"enchant -> No enchant settings.");
            return false;
        }
        int count = 0;
        for(String str : enchants){
            List<String> settings = Arrays.asList(str.split(","));
            if(settings.size() != splitter){
                appendLn(builder,"enchant -> Settings not enough or has a comma that needless.");
                return false;
            }
            for(Enchantment enchant : Enchantment.values()){
                if(settings.get(0).toUpperCase().equals(enchant.getName())) count++;
            }
            int level;
            try{
                level = Integer.valueOf(settings.get(1));
            }catch (Exception e){
                appendLn(builder,"enchant -> Cannot cast a string to Integer.");
                return false;
            }

            if(level < 1){
                appendLn(builder,"enchant -> An invalid enchant level found.");
                return false;
            }

            if(strict){
                String s = settings.get(2).toUpperCase();
                if(!new EnchantUtil().strValuesNoInput().contains(s)){
                    appendLn(builder,"enchant -> An EnchantStrict value is incorrect.");
                    return false;
                }
            }


        }
        if(enchants.size() != count) {
            appendLn(builder,"enchant -> The enchants list contains invalid settings.");
            return false;
        }

        return true;
    }

    private boolean metadataCheck(FileConfiguration config,StringBuilder builder){
        if(!config.contains("metadata")) return true;
        List<String> settings = config.getStringList("metadata");
        if(settings.isEmpty()) {
            appendLn(builder,"metadata -> Data not found on from this section.");
            return false;
        }
        for(String s : settings){
            if(!s.contains(",")) {
                appendLn(builder,"metadata -> The data string splitter(,) is not found in the setting.");
                return false;
            }
            List<String> list = Arrays.asList(s.split(","));
            if(!getMetadataTypeStringList().contains(list.get(0).toUpperCase())) {
                appendLn(builder,"metadata -> This section has an invalid metadata type.");
                return false;
            }

            MetadataType type = MetadataType.valueOf(list.get(0).toUpperCase());
            if(type.equals(MetadataType.ENCHANTMENT)){
                if(list.size() != 3){
                    appendLn(builder,"metadata -> An Enchantment data has wrong parameters.");
                    return false;
                }
                if(!new EnchantUtil().getEnchantmentStrList().contains(list.get(1).toUpperCase())){
                    appendLn(builder,"meta -> An invalid Enchantment found.");
                    return false;
                }
                try{
                    int level = Integer.valueOf(list.get(2));
                    if(level < 1) {
                        appendLn(builder,"metadata -> An invalid enchantment level found.");
                        return false;
                    }
                }catch (Exception e){
                    appendLn(builder,"metadata -> Enchantment level cannot be casted to Integer.");
                    return false;
                }
            }else if(type.equals(MetadataType.CUSTOMMODELDATA)){
                if(list.size() != 2){
                    appendLn(builder,"metadata -> Custom Model Data has wrong parameters.");
                    return false;
                }
                try{
                    Integer.valueOf(list.get(1));
                }catch (Exception e){
                    appendLn(builder,"metadata -> Custom Model Data cannot be casted to Integer.");
                    return false;
                }
            }else if(type.equals(MetadataType.POTIONDATA)){
                if(list.size() != 4) {
                    appendLn(builder,"metadata -> Potion Data has wrong parameters.");
                    return false;
                }
                if(!new PotionUtil().getPotionEffectTypeStringList().contains(list.get(1).toUpperCase())){
                    appendLn(builder,"metadata -> The potion type is invalid value.");
                    return false;
                }
                try{
                    int duration = Integer.valueOf(list.get(2));
                    int amplifier = Integer.valueOf(list.get(3));
                    if(duration < 1 || amplifier < 1){
                        appendLn(builder,"metadata -> Potion duration or amplifier is invalid value.");
                        return false;
                    }
                }catch (Exception e){
                    appendLn(builder,"metadata -> Potion duration or amplifier cannot be casted to Integer.");
                    return false;
                }
                return true;
            }else if(type.equals(MetadataType.POTIONCOLOR)){
                if(list.size() != 4){
                    appendLn(builder,"metadata -> Potion color has wrong parameters.");
                    return false;
                }
                try{
                    int r = Integer.valueOf(list.get(1));
                    int g = Integer.valueOf(list.get(2));
                    int b = Integer.valueOf(list.get(3));
                    List<Integer> color = Arrays.asList(r,g,b);
                    if(Collections.max(color) > 255 || Collections.min(color) < 0){
                        appendLn(builder,"metadata -> Potion color must be on 0 <--> 255");
                        return false;
                    }
                }catch (Exception e){
                    appendLn(builder,"metadata -> Potion color cannot be casted to Integer.");
                    return false;
                }
            }
        }
        return true;
    }

    private boolean potionCheck(FileConfiguration config,StringBuilder builder){
        if(!config.contains("potion")) {
            //appendLn(builder,"potion -> A section is not found.");
            return true;
        }
        if(!config.contains("bottleTypeMatch")) {
            appendLn(builder,"bottleTypeMatch -> A section is not found.");
            return false;
        }

        List<String> settings = config.getStringList("potion");
        if(settings.isEmpty()) {
            appendLn(builder,"potion -> Potion Settings are not enough.");
            return false;
        }

        for(String str : settings){
            List<String> list = Arrays.asList(str.split(","));
            if(list.size() != 4) {
                appendLn(builder,"potion -> Potion Settings : not enough or over.");
                return false;
            }

            if(!new PotionUtil().getPotionEffectTypeStringList().contains(list.get(0).toUpperCase())){
                appendLn(builder,"potion -> Invalid PotionType found.");
                return false;
            }

            int duration;
            int amplifier;
            try{
                duration = Integer.valueOf(list.get(1));
                amplifier = Integer.valueOf(list.get(2));
            }catch (Exception e){
                appendLn(builder,"potion -> Duration or Amplifier is an invalid value. (cast)");
                return false;
            }

            if(duration < 1 || amplifier < 1) {
                appendLn(builder,"potion -> Duration or Amplifier is an invalid value. (range)");
                return false;
            }

            if(!new PotionUtil().getPotionStrictStringList().contains(list.get(3).toUpperCase())){
                appendLn(builder,"potion -> PotionStrict is an invalid value.");
                return false;
            }

        }
        return true;
    }

    public void resultCheck(StringBuilder builder,FileConfiguration config){
        int count = 0;
        if(!nameCheck(config,builder)) count++;
        if(!intCheck(config,builder,"amount",64)) count++;
        // name or regex
        String s = config.getString("nameOrRegex");
        boolean isRegex = s.contains("@");
        String pattern = isRegex ? s.substring(0,s.indexOf("@")) : s;
        if(!isCorrectNameOrRegex(builder,pattern,isRegex,"nameOrRegex")) count++;
        if(isRegex) {
            String afterPattern = s.substring(s.indexOf("@")).replace("{R}","");
            if(!isCorrectNameOrRegex(builder,afterPattern,true,"nameOrRegex")) {
                appendLn(builder,"  -> {R} side regular expression has an error.");
                count++;
            }
        }
        // End name or regex
        // match point
        if(isRegex){
            intCheck(config,builder,"matchPoint",100);
        }else{
            try{
                int point = Integer.valueOf(config.getString("matchPoint"));
                if(point != -1) {
                    appendLn(builder,"matchPoint -> This section must be -1 in this case.");
                    count ++;
                }
            }catch (Exception e){
                appendLn(builder,"matchPoint -> The value cannot be casted to Integer.");
                count ++;
            }
        }
        // End match point
        if(!enchantCheck(config,builder,false,2)) count++;
        if(!metadataCheck(config,builder)) count++;
        if(count != 0) System.out.println(builder.toString());
    }

    private void appendLn(StringBuilder builder,String str){
        builder.append(str);
        builder.append(nl);
    }

    public List<String> getMetadataTypeStringList(){
        List<String> list = new ArrayList<>();
        for(MetadataType type : MetadataType.values()){
            list.add(type.toStr());
        }
        return list;
    }
}
