package com.github.sakakiaruka.customcrafter.customcrafter.command;

import com.github.sakakiaruka.customcrafter.customcrafter.object.Matter.EnchantStrict;
import com.github.sakakiaruka.customcrafter.customcrafter.object.Matter.EnchantWrap;
import com.github.sakakiaruka.customcrafter.customcrafter.object.Matter.Matter;
import com.github.sakakiaruka.customcrafter.customcrafter.object.Matter.Potions.PotionStrict;
import com.github.sakakiaruka.customcrafter.customcrafter.object.Matter.Potions.Potions;
import com.github.sakakiaruka.customcrafter.customcrafter.object.Result.Result;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.github.sakakiaruka.customcrafter.customcrafter.SettingsLoad.*;

public class Give {
    public void main(String args[], CommandSender sender) {
        if(!(sender instanceof Player)) return;
        /*
        * commands
        * - /cc -g -r [RecipeName]
        * - /cc -g -m [MatterName]
         */
        if(args.length == 3) {
            if(args[1].equalsIgnoreCase("-r")){
                give(args[2],sender,false);
            }else if(args[1].equalsIgnoreCase("-m")){
                give(args[2],sender,true);
            }
        }
    }

    public void give(String name, CommandSender sender, boolean isMatter) {
        Player player = (Player) sender;
        if(isMatter) {
            // give matter
            for(String s : matters.keySet()) {
                if(!name.equalsIgnoreCase(s)) continue;
                Matter matter = matters.get(s);
                ItemStack item = new ItemStack(matter.getCandidate().get(0), matter.getAmount());
                ItemMeta meta = item.getItemMeta();
                meta.setLore(getCandidateLine(matter.getCandidate()));
                meta.setLore(Arrays.asList(String.format("mass: %s",matter.isMass())));
                if(matter.hasWrap()) addEnchantInfo(matter, meta);
                if(matter.getClass().equals(Potions.class)) addPotionInfo(matter, meta);
                item.setItemMeta(meta);

                player.getWorld().dropItem(player.getLocation(),item);
            }
        }else{
            // give result
            for(String s : results.keySet()) {
                if(!name.equalsIgnoreCase(s)) continue;
                Result result = results.get(s);
                List<String> candidates = getAllResultCandidate(result);
                if(candidates.isEmpty()) {
                    player.sendMessage(String.format("§f%s%sCould not make an item.%sCause a invalid item id.%s&s%s",bar,nl,nl,nl,bar,nl));
                    return;
                }
                ItemStack item = new ItemStack(Material.valueOf(candidates.get(0).toUpperCase()),result.getAmount());
                ItemMeta meta = item.getItemMeta();

                List<String> modifiedCandidate = new ArrayList<>();
                for(String c : candidates) {
                    modifiedCandidate.add("§f" + c);
                }
                meta.setLore(modifiedCandidate);
                result.setMetaData(item);
                item.setItemMeta(meta);

                player.getWorld().dropItem(player.getLocation(),item);
            }
        }
    }

    private List<String> getCandidateLine(List<Material> candidate) {
        final int LIMIT = 40;
        List<String> list = new ArrayList<>();
        String buffer = "";
        list.add("§f candidate :");
        for(Material material : candidate) {
            int len = material.name().length();
            if(buffer.length() + len + 1 > LIMIT){ // +1 -> separator (,)
                list.add(buffer);
                buffer = material.name();
                continue;
            }
            buffer += "," + material.name();
        }
        return list;
    }

    private void addEnchantInfo(Matter matter, ItemMeta meta) {
        List<String> list = new ArrayList<>();
        for(EnchantWrap wrap : matter.getWrap()) {
            int level = wrap.getLevel();
            Enchantment enchant = wrap.getEnchant();
            EnchantStrict strict = wrap.getStrict();
            meta.addEnchant(enchant,level,true);
            list.add(String.format("§fEnchant: %s | Level: %d | Strict: %s",enchant.toString(),level,strict.toStr()));
        }
        meta.setLore(list);
    }

    private void addPotionInfo(Matter matter, ItemMeta meta) {
        List<String> list = new ArrayList<>();
        for(Map.Entry<PotionEffect, PotionStrict> entry : ((Potions) matter).getData().entrySet()) {
            PotionEffectType type = entry.getKey().getType();
            int level = entry.getKey().getAmplifier();
            int duration = entry.getKey().getDuration();
            PotionStrict strict = entry.getValue();
            boolean match = ((Potions) matter).isBottleTypeMatch();
            ((PotionMeta) meta).addCustomEffect(entry.getKey(),true);
            list.add(String.format("§fType: %s | level: %d | duration: %d | Strict: %s | Bottle: %b",type.getName(),level,duration,strict.toStr(),match));
        }
        meta.setLore(list);
    }

    private List<String> getAllResultCandidate(Result result) {
        List<String> list = new ArrayList<>();
        if(!result.getNameOrRegex().contains("@")){
            list.add(result.getNameOrRegex().toUpperCase());
            return list;
        }

        String section = result.getNameOrRegex();
        String pattern = section.substring(0,section.indexOf("@"));
        String replace = section.substring(section.indexOf("@")+1,section.length());
        if(pattern.isEmpty() || replace.isEmpty()) return list; // return an empty list
        Pattern p = Pattern.compile(pattern);
        for(String s : allMaterials) {
            Matcher m = p.matcher(s);
            if(!m.find()) continue;
            String temp = replace;
            temp = temp.replace("{R}",s);
            list.add(temp.toUpperCase());
        }
        return list;
    }
}
