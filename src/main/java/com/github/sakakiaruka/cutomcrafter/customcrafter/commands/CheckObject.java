package com.github.sakakiaruka.cutomcrafter.customcrafter.commands;

import com.github.sakakiaruka.cutomcrafter.customcrafter.objects.MultiKeys;
import com.github.sakakiaruka.cutomcrafter.customcrafter.objects.OriginalRecipe;
import com.github.sakakiaruka.cutomcrafter.customcrafter.some.RecipeMaterialUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;


import java.util.Map;

import static com.github.sakakiaruka.cutomcrafter.customcrafter.some.SettingsLoad.recipes;


public class CheckObject implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command,String label,String[] args){
        if(!(sender.isOp() || sender instanceof ConsoleCommandSender)){
            return false;
        }
        for(OriginalRecipe recipe:recipes){
            sender.sendMessage("---");
            sender.sendMessage("recipeName:"+recipe.getRecipeName());
            sender.sendMessage("resultItemStack:"+recipe.getResult().toString());
            sender.sendMessage("recipeSize:"+recipe.getSize());
            sender.sendMessage("recipeTotal:"+recipe.getTotal());
            sender.sendMessage("===");

            sender.sendMessage(new RecipeMaterialUtil().graphicalCoordinate(recipe.getRecipeMaterial()));
        }
        return true;
    }

    private String formatInfo(ItemMeta meta){
        StringBuilder builder = new StringBuilder();
        meta.getEnchants().entrySet().forEach(s->{
            builder.append(String.format("Enchant : %s | Level : %d %n",s.getKey().toString(),s.getValue()));
        });
        return builder.toString();
    }
}
