package com.github.sakakiaruka.customcrafter.customcrafter.object.Matter;

import com.github.sakakiaruka.customcrafter.customcrafter.interfaces.Matters;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class Matter implements Matters {
    private String name;
    private List<Material> candidate;
    private List<EnchantWrap> wrap;
    private int amount;
    private boolean mass;
    public Matter(String name,List<Material> candidate,List<EnchantWrap> wrap,int amount,boolean mass){
        this.name = name;
        this.candidate = candidate;
        this.wrap = wrap;
        this.amount = amount;
        this.mass = mass;
    }

    public Matter(List<Material> materials,int amount){
        this.name = "";
        this.candidate = materials;
        this.wrap = null;
        this.amount = amount;
        this.mass = false;
    }

    public Matter(List<Material> materials,int amount, boolean mass){
        this.name = "";
        this.candidate = materials;
        this.wrap = null;
        this.amount = amount;
        this.mass = mass;
    }

    public Matter(Matter matter){
        this.name = matter.getName();
        this.candidate = matter.getCandidate();
        this.wrap = matter.hasWrap() ? matter.getWrap() : null;
        this.amount = matter.getAmount();
        this.mass = matter.isMass();
    }

    public Matter(ItemStack item){
        this.name = "";
        this.candidate = Arrays.asList(item.getType());
        if(item.getItemMeta().hasEnchants() && !candidate.get(0).equals(Material.ENCHANTED_BOOK)){
            // an enchanted item (not an Enchanted_book)
            List<EnchantWrap> list = new ArrayList<>();
            for(Map.Entry<Enchantment,Integer> entry : item.getItemMeta().getEnchants().entrySet()){
                int level = entry.getValue();
                Enchantment enchant = entry.getKey();
                EnchantStrict strict = EnchantStrict.INPUT;
                EnchantWrap wrap = new EnchantWrap(level,enchant,strict);
                list.add(wrap);
            }
            this.wrap = list;
        }else if(candidate.get(0).equals(Material.ENCHANTED_BOOK)){
            // enchanted book
            List<EnchantWrap> list = new ArrayList<>();
            for(Map.Entry<Enchantment,Integer> entry : ((EnchantmentStorageMeta)item.getItemMeta()).getStoredEnchants().entrySet()){
                int level = entry.getValue();
                Enchantment enchant = entry.getKey();
                EnchantStrict strict = EnchantStrict.INPUT;
                EnchantWrap wrap = new EnchantWrap(level,enchant,strict);
                list.add(wrap);
            }
            this.wrap = list;
        }
        this.mass = false;
        this.amount = item.getAmount();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Material> getCandidate() {
        return candidate;
    }

    public void setCandidate(List<Material> candidate) {
        this.candidate = candidate;
    }

    public void addCandidate(List<Material> additional){
        this.candidate.addAll(additional);
    }

    public List<EnchantWrap> getWrap() {
        return wrap;
    }

    public void setWrap(List<EnchantWrap> wrap) {
        this.wrap = wrap;
    }

    public boolean hasWrap(){
        if(wrap == null) return false;
        return wrap.isEmpty();
    }

    public void addWrap(EnchantWrap in){
        if(!hasWrap()) wrap = new ArrayList<>();
        wrap.add(in);
    }

    public void addAllWrap(List<EnchantWrap> in){
        if(!hasWrap()) wrap = new ArrayList<>();
        wrap.addAll(in);
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public boolean isMass() {
        return mass;
    }

    public void setMass(boolean mass) {
        this.mass = mass;
    }

    public int getEnchantLevel(Enchantment enchant){
        if(wrap == null)return -1;
        for(EnchantWrap w : wrap){
            if(w.getEnchant().equals(enchant))return w.getLevel();
        }
        return -1;
    }

    public String getAllWrapInfo(){
        if(!hasWrap())return "";
        StringBuilder builder = new StringBuilder();
        getWrap().forEach(s->builder.append(s.info()+"\n"));
        return builder.toString();
    }

    public boolean contains(Enchantment enchantment){
        if(!hasWrap()) return false;
        for(EnchantWrap w : wrap){
            if(w.getEnchant().equals(enchantment)) return true;
        }
        return false;
    }

    public String info(){
        StringBuilder builder = new StringBuilder();
        builder.append(String.format("name : %s\n",name.isEmpty() ? candidate.get(0).name() : name));
        builder.append(String.format("candidate : %s\n",candidate.toString()));
        builder.append(String.format("wrap : %s",hasWrap() ? getAllWrapInfo() : "null\n"));
        builder.append(String.format("amount : %d\n",amount));
        builder.append(String.format("mass : %b\n",isMass()));
        return builder.toString();
    }
}
