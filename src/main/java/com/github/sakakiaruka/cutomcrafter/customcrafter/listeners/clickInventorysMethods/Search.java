package com.github.sakakiaruka.cutomcrafter.customcrafter.listeners.clickInventorysMethods;

import com.github.sakakiaruka.cutomcrafter.customcrafter.objects.MultiKeys;
import com.github.sakakiaruka.cutomcrafter.customcrafter.objects.MultiOriginalRecipe;
import com.github.sakakiaruka.cutomcrafter.customcrafter.objects.OriginalRecipe;
import com.github.sakakiaruka.cutomcrafter.customcrafter.objects.RecipeMaterial;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.*;

import static com.github.sakakiaruka.cutomcrafter.customcrafter.some.SettingsLoad.recipes;

public class Search {
    /**
    public MultiOriginalRecipe search(MultiOriginalRecipe multiOriginal, Player player){
        try{
            multiOriginal.getClass();
        }catch (NullPointerException e){
            return null;
        }
        OriginalRecipe originalRecipe = multiOriginal.getOriginalRecipe();
        RecipeMaterial model = originalRecipe.getRecipeMaterial();
        int ratio = multiOriginal.getAmount();
        // ---conditions---
        int size = originalRecipe.getSize();
        int total = originalRecipe.getTotal();
        List<Material> rawMaterials = originalRecipe.getRawMaterials();
        // --conditions---
        List<OriginalRecipe> matched = new ArrayList<>();

        for(OriginalRecipe o:recipes){
            if(o.getSize()!=size)continue;
            if(o.getTotal()!=total)continue;
            if(!o.getRawMaterials().equals(rawMaterials))continue;
            matched.add(o);
        }

        List<OriginalRecipe> result = new ArrayList<>();
        for(OriginalRecipe o:matched){
            if(!new CheckDiff().diff(model,o.getRecipeMaterial(),size,o.getSize()))continue;
            result.add(o);
        }

        if(result.isEmpty()) {
            player.sendMessage("The recipe has multi-result-items.\n" +
                    "Failed to create items, because the custom-crafter-system could not judge which you want to.");
            return null;
        }

        MultiOriginalRecipe mop = new MultiOriginalRecipe(ratio,result.get(0),multiOriginal.getRemaining());
        return mop;
    }
     */

    public ItemStack search(Inventory inventory,int size){
        RecipeMaterial real = getRecipeMaterial(inventory,size);
        List<ItemStack> list = new ArrayList<>();
        for(OriginalRecipe recipe:recipes){

            RecipeMaterial model = recipe.getRecipeMaterial();

            //debug
            System.out.println("===debug===");
            System.out.println(String.format("[total] real:%d | model:%d",getTotal(real),getTotal(model)));
            System.out.println(String.format("[Set] real:%s | model:%s",getMaterialSet(real),getMaterialSet(model)));
            System.out.println(String.format("[Squire Size] real:%d | model:%d",getSquareSize(real),getSquareSize(model)));
            System.out.println("[isCongruence] "+isCongruence(real,model));

            if(getTotal(real)!=recipe.getTotal())continue;
            if(!getMaterialSet(real).equals(getMaterialSet(model)))continue;
            if(getSquareSize(real)!=getSquareSize(model))continue;
            if(!isCongruence(real,model))continue;
            list.add(recipe.getResult());
        }

        //debug
        System.out.println("custom-search results:"+list);

        if(list.isEmpty())return null;
        return list.get(0);
    }

    private RecipeMaterial getRecipeMaterial(Inventory inventory,int size){
        Map<Integer,MultiKeys> relation = new HashMap<>();
        List<Integer> tables = new ArrayList<>();
        for(int y=0;y<size;y++){
            for(int x=0;x<size;x++){
                relation.put(x+y*9,new MultiKeys(x,y));
                tables.add(x+y*9);
            }
        }
        RecipeMaterial rm = new RecipeMaterial();
        for(int i:tables){
            ItemStack item;
            if(inventory.getItem(i)==null){
                item=new ItemStack(Material.AIR);
            }else{
                item=inventory.getItem(i);
                if(item.getAmount()<1)item.setAmount(1);
            }
            rm.put(relation.get(i),item);
        }
        return rm;
    }

    private RecipeMaterial remapping(RecipeMaterial recipeMaterial,int size){
        RecipeMaterial result = new RecipeMaterial();

        MultiKeys first=null;
        for(Map.Entry<MultiKeys,ItemStack> entry:recipeMaterial.getRecipeMaterial().entrySet()){
            if(entry.getValue()==null)continue;
            if(entry.getValue().getType().equals(Material.AIR))continue;
            first = entry.getKey();
            break;
        }
        if(first==null)return recipeMaterial;
        int xTop = first.getKey1();
        int yTop = first.getKey2();
        int xUnder = xTop+size;
        int yUnder = yTop+size;
        for(int y=yTop;y<yUnder;y++){
            for(int x=xTop;x<xUnder;x++){
                ItemStack item;
                MultiKeys key = new MultiKeys(x,y);
                if(recipeMaterial.getRecipeMaterial().get(key)==null){
                    item = new ItemStack(Material.AIR);
                }else{
                    item = recipeMaterial.getRecipeMaterial().get(key);
                }
                result.put(key,item);
            }
        }
        return result;
    }

    private boolean isCongruence(RecipeMaterial real,RecipeMaterial model){
        List<MultiKeys> realList = toSortedList(real);
        List<MultiKeys> modelList = toSortedList(model);
        if(realList.size()!= modelList.size()){
            int size = getSquareSize(model);
            realList = toSortedList(remapping(real,size));
        }

        if(realList.size()!=modelList.size())return false;

        int key1Diff = Math.abs(realList.get(0).getKey1() - modelList.get(0).getKey1());
        int key2Diff = Math.abs(realList.get(0).getKey2() - modelList.get(0).getKey2());
        for(int i=1;i<realList.size();i++){
            //not match
            if(key1Diff != Math.abs(realList.get(i).getKey1() - modelList.get(i).getKey1()))return false;
            if(key2Diff != Math.abs(realList.get(i).getKey2() - modelList.get(i).getKey2()))return false;

        }

        return true;
    }


    private List<MultiKeys> toSortedList(RecipeMaterial in){
        List<MultiKeys> list = new ArrayList<>();
        in.getRecipeMaterial().entrySet().forEach(s->list.add(s.getKey()));

        HashMap<Integer,List<MultiKeys>> map = new HashMap<>();
        for(int i=0;i<list.size();i++){
            List<MultiKeys> int1_List = new ArrayList<>();
            for(int j=0;j<list.size();j++){
                if(list.get(i).getKey1()==i)int1_List.add(list.get(i));
            }
            map.put(i,int1_List);
        }

        List<Integer> shuffle = new ArrayList<>();
        map.keySet().forEach(s->shuffle.add(s));
        Collections.sort(shuffle);

        return list;
    }

    private int getTotal(RecipeMaterial in){
        int result = 0;
        for(Map.Entry<MultiKeys,ItemStack> entry:in.getRecipeMaterial().entrySet()){
            if(entry.getValue()==null)continue;
            if(entry.getValue().getType().equals(Material.AIR))continue;
            int amount = entry.getValue().getAmount();
            if(amount>1)amount=1;
            result += amount;
        }
        return result;
    }

    private Set<Material> getMaterialSet(RecipeMaterial in){
        Set<Material> set = new HashSet<>();
        for(Map.Entry<MultiKeys,ItemStack> entry:in.getRecipeMaterial().entrySet()){
            if(entry.getValue()==null)continue;
            if(entry.getValue().getType().equals(Material.AIR))continue;
            set.add(entry.getValue().getType());
        }
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

        int xDiff = Math.abs(x.get(0)-x.get(x.size()-1));
        int yDiff = Math.abs(y.get(0)-y.get(y.size()-1));
        return Math.max(xDiff,yDiff)+1;
    }

}
