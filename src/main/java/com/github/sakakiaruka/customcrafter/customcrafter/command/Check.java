package com.github.sakakiaruka.customcrafter.customcrafter.command;

import com.github.sakakiaruka.customcrafter.customcrafter.CustomCrafter;
import com.github.sakakiaruka.customcrafter.customcrafter.SettingsLoad;
import com.github.sakakiaruka.customcrafter.customcrafter.listener.CloseCraftingTable;
import com.github.sakakiaruka.customcrafter.customcrafter.listener.OpenCraftingTable;
import com.github.sakakiaruka.customcrafter.customcrafter.object.DefinedCommand;
import com.github.sakakiaruka.customcrafter.customcrafter.util.DefinedCommandUtil;
import com.github.sakakiaruka.customcrafter.customcrafter.util.RecipePermissionUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static com.github.sakakiaruka.customcrafter.customcrafter.SettingsLoad.*;
import static com.github.sakakiaruka.customcrafter.customcrafter.listener.OpenCraftingTable.opening;

public class Check implements CommandExecutor{
    private final String nl = System.getProperty("line.separator");
    @Override
    public boolean onCommand(CommandSender sender, Command command,String label,String[] args){
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

        RECIPE_LIST.clear();
        NAMED_RECIPES_MAP.clear();

        WHAT_MAKING.clear();
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
