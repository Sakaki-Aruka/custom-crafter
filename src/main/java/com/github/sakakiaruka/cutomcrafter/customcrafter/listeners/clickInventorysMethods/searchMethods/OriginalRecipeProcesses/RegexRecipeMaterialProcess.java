package com.github.sakakiaruka.cutomcrafter.customcrafter.listeners.clickInventorysMethods.searchMethods.OriginalRecipeProcesses;

import com.github.sakakiaruka.cutomcrafter.customcrafter.listeners.clickInventorysMethods.searchMethods.CommonProcess;
import com.github.sakakiaruka.cutomcrafter.customcrafter.objects.*;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexRecipeMaterialProcess {
    public List<ItemStack> searchRegexRecipe(OriginalRecipe originalModel, RecipeMaterial real){
        RecipeMaterial model = originalModel.getRecipeMaterial();

        CommonProcess shared = new CommonProcess();
        EnchantedMaterialProcess eProcess = new EnchantedMaterialProcess();
        MixedMaterialProcess mProcess = new MixedMaterialProcess();

        if(model.getClass().equals(RecipeMaterial.class)){
            //RecipeMaterial
            Map<Integer,String> matched = null;

            if(model.getTotalItems() != real.getTotalItems())return null;
            if(shared.getSquareSize(model) != shared.getSquareSize(real))return null;
            if(!shared.isSameShape(model.getMultiKeysListNoAir(),real.getMultiKeysListNoAir()))return null;

            real = shared.shapeUp(model,real);
            int size = shared.getSquareSize(model);
            for(int y=0;y<size;y++){
                for(int x=0;x<size;x++){
                    MultiKeys tempKey = new MultiKeys(x,y);
                    if(model.getItemStack(tempKey).getClass().equals(ItemStack.class)){
                        // (Normal) ItemStack
                        if(!model.getItemStack(tempKey).equals(real.getItemStack(tempKey)))return null;

                    }else if(model.getItemStack(tempKey).getClass().equals(EnchantedMaterial.class)){
                        // EnchantedMaterial
                        if(!eProcess.isSameItem(model.getItemStack(tempKey),real.getItemStack(tempKey)))return null;

                    }else if(model.getItemStack(tempKey).getClass().equals(MixedMaterial.class)){
                        // MixedMaterial
                        if(!mProcess.isSameItem(model.getItemStack(tempKey),real.getItemStack(tempKey)))return null;

                    }else if(model.getItemStack(tempKey).getClass().equals(RegexRecipeMaterial.class)){
                        // RegexRecipeMaterial
                        matched = getMatched((RegexRecipeMaterial) model.getItemStack(tempKey),real.getItemStack(tempKey));
                        if(matched.isEmpty())return null;

                    }

                }
            }

            String resultRegex = ((RegexRecipeMaterial) originalModel.getResult()).getRegex();
            Pattern pattern = Pattern.compile("([a-zA-Z_]{0,1000})(\\{[0-9]{1,2}\\})(.{0,1000})");
            Matcher matcher = pattern.matcher(resultRegex);
            int place = 0;
            while(matcher.find()){
                place = Integer.valueOf(matcher.group(1));
            }
            String old = String.format("{%d}",place);
            resultRegex.replace(old,matched.get(place));

            return Arrays.asList(new ItemStack(Material.valueOf(resultRegex)));

        }
        return null;
//        else{
//            //AmorphousRecipe
//            real = shared.toAmorphous(real); // recipeMaterial -> amorphousRecipe
//            List<ItemStack> normalItemSackHas = getNormalItemStackList((AmorphousRecipe) model);
//            List<ItemStack> peculiarItemStackHas = getPeculiarItemStackList((AmorphousRecipe) model);
//            List<ItemStack> realList = real.getItemStackListNoAir();
//            if(model.getTotalItems() != real.getTotalItems())return null;
//
//            for(ItemStack item : realList){
//                if(normalItemSackHas.contains(item)){
//                    // if model contains the real's material.
//                    realList.remove(item);
//                }
//            }
//
//            for(ItemStack item : realList){
//                //
//            }
//        }
    }


//    private boolean hasThisItem(ItemStack real,List<ItemStack> peculiarList){
//        for(ItemStack item : peculiarList){
//            if(item.getClass().equals(MixedMaterial.class)){
//                //MixedMaterial
//
//
//            }else if(item.getClass().equals(RegexRecipeMaterial.class)){
//                //RegexRecipeMaterial
//            }
//        }
//
//    }

    private Map<Integer,String> getMatched(RegexRecipeMaterial model,ItemStack real){
        Pattern pattern = Pattern.compile(model.getRegex());
        Matcher matcher = pattern.matcher(real.getType().name());
        Map<Integer,String> matched = new LinkedHashMap<>();
        while(matcher.find()){
            for(int i=0;i<matcher.groupCount();i++){
                matched.put(i,matcher.group(i));
            }
        }
        return matched;
    }

    private List<ItemStack> getNormalItemStackList(AmorphousRecipe model){
        List<ItemStack> list = new ArrayList<>();
        model.getMaterials().keySet().stream()
                .filter(s->s.getClass().equals(ItemStack.class) || s.getClass().equals(EnchantedMaterial.class))
                .forEach(s->list.add(s));
        return list;
    }

    private List<ItemStack> getPeculiarItemStackList(AmorphousRecipe model){
        List<ItemStack> list = new ArrayList<>();
        model.getMaterials().keySet().stream()
                .filter(s->!s.getClass().equals(ItemStack.class) && !s.getClass().equals(EnchantedMaterial.class))
                .forEach(s->list.add(s));
        return list;
    }


}
