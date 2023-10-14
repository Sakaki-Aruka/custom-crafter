package com.github.sakakiaruka.customcrafter.customcrafter.command;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.github.sakakiaruka.customcrafter.customcrafter.SettingsLoad.*;
import static com.github.sakakiaruka.customcrafter.customcrafter.util.RecipePermissionUtil.RECIPE_PERMISSION_MAP;

public class Processor implements CommandExecutor, TabCompleter {

    private static final String CONTAINER_KEY = "([a-z0-9-_]+)";
    private static final String CONTAINER_TYPE = "(?i)(string|double|int)";
    private static final String NUMBER_ALPHABET = "(.+)";
    private static final String OPERATOR = "(\\+|-|\\*|/|^)";
    private static String RECIPE;
    private static String MATTER;
    private static String RESULT;
    private static String RECIPE_PERMISSION;


    public void init() {
        MATTER = getCustomElementsPattern(CUSTOM_MATTERS.keySet());
        RESULT = getCustomElementsPattern(CUSTOM_RESULTS.keySet());
        RECIPE = getCustomElementsPattern(NAMED_RECIPES_MAP.keySet());
        RECIPE_PERMISSION = getCustomElementsPattern(RECIPE_PERMISSION_MAP.keySet());
    }

    private Map<Integer, String> getCommandPattern() {
        Map<Integer, String> map = new HashMap<>();
        map.put(1, "reload");
        map.put(2 + 100, "open");

        if (noEmpty(RECIPE)) map.put(3, "show "+RECIPE);
        map.put(4, "show all");

        if (noEmpty(MATTER)) map.put(5 + 100, "give "+MATTER);
        if (noEmpty(RESULT)) map.put(6 + 100, "give "+RESULT);

        map.put(7, "file make defaultPotion");

        if (noEmpty(RECIPE_PERMISSION)) map.put(8, "permission "+RECIPE_PERMISSION);
        if (noEmpty(getPlayerListPattern())) map.put(9, "permission permissions "+ getPlayerListPattern());
        if (noEmpty(getPlayerListPattern(), RECIPE_PERMISSION)) map.put(10, "permission permissions modify "+ getPlayerListPattern() + " add " + RECIPE_PERMISSION);
        if (noEmpty(getPlayerListPattern(), RECIPE_PERMISSION)) map.put(11, "permission permissions modify "+ getPlayerListPattern() + " remove " + RECIPE_PERMISSION);

        map.put(12, "help all");
        if (noEmpty(getCustomElementsPattern(COMMAND_ARGS))) map.put(13, "help "+getCustomElementsPattern(COMMAND_ARGS));

        map.put(14 + 100, "container add "+CONTAINER_KEY+" "+CONTAINER_TYPE+" "+NUMBER_ALPHABET);
        map.put(15 + 100, "container remove "+CONTAINER_KEY+" "+CONTAINER_TYPE);
        map.put(16 + 100, "container set "+CONTAINER_KEY+" "+CONTAINER_TYPE+" "+NUMBER_ALPHABET);
        map.put(17 + 100, "container value_modify "+CONTAINER_KEY+" "+CONTAINER_TYPE+" "+OPERATOR+" "+CONTAINER_KEY+" "+NUMBER_ALPHABET);
        map.put(18 + 100, "container data show");
        return map;
    }

    private boolean noEmpty(String... in) {
        for (String s : in) {
            if (s.isEmpty()) return false;
        }
        return true;
    }


    private String getPlayerListPattern() {
        if (Bukkit.getOnlinePlayers().isEmpty()) return "";
        StringBuilder builder = new StringBuilder();
        builder.append("(");
        for (Player player : Bukkit.getOnlinePlayers()) {
            builder.append(player.getName()).append("|");
        }
        builder.deleteCharAt(builder.length()-1);
        builder.append(")");
        return builder.toString();
    }

    private static String getCustomElementsPattern(Set<String> arg) {
        if (arg.isEmpty()) return "";
        StringBuilder builder = new StringBuilder();
        builder.append("(");
        builder.append(String.join("|", arg));
        builder.append(")");
        return builder.toString().equals("()") ? "" : builder.toString();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        int id = -1;
        String sent = String.join(" ", args);
        for (Map.Entry<Integer, String> entry : getCommandPattern().entrySet()) {
            if (sent.matches(entry.getValue())) {
                id = entry.getKey();
                break;
            }
        }

        if (id == -1) {
            sender.sendMessage("[CustomCrafter] This command is not a correct one.");
            return false;
        } else if (!(sender instanceof Player) && 100 < id) {
            sender.sendMessage("[CustomCrafter] You cannot use this command from other than as a player.");
            return false;
        }
        else if (id == 1) new Check().reload();
        else if (id == 2 + 100) new Check().open(sender);
        else if (id == 3) new Show().all(sender);
        else if (id == 4) new Show().one(args, sender);
        else if (id == 5 + 100) new Give().matter(args[1], sender);
        else if (id == 6 + 100) new Give().result(args[1], sender);
        else if (id == 7) new File().defaultPotion();
        else if (id == 8) new PermissionCheck().tree(args[1], sender);
        else if (id == 9) new PermissionCheck().show(args[2], sender);
        else if (id == 10) new PermissionCheck().add(args[3], args[5], sender);
        else if (id == 11) new PermissionCheck().remove(args[3], args[5], sender);
        else if (id == 12) new Help().all(sender);
        else if (id == 13) new Help().one(args[1], sender);
        else if (id == 14 + 100 || id == 16 + 100) new ContainerModify().addSet(args, sender);
        else if (id == 15 + 100) new ContainerModify().remove(args, sender);
        else if (id == 17 + 100) new ContainerModify().valueModify(args, sender);
        else if (id == 18 + 100) new ContainerModify().data(sender);

        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        List<String> list = new ArrayList<>();
        List<String> arg = new ArrayList<>(Arrays.asList("reload", "open", "show", "give", "file", "permission", "help", "container"));
        if (args.length == 1) {
            list.addAll(arg);
            if (!(sender instanceof Player)) {
                list.remove("open");
                list.remove("give");
                list.remove("container");
            }

        } else if (args.length == 2) {
            switch (args[0]) {
                case "show":
                    list.add("all");
                    list.addAll(NAMED_RECIPES_MAP.keySet());
                    break;
                case "give":
                    list.add("matter");
                    list.add("result");
                    break;
                case "file":
                    list.add("make");
                    break;
                case "permission":
                    list.addAll(RECIPE_PERMISSION_MAP.keySet());
                    list.add("permissions");
                    break;
                case "help":
                    list.add("all");
                    // --- command args --- //
                    list.add("reload");
                    list.add("open");
                    list.add("show");
                    list.add("give");
                    list.add("file");
                    list.add("permission");
                    list.add("help");
                    list.add("container");
                    break;
                case "container":
                    list.add("add");
                    list.add("remove");
                    list.add("set");
                    list.add("value_modify");
                    list.add("data");
                    break;
            }
        } else if (args.length == 3) {

            switch (args[0]) {
                case "give":
                    if (args[1].equals("matter")) list.addAll(CUSTOM_MATTERS.keySet());
                    if (args[1].equals("result")) list.addAll(CUSTOM_RESULTS.keySet());
                    break;
                case "file":
                    if (args[1].equals("make")) {
                        list.add("defaultPotion");
                    }
                    break;
                case "permission":
                    if (args[1].equals("permissions")) {
                        list.addAll(getOnlinePlayers());
                        list.add("modify");
                    }
                    break;
                case "container":
                    if (args[1].equals("data")) list.add("show");
                    break;
            }
        } else if (args.length == 4) {
            if (args[0].equals("permission")) {
                if (args[2].equals("modify")) list.addAll(getOnlinePlayers());
            } else if (args[0].equals("container")) {
                list.add("string");
                list.add("double");
                list.add("int");
            }
        } else if (args.length == 5) {
            if (args[0].equals("permission")) {
                list.add("add");
                list.add("remove");
            } else if (args[0].equals("container")) {
                if (args[1].equals("value_modify")) {
                    list.add("+");
                    list.add("-");
                    list.add("*");
                    list.add("/");
                    list.add("^");
                }
            }
        } else if (args.length == 6) {
            if (args[0].equals("permission")) list.addAll(RECIPE_PERMISSION_MAP.keySet());
        }

        if (!list.isEmpty()) {
            List<String> filtered = new ArrayList<>();
            for (String s : list) {
                if (s == null) continue;
                if (s.startsWith(args[args.length-1])) filtered.add(s);
            }
            return filtered;
        }
        return list;
    }

    private List<String> getOnlinePlayers() {
        List<String> list = new ArrayList<>();
        Bukkit.getOnlinePlayers().forEach(player -> list.add(player.getName()));
        return list;
    }
}
