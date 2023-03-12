package com.github.sakakiaruka.customcrafter.customcrafter.util;

import java.util.ArrayList;
import java.util.List;

public class InventoryUtil {
    public List<Integer> getTableSlots(int size){
        List<Integer> list = new ArrayList<>();
        for(int i=0;i<size;i++){
            for(int j=0;j<size;j++){
                int result = i*9+j;
                list.add(result);
            }
        }
        return list;
    }
}
