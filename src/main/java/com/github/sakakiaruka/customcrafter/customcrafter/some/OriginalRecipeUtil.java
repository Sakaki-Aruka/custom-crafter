package com.github.sakakiaruka.customcrafter.customcrafter.some;

import com.github.sakakiaruka.customcrafter.customcrafter.objects.OriginalRecipe;

import java.util.ArrayList;
import java.util.List;

public class OriginalRecipeUtil {
    public List<String> getNames(List<OriginalRecipe> in){
        List<String> list = new ArrayList<>();
        for(OriginalRecipe o:in){
            list.add(o.getRecipeName());
        }
        return list;
    }
}
