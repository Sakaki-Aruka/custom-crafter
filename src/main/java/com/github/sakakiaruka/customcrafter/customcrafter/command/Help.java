package com.github.sakakiaruka.customcrafter.customcrafter.command;

import com.github.sakakiaruka.customcrafter.customcrafter.CustomCrafter;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;

import static com.github.sakakiaruka.customcrafter.customcrafter.SettingsLoad.BAR;
import static com.github.sakakiaruka.customcrafter.customcrafter.SettingsLoad.COMMAND_ARGS;
import static com.github.sakakiaruka.customcrafter.customcrafter.SettingsLoad.LINE_SEPARATOR;

public class Help {
    public void all(CommandSender sender) {
        FileConfiguration config = CustomCrafter.getInstance().getConfig();
        for (String arg : COMMAND_ARGS) {
            sender.sendMessage(BAR);
            sender.sendMessage(arg);
            for (String description : config.getStringList(arg)) {
                sender.sendMessage(description);
            }
            sender.sendMessage(BAR);
        }
    }

    public void one(String type, CommandSender sender) {
        if (!COMMAND_ARGS.contains(type)) {
            sender.sendMessage("[CustomCrafter] The specified argument is not exist.");
            sender.sendMessage("[CustomCrafter] Choose one from these. = "+COMMAND_ARGS);
            return;
        }
        sender.sendMessage(BAR);
        sender.sendMessage(type);
        FileConfiguration config = CustomCrafter.getInstance().getConfig();
        for (String description : config.getStringList(type)) {
            sender.sendMessage(description);
        }
        sender.sendMessage(BAR + LINE_SEPARATOR);
    }
}
