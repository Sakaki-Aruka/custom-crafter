package com.github.sakakiaruka.customcrafter.customcrafter.listeners.clickInventorysMethods;

import com.github.sakakiaruka.customcrafter.customcrafter.objects.*;
import com.github.sakakiaruka.customcrafter.customcrafter.some.RecipeMaterialUtil;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.github.sakakiaruka.customcrafter.customcrafter.some.SettingsLoad.*;


public class Search {

    private List<ItemStack> results = new ArrayList<>();
    private final int size = 6;

    public List<ItemStack> search(Inventory inventory,int size){
        RecipeMaterial real = toRecipeMaterial(inventory,size);
        if(real.isEmpty())return null;

        for(OriginalRecipe original:recipes){
            // Search here

            //Is this method called by "BatchCreate"?
            // if called by BatchCreate, enable to use MassRecipe.
            List<StackTraceElement> stacks = new ArrayList<>(Arrays.asList(new Throwable().getStackTrace()));
            Set<Boolean> bool = new HashSet<>();
            stacks.forEach(s->bool.add(s.getClassName().contains(new BatchCreate().getClass().getSimpleName())));
            if(!bool.contains(true) && original.getRecipeMaterial().getClass().equals(MassRecipe.class)){
                continue;
            }

            if(hasRegexResult(original)){
                if(getResultsRegexOn(original,inventory) == null)continue;
                results.addAll(getResultsRegexOn(original,inventory));
            }else{
                if(getResultsRegexOff(original,inventory) == null)continue;
                results.addAll(getResultsRegexOff(original,inventory));
            }

            if(original.getClass().equals(ReturnableRecipe.class)){
                returnableRecipeProcess(inventory,(ReturnableRecipe)original);
            }
        }

        if(results.isEmpty())return null;
        return results;
    }


    private void returnableRecipeProcess(Inventory crafter,ReturnableRecipe returnable){
        // returnable recipe work (modify inventory items)
        Map<Material,Material> returnItems = returnable.getRelatedReturnItems();
        Set<Material> targetItemsSet = returnItems.keySet();
        Map<MultiKeys,ItemStack> resultItemsPlace = new LinkedHashMap<>();
        for(int i=0;i<crafter.getSize();i++){
            if(crafter.getItem(i) == null)continue;
            if(!targetItemsSet.contains(crafter.getItem(i).getType()))continue;

            int amount = crafter.getItem(i).getAmount();
            int y = i/9;
            int x = i%9;
            MultiKeys key = new MultiKeys(x,y);
            ItemStack returns = new ItemStack(returnItems.get(crafter.getItem(i).getType()),amount);
            resultItemsPlace.put(key,returns);
        }

        for(int y=0;y<crafter.getSize();y++){
            for(int x=0;x< crafter.getSize();x++){
                MultiKeys key = new MultiKeys(x,y);
                if(!resultItemsPlace.keySet().contains(key))continue;
                int slot = x + y*9;
                crafter.setItem(slot,resultItemsPlace.get(key));
            }
        }
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
        for(int i=1;i<size-1;i++){
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
        if(!model.getCandidate().contains(typeOfReal))return "";

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

        }

        //debug
        System.out.println("cannot search amorphous recipe");
        return null;

        //amorphous Recipe process write here.
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

            for(int i=0;i<models.size()-1;i++){
                if(models.get(i).getClass().equals(ItemStack.class)){
                    //ItemStack
                    if(!models.get(i).isSimilar(reals.get(i)))return null;
                }else if(models.get(i).getClass().equals(MixedMaterial.class)){
                    //MixedMaterial

                    //debug
                    System.out.println(String.format("model:%s | real:%s",models.get(i),reals.get(i)));

                    if(!isSameMixedMaterial(models.get(i),reals.get(i)))return null;
                }else if(models.get(i).getClass().equals(EnchantedMaterial.class)){
                    //EnchantedMaterial

                    //debug
                    System.out.println(String.format("isSameEnchantedMaterial : %s",isSameEnchantedMaterial(models.get(i),reals.get(i))));
                    EnchantedMaterial enchantedMaterial = (EnchantedMaterial) models.get(i);
                    Map<Enchantment,Integer> map = reals.get(i).getEnchantments();
                    System.out.println(String.format("getEnchantCongruence : %s",getEnchantCongruence(enchantedMaterial.getRelation(),map)));

                    if(!isSameEnchantedMaterial(models.get(i),reals.get(i)))return null;
                }

            }
            //finish recipeMaterial-check
            return Arrays.asList(original.getResult());

        }
//        //debug
//        System.out.println("cannot search amorphous recipe");
//        return null;

        else {
            // amorphous recipe
            AmorphousRecipe amorphousModel = (AmorphousRecipe) model;
            AmorphousRecipe amorphousReal = toAmorphousRecipe(real);

            //debug
            System.out.println(new RecipeMaterialUtil().graphicalCoordinate(amorphousModel));
            System.out.println(new RecipeMaterialUtil().graphicalCoordinate(amorphousReal));

            if(amorphousModel.getTotalItems() != amorphousReal.getTotalItems())return null;

            int types = getVirtualMapped(amorphousReal).keySet().size(); // amount of types
            Map<Material,Integer> virtual = getVirtualMapped(amorphousModel);
            int before = virtual.keySet().size();
            int ideal = before - types;
            for(Map.Entry<ItemStack,Integer> entry:amorphousReal.getMaterials().entrySet()){
                Material material = entry.getKey().getType();
                int amount = entry.getValue();
                virtual.put(material,virtual.get(material) - amount);
            }

            if(!getVirtualMappedCongruence(virtual,ideal))return null;

            //check EnchantedMaterials congruence
            List<EnchantedMaterial> enchantedMaterialList = getEnchantedList(amorphousModel);
            List<ItemStack> enchantedItemStackList = getEnchantedRealList(amorphousReal);
            if(enchantedMaterialList.size() != enchantedItemStackList.size())return null; // doesn't match enchanted material
            for(int i=0;i<enchantedMaterialList.size()-1;i++){
                Map<IntegratedEnchant,EnchantedMaterialEnum> modelMap = enchantedMaterialList.get(i).getRelation();
                Map<Enchantment,Integer> realMap = enchantedItemStackList.get(i).getEnchantments();
                if(!getEnchantCongruence(modelMap,realMap))return null;
            }

            return Arrays.asList(original.getResult());
        }


    }

    private boolean getVirtualMappedCongruence(Map<Material,Integer> in,int ideal){
        List<Material> buffer = new ArrayList<>();
        for(Map.Entry<Material,Integer> entry:in.entrySet()){
            if(entry.getValue() < 0)return false;
            if(entry.getValue()==0)buffer.add(entry.getKey());
        }
        buffer.forEach(s->in.remove(s));

        //debug
        System.out.println("removed virtual mapped : "+in);

        return in.entrySet().size() == ideal;
    }

    private Map<Material,Integer> getVirtualMapped(AmorphousRecipe model){
        Map<ItemStack,Integer> source = model.getMaterials();
        Map<Material,Integer> map = new HashMap<>();
        for(Map.Entry<ItemStack,Integer> entry: source.entrySet()){
            int amount = entry.getValue();
            if(entry.getKey().getClass().equals(MixedMaterial.class)){
                //MixedMaterial
                MixedMaterial mixed = (MixedMaterial) entry.getKey();
                mixed.getCandidate().forEach(s->map.put(s,amount));
            }else if(entry.getKey().equals(RegexRecipeMaterial.class)){
                //RegexRecipeMaterial
                RegexRecipeMaterial regex = (RegexRecipeMaterial) entry.getKey();
                regex.getCandidate().forEach(s->map.put(s,amount));
            }else{
                map.put(entry.getKey().getType(),amount);
            }
        }

        //debug
        System.out.println(String.format("virtual mapped : %s",map));

        return map;
    }

    private boolean getEnchantCongruence(Map<IntegratedEnchant,EnchantedMaterialEnum> model,Map<Enchantment,Integer> real){
        List<Enchantment> onlyEnchants = new ArrayList<>();
        model.entrySet().forEach(s->{
            if(s.getValue().equals(EnchantedMaterialEnum.OnlyEnchant)){
                onlyEnchants.add(s.getKey().getEnchant());
            }
        });
        if(!real.keySet().containsAll(onlyEnchants))return false;
        onlyEnchants.forEach(s->real.remove(s));

        Map<Enchantment,Integer> strictMode = new HashMap<>();
        model.entrySet().forEach(s->{
            if(s.getValue().equals(EnchantedMaterialEnum.Strict)){
                strictMode.put(s.getKey().getEnchant(),s.getKey().getLevel());
            }
        });

        for(Map.Entry<Enchantment,Integer> entry:strictMode.entrySet()){
            if(!real.containsKey(entry.getKey()))return false; // not found enchant
            if(real.get(entry.getKey()) != entry.getValue())return false; // not equals the enchantment level
        }
        return true;

    }


    private List<EnchantedMaterial> getEnchantedList(AmorphousRecipe in){
        List<EnchantedMaterial> list = new ArrayList<>();
        for(Map.Entry<ItemStack,Integer> entry:in.getMaterials().entrySet()){
            if(entry.getKey().getClass().equals(EnchantedMaterial.class))list.add((EnchantedMaterial) entry.getKey());
        }
        return list;
    }

    private List<ItemStack> getEnchantedRealList(AmorphousRecipe in){
        List<ItemStack> list = new ArrayList<>();
        for(Map.Entry<ItemStack,Integer> entry:in.getMaterials().entrySet()){
            if(entry.getKey().getEnchantments().isEmpty())continue;
            list.add(entry.getKey());
        }
        return list;
    }

    private String getMatchedString(AmorphousRecipe model,AmorphousRecipe real){
        List<Material> reals = new ArrayList<>();
        List<Material> models = new ArrayList<>();
        List<String> patterns = new ArrayList<>();
        if(getRegexRecipeMaterialList(model).isEmpty())return null;
        real.getItemStackListNoAir().forEach(s->reals.add(s.getType()));
        model.getItemStackListNoAir().forEach(s->models.add(s.getType()));
        getRegexRecipeMaterialList(model).forEach(s->patterns.add(s.getPattern()));

        if(!isAllSame(patterns))return null;
        String pattern = patterns.get(0);
        Pattern p = Pattern.compile(pattern);
        int place = getRegexRecipeMaterialList(model).get(0).getMatchPoint();
        int howMatched = 0;
        String matched = "";
        for(Material material:reals){
            String name = material.name();
            Matcher m = p.matcher(name);
            String temp = "";
            while(m.find()){
                temp = m.group(place);
            }
            if(!temp.equals(""))howMatched++;
        }
        if(howMatched != 1)return null;

        return matched;
    }

    private List<RegexRecipeMaterial> getRegexRecipeMaterialList(AmorphousRecipe in){
        List<RegexRecipeMaterial> list = new ArrayList<>();
        for(Map.Entry<ItemStack,Integer> entry:in.getMaterials().entrySet()){
            if(entry.getKey().getClass().equals(RegexRecipeMaterial.class))list.add((RegexRecipeMaterial)entry.getKey());
        }
        return list;
    }

    private boolean isAllSame(List<String> list){
        if(list.isEmpty())return false;
        String s = list.get(0);
        for(String str:list){
            if(!str.equals(s))return false;
        }
        return true;
    }


    private List<Material> containsSet(List<Material> candidate,Set<Material> real){
        List<Material> result = new ArrayList<>();
        candidate.forEach(s->{
            if(real.contains(s))result.add(s);
        });
        return result;
    }
}
