package com.github.sakakiaruka.cutomcrafter.customcrafter.some;

import com.github.sakakiaruka.cutomcrafter.customcrafter.objects.MultiKeys;
import com.github.sakakiaruka.cutomcrafter.customcrafter.objects.RecipeMaterial;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class RecipeMaterialUtil {
    public String graphicalCoordinate(RecipeMaterial rm){
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
}
