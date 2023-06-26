package com.github.sakakiaruka.customcrafter.customcrafter.util;

import com.github.sakakiaruka.customcrafter.customcrafter.CustomCrafter;
import com.github.sakakiaruka.customcrafter.customcrafter.object.DefinedCommand;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.*;

import static com.github.sakakiaruka.customcrafter.customcrafter.SettingsLoad.*;
import static com.github.sakakiaruka.customcrafter.customcrafter.util.RecipePermissionUtil.recipePermissionMap;

public class DefinedCommandUtil {
    public static Map<Integer,List<DefinedCommand>> definedCommands = new HashMap<>();

    public void loader() {
        FileConfiguration config = CustomCrafter.getInstance().getConfig();
        for(String s : config.getStringList("DefinedCommands")) {
            List<String> list = Arrays.asList(s.split(","));
            String name = list.get(0);
            boolean hasArgs = Boolean.valueOf(list.get(1));
            int argsLen = 0;
            List<String> args = new ArrayList<>();
            if (hasArgs) {
                argsLen = Integer.valueOf(list.get(2));
                args.addAll(list.subList(3,list.size()-1));
            }
            DefinedCommand defined = new DefinedCommand(name,hasArgs,argsLen,args);

            if(!definedCommands.containsKey(argsLen)) {
                definedCommands.put(argsLen,new ArrayList<>());
            }

            definedCommands.get(argsLen).add(defined);
        }
    }

    public boolean isCorrectCommand(List<String> input) {
        if (!definedCommands.containsKey(input.size())) return false; //invalid args

        for(DefinedCommand command : definedCommands.get(input.size())) {
            for (int i=0;i<command.getArgsLen();i++) {
                if (command.getArgs().get(i).equals("{Player}")) {
                    Player dummy;
                    try{
                        dummy = Bukkit.getPlayer(input.get(i));
                    }catch (Exception e) {
                        return false;
                    }
                    if (!Bukkit.getOnlinePlayers().contains(dummy)) return false;
                }

                if (command.getArgs().get(i).equals("{Matter}")) {
                    if (!matters.keySet().contains(input.get(i))) return false;
                }

                if (command.getArgs().get(i).equals("{Result}")) {
                    if (!results.keySet().contains(input.get(i))) return false;
                }

                if (command.getArgs().get(i).equals("{Recipe}")) {
                    if (!namedRecipes.containsKey(input.get(i))) return false;
                }

                if (command.getArgs().get(i).equals("{RecipePermission}")) {
                    if (!recipePermissionMap.keySet().contains(input.get(i))) return false;
                }

                if (command.getArgs().get(i).startsWith("-")) {
                    if (!command.getArgs().get(i).equalsIgnoreCase(input.get(i))) return false;
                }

                if (!command.getArgs().get(i).equals(input.get(i))) return false;
            }
        }
        return true;
    }
}