package com.github.sakakiaruka.cutomcrafter.customcrafter.listeners.clickInventorysMethods.searchMethods;

import com.github.sakakiaruka.cutomcrafter.customcrafter.objects.MultiKeys;
import com.github.sakakiaruka.cutomcrafter.customcrafter.objects.RecipeMaterial;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

import static com.github.sakakiaruka.cutomcrafter.customcrafter.some.SettingsLoad.mixedCategories;

public class MixedMaterialMethods {
    public boolean main(RecipeMaterial model,RecipeMaterial real){
        Normal normal = new Normal();
        if(normal.getTotal(model) != normal.getTotal(real))return false;
        if(normal.getSquareSize(model) != normal.getSquareSize(real))return false;
        if(!isSameFigure(model,real))return false;
        if(!isMaterialSetCongruence(model,real))return false;
        RecipeMaterial confirmed;
        if(model.getMapSize() != real.getMapSize()){
            confirmed = normal.getDownSizedRecipeMaterial(real, normal.getSquareSize(model));
        }else{
            confirmed = real.copy();
        }
        if(!isSameMultiKey(model.getMultiKeysList(),confirmed.getMultiKeysList()))return false;
        return true;
    }

    private boolean isSameFigure(RecipeMaterial model,RecipeMaterial real){
        List<MultiKeys> models = removeAir(model, model.getMultiKeysList());
        List<MultiKeys> reals = removeAir(real,real.getMultiKeysList());

        if(models.size() != reals.size())return false;

        int x_gap = Math.abs(models.get(0).getKey1() - reals.get(0).getKey1());
        int y_gap = Math.abs(models.get(0).getKey2() - reals.get(0).getKey2());
        for(int i=0;i<models.size();i++){
            int horizontalGap = Math.abs(models.get(i).getKey1() - reals.get(i).getKey1());
            int verticalGap = Math.abs(models.get(i).getKey2() - reals.get(i).getKey2());
            if(horizontalGap != x_gap)return false;
            if(verticalGap != y_gap)return false;
        }
        return true;
    }

    private List<MultiKeys> removeAir(RecipeMaterial recipeMaterial,List<MultiKeys> keys){
        List<MultiKeys> result = new ArrayList<>();
        for(MultiKeys key:keys) {
            if (recipeMaterial.getItemStack(key) == null)continue;
            if(recipeMaterial.getItemStack(key).getType().equals(Material.AIR))continue;
            result.add(key);
        }
        return result;
    }

    private boolean isMaterialSetCongruence(RecipeMaterial model,RecipeMaterial real){
        List<ItemStack> modelList = model.getItemStackListNoAir();
        List<ItemStack> realList = real.getItemStackListNoAir();
        List<Integer> mixedPlace = getMixedPlace(modelList);
        List<Integer> normalPlace = getNormalPlace(modelList);

        for(int number:normalPlace){
            ItemStack modelItem = modelList.get(number);
            ItemStack realItem = realList.get(number);
            if(!modelItem.equals(realItem))return false;
        }

        int match = 0;
        for(int number:mixedPlace){
            String stringKey = ((com.github.sakakiaruka.cutomcrafter.customcrafter.objects.MixedMaterial)modelList.get(number)).getMaterialCategory();
            List<Material> materials = mixedCategories.get(stringKey);
            Material realMaterial = realList.get(number).getType();
            for(Material material:materials){
                if(material.equals(realMaterial)){
                    match++;
                    break;
                }
            }
        }
        return mixedPlace.size()==match;
    }

    private List<Integer> getMixedPlace(List<ItemStack> in){
        List<Integer> list = new ArrayList<>();
        for(int i=0;i<in.size();i++){
            if(!(in.get(i) instanceof com.github.sakakiaruka.cutomcrafter.customcrafter.objects.MixedMaterial))continue;
            list.add(i);
        }
        return list;
    }

    private List<Integer> getNormalPlace(List<ItemStack> in){
        List<Integer> list = new ArrayList<>();
        for(int i=0;i<in.size();i++){
            if(in.get(i) instanceof com.github.sakakiaruka.cutomcrafter.customcrafter.objects.MixedMaterial) continue;
            list.add(i);
        }
        return list;
    }

    private boolean isSameMultiKey(List<MultiKeys> model,List<MultiKeys> confirmed){
        if(model.size() != confirmed.size())return false;
        for(int i=0;i<model.size();i++){
            MultiKeys modelKey = model.get(i);
            MultiKeys confirmedKey = confirmed.get(i);
            if(!modelKey.same(confirmedKey))return false;
        }
        return true;
    }
}
