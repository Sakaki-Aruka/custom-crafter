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

        //debug
        RecipePermissionUtil util = new RecipePermissionUtil();
        RecipePermission r1 = new RecipePermission("ROOT","CHILD");
        RecipePermission r2 = new RecipePermission("CHILD","GRAND_CHILD");
        RecipePermission r3 = new RecipePermission("GRAND_CHILD","GREAT_GRAND_CHILD");
        RecipePermission rr1 = new RecipePermission("ROOT","CHILD_1");
        RecipePermission rr2 = new RecipePermission("CHILD_1","CHILD_2");
        RecipePermission rr3 = new RecipePermission("CHILD_2","CHILD_3");
        recipePermissionMap.put("CHILD",r1);
        recipePermissionMap.put("GRAND_CHILD",r2);
        recipePermissionMap.put("CHILD_1",rr1);
        System.out.println(String.format("r1 -> r2: %b",util.inSameTree(r1,r2)));
        System.out.println(String.format("r2 -> r1: %b",util.inSameTree(r2,r1)));
        System.out.println(String.format("rr1 -> r1: %b",util.inSameTree(rr1,r1)));
//        System.out.println(String.format("r1,r2 line: %b | r2,r3: %b | r1,rr1: %b | rr1,rr2: %b | rr1,rr3: %b | r1,rr3: %b",util.isInSameLine(r2,r1),util.isInSameLine(r3,r2), util.isInSameLine(rr1,r1),util.isInSameLine(rr2,rr1), util.isInSameLine(rr3,rr1), util.isInSameLine(rr3,r1)));

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
            new RecipePermissionUtil().removePermissionConflicts(playerPermissions.get(target.getUniqueId()));

        }else{
            // remove
            if(!new RecipePermissionUtil().hasPermission(permission,target)) return true;
            playerPermissions.get(target.getUniqueId()).remove(permission);

        }

        return true;
    }

    private boolean isNamePlayerOnline(String name){
        List<String> onlinePlayer = new ArrayList<>();
        Bukkit.getOnlinePlayers().forEach(s->onlinePlayer.add(s.getName()));
        return onlinePlayer.contains(name);
    }
}
