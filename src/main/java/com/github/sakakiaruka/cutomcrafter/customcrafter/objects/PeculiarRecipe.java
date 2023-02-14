package com.github.sakakiaruka.cutomcrafter.customcrafter.objects;


import java.util.UUID;

public class PeculiarRecipe extends OriginalRecipe{
    //private RecipeMaterial recipeMaterial;
    private RegexMaterial resultRegexMaterial;

    public PeculiarRecipe(RecipeMaterial recipeMaterial,RegexMaterial resultRegexMaterial){
        super.setRm(recipeMaterial);
        super.setResult(null);
        super.setRecipeName(String.valueOf(UUID.randomUUID()));
        super.setSize(-1);
        super.setAmountRelation(null);
        super.setTotal(recipeMaterial.getTotalItems());
        this.resultRegexMaterial = resultRegexMaterial;
    }

    public RegexMaterial getResultRegexMaterial() {
        return resultRegexMaterial;
    }

    public void setResultRegexMaterial(RegexMaterial resultRegexMaterial) {
        this.resultRegexMaterial = resultRegexMaterial;
    }
}
