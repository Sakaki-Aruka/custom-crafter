package com.github.sakakiaruka.customcrafter.customcrafter.objects;

import com.github.sakakiaruka.customcrafter.customcrafter.interfaces.Recipes;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class RecipeMaterial implements Recipes {

    private Map<MultiKeys,ItemStack> map = new LinkedHashMap<>();
    public RecipeMaterial(int key1, int key2, ItemStack material){
        MultiKeys mk = new MultiKeys(key1,key2);
        map = new HashMap<MultiKeys,ItemStack>(){{
            put(mk,material);
        }};
    }

    public RecipeMaterial(RecipeMaterial rm){
        this.map = rm.getRecipeMaterial();
    }

    public String info(){
        StringBuilder sb = new StringBuilder();
        map.entrySet().forEach(s->sb.append(String.format("key:%s | Item:%s\n",s.getKey().getKeys(),s.getValue())));
        return sb.toString();
    }

    public RecipeMaterial(Map<MultiKeys,ItemStack> in){
        map = in;
    }

    public RecipeMaterial(MultiKeys key,ItemStack item){
        map = new HashMap<MultiKeys,ItemStack>(){{
            put(key,item);
        }};
    }

    public RecipeMaterial(){}


    public Map<MultiKeys,ItemStack> getRecipeMaterial(){
        return map;
    }

    public void put(MultiKeys multiKeys,ItemStack itemStack){
        map.put(multiKeys,itemStack);
    }

    public ItemStack getItemStack(MultiKeys keys){
        for(Map.Entry<MultiKeys,ItemStack> entry: map.entrySet()){
            if(entry.getKey().same(keys))return entry.getValue();
        }
        return new ItemStack(Material.AIR);
    }

    public int getMapSize(){
        Map<MultiKeys,ItemStack> map = this.map;
        return map.size();
    }

    public boolean isEmpty(){
        for (Map.Entry<MultiKeys,ItemStack> entry:map.entrySet()){
            if(entry.getValue()!=null){
                if(!entry.getValue().getType().equals(Material.AIR))return false;
            }
        }
        return true;
    }



    public RecipeMaterial recipeMaterialClone(){
        RecipeMaterial child = new RecipeMaterial();
        for(Map.Entry<MultiKeys,ItemStack> entry:map.entrySet()){
            child.put(entry.getKey(),entry.getValue());
        }
        return child;
    }

    public void setAllAmount(int amount){
        for(Map.Entry<MultiKeys,ItemStack> entry:map.entrySet()){
            if(entry.getValue().getType()==Material.AIR)continue;
            entry.getValue().setAmount(amount);
        }
    }

    public List<MultiKeys> getMultiKeysList(){
        List<MultiKeys> list = new ArrayList<>();
        map.entrySet().forEach(s->list.add(s.getKey()));
        return list;
    }

    public List<MultiKeys> getMultiKeysListNoAir(){
        List<MultiKeys> list = new ArrayList<>();
        for(Map.Entry<MultiKeys,ItemStack> entry:map.entrySet()){
            if(entry.getValue().getType().equals(Material.AIR))continue;
            list.add(entry.getKey());
        }
        return list;
    }

    public List<ItemStack> getItemStackListNoAir(){
        // the list that returns does not contain AIR and null
        List<ItemStack> list = new ArrayList<>();
        for(Map.Entry<MultiKeys,ItemStack> entry: map.entrySet()){
            if(entry.getValue()==null)continue;
            if(entry.getValue().getType().equals(Material.AIR))continue;
            list.add(entry.getValue());
        }
        return list;
    }

    public List<ItemStack> getItemStackList(){
        // the list that returns that contains AIR
        List<ItemStack> list = new ArrayList<>();
        for(Map.Entry<MultiKeys, ItemStack> entry:map.entrySet()){
            ItemStack item;
            if(entry.getValue()==null){
                item = new ItemStack(Material.AIR);
            }else{
                item = entry.getValue();
            }
            list.add(item);
        }
        return list;
    }

    public RecipeMaterial copy(){
        RecipeMaterial copied = new RecipeMaterial(map);
        return copied;
    }

    public Material getLargestMaterial(){
        Map<Material,Integer> relation = new HashMap<>();
        for(Map.Entry<MultiKeys,ItemStack> entry:map.entrySet()){
            Material m = entry.getValue().getType();
            int i = entry.getValue().getAmount();
            if(m.equals(Material.AIR))continue;
            if(relation.containsKey(m)){
                relation.put(m,relation.get(m)+i);
            }else{
                relation.put(m,i);
            }
        }

        Material material = Material.AIR;
        int integer = 0;
        for(Map.Entry<Material,Integer> entry:relation.entrySet()){
            if(entry.getValue() > integer){
                integer = entry.getValue();
                material = entry.getKey();
            }else if(entry.getValue() == integer){
                ArrayList<String> temp = new ArrayList<>(Arrays.asList(material.name(),entry.getKey().name()));
                Collections.sort(temp);
                material = Material.valueOf(temp.get(0));
                integer = relation.get(material);
            }
        }
        return material;
    }

    public int getLargestAmount(){
        RecipeMaterial rm = new RecipeMaterial(map);
        Material largest = rm.getLargestMaterial();
        int result = 0;
        for(Map.Entry<MultiKeys,ItemStack> entry: map.entrySet()){
            if(!entry.getValue().getType().equals(largest))continue;
            result += entry.getValue().getAmount();
        }
        return result;
    }

    public int getTotalItems(){
        int result = 0;
        for(Map.Entry<MultiKeys,ItemStack> entry:map.entrySet()){
            if(entry.getValue().getType().equals(Material.AIR))continue;
            result += entry.getValue().getAmount();
        }
        return result;
    }

}
