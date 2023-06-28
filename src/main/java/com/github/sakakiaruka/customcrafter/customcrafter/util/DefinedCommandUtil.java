package com.github.sakakiaruka.customcrafter.customcrafter.util;

import com.github.sakakiaruka.customcrafter.customcrafter.CustomCrafter;
import com.github.sakakiaruka.customcrafter.customcrafter.object.DefinedCommand;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;

import java.lang.reflect.Method;
import java.util.*;

import static com.github.sakakiaruka.customcrafter.customcrafter.SettingsLoad.*;
import static com.github.sakakiaruka.customcrafter.customcrafter.util.RecipePermissionUtil.recipePermissionMap;

public class DefinedCommandUtil {
    public static Map<Integer,List<DefinedCommand>> definedCommands = new HashMap<>();

    public static List<DefinedCommand> DEFINED_COMMAND_LIST = new ArrayList<>();

    private final String MATTER_PATTERN = "{Matter}";
    private final String RESULT_PATTERN = "{Result}";
    private final String RECIPE_PATTERN = "{Recipe}";
    private final String RECIPE_PERMISSION_PATTERN = "{RecipePermission}";
    private final String PLAYER_PATTERN = "{Player}";

    public void loader() {
        FileConfiguration config = CustomCrafter.getInstance().getConfig();
        List<String> commands = config.getStringList("args");
        for (String s : commands) {
            boolean child = config.getBoolean(s+".child");
            Permission permission = config.getString(s+".permission").equals("null") ? null : new Permission(config.getString(s+".permission"));
            boolean console = config.getBoolean(s+".console");
            Class<?> processClass;
            Method processMethod;
            try{
                processClass = Class.forName(config.getString(s+".class"));
                //processMethod = processClass.getDeclaredMethod(config.getString(s+".class"));
            }catch (Exception e) {
                e.printStackTrace();
                continue;
            }

            if (child) {
                for (int i=0;i<config.getStringList(s+".args").size();i++) {
                    List<String> args = Arrays.asList(config.getStringList(s+".args").get(i).split(","));
                    try{
                        processMethod = processClass.getDeclaredMethod(processClass.getName());
                    }catch (Exception e) {
                        e.printStackTrace();
                        continue;
                    }
                    DefinedCommand command = new DefinedCommand(s,true,args,console,permission,processClass,processMethod,args.size()+1);
                    DEFINED_COMMAND_LIST.add(command);
                }
                continue;
            }

            try{
                processMethod = processClass.getDeclaredMethod(processClass.getName());
            }catch (Exception e) {
                e.printStackTrace();
                continue;
            }

            DefinedCommand command = new DefinedCommand(s,false,null,console,permission,processClass,processMethod,1);
            DEFINED_COMMAND_LIST.add(command);
        }
    }

    public DefinedCommand getProcessor(String[] args, CommandSender sender) {
        List<String> arg = new ArrayList<>(Arrays.asList(args));
        A:for (DefinedCommand command : DEFINED_COMMAND_LIST) {
            if (command.getCommandLen() != arg.size()) continue;
            if (!command.getCommandName().equalsIgnoreCase(arg.get(0))) continue;
            if (arg.size() == 1) return command;
            if ((sender instanceof ConsoleCommandSender) && !command.isConsole()) continue;
            if (!sender.hasPermission(command.getCommandPermission())) continue;

            B:for(int i=0;i<command.getCommandLen();i++) {
                String commandPart = command.getArgs().get(i);
                String inputPart = arg.get(i);

                if (commandPart.startsWith("-")){
                    if (!commandPart.equalsIgnoreCase(inputPart)) continue A;
                    continue;
                }

                if (commandPart.equals(MATTER_PATTERN)) {
                    if (!matters.containsKey(inputPart)) continue A;
                    continue;
                }

                if (commandPart.equals(RESULT_PATTERN)) {
                    if (!results.containsKey(inputPart)) continue A;
                    continue;
                }

                if (commandPart.equals(RECIPE_PATTERN)) {
                    if (!namedRecipes.containsKey(inputPart)) continue A;
                    continue;
                }

                if (commandPart.equals(RECIPE_PERMISSION_PATTERN)) {
                    if (!recipePermissionMap.containsKey(inputPart)) continue A;
                    continue;
                }

                if (commandPart.equals(PLAYER_PATTERN)) {
                    if (!getPlayerNames().contains(inputPart)) continue A;
                    continue;
                }

                if (commandPart.startsWith("{")) {
                    String contents = commandPart.replace("{","").replace("}","");
                    List<String> list = Arrays.asList(contents.split("&"));
                    for (String s : list) {
                        if (inputPart.equalsIgnoreCase(s)) continue B;
                    }
                    continue A;
                }

                if (inputPart.equals(commandPart)) continue A;
                continue B;
            }

            return command;
        }
        return null;
    }

    public void runCommand(DefinedCommand command, String[] args, CommandSender sender) {
        Method m = command.getProcessMethod();
        Class<?> c = command.getProcessClass();
        try{
            m.invoke(c,args,sender);
        }catch (Exception e) {
            e.printStackTrace();
        }
    }


    private List<String> getPlayerNames() {
        List<String> list = new ArrayList<>();
        Bukkit.getOnlinePlayers().forEach(s->list.add(s.getName()));
        return list;
    }
}