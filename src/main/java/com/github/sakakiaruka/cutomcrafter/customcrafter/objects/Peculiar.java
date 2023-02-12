package com.github.sakakiaruka.cutomcrafter.customcrafter.objects;

import com.github.sakakiaruka.cutomcrafter.customcrafter.some.RecipeMaterialUtil;

public class Peculiar extends OriginalRecipe{
    private PeculiarEnum enumType;
    private String requireRegex;
    private String resultRegex;
    private int matcherPlace;

    public Peculiar(RecipeMaterial recipeMaterial,String requireRegex,String resultRegex,int matcherPlace){
        super.setRm(recipeMaterial);
        super.setTotal(recipeMaterial.getTotalItems());
        this.enumType = this.judge();
        this.requireRegex = requireRegex;
        this.resultRegex = resultRegex;
        this.matcherPlace = matcherPlace;
    }

    public PeculiarEnum judge(){
        if(super.getRecipeMaterial() instanceof AmorphousRecipe)return PeculiarEnum.UNCOORDINATED;
        return PeculiarEnum.COORDINATED;
    }

    public String getPeculiarInfo(){
        String recipeMaterial = new RecipeMaterialUtil().graphicalCoordinate(super.getRecipeMaterial());
        String total = String.valueOf(super.getRecipeMaterial().getTotalItems());
        String enumType = String.valueOf(getEnumType());
        String result = String.format("rm:%s%ntotal:%s%nenum:%s%nrequire:%s%nresult:%s",recipeMaterial,total,enumType,requireRegex,resultRegex);
        return result;
    }

    public PeculiarEnum getEnumType() {
        return enumType;
    }

    public void setEnumType(PeculiarEnum enumType) {
        this.enumType = enumType;
    }

    public String getRequireRegex() {
        return requireRegex;
    }

    public void setRequireRegex(String requireRegex) {
        this.requireRegex = requireRegex;
    }

    public String getResultRegex() {
        return resultRegex;
    }

    public void setResultRegex(String resultRegex) {
        this.resultRegex = resultRegex;
    }

    public int getMatcherPlace() {
        return matcherPlace;
    }
}
