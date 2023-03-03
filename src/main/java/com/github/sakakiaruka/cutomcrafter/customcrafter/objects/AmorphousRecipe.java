package com.github.sakakiaruka.cutomcrafter.customcrafter.objects;

import com.github.sakakiaruka.cutomcrafter.customcrafter.interfaces.Recipes;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class AmorphousRecipe extends RecipeMaterial implements Recipes {

    private AmorphousEnum typeEnum;
    private Map<ItemStack,Integer> materials;

    public AmorphousRecipe(AmorphousEnum typeEnum,Map<ItemStack,Integer> materials){
        super();
        this.typeEnum = typeEnum;
        this.materials = materials;
    }

    public AmorphousEnum getTypeEnum() {
        return typeEnum;
    }

    public void setTypeEnum(AmorphousEnum typeEnum) {
        this.typeEnum = typeEnum;
    }

    public Map<ItemStack, Integer> getMaterials() {
        return materials;
    }

    public void setMaterials(Map<ItemStack, Integer> materials) {
        this.materials = materials;
    }

    public int getTotalItems(){
        int result = 0;
        for(Map.Entry<ItemStack,Integer> entry:materials.entrySet()){
            if(entry.getKey().getType().equals(Material.AIR))continue;
            result += entry.getValue();
        }
        return result;
    }

    public List<ItemStack> getItemStackListNoAir(){
        List<ItemStack> list = new ArrayList<>();
        materials.entrySet().forEach(s->list.add(s.getKey()));
        ItemStack air = new ItemStack(Material.AIR);
        if(list.contains(air))list.remove(air);
        return list;
    }

    public Material getLargestMaterial(){
        List<ItemStack> items = getLargestItemStack();
        List<String> names = new ArrayList<>();
        items.forEach(s->names.add(s.getType().name()));
        Collections.sort(names);
        return Material.valueOf(names.get(0));
    }

    public int getLargestAmount(){
        return getLargestItemStack().get(0).getAmount();
    }

    public String info(){
        String enumType = typeEnum.toString();
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("enumType:%s%n",enumType));
        for(Map.Entry<ItemStack,Integer> entry:materials.entrySet()){
            String amount = String.valueOf(entry.getValue());
            String type = entry.getKey().getType().name();
            String otherData = entry.getKey().getData().toString();

            sb.append(String.format("amount:%s%n",amount));
            sb.append(String.format("Material Type:%s%n",type));
            sb.append(String.format("other Data:%s%n",otherData));
        }
        return sb.toString();
    }

    public boolean isEmpty(){
        return materials.isEmpty();
    }

    private List<ItemStack> getLargestItemStack(){
        List<ItemStack> items = new ArrayList<>();
        for(Map.Entry<ItemStack,Integer> entry:materials.entrySet()){
            if(items.size()==0){
                items.add(entry.getKey());
            }else if(entry.getKey().getAmount()==items.get(0).getAmount()){
                //same amount
                items.add(entry.getKey());
            }else if(entry.getKey().getAmount() > items.get(0).getAmount()){
                items.clear();
                items.add(entry.getKey());
            }
        }
        return items;
    }
}
