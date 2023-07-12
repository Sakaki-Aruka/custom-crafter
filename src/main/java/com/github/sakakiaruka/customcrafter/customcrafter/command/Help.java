package com.github.sakakiaruka.customcrafter.customcrafter.command;

import com.github.sakakiaruka.customcrafter.customcrafter.object.DefinedCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.github.sakakiaruka.customcrafter.customcrafter.CustomCrafter.getInstance;
import static com.github.sakakiaruka.customcrafter.customcrafter.SettingsLoad.bar;
import static com.github.sakakiaruka.customcrafter.customcrafter.SettingsLoad.nl;
import static com.github.sakakiaruka.customcrafter.customcrafter.util.DefinedCommandUtil.DEFINED_COMMAND_LIST;

public class Help {
    private String separateBar = bar + nl + nl + nl;
    private String nl2 = nl + nl;
    public void all(String[] args, CommandSender sender) {
        FileConfiguration config = getInstance().getConfig();
        StringBuilder builder = new StringBuilder();
        for (DefinedCommand command : DEFINED_COMMAND_LIST) {
            builder.append(separateBar);
            builder.append(getHelpString(command, config));
            builder.append(nl2);
        }
        builder.append(separateBar);
        sender.sendMessage(builder.toString());
    }

    public void one(String[] args,CommandSender sender) {
        FileConfiguration config = getInstance().getConfig();
        StringBuilder builder = new StringBuilder();
        String type = args[1];

        for (DefinedCommand command : DEFINED_COMMAND_LIST) {
            if (!command.getCommandName().equals(type)) continue;
            builder.append(separateBar);
            builder.append(getHelpString(command, config));
            builder.append(nl2);
        }
        builder.append(separateBar);
        sender.sendMessage(builder.toString());
    }

    private String getHelpString(DefinedCommand command, FileConfiguration config) {
        StringBuilder builder = new StringBuilder();
        builder.append("CommandName: "+command.getCommandName()+nl);

        String usage = "/cc "+command.getCommandName()+ " ";
        usage += command.isHasArgs() ? String.join(" ",command.getArgs()) : "";
        builder.append(String.format("Command usage: %s %s",usage,nl));

        String permission = command.getCommandPermission().isEmpty() ? "This command does not require any permissions." : String.join(" | ",command.getCommandPermission());
        builder.append("Permission required: "+permission+nl);

        builder.append("description: "+nl);
        String notFound = "  Sorry, this command pattern has not any descriptions." + nl;
        if (config.contains("DefinedCommands."+command.getCommandName()+".descriptions")) {
            List<String> descriptions = getIndividualDescription(command, config);
            if (!descriptions.isEmpty()) descriptions.forEach(s->builder.append("  "+s+nl));
            else builder.append(notFound);
        } else {
            builder.append(notFound);
        }
        return builder.toString();
    }

    private List<String> getIndividualDescription(DefinedCommand command, FileConfiguration config) {
        List<String> result = new ArrayList<>();
        if (!command.isHasArgs()) {
            if (config.contains("DefinedCommands."+command.getCommandName()+".descriptions")) {
                result.addAll(config.getStringList("DefinedCommands."+command.getCommandName()+".descriptions"));
            }
            return result;
        }

        int index = -1;
        for (int i=0;i<config.getStringList("DefinedCommands."+command.getCommandName()+".args").size();i++) {
            String source = String.join(",",command.getArgs());
            String files = config.getStringList("DefinedCommands."+command.getCommandName()+".args").get(i);
            if (!source.equals(files)) continue;
            index = i;
            break;
        }

        if (index == -1) return result; // an empty list
        for (String s : config.getStringList("DefinedCommands."+command.getCommandName()+".descriptions")) {
            if (s.startsWith(index+":")) {
                String description = String.join("",Arrays.asList(s.split("")).subList(2,s.length()));
                result.add(description);
            }
        }
        return result;
    }
}
