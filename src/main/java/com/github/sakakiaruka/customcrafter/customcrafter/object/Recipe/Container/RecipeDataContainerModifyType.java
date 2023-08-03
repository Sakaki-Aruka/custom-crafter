package com.github.sakakiaruka.customcrafter.customcrafter.object.Recipe.Container;

public enum RecipeDataContainerModifyType {
    MODIFY("MODIFY"),
    MAKE("MAKE");

    private final String modifyTypeString;

    private RecipeDataContainerModifyType(final String modifyTypeString) {
        this.modifyTypeString = modifyTypeString;
    }

    public String getModifyTypeString() {
        return this.modifyTypeString;
    }
}
