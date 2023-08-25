package com.github.sakakiaruka.customcrafter.customcrafter.command;

import com.github.sakakiaruka.customcrafter.customcrafter.util.PotionUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import static com.github.sakakiaruka.customcrafter.customcrafter.SettingsLoad.bar;
import static com.github.sakakiaruka.customcrafter.customcrafter.SettingsLoad.nl;

public class File {
    public void defaultPotion(String[] args, CommandSender sender) {
        Bukkit.getLogger().info(bar + nl);
        Bukkit.getLogger().info("The system is making default potion files.");
        Bukkit.getLogger().info("Do not shutdown or stop a server.");
        Bukkit.getLogger().info(nl + bar);
        new PotionUtil().makeDefaultPotionFilesWrapper();
    }
}
