package com.github.sakakiaruka.customcrafter.customcrafter.util;

import com.github.sakakiaruka.customcrafter.customcrafter.object.Recipe.Recipe;
import com.github.sakakiaruka.customcrafter.customcrafter.object.Result.MetadataType;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;

import java.nio.file.Path;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.github.sakakiaruka.customcrafter.customcrafter.SettingsLoad.*;

public class DataCheckerUtil {

    public void matterCheck(StringBuilder builder, FileConfiguration config, Path path){
        int count = 0;
        appendLn(builder, BAR);
        appendLn(builder,"Target file is "+path.toString());
        builder.append(LINE_SEPARATOR);
        if(!nameCheck(config,builder)) count++;
        builder.append(LINE_SEPARATOR);
        if(!intCheck(config,builder,"amount",64)) count++;
        builder.append(LINE_SEPARATOR);
        if(!candidateCheck(config,builder)) count++;
        builder.append(LINE_SEPARATOR);
        if(!massCheck(config, builder)) count++;
        if(config.contains("enchant")) {
            if(!enchantCheck(config,builder,true,3)) count++;
            builder.append(LINE_SEPARATOR);
        }
        if(config.contains("potion")) if(!potionCheck(config,builder)) count++;
        if(count != 0) Bukkit.getLogger().info(builder.toString());
    }

    private boolean nameCheck(FileConfiguration config,StringBuilder builder){
        if(!config.contains("name")) {
            appendLn(builder,"name -> A section is not found.");
            appendLn(builder,"  -> Have to write 'name: itemName'.");
            return false;
        }
        String name = config.getString("name");
        if(!name.matches("[a-zA-Z_]+")) {
            appendLn(builder,"name -> This name does not follow the naming rules.");
            appendLn(builder,"  -> Follow the pattern that is '[a-zA-Z_]+'.");
            appendLn(builder,"  -> And, do not duplicate with Vanilla item ids.");
            return false;
        }
        for(String str : ALL_MATERIALS){
            if(name.toUpperCase().equals(str)) {
                appendLn(builder,"name -> This name conflicts with a vanilla item id.");
                appendLn(builder,"  -> Do not duplicate with Vanilla item ids.");
                return false;
            }
        }
        return true;
    }

    private boolean intCheck(FileConfiguration config,StringBuilder builder,String section,int limit){
        if(!config.contains(section)) {
            appendLn(builder,section + " -> A section is not found.");
            appendLn(builder,String.format("  -> Have to write %s",section));
            return false;
        }
        int amount;
        try{
            amount = Integer.valueOf(config.getString(section));
        }catch (Exception e){
            appendLn(builder,section + " -> The system cannot cast to integer.");
            appendLn(builder,"  -> Have to write a number.");
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
            appendLn(builder,"  -> Have to write 'mass: true' or 'mass: false'.");
            return false;
        }
        return true;
    }


    private boolean candidateCheck(FileConfiguration config,StringBuilder builder){
        if(!config.contains("candidate")) {
            appendLn(builder,"candidate -> A section is not found.");
            appendLn(builder,"  -> Have to write 'candidate' section.");
            return false;
        }
        List<String> candidates;
        try{
            candidates = config.getStringList("candidate");
        }catch (Exception e){
            appendLn(builder,"candidate -> Settings format error.");
            appendLn(builder,"  -> This section have to be written by a string list.");
            return false;
        }
        if(candidates.isEmpty()) {
            appendLn(builder,"candidate -> The candidate section has no settings.");
            appendLn(builder,"  -> Have to write more than one settings.");
            return false;
        }

        if(candidates.get(0).startsWith("R|") && candidates.size() != 1){
            appendLn(builder,"candidate -> Too many regular expression error.");
            appendLn(builder,"  -> You can write only one regular expression to this section.");
            return false;
        }

        if(candidates.get(0).startsWith("R|")) {
            // use regular expression
            String expression = candidates.get(0).replace("R|","");
            Pattern p;
            try{
                p = Pattern.compile(expression);
            }catch (Exception e) {
                appendLn(builder, "candidate -> The Java regular expression string is invalid.");
                appendLn(builder, "  -> You have to rewrite this sections.");
                appendLn(builder, LINE_SEPARATOR);
                return false;
            }

            List<String> buffer = new ArrayList<>();
            for(String s : ALL_MATERIALS) {
                Matcher m = p.matcher(s);
                if(!m.find()) continue;
                buffer.add(s);
            }

            if(buffer.isEmpty()) {
                appendLn(builder, "candidate -> The Java regular expression has not contain item ids.");
                appendLn(builder, "  -> You have to rewrite this section.");
                appendLn(builder, LINE_SEPARATOR);
                return false;
            }

        }else{
            // not use regular expression
            A: for(String s : candidates) {
                for(String ss : ALL_MATERIALS) {
                    if(s.equalsIgnoreCase(ss)) continue A;
                }

                appendLn(builder, "candidate -> This string is not an item id.");
                appendLn(builder, String.format("  -> '%s'",s));
                return false;
            }
        }
        return true;
    }

    private boolean isCorrectResultNameOrRegex(FileConfiguration config, StringBuilder builder) {
        if(!config.contains("nameOrRegex")) {
            appendLn(builder, "nameOrRegex -> A section is not found.");
            appendLn(builder, "  -> This section is need to make a custom recipe.");
            appendLn(builder, LINE_SEPARATOR);
            return false;
        }

        if(!config.contains("matchPoint")) {
            appendLn(builder, "matchPoint -> A section is not found.");
            appendLn(builder, "  -> This section is need to make a custom recipe.");
            appendLn(builder, LINE_SEPARATOR);
            return false;
        }

        int matchPoint = config.getInt("matchPoint");
        String section = config.getString("nameOrRegex");
        boolean isRegex = section.contains("@");
        if(isRegex) {
            // use regex
            List<String> inputs = new ArrayList<>();
            String input = section.substring(0,section.indexOf("@"));
            String replace = section.substring(section.indexOf("@")+1,section.length());

            if(input.isEmpty() || replace.isEmpty()) {
                appendLn(builder, "nameOrRegex -> A Java expression string or replace string is invalid.");
                appendLn(builder, LINE_SEPARATOR);
                return false;
            }

            Pattern p;
            try{
                p = Pattern.compile(input);
            }catch (Exception e) {
                appendLn(builder, "nameOrRegex -> The string does not according to the Java regular expression rule.");
                appendLn(builder, "  -> So, you have to rewrite this section as soon as.");
                appendLn(builder, LINE_SEPARATOR);
                return false;
            }

            for(String s : ALL_MATERIALS) {
                Matcher m = p.matcher(s);
                if(!m.find()) continue;
                inputs.add(m.group(matchPoint));
            }

            if(inputs.isEmpty()) {
                appendLn(builder, "nameOrRegex -> The Java regular expression has no match string in all materials name.");
                appendLn(builder, "  -> So, you have to rewrite this section to match any string more than one.");
                appendLn(builder, LINE_SEPARATOR);
                return false;
            }

            for(String s : inputs) {
                String replaced = replace.replace("{R}",s);
                try{
                    Material.valueOf(replaced);
                }catch (Exception e) {
                    appendLn(builder, "nameOrRegex -> The replaced string is not an item id.");
                    appendLn(builder, String.format("  -> '%s' is invalid item id.",replaced));
                    return false;
                }
            }

            return true;

        }else {
            // not use regex
            for (String s : ALL_MATERIALS) {
                if (section.equalsIgnoreCase(s)) return true;
            }

            appendLn(builder, "nameOrRegex -> The string is not an item id.");
            appendLn(builder, "  -> You have to rewrite this section to a correct id.");
            appendLn(builder, LINE_SEPARATOR);
            return false;
        }
    }



    private boolean enchantCheck(FileConfiguration config,StringBuilder builder,boolean strict,int splitter){
        if(!config.contains("enchant")) {
            //appendLn(builder,"enchant -> A section is not found.");
            return true;
        }
        List<String> enchants;
        try{
            enchants = config.getStringList("enchant");
        } catch (Exception e){
            appendLn(builder,"enchant -> Section format error.");
            appendLn(builder,"  -> This section have to written as a string list.");
            return false;
        }
        if(enchants.isEmpty()) {
            appendLn(builder,"enchant -> No enchant settings.");
            appendLn(builder,"  -> You have to write some settings here about enchantment.");
            return false;
        }
        int count = 0;
        for(String str : enchants){
            List<String> settings = Arrays.asList(str.split(","));
            if(settings.size() != splitter){
                appendLn(builder,"enchant -> This section has some wrong parameters.");
                appendLn(builder,splitter == 2 ? "  -> 'EnchantName,Level'" : "  -> 'EnchantName,Level,EnchantStrict'");
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
                appendLn(builder,"  -> Do not write a number that is less than 1.");
                return false;
            }

            if(strict){
                String s = settings.get(2).toUpperCase();
                if(!new EnchantUtil().strValuesNoInput().contains(s)){
                    appendLn(builder,"enchant -> An EnchantStrict value is incorrect.");
                    appendLn(builder,"  -> You can choose EnchantStrict from these -> NotStrict, OnlyEnchant, Strict");
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
            appendLn(builder,"metadata -> Data not found from this section.");
            appendLn(builder,"  -> Write data or remove this section.");
            return false;
        }
        for(String s : settings){
            if(!s.contains(",")) {
                appendLn(builder,"metadata -> The data string splitter(,) is not found in the setting.");
                appendLn(builder,"  -> Have to write comma to divide a string.");
                return false;
            }
            List<String> list = Arrays.asList(s.split(","));
            if(!getMetadataTypeStringList().contains(list.get(0).toUpperCase())) {
                appendLn(builder,"metadata -> This section has an invalid metadata type.");
                appendLn(builder,"  -> lore, displayName, enchantment, itemFlag, unbreakable, customModelData, potionData, potionColor");
                return false;
            }

            MetadataType type = MetadataType.valueOf(list.get(0).toUpperCase());
            if(type.equals(MetadataType.ENCHANTMENT)){
                if(list.size() != 3){
                    appendLn(builder,"metadata -> An Enchantment data has wrong parameters.");
                    appendLn(builder,"  -> Have to write two parameters. EnchantName and those level.");
                    return false;
                }
                if(!new EnchantUtil().getEnchantmentStrList().contains(list.get(1).toUpperCase())){
                    appendLn(builder,"meta -> An invalid Enchantment found.");
                    appendLn(builder,"  -> Write correct Enchantment name.");
                    return false;
                }
                try{
                    int level = Integer.valueOf(list.get(2));
                    if(level < 1) {
                        appendLn(builder,"metadata -> An invalid enchantment level found.");
                        appendLn(builder,"  -> Have to write in the range that is 1 to 255.");
                        return false;
                    }
                }catch (Exception e){
                    appendLn(builder,"metadata -> Enchantment level cannot be casted to Integer.");
                    appendLn(builder,"  -> Write a number. ");
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
                    appendLn(builder,"  -> PotionEffectType, duration, amplifier");
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
                    appendLn(builder,"  -> Potion color has 3 parameters. Red, Green, Blue");
                    return false;
                }
                try{
                    int r = Integer.valueOf(list.get(1));
                    int g = Integer.valueOf(list.get(2));
                    int b = Integer.valueOf(list.get(3));
                    List<Integer> color = Arrays.asList(r,g,b);
                    if(Collections.max(color) > 255 || Collections.min(color) < 0){
                        appendLn(builder,"metadata -> Out of the color range error.");
                        appendLn(builder,"  -> Must be in the range 0 <--> 255.");
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
            appendLn(builder,"  -> When you write 'potion' section, have to write 'bottleTypeMatch' section.");
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
                appendLn(builder,"  -> Need 4 parameters : PotionEffectTYpe, duration, amplifier, PotionStrict");
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

            if(duration < -1 || amplifier < -1) {
                appendLn(builder,"potion -> Duration or Amplifier is an invalid value. (range)");
                return false;
            }

            if(!new PotionUtil().getPotionStrictStringList().contains(list.get(3).toUpperCase())){
                appendLn(builder,"potion -> PotionStrict is an invalid value.");
                appendLn(builder,"  -> Not_Strict, Only_Effect, Only_Duration, Only_Amplifier, Strict");
                return false;
            }

        }
        return true;
    }

    public void resultCheck(StringBuilder builder,FileConfiguration config,Path path){
        int count = 0;
        appendLn(builder, BAR);
        appendLn(builder,"Target file is "+path.toString());
        appendLn(builder, LINE_SEPARATOR);
        if(!nameCheck(config,builder)) count++;
        builder.append(LINE_SEPARATOR);
        if(!intCheck(config,builder,"amount",64)) count++;
        builder.append(LINE_SEPARATOR);
        // name or regex
        if(!isCorrectResultNameOrRegex(config,builder)) count++;
        builder.append(LINE_SEPARATOR);
        if(!enchantCheck(config,builder,false,2)) count++;
        builder.append(LINE_SEPARATOR);
        if(!metadataCheck(config,builder)) count++;
        builder.append(LINE_SEPARATOR);
        if(count != 0) Bukkit.getLogger().info(builder.toString());
    }

    public void recipeCheck(StringBuilder builder,FileConfiguration config, Path path){
        int count = 0;
        appendLn(builder, BAR);
        appendLn(builder,"Target file is "+path.toString());
        appendLn(builder, LINE_SEPARATOR);
        if(!recipeNameCheck(config,builder)) count++;
        appendLn(builder, LINE_SEPARATOR);
        if(!tagCheck(config,builder)) count++;
        if(!resultSectionCheck(config, builder)) count++;
        if(!overrideCheck(config,builder)) count++;
        if(!coordinateCheck(config,builder)) count++;
        if(!returnsCheck(config,builder)) count++;
        if(count != 0) Bukkit.getLogger().info(builder.toString());

    }

    private boolean recipeNameCheck(FileConfiguration config, StringBuilder builder) {
        if(!config.contains("name")) {
            appendLn(builder, "name -> A section is not found.");
            appendLn(builder, "  -> Must write this section.'");
            appendLn(builder, LINE_SEPARATOR);
            return false;
        }

        String name = config.getString("name");
        for(Recipe recipe : RECIPE_LIST) {
            if(name.equalsIgnoreCase(recipe.getName())) {
                appendLn(builder, "name -> A recipe name is conflict found.");
                appendLn(builder, String.format("  -> '%s' is conflict with other matter files.",name));
                appendLn(builder, LINE_SEPARATOR);
                return false;
            }
        }
        return true;
    }

    private boolean tagCheck(FileConfiguration config, StringBuilder builder){
        if(!config.contains("tag")){
            appendLn(builder,"tag -> A section is not found.");
            appendLn(builder,"  -> Must write this section. The values are 'normal' and 'amorphous'.");
            appendLn(builder, LINE_SEPARATOR);
            return false;
        }
        return true;
    }

    private boolean resultSectionCheck(FileConfiguration config, StringBuilder builder){
        if(!config.contains("result")) {
            appendLn(builder,"result -> A section is not found.");
            appendLn(builder,"  -> Must write this section to use this recipe.");
            appendLn(builder, LINE_SEPARATOR);
            return false;
        }

        String resultName = config.getString("result");
        for(String ss : RESULTS.keySet()){
            if(ss.equals(resultName)) return true;
        }

        appendLn(builder,"result -> The result item that you wrote on the config not found from the result items list.");
        appendLn(builder,"  -> Remember the result file and the spelling.");
        appendLn(builder, LINE_SEPARATOR);
        return false;
    }

    private boolean overrideCheck(FileConfiguration config, StringBuilder builder){
        if(!config.contains("override")) return true;
        List<String> overrides = config.getStringList("override");
        if(overrides.isEmpty()) return true;

        Map<String,String> relates = new HashMap<>();

        int counter  = 0;
        String pattern = "([\\w]+) -> ([\\w]+)";
        for(String s : overrides){
            if(!s.matches(pattern)){
                appendLn(builder,String.format("override -> '%s' is not a correct override pattern.",s));
                appendLn(builder, LINE_SEPARATOR);
                counter++;
            }else{
                // correct pattern
                List<String> relate = Arrays.asList(s.split(" -> "));
                String base = relate.get(0);
                String shorter = relate.get(1);
                if(relates.containsKey(shorter)){
                    appendLn(builder,"override -> An override conflict was found.");
                    appendLn(builder,String.format("  -> Conflicted %s and %s",base,relates.get(shorter)));
                    appendLn(builder, LINE_SEPARATOR);
                }else{
                    relates.put(shorter,base);
                }
            }

        }
        if(counter != 0){
            appendLn(builder,"override -> The override patterns must follow the pattern.");
            appendLn(builder,"  -> ([\\w]+) -> ([\\w])");
            appendLn(builder, LINE_SEPARATOR);
        }

        return counter == 0;
    }

    private boolean coordinateCheck(FileConfiguration config, StringBuilder builder){
        if(!config.contains("coordinate")){
            appendLn(builder,"coordinate -> A section is not found.");
            appendLn(builder,"  -> You must write this section.");
            appendLn(builder, LINE_SEPARATOR);
            return false;
        }
        if(config.getStringList("coordinate").isEmpty()){
            appendLn(builder,"coordinate -> This section is empty.");
            appendLn(builder,"  -> You must write more information about this recipe.");
            appendLn(builder, LINE_SEPARATOR);
            return false;
        }
        if(!config.contains("tag")) return false;
        if(!isTagType(config.getString("tag"))) return false;
        String tagType = config.getString("tag");
        if(tagType.equalsIgnoreCase("normal")){
            int vertical = config.getStringList("coordinate").size();
            if(vertical > CRAFTING_TABLE_SIZE){
                appendLn(builder,"coordinate -> Recipe size over the limit.");
                appendLn(builder,"  -> Recipe size must be in the range that is 1 ~ 6. (Normal)");
                appendLn(builder, LINE_SEPARATOR);
                return false;
            }
            for(String s : config.getStringList("coordinate")){
                List<String> list = Arrays.asList(s.split(","));
                if(list.isEmpty()){
                    appendLn(builder,"coordinate -> An empty line found.");
                    appendLn(builder,"  -> You must write anything to 'coordinate' section.");
                    appendLn(builder, LINE_SEPARATOR);
                    return false;
                }
                if(list.size() != vertical){
                    appendLn(builder,"coordinate -> The recipe width is invalid.");
                    appendLn(builder,"  -> When 'tag' is normal, 'coordinate' shape must be a square.");
                    appendLn(builder, LINE_SEPARATOR);
                    return false;
                }
                if(list.size() > CRAFTING_TABLE_SIZE){
                    appendLn(builder,"coordinate -> Recipe size over the limit.");
                    appendLn(builder,"  -> Recipe size must be in the range that is 1 ~ 6. (Normal)");
                    appendLn(builder, LINE_SEPARATOR);
                    return false;
                }
            }
        }else{
            // amorphous
            if(config.getStringList("coordinate").isEmpty()){
                appendLn(builder,"coordinate -> An empty line found.");
                appendLn(builder,"  -> You must write anything to 'coordinate' section.");
                appendLn(builder, LINE_SEPARATOR);
                return false;
            }
            if(config.getStringList("coordinate").size() > 36){
                appendLn(builder,"coordinate -> Recipe size over the limit.");
                appendLn(builder,"  -> Recipe size must be in the range that is 1 ~ 36. (Amorphous)");
            }
        }
        return true;
    }

    private boolean isTagType(String in){
        if(in.isEmpty()) return false;
        if(!in.equalsIgnoreCase("normal") && !in.equalsIgnoreCase("amorphous")) return false;
        return true;
    }

    private boolean returnsCheck(FileConfiguration config,StringBuilder builder){
        if(!config.contains("returns")) return true;
        if(!config.getStringList("returns").isEmpty()) return true;
        List<String> returns = config.getStringList("returns");
        for(String s : returns){
            List<String> list = Arrays.asList(s.split(","));
            if(list.size() != 3){
                appendLn(builder,"returns -> Parameters not enough or over.");
                appendLn(builder,"  -> TargetMaterial,ReturnMaterial,Amount");
                appendLn(builder, LINE_SEPARATOR);
                return false;
            }
        }
        return true;
    }

    private void appendLn(StringBuilder builder,String str){
        builder.append(str);
        builder.append(LINE_SEPARATOR);
    }

    public List<String> getMetadataTypeStringList(){
        List<String> list = new ArrayList<>();
        for(MetadataType type : MetadataType.values()){
            list.add(type.toStr());
        }
        return list;
    }
}