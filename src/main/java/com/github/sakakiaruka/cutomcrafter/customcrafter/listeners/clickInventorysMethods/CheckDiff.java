package com.github.sakakiaruka.cutomcrafter.customcrafter.listeners.clickInventorysMethods;

import com.github.sakakiaruka.cutomcrafter.customcrafter.objects.MultiKeys;
import com.github.sakakiaruka.cutomcrafter.customcrafter.objects.OriginalRecipe;

import java.util.ArrayList;
import java.util.List;

public class CheckDiff {
    public boolean diff(OriginalRecipe model,OriginalRecipe input){
        int modelSize = model.getSize();
        int inputSize = input.getSize();
        int modelRecipeMaterialSize = model.getRecipeMaterial().getMapSize();
        int inputRecipeMaterialSize = input.getRecipeMaterial().getMapSize();
        if(modelSize != inputSize
        && modelRecipeMaterialSize != inputRecipeMaterialSize)
            return false; // size not match

        List<MultiKeys> modelMultiKeys = model.getRecipeMaterial().getCoordinateList();
        List<MultiKeys> inputMultiKeys = input.getRecipeMaterial().getCoordinateList();

        List<Integer> xDiffs = new ArrayList<>();
        List<Integer> yDiffs = new ArrayList<>();

        for(int i=0;i<modelMultiKeys.size();i++){
            int xDiff = Math.abs(modelMultiKeys.get(i).getKey1() - inputMultiKeys.get(i).getKey1());
            int yDiff = Math.abs(modelMultiKeys.get(i).getKey2() - inputMultiKeys.get(i).getKey2());
            xDiffs.add(xDiff);
            yDiffs.add(yDiff);
        }
        if(this.isConsistency(xDiffs) && this.isConsistency(yDiffs)){
            return true;
        }
        return false;
    }

    private boolean isConsistency(List<Integer> list){
        int model = list.get(0);
        for(int i=0;i<list.size();i++){
            if(list.get(i) != model)return false;
        }
        return true;
    }
}
