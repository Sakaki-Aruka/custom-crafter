package com.github.sakakiaruka.customcrafter.customcrafter.command;

import com.github.sakakiaruka.customcrafter.customcrafter.object.Permission.RecipePermission;
import com.github.sakakiaruka.customcrafter.customcrafter.util.RecipePermissionUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static com.github.sakakiaruka.customcrafter.customcrafter.SettingsLoad.LINE_SEPARATOR;
import static com.github.sakakiaruka.customcrafter.customcrafter.util.RecipePermissionUtil.PLAYER_PERMISSIONS;
import static com.github.sakakiaruka.customcrafter.customcrafter.util.RecipePermissionUtil.RECIPE_PERMISSION_MAP;

public class PermissionCheck {
    public void tree(String[] args, CommandSender sender) {
        displayPermissionInfo(args[1],sender);
    }

    public void show(String[] args, CommandSender sender) {
        UUID uuid = Bukkit.getPlayer(args[2]).getUniqueId();
        displayPlayerPermissions(uuid,sender);
    }

    public void add(String[] args, CommandSender sender) {
        if (!isNamePlayerOnline(args[3])) return;
        Player player = Bukkit.getPlayer(args[3]);
        UUID uuid = player.getUniqueId();
        RecipePermission permission = RECIPE_PERMISSION_MAP.get(args[5]);
        if (new RecipePermissionUtil().hasPermission(permission,player)) return;
        if (!PLAYER_PERMISSIONS.containsKey(uuid)) PLAYER_PERMISSIONS.put(uuid,new HashSet<>());
        PLAYER_PERMISSIONS.get(uuid).add(permission);
        Bukkit.getLogger().info(String.format("Permission added: %s  Permission: %s%s  Target: %s", LINE_SEPARATOR,permission.getPermissionName(), LINE_SEPARATOR,player.getName()));
        displayPlayerPermissions(uuid,sender);
    }

    public void remove(String[] args, CommandSender sender) {
        if (!isNamePlayerOnline(args[3])) return;
        Player player = Bukkit.getPlayer(args[3]);
        UUID uuid = player.getUniqueId();
        if (!PLAYER_PERMISSIONS.containsKey(uuid)) return;
        RecipePermission permission = RECIPE_PERMISSION_MAP.get(args[5]);
        Bukkit.getLogger().info(String.format("Permission removed: %s  Permission: %s%s  Target: %s", LINE_SEPARATOR,permission.getPermissionName(), LINE_SEPARATOR,player.getName()));
        PLAYER_PERMISSIONS.get(uuid).remove(permission);
        displayPlayerPermissions(uuid,sender);
    }

    private void displayPermissionInfo(String name, CommandSender sender){
        // /cc -p [permissionName]
        RecipePermission perm = RECIPE_PERMISSION_MAP.get(name);
        sender.sendMessage(new RecipePermissionUtil().getPermissionTree(perm));
    }

    private void displayPlayerPermissions(UUID uuid, CommandSender sender){
        // /cc -p -p [targetPlayerName]
        StringBuilder builder = new StringBuilder();
        builder.append("=== RecipePermissions (The player has) ==="+ LINE_SEPARATOR);
        if(!PLAYER_PERMISSIONS.containsKey(uuid)) {
            builder.append(String.format("Target player has no permissions.%s", LINE_SEPARATOR));
            Bukkit.getLogger().info(builder.toString());
            return;
        }

        Set<RecipePermission> permissions = PLAYER_PERMISSIONS.get(uuid);
        permissions.forEach(s->builder.append(String.format("Parent: %s | Name: %s %s",s.getParent(),s.getPermissionName(), LINE_SEPARATOR)));
        builder.append("=== RecipePermissions (END) ===");
        sender.sendMessage(builder.toString());
    }

    private boolean isNamePlayerOnline(String name){
        List<String> onlinePlayer = new ArrayList<>();
        Bukkit.getOnlinePlayers().forEach(s->onlinePlayer.add(s.getName()));
        return onlinePlayer.contains(name);
    }
}
