package com.github.sakakiaruka.cutomcrafter.customcrafter.listeners.clickInventorysMethods;

import com.github.sakakiaruka.cutomcrafter.customcrafter.objects.*;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.github.sakakiaruka.cutomcrafter.customcrafter.some.SettingsLoad.*;


public class Search {

    private List<ItemStack> results = new ArrayList<>();
    private final int size = 6;

    public List<ItemStack> search(Inventory inventory,int size){
        RecipeMaterial real = toRecipeMaterial(inventory,size);
        if(real.isEmpty())return null;


        Set<OriginalRecipe> originals = new HashSet<>();
        Material top_material = real.getLargestMaterial();
        int top_amount = real.getLargestAmount();

        if(recipesMaterial.get(top_material) == null
        && recipesAmount.get(top_amount) == null)return null;

        if(recipesMaterial.get(top_material) != null)recipesMaterial.get(top_material).forEach(s->originals.add(s)); // material
        if(recipesAmount.get(top_amount) != null)recipesAmount.get(top_amount).forEach(s->originals.add(s)); // amount
        if(originals.isEmpty())return null;

        for(OriginalRecipe original:originals){
            // Search here
            if(hasRegexResult(original)){
                if(getResultsRegexOn(original,inventory) == null)continue;
                results.addAll(getResultsRegexOn(original,inventory));
            }else{
                if(getResultsRegexOff(original,inventory) == null)continue;
                results.addAll(getResultsRegexOff(original,inventory));
            }
        }

        if(results.isEmpty())return null;
        return results;
    }

    private boolean hasRegexResult(OriginalRecipe in){
        return in.getResult().getClass().equals(RegexRecipeMaterial.class);
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


    private int getSquareSize(RecipeMaterial in){
        List<MultiKeys> keys = in.getMultiKeysListNoAir();
        List<Integer> x = new ArrayList<>();
        List<Integer> y = new ArrayList<>();
        keys.forEach(s -> {
            x.add(s.getKey1());
            y.add(s.getKey2());
        });
        Collections.sort(x);
        Collections.sort(y);
        int width = Math.abs(x.get(0) - x.get(x.size()-1)) + 1;
        int height = Math.abs(y.get(0) - y.get(y.size()-1) + 1);
        return Math.max(width,height);
    }

    private boolean isSameShape(List<MultiKeys> models,List<MultiKeys> reals){
        int xGap = models.get(0).getKey1() - reals.get(0).getKey1();
        int yGap = models.get(0).getKey2() - reals.get(0).getKey2();
        int size = models.size();
        for(int i=1;i<size;i++){
            if(models.get(i).getKey1() - reals.get(i).getKey1() != xGap)return false;
            if(models.get(i).getKey2() - reals.get(i).getKey2() != yGap)return false;
        }
        return true;
    }

    private boolean hasRegexMaterial(List<ItemStack> model){
        for(ItemStack item:model){
            if(item.getClass().equals(RegexRecipeMaterial.class))return true;
        }
        return false;
    }

    private boolean isSameEnchantedMaterial(ItemStack in,ItemStack real){
        EnchantedMaterial model = (EnchantedMaterial) in;
        Map<Enchantment,Integer> enchants = real.getEnchantments();
        for(Map.Entry<IntegratedEnchant,EnchantedMaterialEnum> entry:model.getRelation().entrySet()){
            if(entry.getValue().equals(EnchantedMaterialEnum.NotStrict))continue;
            if(!enchants.containsKey(entry.getKey().getEnchant()))return false;
            if(entry.getValue().equals(EnchantedMaterialEnum.OnlyEnchant))continue;
            if(enchants.get(entry.getKey().getEnchant()) != entry.getKey().getLevel())return false;
        }
        return true;
    }

    private boolean isSameMixedMaterial(ItemStack in,ItemStack real){
        MixedMaterial model = (MixedMaterial) in;
        Material typeOfReal = real.getType();
        return mixedCategories.get(model.getMaterialCategory()).contains(typeOfReal);
    }

    private String isSameRegexRecipeMaterial(ItemStack in,ItemStack real){
        // if return "", a RegexRecipeMaterial and an ItemStack are not same.
        RegexRecipeMaterial model = (RegexRecipeMaterial) in;
        Material typeOfReal = real.getType();
        if(!model.getMatched().contains(typeOfReal))return "";

        Pattern pattern = Pattern.compile(model.getPattern());
        Matcher matcher = pattern.matcher(typeOfReal.name());
        String matched = "";
        while(matcher.find()){
            matched = matcher.group(model.getMatchPoint());
        }
        return matched;
    }


    private AmorphousRecipe toAmorphousRecipe(RecipeMaterial in){
        Map<ItemStack,Integer> map = new LinkedHashMap<>();
        for(ItemStack item:in.getItemStackListNoAir()){
            if(map.containsKey(item)){
                int amount = map.get(item) + item.getAmount();
                map.put(item,amount);
            }else{
                map.put(item,item.getAmount());
            }
        }

        int size = getSquareSize(in);
        AmorphousEnum enumType;
        if(size <= 3){
            enumType = AmorphousEnum.NEIGHBOR;
        }else{
            enumType = AmorphousEnum.ANYWHERE;
        }

        AmorphousRecipe result = new AmorphousRecipe(enumType,map);
        return result;
    }


    private List<ItemStack> getResultsRegexOn(OriginalRecipe original,Inventory inventory){
        RecipeMaterial model = original.getRecipeMaterial();
        RecipeMaterial real = toRecipeMaterial(inventory,size);

        if(original.getRecipeMaterial().getClass().equals(RecipeMaterial.class)){
            //natural RecipeMaterial
            if(model.getTotalItems() != real.getTotalItems())return null;
            if(getSquareSize(model) != getSquareSize(real))return null;
            if(!isSameShape(model.getMultiKeysListNoAir(),real.getMultiKeysListNoAir()))return null;

            List<ItemStack> models = model.getItemStackListNoAir();
            List<ItemStack> reals = real.getItemStackListNoAir();
            if(!hasRegexMaterial(models))return null;

            String matched = "";
            for(int i=0;i<models.size();i++){
                ItemStack m = models.get(i);
                ItemStack r = reals.get(i);
                if(m.getClass().equals(EnchantedMaterial.class)){
                    if(!isSameEnchantedMaterial(m,r))return null;
                }else if(m.getClass().equals(MixedMaterial.class)){
                    if(!isSameMixedMaterial(m,r))return null;
                }else if(m.getClass().equals(RegexRecipeMaterial.class)){
                    if(((matched = isSameRegexRecipeMaterial(m,r))) == "")return null;
                }else if(m.getClass().equals(ItemStack.class)){
                    if(!m.isSimilar(r))return null;
                }
            }

            String resultMaterialName = ((RegexRecipeMaterial)original.getResult()).getPattern();
            resultMaterialName.replace("{R}",matched);
            Material resultMaterial = Material.valueOf(resultMaterialName.toUpperCase());
            ItemStack result = ((RegexRecipeMaterial)original.getResult()).getProvisional();
            result.setType(resultMaterial); // change result item's Material Type.
            return Arrays.asList(result);

        }else{
            //AmorphousRecipe
        }


        //debug
        return null;
    }

    private List<ItemStack> getResultsRegexOff(OriginalRecipe original,Inventory inventory){
        RecipeMaterial model = original.getRecipeMaterial();
        RecipeMaterial real = toRecipeMaterial(inventory,size);

        if(original.getRecipeMaterial().getClass().equals(RecipeMaterial.class)){
            // natural RecipeMaterial
            if(model.getTotalItems() != real.getTotalItems())return null;
            if(getSquareSize(model) != getSquareSize(real))return null;
            if(!isSameShape(model.getMultiKeysListNoAir(),real.getMultiKeysListNoAir()))return null;

            List<ItemStack> models = model.getItemStackListNoAir();
            List<ItemStack> reals = real.getItemStackListNoAir();

            for(int i=0;i<models.size();i++){
                if(!models.get(i).isSimilar(reals.get(i)))return null;
            }
            //finish recipeMaterial-check
            return Arrays.asList(original.getResult());

        }else {
            // amorphous recipe
            AmorphousRecipe amorphousModel = (AmorphousRecipe) model;
            AmorphousRecipe amorphousReal = toAmorphousRecipe(real);

            //create relation
            Map<Material,Integer> relationReal = new HashMap<>();
            amorphousReal.getMaterials().entrySet().forEach(s->{
                if(relationReal.keySet().contains(s.getKey().getType())){
                    int amount = relationReal.get(s.getKey().getType()) + s.getKey().getAmount();
                    relationReal.put(s.getKey().getType(),amount);
                }else{
                    relationReal.put(s.getKey().getType(),s.getKey().getAmount());
                }
            });

            for(Map.Entry<ItemStack,Integer> entry:amorphousReal.getMaterials().entrySet()){
                //remove
                if(entry.getKey().getClass().equals(MixedMaterial.class)){
                    List<Material> candidate = ((MixedMaterial)entry.getKey()).getCandidate();
                    if(!containsSet(candidate,relationReal.keySet()))return null;

                }
            }


        }
        //debug
        return null;
    }

    private boolean containsSet(List<Material> candidate,Set<Material> real){
        for(Material m:real){
            if(candidate.contains(m))return true;
        }
        return false;
    }
}
