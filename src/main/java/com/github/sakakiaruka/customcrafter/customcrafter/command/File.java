package com.github.sakakiaruka.customcrafter.customcrafter.command;

import com.github.sakakiaruka.customcrafter.customcrafter.util.PotionUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import static com.github.sakakiaruka.customcrafter.customcrafter.SettingsLoad.BAR;
import static com.github.sakakiaruka.customcrafter.customcrafter.SettingsLoad.LINE_SEPARATOR;

public class File {
    public void defaultPotion() {
        Bukkit.getLogger().info(BAR + LINE_SEPARATOR);
        Bukkit.getLogger().info("The system is making default potion files.");
        Bukkit.getLogger().info("Do not shutdown or stop a server.");
        Bukkit.getLogger().info(LINE_SEPARATOR + BAR);
        new PotionUtil().makeDefaultPotionFilesWrapper();
    }
}
