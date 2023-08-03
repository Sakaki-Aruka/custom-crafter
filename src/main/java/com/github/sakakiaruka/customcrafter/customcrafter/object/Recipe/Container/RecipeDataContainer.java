package com.github.sakakiaruka.customcrafter.customcrafter.object.Recipe.Container;

import org.bukkit.persistence.PersistentDataType;

public class RecipeDataContainer {
    private PersistentDataType type;
    private String term;
    private String action;
    private boolean end;
    private RecipeDataContainerModifyType modifyType;


    public RecipeDataContainer(PersistentDataType type, String term, String action, boolean end, RecipeDataContainerModifyType modifyType) {
        this.type = type;
        this.term = term;
        this.action = action;
        this.end = end;
        this.modifyType = modifyType;
    }

    public PersistentDataType getDataType() {
        return type;
    }

    public void setDataType(PersistentDataType type) {
        this.type = type;
    }

    public String getTerm() {
        return term;
    }

    public void setTerm(String term) {
        this.term = term;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public boolean isEnd() {
        return end;
    }

    public void setEnd(boolean end) {
        this.end = end;
    }

    public RecipeDataContainerModifyType getModifyType() {
        return modifyType;
    }

    public void setModifyType(RecipeDataContainerModifyType modifyType) {
        this.modifyType = modifyType;
    }
}
