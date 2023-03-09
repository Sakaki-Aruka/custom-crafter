package com.github.sakakiaruka.customcrafter.customcrafter.some;

import com.github.sakakiaruka.customcrafter.customcrafter.CustomCrafter;
import com.github.sakakiaruka.customcrafter.customcrafter.objects.OriginalRecipe;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;

public class Reload implements CommandExecutor {

    private FileConfiguration config = CustomCrafter.getInstance().getConfig();
    @Override
    public boolean onCommand(CommandSender sender, Command command,String label,String[] args){
        // "/cc list"
        // "/cc reload <recipe name>"
        // "/cc reload"
        if(args.length == 1){
            if(args[0].equalsIgnoreCase("reload")){
                SettingsLoad.recipes.clear();
                new SettingsLoad().set();
                return true;
            }else if(args[0].equalsIgnoreCase("list")){
                for(OriginalRecipe or: SettingsLoad.recipes){
                    sender.sendMessage("list:"+or.getRecipeName());
                }
                return true;
            }

        }else if(args.length == 2){
            // reload entered recipe.
            if(!SettingsLoad.recipes.contains(args[1])){
                sender.sendMessage("[Reload Error]:The recipe is not exist.");
                return false;
            }
            FileConfiguration config = CustomCrafter.getInstance().getConfig();


        }

        return false;
    }
}
