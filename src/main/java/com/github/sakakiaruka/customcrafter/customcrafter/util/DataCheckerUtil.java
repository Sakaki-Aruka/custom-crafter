package com.github.sakakiaruka.customcrafter.customcrafter.util;

import com.github.sakakiaruka.customcrafter.customcrafter.SettingsLoad;
import com.github.sakakiaruka.customcrafter.customcrafter.object.Matter.EnchantStrict;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.potion.PotionEffectType;

import java.nio.file.Path;
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
    public boolean isMatterSafe(Path path){
        /*
        * matter files checker.
        * checking about name,amount,mass,candidate and optional settings.
        *
        * if the file has invalid settings, this method send alert to the console.
         */
        FileConfiguration config = YamlConfiguration.loadConfiguration(path.toFile());
        StringBuilder builder = new StringBuilder();
        appendLn(builder,bar);
        appendLn(builder,String.format("Alert about '%s'",path));

        // section contain check
        if(!config.contains("name")) appendLn(builder,"- 'name' section is not found.");
        if(!config.contains("amount")) appendLn(builder,"- 'amount' section is not found.");
        if(!config.contains("mass")) appendLn(builder,"- 'mass' section is not found.");
        if(!config.contains("candidate")) appendLn(builder,"- 'candidate' section is not found.");

        // about the section contains valid values check
        if(config.contains("name")){
            String name = config.getString("name");
            if(!name.matches("[a-zA-Z_]+")) appendLn(builder,"- 'name' section has invalid characters.");
        }
        if(config.contains("amount")){
            try{
                if(config.getInt("amount") < 1){
                    appendLn(builder,"- 'amount' has a invalid value. Invalid range.");
                }
            }catch (Exception e){
                appendLn(builder,"- 'amount' has a invalid value. It is not a Integer. NOT INT.");
            }
        }
        if(config.contains("mass")){
            if(!config.getString("mass").equalsIgnoreCase("true")
            && !config.getString("mass").equalsIgnoreCase("false")){
                appendLn(builder,"- 'mass' has an invalid value. NOT BOOL.");
            }
        }
        if(config.contains("candidate")){
            try{
                List<String> candidates = config.getStringList("candidate");
                if(candidates.isEmpty()) throw new Exception();
                for(String s : candidates){
                    if(s.startsWith("R|")){
                        try{
                            String regex = s.replace("R|","");
                            Pattern pattern = Pattern.compile(s);
                            Matcher matcher = pattern.matcher(regex);
                            while(matcher.find()){
                                String errorMessage = "- 'candidate' has an invalid material name. (from regular compression)";
                                for(Material m : Material.values()){
                                    if(matcher.group().toUpperCase().equals(m.name())) errorMessage = "";
                                }
                                if(!errorMessage.isEmpty()) appendLn(builder,errorMessage);
                            }
                        }catch (PatternSyntaxException e){
                            appendLn(builder,"- 'candidate' has an invalid regex pattern.");
                        }
                    }else{
                        String errorMessage = "- 'candidate' has an invalid material name." + s;
                        for(Material m : Material.values()){
                            if(s.toUpperCase().equals(m.name())) errorMessage = "";
                        }
                        if(!errorMessage.isEmpty()) appendLn(builder,errorMessage);
                    }
                }

            }catch (Exception e){
                appendLn(builder,"- 'candidate' is not a string list. NOT STRING LIST.");
            }
        }

        // optional settings check
        if(config.contains("enchant")){
            try{
                List<String> enchants = config.getStringList("enchant");
                if(enchants.isEmpty()) throw new Exception("- Enchantments settings are not found. "+nl+"  ->The list is empty.");
                for(String str : enchants){
                    List<String> settings = Arrays.asList(str.split(","));
                    if(settings.size() != 3) throw new Exception("- Enchantments settings are not correct. "+nl+"  ->Require Enchantment | level | Strict");
                    try{
                        Enchantment.getByName(settings.get(0).toUpperCase());
                    }catch (Exception e){
                        throw new Exception("- 'enchant' has an invalid enchantment name.");
                    }

                    try{
                        int level = Integer.valueOf(settings.get(1));
                        if(level < 1) throw new Exception("- 'enchant' has an invalid value."+nl+"  -> Level is not on the correct range.");

                    }catch (Exception e){
                        String errorMessage = e.getMessage().isEmpty() ? "- 'enchant' has an invalid value. NOT INT." : e.getMessage();
                        throw new Exception(errorMessage);
                    }

                    if(new EnchantUtil().getStrictByName(settings.get(2).toUpperCase()) == null) throw new Exception("- 'enchant' has an invalid value."+nl+"  -> Strict must be 'NotStrict', 'OnlyEnchant' or 'Strict'.");

                }
            }catch (Exception e){
                String message = e.getMessage().isEmpty() ? "- 'enchant' is not a string list. NOT STRING LIST." : e.getMessage();
                appendLn(builder,message);
            }
        }

        if(!(config.contains("potion") && config.contains("bottleTypeMatch"))){
            appendLn(builder,"- 'potion' or 'bottleTypeMatch' is not contained.");
        }

        if(config.contains("potion") && config.contains("bottleTypeMatch")){
            try{
                List<String> settings = config.getStringList("potion");
                for(String s : settings){
                    List<String> values = Arrays.asList(s.split(","));
                    if(values.size() != 4) throw new Exception("- 'potion' settings have invalid parameters or not enough.");
                    String errorMessage = "- 'potionEffectType' is not correct.";
                    for(PotionEffectType effectType : PotionEffectType.values()){
                        if(effectType.getName().equals(values.get(0).toUpperCase())) errorMessage = "";
                    }
                    if(!errorMessage.isEmpty()) appendLn(builder,errorMessage);
                    int duration = Integer.valueOf(settings.get(1));
                    int amplifier = Integer.valueOf(settings.get(2));
                    if(duration < 1 || amplifier < 1) appendLn(builder,"'- 'potion' has an invalid duration or amplifier.");
                    Boolean.valueOf(settings.get(3));
                }


            }catch (Exception e){
                String errorMessage = e.getMessage().isEmpty() ? "- 'potion' section has errors." : e.getMessage();
                appendLn(builder,errorMessage);
            }
        }

        try{
            new SettingsLoad().getMatter(Arrays.asList(path));
            matters.clear();
            return true;
        }catch (Exception e){
            return false;
        }
    }

    public boolean isResultSafe(Path path){
        FileConfiguration config = YamlConfiguration.loadConfiguration(path.toFile());
        //
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
        List<String> list = new ArrayList<>();

        if(candidates.size()==1 && candidates.get(0).startsWith("R|")){
            String regex = candidates.get(0).replace("R|","");
            try{
                Pattern pattern = Pattern.compile(regex);
                for(Material material : Material.values()){
                    Matcher matcher = pattern.matcher(material.name());
                    if(matcher.matches()) list.add(material.name());
                }
                if(list.isEmpty()) {
                    appendLn(builder,"candidate -> No materials are there that collected from the regular expression.");
                    return false;
                }
            }catch (Exception e){
                appendLn(builder,"candidate -> Found an invalid regular expression.");
                return false;
            }
        }else{
            for(Material material : Material.values()){
                for(String str : candidates){
                    if(material.name().equals(str.toUpperCase())) list.add(str);
                }
            }
            if(candidates.size() != list.size()) {
                appendLn(builder,"candidate -> Contains invalid material names.");
                return false;
            }
        }

        return true;
    }

    private boolean enchantCheck(FileConfiguration config,StringBuilder builder){
        if(!config.contains("enchant")) {
            appendLn(builder,"enchant -> A section is not found.");
            return false;
        }
        List<String> enchants = config.getStringList("enchant");
        if(enchants.isEmpty()) {
            appendLn(builder,"enchant -> No enchant settings.");
            return false;
        }
        int count = 0;
        for(String str : enchants){
            List<String> settings = Arrays.asList(str.split(","));
            if(settings.size() != 3){
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

            String strict = settings.get(2).toUpperCase();
            if(!new EnchantUtil().strValuesNoInput().contains(strict)){
                appendLn(builder,"enchant -> An EnchantStrict value is incorrect.");
                return false;
            }

        }
        if(enchants.size() != count) {
            appendLn(builder,"enchant -> The enchants list contains invalid settings.");
            return false;
        }

        return true;
    }

    private boolean potionCheck(FileConfiguration config,StringBuilder builder){
        if(!config.contains("potion")) {
            appendLn(builder,"potion -> A section is not found.");
            return false;
        }
        if(!config.contains("bottleTypeMatch")) {
            appendLn(builder,"bottleTypeMatch -> A section is not found.");
            return false;
        }

        try{
            Boolean.valueOf(config.getString("bottleTypeMatch"));
        }catch (Exception e){
            appendLn(builder,"bottleTypeMatch -> An invalid bool value.");
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
                appendLn(builder,"potion -> Duration or Amplifier is an invalid value. (range)")
                return false;
            }

            if(!new PotionUtil().getPotionStrictStringList().contains(list.get(3).toUpperCase())){
                appendLn(builder,"potion -> PotionStrict is an invalid value.");
                return false;
            }

        }
        return true;
    }

    private void appendLn(StringBuilder builder,String str){
        builder.append(str);
        builder.append(nl);
    }


}
