package com.github.sakakiaruka.customcrafter.customcrafter.util;

import com.github.sakakiaruka.customcrafter.customcrafter.CustomCrafter;
import com.github.sakakiaruka.customcrafter.customcrafter.object.DefinedCommand;
import com.github.sakakiaruka.customcrafter.customcrafter.object.Matter.Matter;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachmentInfo;

import javax.crypto.spec.PSource;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.*;

import static com.github.sakakiaruka.customcrafter.customcrafter.CustomCrafter.getInstance;
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
    private final String COMMAND_ARGS_PATTERN = "{CommandArgs}";

    private static List<String> ARGS_INITIAL;
    public void loader() {
        FileConfiguration config = getInstance().getConfig();
        List<String> commands = config.getStringList("DefinedCommands.args");
        ARGS_INITIAL = new ArrayList<>(commands);
        for (String s : commands) {

            //debug
            System.out.println("s: "+s);

            boolean child = config.getBoolean("DefinedCommands."+s+".child");
            List<String> permission;
            if (config.contains("DefinedCommands."+s+".permission")){
                permission = loadPermissions(config.getString(String.format("DefinedCommands.%s.permission",s)));
            }else{
                permission = new ArrayList<>();
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

            DefinedCommand command = new DefinedCommand(s,false,null,console,permission,processClass,processMethod,0);
            DEFINED_COMMAND_LIST.add(command);

            //debug
            System.out.println(String.format("%s%n%s%n%s",bar,command.info(),bar));
        }
    }


    private List<String> loadPermissions(String input) {
        List<String> list = parsed(input);
        List<String> result = new ArrayList<>();
        Map<Integer,List<String>> map = new HashMap<>();

        for (int i=0;i<list.size();i++) {
            map.put(i, separator(list.get(i)));
        }

        for (Map.Entry<Integer,List<String>> entry : map.entrySet()) {
            for (String s : entry.getValue()) {
                result.add(s);
            }
        }

        return result;
    }

    private List<String> separator(String input) {
        List<String> buffer = new ArrayList<>();
        Map<Integer,List<String>> map = new HashMap<>();
        int dim = 0;
        int start = 0;
        input = input.replace(" ", "");
        List<String> list = Arrays.asList(input.split(""));
        for (int i=0;i<input.length();i++) {
            String s = String.valueOf(input.charAt(i));
            if (s.equals("(")) {
                dim++;
                continue;
            }

            if (s.equals(")")) {
                dim--;
                continue;
            }

            if (s.equals("&") && dim == 0) {
                int index = map.isEmpty() ? 0 : Collections.max(map.keySet()) + 1;
                map.put(index, new ArrayList<>(Arrays.asList(String.join("",list.subList(start, i)))));
                start = i;
                continue;
            }
        }

        map.put(map.isEmpty() ? 0 : Collections.max(map.keySet()) + 1, new ArrayList<>(Arrays.asList(String.join("",list.subList(start, list.size())))));

        List<Integer> sizes = new ArrayList<>();
        List<Map<Integer,Integer>> orders = new ArrayList<>();
        for (Map.Entry<Integer,List<String>> entry : map.entrySet()) {
            List<String> l = entry.getValue();
            String s = l.get(0).startsWith("&") ? l.get(0).substring(1, l.get(0).length()) : l.get(0);
            s = s.replace("(", "").replace(")", "");
            entry.getValue().addAll(Arrays.asList(s.split("\\|")));
            entry.getValue().remove(0);
            sizes.add(entry.getValue().size());
        }

        for (int i=0;i<getAllElementsProduct(sizes);i++) {
            orders.add(new HashMap<>());
        }

        for (int i=0;i<sizes.size();i++) {
            make(i,sizes,orders);
        }

        for (Map<Integer,Integer> m : orders) {
            List<String> ll = new ArrayList<>();
            for (Map.Entry<Integer,Integer> entry : m.entrySet()) {
                String temp = map.get(entry.getKey()).get(entry.getValue());
                temp = temp.replace("&",",");
                //ll.add(map.get(entry.getKey()).get(entry.getValue()));
                ll.add(temp);
            }
            buffer.add(String.join(",", ll));
        }

        return buffer;

    }

    private int getAllElementsProduct(List<Integer> list) {
        int result = 1;
        for (int i : list) {
            result *= i;
        }
        return result;
    }

    private void make(int index, List<Integer> source, List<Map<Integer,Integer>> map) {
        int c = getAllElementsProduct(source.subList(index, source.size()));
        int count = getAllElementsProduct(source.subList(index+1, source.size()));
        int all = getAllElementsProduct(source);
        int now = 0;
        for (int k=0;k<all/c;k++) {
            for (int i=0;i<source.get(index);i++) {
                for (int j=0;j<count;j++) {
                    map.get(now).put(index, i);
                    now++;
                }
            }
        }
    }

    private List<String> parsed(String input) {
        StringBuilder builder = new StringBuilder();
        int depth = getDepth(input);
        int dim = 0;
        String separator = String.join("", Collections.nCopies(depth+1,"|"));
        for (String s : input.split("")) {
            if (s.equals("(")) dim++;
            if (s.equals(")")) dim--;
            if (s.equals("|") && dim ==0) {
                builder.append(separator);
                continue;
            }

            builder.append(s);
        }

        String sep = String.join("", Collections.nCopies(2, "\\|"));
        return new ArrayList<>(Arrays.asList(builder.toString().split(sep)));
    }

    private int getDepth(String input) {
        Set<Integer> set = new HashSet<>();
        int d = 0;
        for (String s : input.split("")) {
            if (s.equals("(")) d++;
            if (s.equals(")")) d--;
            set.add(d);
        }
        return Collections.max(set);
    }

    public DefinedCommand getProcessor(String[] args, CommandSender sender) {
        List<String> arg = new ArrayList<>(Arrays.asList(args));
        A:for (DefinedCommand command : DEFINED_COMMAND_LIST) {
            if (command.getCommandLen() != arg.size()-1) continue;
            if (!command.getCommandName().equalsIgnoreCase(arg.get(0))) continue;
            if ((sender instanceof ConsoleCommandSender) && !command.isConsole()) continue;
            if (arg.size() == 1) return command;
            // permission checker here
            if (!senderContainsPermission(sender, command)) continue;

            B:for(int i=0;i<command.getCommandLen();i++) {
                String commandPart = command.getArgs().get(i);
                String inputPart = arg.get(i+1);

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

                if (commandPart.equals(COMMAND_ARGS_PATTERN)) {
                    if (!ARGS_INITIAL.contains(inputPart)) continue A;
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

                if (!inputPart.equals(commandPart)) continue A;
            }

            return command;
        }
        return null;
    }

    private boolean senderContainsPermission(CommandSender sender, DefinedCommand command) {
        if (sender instanceof ConsoleCommandSender) return true;
        Player player = (Player) sender;
        Set<String> perms = new HashSet<>();
        for (PermissionAttachmentInfo pai : player.getEffectivePermissions()) {
            perms.add(pai.getPermission());
        }
        if (command.getCommandPermission().isEmpty()) return true;
        if (perms == null || perms.isEmpty()) return false;

        int counter = 0;
        for (String s : command.getCommandPermission()) {
            for (String t : s.split(",")) {
                if (t.equals("OP") && player.isOp()) counter++;
                if (perms.contains(t)) counter++;
            }
            if (counter == s.split(",").length) return true;
            counter = 0;
        }
        return false;
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