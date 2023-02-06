package com.github.sakakiaruka.cutomcrafter.customcrafter.listeners.clickInventorysMethods.searchMethods;

import com.github.sakakiaruka.cutomcrafter.customcrafter.objects.MultiKeys;
import com.github.sakakiaruka.cutomcrafter.customcrafter.objects.RecipeMaterial;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class Normal {
    public boolean main(RecipeMaterial model,RecipeMaterial real){
        if(getTotal(model) != getTotal(real))return false;
        if(getSquareSize(model) != getSquareSize(real))return false;
        if(!getMaterialSet(model).equals(getMaterialSet(real)))return false;
        if(!isCongruence(model, real))return false;
        return true;
    }

    protected int getTotal(RecipeMaterial in){
        int total = 0;
        for(ItemStack item : in.getItemStackListNoAir()){
            total += item.getAmount();
        }
        return total;
    }

    protected int getSquareSize(RecipeMaterial in){
        List<Integer> x = new ArrayList<>();
        List<Integer> y = new ArrayList<>();
        for(Map.Entry<MultiKeys,ItemStack> entry:in.getRecipeMaterial().entrySet()){
            if(entry.getValue()==null)continue;
            if(entry.getValue().getType().equals(Material.AIR))continue;
            x.add(entry.getKey().getKey1());
            y.add(entry.getKey().getKey2());
        }

        Collections.sort(x);
        Collections.sort(y);
        int width = Math.abs(x.get(0) - x.get(x.size()-1));
        int length = Math.abs(y.get(0) - y.get(y.size()-1));
        return Math.max(width,length);
    }

    private Set<ItemStack> getMaterialSet(RecipeMaterial in){
        Set<ItemStack> set = new HashSet<>();
        in.getItemStackListNoAir().forEach(s->set.add(s));
        return set;
    }

    private boolean isCongruence(RecipeMaterial model,RecipeMaterial real){
        RecipeMaterial confirmed;
        if(model.getMapSize() != real.getMapSize()){
            confirmed = getDownSizedRecipeMaterial(real,getSquareSize(model));
        }else{
            confirmed = real.copy();
        }

        List<ItemStack> models = model.getItemStackList();
        List<ItemStack> reals = confirmed.getItemStackList();

        if(models.size() != reals.size())return false;

        for(int i=0;i<models.size();i++){
            if(!models.get(i).equals(reals.get(i)))return false;
        }
        return true;
    }

    protected RecipeMaterial getDownSizedRecipeMaterial(RecipeMaterial in,int size){
        RecipeMaterial recipeMaterial = new RecipeMaterial();
        List<MultiKeys> keys = in.getMultiKeysList();

        MultiKeys top = null;
        for(MultiKeys m : keys){
            if(in.getItemStack(m).getType().equals(Material.AIR))continue;
            top = m;
            break;
        }

        int x_top = top.getKey1();
        int y_top = top.getKey2();

        for(int y=0;y<=size;y++){
            for(int x=0;x<=size;x++){
                MultiKeys pullKey = new MultiKeys((x+x_top),(y+y_top));

                ItemStack item = in.getItemStack(pullKey);
                MultiKeys key = new MultiKeys(x,y);
                recipeMaterial.put(key,item);
            }
        }

        return recipeMaterial;
    }
}
