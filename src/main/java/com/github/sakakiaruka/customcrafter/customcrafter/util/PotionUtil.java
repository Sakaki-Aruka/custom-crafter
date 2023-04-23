package com.github.sakakiaruka.customcrafter.customcrafter.util;

import com.github.sakakiaruka.customcrafter.customcrafter.object.Matter.PotionData.PotionDuration;

public class PotionUtil {
    public int getDuration(String key,boolean upgraded,boolean extended){
        for(PotionDuration pd : PotionDuration.values()){
            if(!pd.toString().equalsIgnoreCase(key)) continue;
            if(upgraded) return pd.getUpgraded();
            if(extended) return pd.getExtended();
            return pd.getNormal();
        }
        return -1;
    }
}
