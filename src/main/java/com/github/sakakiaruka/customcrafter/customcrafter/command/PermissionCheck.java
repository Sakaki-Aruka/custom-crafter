package com.github.sakakiaruka.customcrafter.customcrafter.command;

import com.github.sakakiaruka.customcrafter.customcrafter.object.Permission.RecipePermission;
import com.github.sakakiaruka.customcrafter.customcrafter.util.RecipePermissionUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.github.sakakiaruka.customcrafter.customcrafter.SettingsLoad.nl;
import static com.github.sakakiaruka.customcrafter.customcrafter.util.RecipePermissionUtil.playerPermissions;
import static com.github.sakakiaruka.customcrafter.customcrafter.util.RecipePermissionUtil.recipePermissionMap;

public class PermissionCheck {
    public void main(String[] args, CommandSender sender){

        String name = args[1];
        if(args.length == 3 && args[1].equals("-p")) {
            if(args.length != 3) return;
            String targetName = args[2];
            if(!isNamePlayerOnline(targetName)) {
                System.out.println("Permission Query Error: The target player is not online mode.");
                return;
            }
            UUID uuid = Bukkit.getPlayer(targetName).getUniqueId();
            displayPlayerPermissions(uuid);

        }else if(args[1].equals("-m")){
            if(!sender.isOp()) return;
            playersPermissionModify(args);

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

    private boolean playersPermissionModify(String[] args){
        // /cc -p -m [targetPlayerName] [operation] [targetRecipePermission]
        // operation -> [add | remove]

        if(args.length != 5) return false;
        if(!isNamePlayerOnline(args[2])) return false;
        Player target = Bukkit.getPlayer(args[2]);
        String operation = args[3];
        if(!recipePermissionMap.containsKey(args[4])) return false;
        RecipePermission permission = recipePermissionMap.get(args[4]);
        if(!operation.equalsIgnoreCase("add") && !operation.equalsIgnoreCase("remove")) return false;

        if(operation.equalsIgnoreCase("add")){
            // add
            if(new RecipePermissionUtil().hasPermission(permission,target)) return true;
            playerPermissions.get(target.getUniqueId()).add(permission);
            new RecipePermissionUtil().removePermissionDuplications(playerPermissions.get(target.getUniqueId()));
            System.out.println(String.format("Permission added: %s  Permission: %s%s  Target: %s",nl,permission.getPermissionName(),nl,target.getName()));

        }else{
            // remove
            if(!new RecipePermissionUtil().hasPermission(permission,target)) return true;
            System.out.println(String.format("Permission removed: %s  Permission: %s%s  Target: %s",nl,permission.getPermissionName(),nl,target.getName()));
            playerPermissions.get(target.getUniqueId()).remove(permission);

        }

        displayPlayerPermissions(target.getUniqueId());
        return true;
    }

    private boolean isNamePlayerOnline(String name){
        List<String> onlinePlayer = new ArrayList<>();
        Bukkit.getOnlinePlayers().forEach(s->onlinePlayer.add(s.getName()));
        return onlinePlayer.contains(name);
    }
}
