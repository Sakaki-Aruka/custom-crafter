package com.github.sakakiaruka.cutomcrafter.customcrafter.commands;

import com.github.sakakiaruka.cutomcrafter.customcrafter.objects.MultiKeys;
import com.github.sakakiaruka.cutomcrafter.customcrafter.objects.OriginalRecipe;
import com.github.sakakiaruka.cutomcrafter.customcrafter.some.RecipeMaterialUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.inventory.ItemStack;


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

//            for(Map.Entry<MultiKeys, ItemStack> entry:recipe.getRecipeMaterial().getRecipeMaterial().entrySet()){
//                sender.sendMessage("key:"+entry.getKey().getKeys());
//                sender.sendMessage("item:"+entry.getValue().getType()+" / "+entry.getValue().getAmount());
//            }
        }
        return true;
    }
}
