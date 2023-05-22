package com.github.sakakiaruka.customcrafter.customcrafter.command;

import com.github.sakakiaruka.customcrafter.customcrafter.object.Permission.RecipePermission;
import com.github.sakakiaruka.customcrafter.customcrafter.util.RecipePermissionUtil;
import org.bukkit.Bukkit;

import java.util.List;
import java.util.UUID;

import static com.github.sakakiaruka.customcrafter.customcrafter.SettingsLoad.nl;
import static com.github.sakakiaruka.customcrafter.customcrafter.util.RecipePermissionUtil.playerPermissions;
import static com.github.sakakiaruka.customcrafter.customcrafter.util.RecipePermissionUtil.recipePermissionMap;

public class PermissionCheck {
    public void main(String[] args){

        String name = args[1];
        if(args.length == 3 && args[1].equals("-p")) {
            if(args.length != 3) return;
            String targetName = args[2];
            if(!Bukkit.getOnlinePlayers().contains(targetName)) {
                System.out.println("Permission Query Error: The target player is not online mode.");
                return;
            }
            UUID uuid = Bukkit.getPlayer(targetName).getUniqueId();
            displayPlayerPermissions(uuid);

        }else if(args.length == 2 && recipePermissionMap.containsKey(name)){
            displayPermissionInfo(name);

        }
    }

    private void displayPermissionInfo(String name){
        // /cc -p [permissionName]
        RecipePermission perm = recipePermissionMap.get(name);
        System.out.println(new RecipePermissionUtil().getPermissionTree(perm));
    }

    private void displayPlayerPermissions(UUID uuid){
        // /cc -p -p [targetPlayerName]
        StringBuilder builder = new StringBuilder();
        builder.append("=== RecipePermissions (The player has) ==="+nl);
        if(!playerPermissions.containsKey(uuid)) {
            builder.append(String.format("Target player has no permissions.%s",nl));
            System.out.println(builder);
            return;
        }

        List<RecipePermission> permissions = playerPermissions.get(uuid);
        permissions.forEach(s->builder.append(String.format("Parent: %s | Name: %s %s",s.getParent(),s.getPermissionName(),nl)));
        builder.append("=== RecipePermissions (END) ===");
        System.out.println(builder);
    }
}
