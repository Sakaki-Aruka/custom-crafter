package com.github.sakakiaruka.customcrafter.customcrafter.object.Matter;

import com.github.sakakiaruka.customcrafter.customcrafter.object.Matter.Container.MatterContainer;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static com.github.sakakiaruka.customcrafter.customcrafter.SettingsLoad.LINE_SEPARATOR;

public class Matter {
    private String name;
    private List<Material> candidate;
    @Nullable
    private List<EnchantWrap> wrap;
    private int amount;
    private boolean mass;
    private List<MatterContainer> containers;
    private PersistentDataContainer pdc;

    public Matter(String name,List<Material> candidate,List<EnchantWrap> wrap,int amount,boolean mass, List<MatterContainer> containers){
        this.name = name;
        this.candidate = candidate;
        this.wrap = wrap;
        this.amount = amount;
        this.mass = mass;
        this.containers = containers;
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
        this.containers = matter.getContainers();
        this.pdc = matter.getPDC();
    }

    public Matter(ItemStack item){
        this.name = "";
        this.candidate = List.of(item.getType());
        if(item.hasItemMeta() && item.getItemMeta().hasEnchants() && !candidate.get(0).equals(Material.ENCHANTED_BOOK)){
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
        this.pdc = item.getItemMeta().getPersistentDataContainer();
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
        return !((wrap == null) || wrap.isEmpty());
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
        getWrap().forEach(s-> builder.append(s.info()).append(LINE_SEPARATOR));
        return builder.toString();
    }

    public boolean contains(Enchantment enchantment){
        if(!hasWrap()) return false;
        for(EnchantWrap w : wrap){
            if(w.getEnchant().equals(enchantment)) return true;
        }
        return false;
    }

    public Matter copy(){
        List<Material> candidate = this.candidate;
        String name = this.name;
        int amount = this.amount;
        List<EnchantWrap> wrap = hasWrap() ? this.wrap : null;
        boolean mass = this.mass;
        List<MatterContainer> containers = this.containers;
        Matter matter = new Matter(name, candidate, wrap, amount, mass, containers);
        matter.setPDC(this.pdc);
        return matter;
    }

    public Matter oneCopy(){
        List<Material> candidate = this.candidate;
        String name = this.name;
        int amount = 1;
        List<EnchantWrap> wrap = hasWrap() ? this.wrap : null;
        boolean mass = this.mass;
        List<MatterContainer> containers = this.containers;
        return new Matter(name, candidate, wrap, amount, mass, containers);
    }



    public String info(){
        return String.format("name : %s" + LINE_SEPARATOR, name != null && !name.isEmpty() ? candidate.get(0).name() : name) +
                String.format("candidate : %s" + LINE_SEPARATOR, candidate.toString()) +
                String.format("wrap : %s", hasWrap() ? getAllWrapInfo() : "null" + LINE_SEPARATOR) +
                String.format("amount : %d" + LINE_SEPARATOR, amount) +
                String.format("mass : %b" + LINE_SEPARATOR, isMass());
    }


    public boolean hasContainers() {
        return !((containers == null) || containers.isEmpty());
    }

    public List<MatterContainer> getContainers() {
        return containers;
    }

    public void setContainers(List<MatterContainer> containers) {
        this.containers = containers;
    }

    public void addContainers(List<MatterContainer> containers) {
        this.containers.addAll(containers);
    }

    public PersistentDataContainer getPDC() {
        if (!hasPDC()) return null;
        return this.pdc;
    }

    public boolean hasPDC() {
        return pdc != null;
    }

    public void setPDC(PersistentDataContainer pdc) {
        this.pdc = pdc;
    }



    @Override
    public boolean equals(Object obj) {
        /*
         * return the congruence with given object and this.
         *
         * check elements
         * - candidate (all match)
         * - mass
         * - amount
         */
        if (this == obj) return true;
        if (!(obj instanceof Matter)) return false;
        Matter matter =(Matter) obj;
        if (!this.candidate.equals(matter.candidate)) return false;
        if (this.mass != matter.mass) return false;
        return this.amount == matter.amount;
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = result * 31 + candidate.hashCode();
        result = result * 31 + (mass ? 1 : 0);
        result = result * 31 + amount;
        return result;
    }
}
