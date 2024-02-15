package com.github.sakakiaruka.customcrafter.customcrafter.interfaces;

import com.github.sakakiaruka.customcrafter.customcrafter.object.ContainerWrapper;
import com.github.sakakiaruka.customcrafter.customcrafter.object.Matter.EnchantWrap;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;

import java.util.List;
import java.util.Map;

public interface Matters {
    String getName();
    void setName(String name);
    List<Material> getCandidate();
    void setCandidate(List<Material> candidate);
    void addCandidate(List<Material> additional);
    List<EnchantWrap> getWrap();
    void setWrap(List<EnchantWrap> wrap);
    boolean hasWrap();
    void addWrap(EnchantWrap in );
    void addAllWrap(List<EnchantWrap> in);
    int getAmount();
    void setAmount(int amount);
    boolean isMass();
    void setMass(boolean mass);
    int getEnchantLevel(Enchantment enchantment);
    String getAllWrapInfo();
    boolean contains(Enchantment enchantment);
    String info();


}
