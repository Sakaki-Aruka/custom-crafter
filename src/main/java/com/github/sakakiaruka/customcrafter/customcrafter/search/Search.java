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
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.github.sakakiaruka.customcrafter.customcrafter.SettingsLoad.allMaterials;
import static com.github.sakakiaruka.customcrafter.customcrafter.SettingsLoad.recipes;


public class Search {
    private static final int resultSlot = 44;
    public void main(Player player, Inventory inventory){
        // normal
        Map<Recipe,Recipe> recipeInputResultMap = new HashMap<>(); //key : Recipe | value : Input
        Recipe:for(Recipe recipe : recipes){
            Recipe input = toRecipe(inventory);
            if(recipe.getTag().equals(Tag.Normal)){
                //normal
                if(getSquareSize(recipe) != getSquareSize(input))continue ;
                if(!isSameShape(getCoordinateNoAir(recipe),getCoordinateNoAir(input)))continue ;
                if(getTotal(recipe) != getTotal(input))continue ;

                List<Matter> recipeMatters = recipe.getContentsNoAir();
                List<Matter> inputMatters = input.getContentsNoAir();
                if(recipeMatters.size() != inputMatters.size())continue;
                for(int i=0;i<recipeMatters.size();i++){
                    if(!isSameMatter(recipeMatters.get(i),inputMatters.get(i)))continue Recipe;
                }
                recipeInputResultMap.put(recipe,input);

            }else{
                //amorphous
                Map<Material,Integer> virtual = getMaterialAmountMap(recipe);
                Map<Material,Integer> real = getMaterialAmountMap(input);
                int ideal = getMapEachSum(virtual) - getMapEachSum(real);

                for(Map.Entry<Material,Integer> entry:real.entrySet()){
                    Material m = entry.getKey();
                    if(!virtual.containsKey(m))continue Recipe; // exit recipe loop
                    virtual.put(m,virtual.get(m) - entry.getValue());
                }
                if(!getAmountCollectionCongruence(virtual))continue;
                if(getMapEachSum(virtual) != ideal)continue;

                // enchantment detail check
                Map<Material,List<Matter>> map = getMaterialMatterRelation(recipe);
                if(!getEnchantCongruenceAmorphous(map,input))continue;

                recipeInputResultMap.put(recipe,input);
            }
        }

        if(recipeInputResultMap.size() > 1){
            System.out.println("The search system cannot determine a result item.");
            recipeInputResultMap.entrySet().forEach(s->System.out.println(s.getKey().getName()));
            System.out.println("The candidate list. ↑");
            return;
        }

        //
        setResultItem(inventory,recipeInputResultMap,player);

    }

    public void batchSearch(Player player,Inventory inventory){
        // batch
        List<Object> recipeInputResultAmountList = new ArrayList<>();
        Recipe input = toRecipe(inventory);
        Recipe:for(Recipe recipe:recipes){
            for(Matter matter:recipe.getContentsNoAir()){
                if(!matter.isMass())continue Recipe;
            }

            //search here
            // isMass = true -> skip to "check amount" process
            if(recipe.getTag().equals(Tag.Normal)){
                //normal
                if(getSquareSize(recipe) != getSquareSize(input))continue;
                if(!isSameShape(getCoordinateNoAir(recipe),getCoordinateNoAir(input)))continue;

                List<Material> without = getWithout(recipe);
                int withoutMassMatter = getTotalWithoutMassMatter(recipe);
                int inputAmount  = 0;
                int minimalAmount = 100;
                for(Matter matter:input.getContentsNoAir()){
                    Material m = matter.getCandidate().get(0); //An input candidate is always only one material.
                    if(without.contains(m))continue;
                    inputAmount += matter.getAmount();
                    if(matter.getAmount() < minimalAmount)minimalAmount = matter.getAmount();
                }

                int amount = inputAmount / withoutMassMatter;
                if(amount < 1)continue; // not enough

                List<Matter> recipeMatters = recipe.getContentsNoAir();
                List<Matter> inputMatters = input.getContentsNoAir();
                if(recipeMatters.size() != inputMatters.size())continue Recipe;
                for(int i=0;i<recipeMatters.size();i++){
                    if(recipeMatters.get(i).isMass()){
                        // if mass true ->
                        if(!recipeMatters.get(i).getCandidate().containsAll(inputMatters.get(i).getCandidate()))continue Recipe;
                    }else{
                        // if mass false ->
                        Matter rTemp = recipeMatters.get(i);
                        Matter iTemp = inputMatters.get(i);
                        if(!rTemp.getCandidate().containsAll(iTemp.getCandidate()))continue Recipe;
                        if(!getEnchantWrapCongruence(rTemp.getWarp(),iTemp.getWarp()))continue Recipe;
                    }
                }

                recipeInputResultAmountList.add(Arrays.asList(recipe,input,minimalAmount));

            }else{
                //amorphous
            }
        }
    }

    private List<Material> getWithout(Recipe recipe){
        List<Material> list = new ArrayList<>();
        for(Matter matter:recipe.getContentsNoAir()){
            if(matter.isMass())continue;
            // A candidate have to be composed by only one material.
            if(matter.getCandidate().size() != 1)continue;
            Material material = matter.getCandidate().get(0);
            list.add(material);
        }
        return list;
    }

    private int getTotalWithoutMassMatter(Recipe recipe){
        int result = 0;
        for(Matter matter:recipe.getContentsNoAir()){
            if(matter.isMass())continue;
            result += matter.getAmount();
        }
        return result;
    }

    private void setResultItem(Inventory inventory, Map<Recipe,Recipe> candidate,Player player){
        // get result items
        List<ItemStack> resultItems = new ArrayList<>();
        for(Map.Entry<Recipe,Recipe> entry: candidate.entrySet()){
            // key -> Recipe | value -> Input
            Recipe recipe = entry.getKey();
            Recipe input = entry.getValue();
            if(allMaterials.contains(recipe.getResult().getNameOrRegex())
            || recipe.getResult().getMatchPoint() == -1
            || !recipe.getResult().getNameOrRegex().contains(",")){
                // result has definite material
                Material m = Material.valueOf(recipe.getResult().getNameOrRegex());
                ItemStack item = new ItemStack(m,recipe.getResult().getAmount());
                setMetaData(item,recipe.getResult()); //set result itemStack's metadata
                resultItems.add(item);
            }else{
                // not contains -> A result has written by regex pattern.
                List<String> list = Arrays.asList(recipe.getResult().getNameOrRegex().split(","));
                String p = list.get(0);
                String replaced = list.get(1);
                Pattern pattern = Pattern.compile(p);
                List<String> materials = new ArrayList<>();
                for(Material m:getContainsMaterials(input)){
                    String name = m.name();
                    Matcher matcher = pattern.matcher(name);
                    if(matcher.find()){
                        int point = recipe.getResult().getMatchPoint();
                        replaced.replace("{R}",matcher.group(point));
                        materials.add(replaced);
                    }
                }
                Collections.sort(materials);

                Material material = Material.valueOf(list.get(0).toUpperCase());
                ItemStack item = new ItemStack(material,recipe.getResult().getAmount());
                setMetaData(item,recipe.getResult()); //set metadata
                resultItems.add(item);
            }

        }

        //debug
        resultItems.forEach(s->System.out.println(String.format("result item : %s",s)));

        ItemStack item = resultItems.get(0);
        if(inventory.getItem(resultSlot) == null){
            // empty a result item's slot
            player.getWorld().dropItem(player.getLocation(),item);
        }else{
            if(item.getAmount() > item.getType().getMaxStackSize()){
                // over the max amount
                player.getWorld().dropItem(player.getLocation(),item);
            }else{
                // in the limit
                inventory.setItem(resultSlot,item);
            }
        }

    }

    private void setMetaData(ItemStack item,Result result){
        Map<String,List<String>> metadata = result.getMetadata();
        ItemMeta meta = item.getItemMeta();
        for(Map.Entry<String,List<String>> e:metadata.entrySet()){
            //metadata set
            // metadata -> lore, displayName, enchantment, itemFlag, unbreakable, customModelData
            /*
             * lore -> split ",".
             * displayName -> used directly itemName
             * enchantment -> a split with enchantment with level is ":", a split with other is ",".
             * itemFlag -> split ","
             * unbreakable -> "true" (or "false")
             * customModelData -> "customModelData:(modelNumber)"
             *
             */
            String type = e.getKey();
            List<String> types = Arrays.asList("lore","displayName","enchantment","itemFlag","unbreakable","customModelData");
            List<String> content = e.getValue();
            if(!types.contains(type)){
                //debug
                System.out.println("This type is not a correct metadata.");
                System.out.println(String.format("name:%s | content:%s",type,types));
                continue;
            }

            if(type.equalsIgnoreCase("lore"))meta.setLore(content);
            if(type.equalsIgnoreCase("displayName"))meta.setDisplayName(content.get(0));
            if(type.equalsIgnoreCase("enchantment")){
                for(String s:content){
                    List<String> l = Arrays.asList(s.split(","));
                    Enchantment enchant = Enchantment.getByName(l.get(0).toUpperCase());
                    int level = Integer.valueOf(l.get(1));
                    item.addUnsafeEnchantment(enchant,level);
                }
            }
            if(type.equalsIgnoreCase("itemFlag")) content.forEach(s->meta.addItemFlags(ItemFlag.valueOf(s.toUpperCase())));
            if(type.equalsIgnoreCase("unbreakable"))meta.setUnbreakable(Boolean.valueOf(content.get(0)));
            if(type.equalsIgnoreCase("customModelData"))meta.setCustomModelData(Integer.valueOf(content.get(0)));
        }
        item.setItemMeta(meta);
    }

    private List<Material> getContainsMaterials(Recipe input){
        Set<Material> set = new HashSet<>();
        input.getContentsNoAir().forEach(s->{
            set.addAll(s.getCandidate());
        });
        List<Material> list = new ArrayList<>();
        set.forEach(s->list.add(s));

        return list;
    }

    private boolean getEnchantCongruenceAmorphous(Map<Material,List<Matter>> map,Recipe input){
        for(Matter matter:input.getContentsNoAir()){ // matter = input
            List<Matter> list = map.get(matter.getCandidate().get(0));
            for(int i=0;i<list.size();i++){ // matter = recipe
                Matter inRecipe = list.get(i);
                if(getEnchantWrapCongruence(inRecipe.getWarp(),matter.getWarp()))break;
                if(i == list.size()-1){
                    // not found match Matter.
                    return false;
                }
            }
        }
        return true;
    }

    private Map<Material,List<Matter>> getMaterialMatterRelation(Recipe recipe){
        Map<Material,List<Matter>> map = new HashMap<>();
        for(Matter matter:recipe.getContentsNoAir()){
            List<Material> candidate = matter.getCandidate();
            candidate.forEach(s->{
                if(map.containsKey(s))map.get(s).add(matter);
                else map.put(s,Arrays.asList(matter));
            });
        }
        return map;
    }

    private boolean getAmountCollectionCongruence(Map<Material,Integer> map){
        for(int i:map.values()){
            if(i < 0)return false;
        }
        return true;
    }

    private Map<Material,Integer> getMaterialAmountMap(Recipe recipe){
        Map<Material,Integer> map = new HashMap<>();
        for(Matter matter:recipe.getContentsNoAir()){
            for(Material material:matter.getCandidate()){
                if(map.containsKey(material))map.put(material,map.get(material) + matter.getAmount());
                else map.put(material,matter.getAmount());
            }
        }
        return map;
    }

    private int getMapEachSum(Map<Material,Integer> map){
        int result = 0;
        for(int i:map.values()){
            result += i;
        }
        return result;
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
        if(map.isEmpty())return null;
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
