package com.github.sakakiaruka.customcrafter.customcrafter.command;

import com.github.sakakiaruka.customcrafter.customcrafter.util.PotionUtil;
import org.bukkit.command.CommandSender;

import static com.github.sakakiaruka.customcrafter.customcrafter.SettingsLoad.bar;
import static com.github.sakakiaruka.customcrafter.customcrafter.SettingsLoad.nl;

public class File {
    public void defaultPotion(String[] args, CommandSender sender) {
        System.out.println(bar + nl);
        System.out.println("The system is making default potion files.");
        System.out.println("Do not shutdown or stop a server.");
        System.out.println(nl + bar);
        new PotionUtil().makeDefaultPotionFilesWrapper();
    }
}
