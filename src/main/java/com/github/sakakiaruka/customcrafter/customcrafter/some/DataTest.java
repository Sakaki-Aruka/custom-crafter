package com.github.sakakiaruka.customcrafter.customcrafter.some;

import org.bukkit.Material;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.github.sakakiaruka.customcrafter.customcrafter.some.SettingsLoad.mixedCategories;

public class DataTest {
    public void main(){
        //
    }

    private void checkMixedMaterialCongruence(){
        List<Material> checked = new ArrayList<>();
        List<String> noticeBuffer = new ArrayList<>();
        for(Map.Entry<String,List<Material>> entry:mixedCategories.entrySet()){
            List<Material> materials = entry.getValue();
            boolean temporary = true;
            for(Material m : materials){
                if(materials.contains(m))noticeBuffer.add(entry.getKey());
                temporary = false;
            }
            if(temporary)checked.addAll(materials);
        }

        noticeBuffer.forEach(s->{
            mixedCategories.remove(s);
            System.out.println(String.format("=== Warning [CustomCrafter] ===\nA MixedMaterial '%s' is duplicate with other materials. You must resolve this problem. The custom crafter system will not work.\n=== [CustomCrafter] ===",s));
        });
    }
}
