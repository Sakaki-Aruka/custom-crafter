package com.github.sakakiaruka.customcrafter.customcrafter.util;

import com.github.sakakiaruka.customcrafter.customcrafter.object.Matter.EnchantStrict;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class DataCheckerUtil {

    private final String bar = String.join("",Collections.nCopies(40,"="));
    private final String nl = System.getProperty("line.separator");
    public String isMatterSafe(Path path){
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
                    String errorMessage = "- 'potionEffectType' is not correct."
                }
            }catch (Exception e){
                String errorMessage = e.getMessage().isEmpty() ? "- 'potion' is empty." : e.getMessage();
                appendLn(builder,errorMessage);
            }
        }


    }

    private void appendLn(StringBuilder builder,String str){
        builder.append(str);
        builder.append(nl);
    }

    private boolean hasError(String str){
        str = str.replace("\n","A");
        int len = str.length();
        int templateLen = bar.length()*2 + 1;
        return len == templateLen;
    }

}
