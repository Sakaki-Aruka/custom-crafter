package com.github.sakakiaruka.cutomcrafter.customcrafter.objects;

public class RegexMaterial extends MixedMaterial{
    private RegexMaterialEnum enumType;
    private String regex;
    private int matchPoint;

    public RegexMaterial(RegexMaterialEnum enumType,String regex,int matchPoint){
        super();
        this.enumType = enumType;
        this.regex = regex;
        this.matchPoint = matchPoint;
    }

    public RegexMaterialEnum getEnumType() {
        return enumType;
    }

    public void setEnumType(RegexMaterialEnum enumType) {
        this.enumType = enumType;
    }

    public String getRegex() {
        return regex;
    }

    public void setRegex(String regex) {
        this.regex = regex;
    }

    public MixedMaterial getMixedMaterial(){
        MixedMaterial mm = new MixedMaterial(super.getMaterialCategory(),super.getType(),super.getAmount());
        return mm;
    }

    public int getMatchPoint() {
        return matchPoint;
    }

    public void setMatchPoint(int matchPoint) {
        this.matchPoint = matchPoint;
    }
}
