package com.github.sakakiaruka.customcrafter.customcrafter.object.Permission;

import static com.github.sakakiaruka.customcrafter.customcrafter.SettingsLoad.LINE_SEPARATOR;

public class RecipePermission {

    public static final RecipePermission ROOT = new RecipePermission("NULL","ROOT");
    private String parent;
    private String name;

    public RecipePermission(String parent, String name){
        this.parent = parent;
        this.name = name;
    }

    public String getParent() {
        return parent;
    }

    public void setParent(String parent) {
        this.parent = parent;
    }

    public String getPermissionName() {
        return name;
    }

    public void setPermissionName(String name) {
        this.name = name;
    }

    public String toStr(){
        StringBuilder builder = new StringBuilder();
        builder.append(String.format("parent: %s | name: %s%s",parent,name, LINE_SEPARATOR));
        return builder.toString();

    }
}
