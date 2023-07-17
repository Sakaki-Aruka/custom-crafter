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

    public void matter(String[] args, CommandSender sender) {
        give(args[2],sender,true);
    }

    public void result(String[] args, CommandSender sender) {
        give(args[2],sender,false);
    }

    public void give(String name, CommandSender sender, boolean isMatter) {
        Player player = (Player) sender;
        if(isMatter) {
            // give matter
            for(String s : matters.keySet()) {
                if(!name.equalsIgnoreCase(s)) continue;
                Matter matter = matters.get(s);
                ItemStack item = new ItemStack(matter.getCandidate().get(0), matter.getAmount());
                List<String> list = new ArrayList<>();
                list.addAll(setCandidateLine(matter.getCandidate()));
                list.add(String.format("%s mass:","§f"));
                list.add(String.format("%s%b","§b",matter.isMass()));
                if(matter.hasWrap()) list.addAll(getEnchantInfo(matter, item));
                if(matter.getClass().equals(Potions.class)) list.addAll(getPotionInfo(matter, item));
                ItemMeta meta = item.getItemMeta();
                meta.setLore(list);
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
                modifiedCandidate.add("§f"+shortBar);
                modifiedCandidate.add("§fResult candidate: ");
                for(String c : candidates) {
                    modifiedCandidate.add("§f" + c);
                }
                modifiedCandidate.add("§f"+shortBar);
                meta.setLore(modifiedCandidate);
                item.setItemMeta(meta);
                result.setMetaData(item);

                player.getWorld().dropItem(player.getLocation(),item);
            }
        }
    }


    private List<String> setCandidateLine(List<Material> candidate) {
        final int LIMIT = 40;
        List<String> list = new ArrayList<>();
        String buffer = "";
        list.add("§f candidate :");
        for(Material material : candidate) {
            int len = material.name().length();
            if(buffer.length() + len + 1 > LIMIT){ // +1 -> separator (,)
                list.add("§b" + buffer);
                buffer = material.name();
                continue;
            }
            buffer += (buffer.isEmpty() ? "" : ",") + material.name();
        }
        list.add("§b" + buffer);

        //debug
        System.out.println(String.format("candidate list : %s",list));

        return list;
    }

    private List<String> getEnchantInfo(Matter matter, ItemStack item) {
        List<String> list = new ArrayList<>();
        ItemMeta meta = item.getItemMeta();
        for(EnchantWrap wrap : matter.getWrap()) {
            int level = wrap.getLevel();
            Enchantment enchant = wrap.getEnchant();
            EnchantStrict strict = wrap.getStrict();
            meta.addEnchant(enchant,level,true);
            list.add(String.format("§fEnchant: %s | Level: %d | Strict: %s",enchant.toString(),level,strict.toStr()));
        }
        return list;
    }

    private List<String> getPotionInfo(Matter matter, ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        List<String> list = new ArrayList<>();
        list.add("§f Potion Info: ");
        for(Map.Entry<PotionEffect, PotionStrict> entry : ((Potions) matter).getData().entrySet()) {
            PotionEffectType type = entry.getKey().getType();
            int level = entry.getKey().getAmplifier();
            int duration = entry.getKey().getDuration();
            PotionStrict strict = entry.getValue();
            boolean match = ((Potions) matter).isBottleTypeMatch();
            ((PotionMeta) meta).addCustomEffect(entry.getKey(),true);
            list.add(String.format("§bType: %s | level: %d | duration: %d | Strict: %s | Bottle: %b",type.getName(),level,duration,strict.toStr(),match));
        }
        meta.setLore(list);
        item.setItemMeta(meta);

        return list;
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
