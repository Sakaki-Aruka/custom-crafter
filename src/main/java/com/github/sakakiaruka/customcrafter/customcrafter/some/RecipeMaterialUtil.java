package com.github.sakakiaruka.customcrafter.customcrafter.some;

import com.github.sakakiaruka.customcrafter.customcrafter.objects.*;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class RecipeMaterialUtil {
    public String graphicalCoordinate(RecipeMaterial rm){
        if(rm instanceof AmorphousRecipe)return getGraphicalAmorphousRecipe((AmorphousRecipe) rm);
        int size = (int)Math.round(Math.sqrt(rm.getRecipeMaterial().size()));
        List<String> names = toStringList(rm.getItemStackList());
        int longest = longestCharaLen(names);
        StringBuilder result = new StringBuilder();
        for(int y=0;y<size;y++){
            StringBuilder builder = new StringBuilder();
            for(int x=0;x<size;x++){
                String name = rm.getItemStack(new MultiKeys(x,y)).getType().name();
                if(name.length() < longest){
                    name = lenEdit(name,longest," ");
                }
                builder.append(String.format("%s | ",name));
            }
            result.append(builder+"\n");
        }
        return result.toString();
    }

    private List<String> toStringList(List<ItemStack> in){
        List<String> list = new ArrayList<>();
        for(ItemStack item:in){
            list.add(item.getType().name());
        }
        return list;
    }

    private int longestCharaLen(List<String> str){
        int len = 0;
        for(String s:str){
            if(s.length() > len)len = s.length();
        }
        return len;
    }

    private String lenEdit(String in,int goal,String add){
        StringBuilder b = new StringBuilder(in);
        int diff = goal - in.length();
        for(int i=0;i<diff;i++){
            b.append(add);
        }
        return b.toString();
    }

    private String getGraphicalAmorphousRecipe(AmorphousRecipe in){
        StringBuilder sb = new StringBuilder();
        for(Map.Entry<ItemStack,Integer> entry:in.getMaterials().entrySet()){
            List<Material> materials;

            if(entry.getKey().getClass().equals(MixedMaterial.class)){
                materials = ((MixedMaterial)entry.getKey()).getCandidate();
            }else if(entry.getKey().getClass().equals(RegexRecipeMaterial.class)){
                materials = ((RegexRecipeMaterial)entry.getKey()).getCandidate();
            }else{
                materials = Arrays.asList(entry.getKey().getType());
            }

            StringBuilder detail = new StringBuilder();
            detail.append("Detail :\n  Materials :\n  ");
            int buffer = 2;
            for(Material m:materials){
                if(buffer + m.name().length() + 3> 80){
                    buffer = 2;
                    detail.append("\n  ");
                }
                detail.append(m.name()+" | ");
                buffer += m.name().length() + 3;
            }
            detail.append("\n");

            if(entry.getKey().getClass().equals(RegexRecipeMaterial.class)){
                String pattern = ((RegexRecipeMaterial)entry.getKey()).getPattern();
                detail.append(String.format("  Regex Pattern : %s\n",pattern));
            }
            if(entry.getKey().getClass().equals(EnchantedMaterial.class)){
                Map<IntegratedEnchant,EnchantedMaterialEnum> map = ((EnchantedMaterial)entry.getKey()).getRelation();
                StringBuilder enchant = new StringBuilder();
                enchant.append("  Enchants :\n");
                for(Map.Entry<IntegratedEnchant,EnchantedMaterialEnum> enchants:map.entrySet()){
                    enchant.append(String.format("  Enum : %s | Enchant : %s | Level : %d\n",enchants.getValue().toString(),enchants.getKey().getEnchant(),enchants.getKey().getLevel()));
                }
                detail.append(enchant);
            }

            sb.append(detail);
            sb.append(String.format("Amount : %d\n",entry.getValue()));
            sb.append(String.format("Type : %s\n",entry.getKey().getClass().getSimpleName()));
            sb.append("---");

        }
        return sb.toString();
    }
}
