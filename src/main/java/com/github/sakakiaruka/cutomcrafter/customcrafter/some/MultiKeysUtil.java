package com.github.sakakiaruka.cutomcrafter.customcrafter.some;

import com.github.sakakiaruka.cutomcrafter.customcrafter.objects.MultiKeys;

import java.util.List;

public class MultiKeysUtil {
    public String toString(List<MultiKeys> list){
        StringBuilder b = new StringBuilder();
        for(MultiKeys key:list){
            b.append(String.format("%s | ",key.getKeys()));
        }
        return b.toString();
    }
}
