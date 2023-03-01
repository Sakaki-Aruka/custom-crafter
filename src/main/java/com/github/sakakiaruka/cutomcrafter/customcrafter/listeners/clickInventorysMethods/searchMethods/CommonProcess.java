package com.github.sakakiaruka.cutomcrafter.customcrafter.listeners.clickInventorysMethods.searchMethods;

import com.github.sakakiaruka.cutomcrafter.customcrafter.objects.*;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class CommonProcess {

    public int getSquareSize(RecipeMaterial in) {
        List<MultiKeys> coordinates = in.getMultiKeysListNoAir();
        List<Integer> xs = new ArrayList<>();
        List<Integer> ys = new ArrayList<>();
        coordinates.forEach(s -> {
            xs.add(s.getKey1());
            ys.add(s.getKey2());
        });
        Collections.sort(xs);
        Collections.sort(ys);
        int x = Math.abs(xs.get(0) - xs.get(xs.size() - 1)) + 1;
        int y = Math.abs(ys.get(0) - ys.get(ys.size() - 1)) + 1;
        return Math.max(x, y);
    }

    public boolean isSameShape(List<MultiKeys> models,List<MultiKeys> reals){
        int xGap = models.get(0).getKey1() - reals.get(0).getKey1();
        int yGap = models.get(0).getKey2() - reals.get(0).getKey2();
        int size = models.size();
        for(int i=0;i<size;i++){
            if(models.get(i).getKey1() - reals.get(i).getKey1() != xGap)return false;
            if(models.get(i).getKey2() - reals.get(i).getKey2() != yGap)return false;
        }
        return true;
    }

    public RecipeMaterial shapeUp(RecipeMaterial model,RecipeMaterial real){
        List<MultiKeys> models = model.getMultiKeysListNoAir();
        List<ItemStack> reals = real.getItemStackListNoAir();
        RecipeMaterial result = new RecipeMaterial();
        for(int i=0;i<models.size();i++){
            result.put(models.get(i),reals.get(i));
        }
        return result;
    }

    public AmorphousRecipe toAmorphous(RecipeMaterial in){
        List<ItemStack> list = in.getItemStackListNoAir();
        Map<ItemStack,Integer> map = new HashMap<>();
        for(ItemStack item:list){
            if(map.containsKey(item)){
                int amount = item.getAmount() + map.get(item);
                map.put(item,amount);
            }else{
                map.put(item,item.getAmount());
            }
        }
        int size = getSquareSize(in);
        AmorphousEnum enumType;
        if(size>3)enumType = AmorphousEnum.ANYWHERE;
        else enumType=AmorphousEnum.NEIGHBOR;

        AmorphousRecipe amorphous = new AmorphousRecipe(enumType,map);
        return amorphous;
    }

    public boolean containsMixedMaterial(RecipeMaterial in){
        return getClassSet(in).contains(MixedMaterial.class);
    }

    public boolean containsEnchantedMaterial(RecipeMaterial in){
        return getClassSet(in).contains(EnchantedMaterial.class);
    }

    public boolean containsRegexRecipeMaterial(RecipeMaterial in){
        return getClassSet(in).contains(RegexRecipeMaterial.class);
    }

    private Set<Class> getClassSet(RecipeMaterial in){
        List<ItemStack> items = in.getItemStackListNoAir();
        Set<Class> classes = new HashSet<>();
        items.forEach(s->classes.add(s.getClass()));
        return classes;
    }


}
