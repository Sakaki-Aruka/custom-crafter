package com.github.sakakiaruka.customcrafter.customcrafter.command;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.*;

import static com.github.sakakiaruka.customcrafter.customcrafter.SettingsLoad.*;
import static com.github.sakakiaruka.customcrafter.customcrafter.util.RecipePermissionUtil.RECIPE_PERMISSION_MAP;

public class Processor implements CommandExecutor, TabCompleter {

    private static final String CONTAINER_KEY = "([a-z0-9-_]+)";
    private static final String CONTAINER_TYPE = "(?i)(string|double|int)";
    private static final String NUMBER_ALPHABET = "(.+)";
    private static final String OPERATOR = "(+|-|*|/|^)";
    private static final String RECIPE = getCustomElementsPattern(NAMED_RECIPES_MAP.keySet());
    private static final String MATTER = getCustomElementsPattern(CUSTOM_MATTERS.keySet());
    private static final String RESULT = getCustomElementsPattern(CUSTOM_RESULTS.keySet());
    private static final String RECIPE_PERMISSION = getCustomElementsPattern(RECIPE_PERMISSION_MAP.keySet());

    private List<String> getCommandPattern() {
        List<String> list = new ArrayList<>();
        list.add("reload");
        list.add("open");

        list.add("show "+RECIPE);
        list.add("show all");

        list.add("give "+MATTER);
        list.add("give "+RESULT);

        list.add("file make defaultPotion");

        list.add("permission "+RECIPE_PERMISSION);
        list.add("permission permissions "+ getPlayerListPattern());
        list.add("permission permissions modify "+ getPlayerListPattern() + " add " + RECIPE_PERMISSION);
        list.add("permission permissions modify "+ getPlayerListPattern() + " remove " + RECIPE_PERMISSION);

        list.add("help all");
        list.add("help "+getCustomElementsPattern(COMMAND_ARGS));

        list.add("container add "+CONTAINER_KEY+" "+CONTAINER_TYPE+" "+NUMBER_ALPHABET);
        list.add("container remove "+CONTAINER_KEY+" "+CONTAINER_TYPE);
        list.add("container set "+CONTAINER_KEY+" "+CONTAINER_TYPE+" "+NUMBER_ALPHABET);
        list.add("container value_modify "+CONTAINER_KEY+" "+CONTAINER_TYPE+" "+OPERATOR+" "+CONTAINER_KEY+" "+NUMBER_ALPHABET);
        list.add("container data show");
        return list;
    }

    private String getPlayerListPattern() {
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
        StringBuilder builder = new StringBuilder();
        builder.append("(");
        for (String name : arg) {
            builder.append(name).append("|");
        }
        builder.deleteCharAt(builder.length()-1);
        builder.append(")");
        return builder.toString();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        //debug
        String matched = "";
        String send = String.join(" ", args);
        for (String c : getCommandPattern()) {
            if (send.matches(c)) {
                matched = c;
                break;
            }
        }

        if (matched.isEmpty()) {
            sender.sendMessage("[CustomCrafter] Send a command with arguments.");
            return false;
        }

        if (args[0].equals("reload")) new Check().reload();
        if (args[0].equals("open")) new Check().open(sender);

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
