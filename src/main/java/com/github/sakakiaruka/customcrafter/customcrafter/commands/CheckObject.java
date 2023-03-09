package com.github.sakakiaruka.customcrafter.customcrafter.commands;

import com.github.sakakiaruka.customcrafter.customcrafter.objects.OriginalRecipe;
import com.github.sakakiaruka.customcrafter.customcrafter.some.RecipeMaterialUtil;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.github.sakakiaruka.customcrafter.customcrafter.some.SettingsLoad.recipes;


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

            //debug
            if(sender instanceof Player){
                ((Player) sender).getInventory().setItem(0,new ItemStack(Material.WATER_BUCKET,10));
            }
            this.getCalledFromWhere(sender);


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

    private void getCalledFromWhere(CommandSender sender){
        List<StackTraceElement> stacks = new ArrayList<>(Arrays.asList(new Throwable().getStackTrace()));
        StringBuilder info = new StringBuilder();
        //stacks.forEach(s->info.append(String.format("content : %s%n",s.getClassName().contains(this.getClass().getSimpleName()))));
        sender.sendMessage(info.toString());
        System.out.println(this.getClass().toGenericString());
        System.out.println(this.getClass().getSimpleName());
    }
}
