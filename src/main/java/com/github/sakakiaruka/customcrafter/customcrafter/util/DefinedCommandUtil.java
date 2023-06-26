package com.github.sakakiaruka.customcrafter.customcrafter.util;

import com.github.sakakiaruka.customcrafter.customcrafter.CustomCrafter;
import com.github.sakakiaruka.customcrafter.customcrafter.object.DefinedCommand;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.*;

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
            //
        }
    }
}