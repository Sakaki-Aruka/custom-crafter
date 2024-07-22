package com.github.sakakiaruka.customcrafter.customcrafter.command;

import com.github.sakakiaruka.customcrafter.customcrafter.CustomCrafter;
import com.github.sakakiaruka.customcrafter.customcrafter.util.PotionUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import static com.github.sakakiaruka.customcrafter.customcrafter.SettingsLoad.BAR;
import static com.github.sakakiaruka.customcrafter.customcrafter.SettingsLoad.LINE_SEPARATOR;

public class File {
    public void defaultPotion() {
        CustomCrafter.getInstance().getLogger().info(BAR + LINE_SEPARATOR);
        CustomCrafter.getInstance().getLogger().info("The system is making default potion files.");
        CustomCrafter.getInstance().getLogger().info("Do not shutdown or stop a server.");
        CustomCrafter.getInstance().getLogger().info(LINE_SEPARATOR + BAR);
        PotionUtil.makeDefaultPotionFilesWrapper();
    }
}
