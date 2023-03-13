package com.github.sakakiaruka.customcrafter.customcrafter.search;

import com.github.sakakiaruka.customcrafter.customcrafter.object.Matter.EnchantStrict;
import com.github.sakakiaruka.customcrafter.customcrafter.object.Matter.EnchantWrap;
import com.github.sakakiaruka.customcrafter.customcrafter.object.Matter.Matter;
import com.github.sakakiaruka.customcrafter.customcrafter.object.Recipe.Coordinate;
import com.github.sakakiaruka.customcrafter.customcrafter.object.Recipe.Recipe;
import com.github.sakakiaruka.customcrafter.customcrafter.object.Recipe.Tag;
import com.github.sakakiaruka.customcrafter.customcrafter.object.Result.Result;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.sql.Array;
import java.util.*;

import static com.github.sakakiaruka.customcrafter.customcrafter.SettingsLoad.recipes;

public class Search {
    public void main(Player player, Inventory inventory){
        // normal
        List<Recipe> resultCandidate = new ArrayList<>();
        for(Recipe recipe : recipes){
            if(recipe.getTag().equals(Tag.Normal)){
                //normal
                Recipe input = toRecipe(inventory);
                if(getSquareSize(recipe) != getSquareSize(input))return;
                if(!isSameShape(getCoordinateNoAir(recipe),getCoordinateNoAir(input)))return;
                if(getTotal(recipe) != getTotal(input))return;

                List<Matter> recipeMatters = recipe.getContentsNoAir();
                List<Matter> inputMatters = input.getContentsNoAir();
                for(int i=0;i<recipeMatters.size();i++){
                    if(!isSameMatter(recipeMatters.get(i),inputMatters.get(i)))return;
                }
                resultCandidate.add(recipe);

            }else{
                //amorphous

                resultCandidate.add(recipe);
            }
        }
    }

    public void batchSearch(Player player,Inventory inventory){
        // batch
    }

    private void setResultItem(Inventory inventory, List<ItemStack> results){
        // set results
    }

    private boolean isSameMatter(Matter recipe,Matter input){
        if(!recipe.getCandidate().containsAll(input.getCandidate()))return false;
        if(recipe.getAmount() != input.getAmount())return false;
        if(!getEnchantWrapCongruence(recipe.getWarp(),input.getWarp()))return false;
        return true;
    }

    private boolean getEnchantWrapCongruence(List<EnchantWrap> recipe,List<EnchantWrap> input){
        for(EnchantWrap wrap:recipe){
            if(wrap.getStrict().equals(EnchantStrict.NotStrict))continue;
            if(!getEnchantmentList(input).contains(wrap.getEnchant()))return false;
            if(wrap.getStrict().equals(EnchantStrict.OnlyEnchant))continue;

            EnchantWrap w = null;
            for(EnchantWrap t:input){
                if(t.getEnchant().equals(wrap.getEnchant()))w = t;
            }
            if(wrap.getLevel() != w.getLevel())return false;
        }
        return true;
    }

    private List<Enchantment> getEnchantmentList(List<EnchantWrap> wrap){
        List<Enchantment> list = new ArrayList<>();
        wrap.forEach(s->list.add(s.getEnchant()));
        return list;
    }

    private List<EnchantWrap> getEnchantWrap(ItemStack item){
        List<EnchantWrap> list = new ArrayList<>();
        Map<Enchantment,Integer> map = item.getEnchantments();
        EnchantStrict strict = EnchantStrict.Input;
        for(Map.Entry<Enchantment,Integer> entry:map.entrySet()){
            Enchantment enchant = entry.getKey();
            int level = entry.getValue();
            EnchantWrap wrap = new EnchantWrap(level,enchant,strict);
            list.add(wrap);
        }
        return list;
    }

    private List<Coordinate> getCoordinateNoAir(Recipe recipe){
        List<Coordinate> list = new ArrayList<>();
        for(Map.Entry<Coordinate,Matter> entry:recipe.getCoordinate().entrySet()){
            if(entry.getValue().getCandidate().get(0).equals(Material.AIR))continue;
            list.add(entry.getKey());
        }
        return list;
    }

    private int getTotal(Recipe recipe){
        Map<Coordinate,Matter> map = recipe.getCoordinate();
        int result =0;
        for(Map.Entry<Coordinate,Matter> entry: map.entrySet()){
            if(entry.getValue().getCandidate().get(0).equals(Material.AIR))continue;
            result += entry.getValue().getAmount();
        }
        return result;
    }

    private int getSquareSize(Recipe recipe){
        List<Coordinate> list = getCoordinateNoAir(recipe);
        if(list.get(0).getX() < 0 || list.get(0).getY() < 0)return -1;

        List<Integer> x = new ArrayList<>();
        List<Integer> y = new ArrayList<>();
        list.forEach(s->{
            x.add(s.getX());
            y.add(s.getY());
        });
        Collections.sort(x);
        Collections.sort(y);
        int width = Math.abs(x.get(0) - x.get(x.size()-1)) + 1;
        int height = Math.abs(y.get(0) - y.get(y.size()-1)) + 1;
        return Math.max(width,height);
    }

    private boolean isSameShape(List<Coordinate> models,List<Coordinate> reals){
        int xGap = models.get(0).getX() - reals.get(0).getX();
        int yGap = models.get(0).getY() - reals.get(0).getY();
        if(models.size() != reals.size())return false;
        int size = models.size();
        for(int i=1;i<size;i++){
            if(models.get(i).getX() - reals.get(i).getX() != xGap)return false;
            if(models.get(i).getY() - reals.get(i).getY() != yGap)return false;
        }
        return true;
    }

    private Recipe toRecipe(Inventory inventory){
        Recipe recipe = new Recipe();
        for(int y=0;y<6;y++){
            for(int x=0;x<6;x++){
                int i = x+y*9;
                Matter matter = inventory.getItem(i)==null
                        ? new Matter(Arrays.asList(Material.AIR),0)
                        : new Matter(Arrays.asList(inventory.getItem(i).getType()),inventory.getItem(i).getAmount());
                matter.setWarp(getEnchantWrap(inventory.getItem(i))); //set enchantments information
                recipe.addCoordinate(x,y,matter);
            }
        }
        return recipe;
    }
}
