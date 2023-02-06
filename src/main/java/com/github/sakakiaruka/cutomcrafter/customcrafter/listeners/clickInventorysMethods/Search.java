package com.github.sakakiaruka.cutomcrafter.customcrafter.listeners.clickInventorysMethods;

import com.github.sakakiaruka.cutomcrafter.customcrafter.objects.MixedMaterial;
import com.github.sakakiaruka.cutomcrafter.customcrafter.objects.MultiKeys;
import com.github.sakakiaruka.cutomcrafter.customcrafter.objects.OriginalRecipe;
import com.github.sakakiaruka.cutomcrafter.customcrafter.objects.RecipeMaterial;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.*;

import static com.github.sakakiaruka.cutomcrafter.customcrafter.some.SettingsLoad.mixedCategories;
import static com.github.sakakiaruka.cutomcrafter.customcrafter.some.SettingsLoad.recipes;

public class Search {
    public List<ItemStack> search(Inventory inventory,int size){
        RecipeMaterial real = toRecipeMaterial(inventory,size);
        if(real.isEmpty())return null;
        List<ItemStack> list = new ArrayList<>();

        for(OriginalRecipe original:recipes){
            RecipeMaterial model = original.getRecipeMaterial();

            if(getTotal(model) != getTotal(real))continue;
            if(getSquareSize(model) != getSquareSize(real))continue;

            if(containsMixedMaterial(model)){
                // MixedMaterial

                if(!isSameFigure(model,real))continue;
                if(!isMaterialSetCongruence(model,real))continue;
                RecipeMaterial confirmed;
                if(model.getMapSize() != real.getMapSize()){
                    confirmed = getDownSizedRecipeMaterial(real,getSquareSize(model));
                }else{
                    confirmed = real.copy();
                }
                if(!isSameMultiKey(model.getMultiKeysList(),confirmed.getMultiKeysList()))continue;
                list.add(original.getResult());

            }else{
                // Normal Recipe
                if(!getMaterialSet(model).equals(getMaterialSet(real)))continue;
                if(!isCongruence(model,real))continue;
                list.add(original.getResult());
            }
        }

        if(list.isEmpty())return null;
        return list;
    }

    // --- common processes --- //
    private int getTotal(RecipeMaterial in){
        int total = 0;
        for(ItemStack item : in.getItemStackListNoAir()){
            total += item.getAmount();
        }
        return total;
    }

    private Set<ItemStack> getMaterialSet(RecipeMaterial in){
        Set<ItemStack> set = new HashSet<>();
        in.getItemStackListNoAir().forEach(s->set.add(s));
        return set;
    }

    private int getSquareSize(RecipeMaterial in){
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

    // --- common processes end --- //

    private RecipeMaterial getDownSizedRecipeMaterial(RecipeMaterial in,int size){
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

    private boolean containsMixedMaterial(RecipeMaterial model){
        for(Map.Entry<MultiKeys,ItemStack> entry : model.getRecipeMaterial().entrySet()){
            if(entry.getValue() instanceof MixedMaterial) return true;
        }
        return false;
    }

    private RecipeMaterial toRecipeMaterial(Inventory inventory,int size){
        RecipeMaterial recipeMaterial = new RecipeMaterial();
        for(int y=0;y<size;y++){
            for(int x=0;x<size;x++){
                MultiKeys key = new MultiKeys(x,y);
                ItemStack item;
                int slot = x+y*9;
                if(inventory.getItem(slot)==null){
                    item = new ItemStack(Material.AIR);
                }else if(inventory.getItem(slot).getType().equals(Material.AIR)){
                    item = new ItemStack(Material.AIR);
                }else{
                    item = inventory.getItem(slot);
                }

                recipeMaterial.put(key,item);
            }
        }
        return recipeMaterial;
    }

    // --- for MixedMaterial methods --- //

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
            String stringKey = ((MixedMaterial)modelList.get(number)).getMaterialCategory();
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
            if(!(in.get(i) instanceof MixedMaterial))continue;
            list.add(i);
        }
        return list;
    }

    private List<Integer> getNormalPlace(List<ItemStack> in){
        List<Integer> list = new ArrayList<>();
        for(int i=0;i<in.size();i++){
            if(in.get(i) instanceof MixedMaterial) continue;
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
