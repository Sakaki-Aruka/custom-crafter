package com.github.sakakiaruka.customcrafter.customcrafter.object.Permission;

public class RecipePermission {

    public static final RecipePermission ROOT = new RecipePermission(null,"ROOT");
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
}
