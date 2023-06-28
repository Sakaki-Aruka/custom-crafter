package com.github.sakakiaruka.customcrafter.customcrafter.util;

import com.github.sakakiaruka.customcrafter.customcrafter.CustomCrafter;
import com.github.sakakiaruka.customcrafter.customcrafter.object.DefinedCommand;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachmentInfo;

import java.lang.reflect.Constructor;
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
        List<String> commands = config.getStringList("DefinedCommands.args");
        for (String s : commands) {

            //debug
            System.out.println("s: "+s);

            boolean child = config.getBoolean("DefinedCommands."+s+".child");
            Permission permission;
            if (config.contains("DefinedCommands."+s+".permission")){
                permission = new Permission(config.getString("DefinedCommands."+s+".permission"));
            }else{
                permission = null;
            }
            boolean console = config.getBoolean("DefinedCommands."+s+".console");
            Class<?> processClass;
            Method processMethod;
            try{
                processClass = Class.forName(config.getString("DefinedCommands."+s+".class"));
            }catch (Exception e) {
                e.printStackTrace();
                continue;
            }

            if (child) {
                for (int i=0;i<config.getStringList("DefinedCommands."+s+".args").size();i++) {
                    List<String> args = Arrays.asList(config.getStringList("DefinedCommands."+s+".args").get(i).split(","));
                    String method = config.getStringList("DefinedCommands."+s+".method").get(i);
                    try{
                        Class[] argTypes = {String[].class, CommandSender.class};
                        processMethod = processClass.getDeclaredMethod(method,argTypes);
                    }catch (Exception e) {
                        e.printStackTrace();
                        continue;
                    }
                    DefinedCommand command = new DefinedCommand(s,true,args,console,permission,processClass,processMethod,args.size());
                    DEFINED_COMMAND_LIST.add(command);

                    //debug
                    System.out.println(String.format("%s%n%s%n%s",bar,command.info(),bar));

                }
                continue;
            }

            try{
                Class[] argTypes = {String[].class, CommandSender.class};
                processMethod = processClass.getDeclaredMethod(config.getString("DefinedCommands."+s+".method"),argTypes);
            }catch (Exception e) {
                e.printStackTrace();
                continue;
            }

            DefinedCommand command = new DefinedCommand(s,false,null,console,permission,processClass,processMethod,1);
            DEFINED_COMMAND_LIST.add(command);

            //debug
            System.out.println(String.format("%s%n%s%n%s",bar,command.info(),bar));
        }
    }

    public DefinedCommand getProcessor(String[] args, CommandSender sender) {
        List<String> arg = new ArrayList<>(Arrays.asList(args));
        A:for (DefinedCommand command : DEFINED_COMMAND_LIST) {
            if (command.getCommandLen() != arg.size()-1) continue;
            if (!command.getCommandName().equalsIgnoreCase(arg.get(0))) continue;
            if (arg.size() == 1) return command;
            if ((sender instanceof ConsoleCommandSender) && !command.isConsole()) continue;
            if (!sender.hasPermission(command.getCommandPermission())) continue;

            //debug
            System.out.println("through");

            B:for(int i=0;i<command.getCommandLen();i++) {
                String commandPart = command.getArgs().get(i);
                String inputPart = arg.get(i+1);

                //debug
                System.out.println(String.format("command part: %s / input part: %s",commandPart,inputPart));

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

    private boolean containsPermissions(Set<PermissionAttachmentInfo> players, Map<String,List<Permission>> required) {
        //
    }

    public void runCommand(DefinedCommand command, String[] args, CommandSender sender) {
        Method m = command.getProcessMethod();
        Class<?> c = command.getProcessClass();
        Object[] objects = {args, sender};
        try{
            Constructor constructor = c.getConstructor();
            Object o = constructor.newInstance();
            m.invoke(o,objects);
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