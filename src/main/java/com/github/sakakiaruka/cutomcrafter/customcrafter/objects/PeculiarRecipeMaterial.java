package com.github.sakakiaruka.cutomcrafter.customcrafter.objects;

import org.bukkit.inventory.ItemStack;

import java.util.List;

public class PeculiarRecipeMaterial extends OriginalRecipe{
    private boolean regexUse;
    private boolean returnableUse;
    private boolean amorphousUse;
    private String requireRegexPattern;
    private String resultRegexPattern;
    private List<ItemStack> returnItems;
    private AmorphousRecipe amorphous;

    public PeculiarRecipeMaterial(boolean regexUse, String requireRegexPattern, String resultRegexPattern, boolean returnableUse, List<ItemStack> returnItems,boolean amorphousUse,AmorphousRecipe amorphous){
        super();
        this.regexUse = regexUse;
        this.returnableUse = returnableUse;
        this.amorphousUse = amorphousUse;
        this.requireRegexPattern = requireRegexPattern;
        this.resultRegexPattern = resultRegexPattern;
        this.returnItems = returnItems;
        this.amorphous = amorphous;
    }
}
