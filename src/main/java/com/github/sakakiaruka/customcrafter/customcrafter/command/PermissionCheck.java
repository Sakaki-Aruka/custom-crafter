package com.github.sakakiaruka.customcrafter.customcrafter.command;

import com.github.sakakiaruka.customcrafter.customcrafter.object.Permission.RecipePermission;
import com.github.sakakiaruka.customcrafter.customcrafter.util.RecipePermissionUtil;

import static com.github.sakakiaruka.customcrafter.customcrafter.util.RecipePermissionUtil.recipePermissionMap;

public class PermissionCheck {
    public void main(String name){
        if(!recipePermissionMap.containsKey(name)) return;
        RecipePermission perm = recipePermissionMap.get(name);

        //debug
        System.out.println(recipePermissionMap);
        System.out.println("name: "+name);
        System.out.println(perm.toStr());

        System.out.println(new RecipePermissionUtil().getPermissionTree(perm));
    }
}
