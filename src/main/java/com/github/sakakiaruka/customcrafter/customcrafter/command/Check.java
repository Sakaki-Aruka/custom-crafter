package com.github.sakakiaruka.customcrafter.customcrafter.command;

import com.github.sakakiaruka.customcrafter.customcrafter.CustomCrafter;
import com.github.sakakiaruka.customcrafter.customcrafter.SettingsLoad;
import com.github.sakakiaruka.customcrafter.customcrafter.listener.CloseCraftingTable;
import com.github.sakakiaruka.customcrafter.customcrafter.listener.OpenCraftingTable;
import com.github.sakakiaruka.customcrafter.customcrafter.object.DefinedCommand;
import com.github.sakakiaruka.customcrafter.customcrafter.object.Matter.Matter;
import com.github.sakakiaruka.customcrafter.customcrafter.object.Recipe.Coordinate;
import com.github.sakakiaruka.customcrafter.customcrafter.object.Recipe.Recipe;
import com.github.sakakiaruka.customcrafter.customcrafter.object.Recipe.Tag;
import com.github.sakakiaruka.customcrafter.customcrafter.search.Search;
import com.github.sakakiaruka.customcrafter.customcrafter.util.DefinedCommandUtil;
import com.github.sakakiaruka.customcrafter.customcrafter.util.PotionUtil;
import com.github.sakakiaruka.customcrafter.customcrafter.util.RecipePermissionUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static com.github.sakakiaruka.customcrafter.customcrafter.SettingsLoad.*;
import static com.github.sakakiaruka.customcrafter.customcrafter.listener.OpenCraftingTable.opening;

public class Check implements CommandExecutor {
    private final String nl = System.getProperty("line.separator");
    @Override
    public boolean onCommand(CommandSender sender, Command command,String label,String[] args){
//        //no args -> all recipes show
//        //one argument -> search recipes
//        if(args.length == 0){
//            if (!(sender instanceof ConsoleCommandSender) && !sender.hasPermission("cc.show")){
//                return false;
//            }
//            recipes.forEach(s->System.out.println(getGraphicalRecipe(s.getName())));
//        }
//        else if(args[0].equalsIgnoreCase("-reload")) {
//            if(!(sender instanceof ConsoleCommandSender) && !sender.hasPermission("cc.reload")){
//                return false;
//            }
//            reload();
//        }
//        else if(args[0].equalsIgnoreCase("-permission")){
//            if(!(sender instanceof ConsoleCommandSender) && !sender.hasPermission("cc.permission")){
//                return false;
//            }
//            if(args[1] == null) return false;
//            new PermissionCheck().main(args,sender);
//        }
//        else if(args[0].equalsIgnoreCase("-file")){
//            if(!(sender instanceof ConsoleCommandSender) && !sender.hasPermission("cc.file")){
//                return false;
//            }
//            if(args.length != 3) return false;
//            if(args[1].equalsIgnoreCase("-make")){
//                if(args[2].equalsIgnoreCase("defaultPotion")){
//                    System.out.println(bar + nl);
//                    System.out.println("The system is making default potion files.");
//                    System.out.println("Do not shutdown or stop a server.");
//                    System.out.println(nl + bar);
//                    new PotionUtil().makeDefaultPotionFilesWrapper();
//                }else{
//                    return false;
//                }
//            }
//        }else if(args[0].equalsIgnoreCase("-give")) {
//            if(sender instanceof ConsoleCommandSender) return false;
//            if(!sender.hasPermission(("cc.give"))) return false;
//            new Give().main(args, sender);
//        }
//        else if(args[0].equalsIgnoreCase("-open")){
//            if(sender instanceof ConsoleCommandSender) return false;
//            if(!sender.hasPermission("cc.open")) return false;
//            ((Player) sender).openInventory(new OpenCraftingTable().setCraftingInventory());
//            opening.add((Player) sender);
//        }
//        else {
//            System.out.println(getGraphicalRecipe(args[0]));
//        }
//        return true;

        DefinedCommand defCommand;
        if ((defCommand = new DefinedCommandUtil().getProcessor(args, sender)) != null){
            new DefinedCommandUtil().runCommand(defCommand,args,sender);
            return true;
        }else{
            // show help page
            return false;
        }
    }

    public void open(String[] args, CommandSender sender) {
        ((Player) sender).openInventory(new OpenCraftingTable().setCraftingInventory());
        opening.add((Player) sender);
    }

    public void reload(String[] args, CommandSender sender){
        /*
        * /cc reload
        * -> close all players custom crafter GUI (contained "opening")
        * -> clear "whatMaking"
        * -> clear "opening"
        * -> reloadConfig
        * -> load config files
        * -> notice to all players that the system reloaded.
        *
         */

        List<Player> copy = new ArrayList<>();
        opening.forEach(s->copy.add(s));
        copy.forEach(s->{
            new CloseCraftingTable().close(s,s.getInventory());
            s.closeInventory();
        });

        recipes.clear();
        namedRecipes.clear();

        whatMaking.clear();
        opening.clear();

        FileConfiguration oldConfig = CustomCrafter.getInstance().getConfig();
        if(oldConfig.contains("relate")){
            Path relate = Paths.get(oldConfig.getString("relate"));
            new RecipePermissionUtil().playerPermissionWriter(relate);
        }

        // ==============

        CustomCrafter.getInstance().reloadConfig();
        new SettingsLoad().load();
        FileConfiguration config = CustomCrafter.getInstance().getConfig();
        if(config.contains("notice")){
            if(config.getStringList("notice") == null)return;
            if(config.getStringList("notice").isEmpty())return;
            config.getStringList("notice").forEach(s->Bukkit.broadcastMessage(s));
        }
    }
}
