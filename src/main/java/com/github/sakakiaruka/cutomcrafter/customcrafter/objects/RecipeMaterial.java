package com.github.sakakiaruka.cutomcrafter.customcrafter.objects;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class RecipeMaterial {

    private Map<MultiKeys,ItemStack> map = new LinkedHashMap<>();
    public RecipeMaterial(int key1, int key2, ItemStack material){
        MultiKeys mk = new MultiKeys(key1,key2);
        map = new HashMap<MultiKeys,ItemStack>(){{
            put(mk,material);
        }};
    }

    public String toString(){
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

    public RecipeMaterial(){
    }

    public Map<MultiKeys,ItemStack> getRecipeMaterial(){
        return map;
    }

    public void put(MultiKeys multiKeys,ItemStack itemStack){
        map.put(multiKeys,itemStack);
    }

    public ItemStack getItemStack(RecipeMaterial rm,MultiKeys keys){
        Map<MultiKeys,ItemStack> map = rm.getRecipeMaterial();
        return map.get(keys);
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

    public List<MultiKeys> getCoordinateList(){
        Map<MultiKeys,ItemStack> map = this.map;
        List<MultiKeys> keyList = new ArrayList<>();
        for(Map.Entry<MultiKeys,ItemStack> entry:map.entrySet()){
            keyList.add(entry.getKey());
        }
        return keyList;
    }

    public ItemStack[] formatVanilla(RecipeMaterial rm){
        int size = 3;
        ItemStack[] stacks = new ItemStack[9];
        for(int i=0;i<size;i++){
            for(int j=0;j<size;j++){
                int x =i;
                int y = j;
                MultiKeys key = new MultiKeys(x,y);
                ItemStack temporaryStack = rm.getItemStack(rm,key);
                stacks[i+j] = temporaryStack;
            }
        }
        return stacks;
    }
}
