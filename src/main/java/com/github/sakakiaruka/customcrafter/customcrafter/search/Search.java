package com.github.sakakiaruka.customcrafter.customcrafter.search;

import com.github.sakakiaruka.customcrafter.customcrafter.object.Matter.EnchantStrict;
import com.github.sakakiaruka.customcrafter.customcrafter.object.Matter.EnchantWrap;
import com.github.sakakiaruka.customcrafter.customcrafter.object.Matter.Matter;
import com.github.sakakiaruka.customcrafter.customcrafter.object.Recipe.Coordinate;
import com.github.sakakiaruka.customcrafter.customcrafter.object.Recipe.Recipe;
import com.github.sakakiaruka.customcrafter.customcrafter.object.Recipe.Tag;
import com.github.sakakiaruka.customcrafter.customcrafter.object.Result.Result;
import com.github.sakakiaruka.customcrafter.customcrafter.util.InventoryUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.github.sakakiaruka.customcrafter.customcrafter.SettingsLoad.*;


public class Search {

    public void main(Player player, Inventory inventory){
        // normal
        Recipe r = null;
        int amount = 0;

        Recipe input = toRecipe(inventory);
        Recipe:for(Recipe recipe : recipes){
            if(recipe.getTag().equals(Tag.NORMAL)){

                //debug
                System.out.println(String.format("square : %d & %d | shape : %b | total : %d & %d",getSquareSize(recipe),getSquareSize(input),isSameShape(getCoordinateNoAir(recipe),getCoordinateNoAir(input)),getTotal(recipe),getTotal(input)));

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
                //recipeInputResultMap.put(recipe,input);
                amount = recipe.getResult().getAmount();
                r = recipe;
                break Recipe;

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

                //recipeInputResultMap.put(recipe,input);
                amount = recipe.getResult().getAmount();
                r = recipe;
                break Recipe;
            }
        }


        //
        if(r != null){
            // custom recipe found
            Map<Coordinate,Integer> remove = new HashMap<>();
            for(Map.Entry<Coordinate,Matter> entry:r.getCoordinate().entrySet()){
                remove.put(entry.getKey(),1);
            }

            setResultItem(inventory,r,input,player,amount);
//            removeItemAndSetReturnItems(inventory,remove,r.getReturnItems(),player);
        }else{
            // no custom recipe found -> search from vanilla recipes
            new VanillaSearch().main(player,inventory,false);
        }


    }

//    public void batchSearch(Player player,Inventory inventory){
//        // batch
//        Recipe result = null;
//        int massAmount = 0;
//        Map<Coordinate,Integer> remove = new HashMap<>();
//
//        Recipe input = toRecipe(inventory);
//        Recipe:for(Recipe recipe:recipes){
//            for(Matter matter:recipe.getContentsNoAir()){
//                if(!matter.isMass())continue Recipe;
//            }
//
//            //search here
//            // isMass = true -> skip to "check amount" process
//            if(recipe.getTag().equals(Tag.NORMAL)){
//                //normal
//                if(getSquareSize(recipe) != getSquareSize(input))continue;
//                if(!isSameShape(getCoordinateNoAir(recipe),getCoordinateNoAir(input)))continue;
//                //
//                List<Material> massList = getMassItems(recipe); // contains isMass(true) items
//                Recipe modified = getMassRemoved(recipe); // contains Recipe that was isMass items removed.
//                Recipe massRemovedInput = getRecipeRemovedMassItems(input,massList); // input(Recipe) removed mass items
//
//                List<Matter> recipeMatters = modified.getContentsNoAir();
//                List<Matter> inputMatters = massRemovedInput.getContentsNoAir();
//
//                if(recipeMatters.size() != inputMatters.size())continue ;
//                for(int i=0;i<recipeMatters.size();i++){
//                    if(!isSameMatter(recipeMatters.get(i),inputMatters.get(i)))continue Recipe;
//                }
//
//                result = recipe;
//                massAmount = getMinimalAmount(massRemovedInput.getContentsNoAir());
//
//                for(Map.Entry<Coordinate,Matter> entry:recipe.getCoordinate().entrySet()){
//                    if(entry.getValue().getCandidate().get(0).equals(Material.AIR))continue;
//                    remove.put(entry.getKey(),massAmount);
//                }
//
//            }else{
//                //amorphous
//            }
//        }
//
//        if(result != null){
//            // custom recipe found
//            setResultItem(inventory,result,input,player,massAmount);
//        }else{
//            // no custom recipe found -> search from vanilla recipes
//            new VanillaSearch().main(player, inventory,true);
//        }
//
//    }

    public void massSearch(Player player,Inventory inventory){
        // mass (in batch)
        Recipe result = null;
        int massAmount = 0;
        Recipe input = toRecipe(inventory);
        Top:for(Recipe recipe:recipes){
            if(recipe.getTag().equals(Tag.NORMAL)){
                //normal

                //debug
                System.out.println("==========");
                System.out.println(String.format("square : %d & %d | shape : %b",getSquareSize(recipe),getSquareSize(input),isSameShape(getCoordinateNoAir(recipe),getCoordinateNoAir(input))));

                if(getSquareSize(recipe) != getSquareSize(input))continue;
                if(!isSameShape(getCoordinateNoAir(recipe),getCoordinateNoAir(input)))continue;

                // TODO: write here
                // check mass matter is one
                for(int i=0;i<recipe.getContentsNoAir().size();i++){
                    Matter recipeMatter = recipe.getContentsNoAir().get(i);
                    Matter inputMatter = input.getContentsNoAir().get(i);

                    //debug
                    System.out.println(String.format("recipe*M : %d | input*M : %d",recipeMatter.getAmount(),inputMatter.getAmount()));

                    if(recipe.getContentsNoAir().get(i).isMass()){
                        if(inputMatter.getAmount() != 1)continue Top;
                    }

                    if(inputMatter.getAmount() < recipeMatter.getAmount())continue Top;
                }

                result = recipe;
                massAmount  = getMinimalAmount(result,input);

                //debug
                System.out.println("mass amount : "+massAmount);

                break Top;

            }else{
                //amorphous
            }
        }

        if(result != null){
            // custom recipe found
            setResultItem(inventory,result,input,player,massAmount*result.getResult().getAmount());
        }else{
            // not found
            new VanillaSearch().main(player,inventory,true);
        }
    }

    private int getMinimalAmount(Recipe recipe,Recipe input){
        List<Integer> list = new ArrayList<>();
        for(int i=0;i<recipe.getContentsNoAir().size();i++){
            if(recipe.getContentsNoAir().get(i).isMass())continue;
            list.add(input.getContentsNoAir().get(i).getAmount());
        }
        //debug
        System.out.println("minimal amount : "+list);

        if(list.isEmpty())return -1;
        Collections.sort(list);
        return list.get(0);
    }



    private Recipe getRecipeRemovedMassItems(Recipe input,List<Material> list){
        Map<Coordinate,Matter> m = new HashMap<>();
        for(Map.Entry<Coordinate,Matter> entry:input.getCoordinate().entrySet()){
            if(list.containsAll(entry.getValue().getCandidate())){
                Matter mt = new Matter(entry.getValue().getName(),Arrays.asList(Material.AIR),null,0,false);
                m.put(entry.getKey(),mt);
            }else{
                m.put(entry.getKey(),entry.getValue());
            }
        }
        Recipe r = new Recipe(input.getName(),input.getTag().toString(),m,null,null);
        return r;
    }
    private List<Material> getMassItems(Recipe recipe){
        List<Material> list = new ArrayList<>();
        for(Map.Entry<Coordinate,Matter> entry:recipe.getCoordinate().entrySet()){
            if(entry.getValue().isMass())list.addAll(entry.getValue().getCandidate());
        }
        return list;
    }

    private Recipe getMassRemoved(Recipe recipe){
        Map<Coordinate,Matter> m = new HashMap<>();
        for(Map.Entry<Coordinate,Matter> entry:recipe.getCoordinate().entrySet()){
            if(entry.getValue().isMass())m.put(entry.getKey(),new Matter(Arrays.asList(Material.AIR),0));
            else m.put(entry.getKey(),entry.getValue());
        }
        Recipe r = new Recipe(recipe.getName(),recipe.getTag().toString(),m,null,recipe.getResult());
        return r;
    }
    private void setResultItem(Inventory inventory,Recipe recipe,Recipe input,Player player,int amount){
        ItemStack item = null;
        if(allMaterials.contains(recipe.getResult().getNameOrRegex())
        || recipe.getResult().getMatchPoint() == -1
        || !recipe.getResult().getNameOrRegex().contains(",")){
            // result has defined material
            Material m = Material.valueOf(recipe.getResult().getNameOrRegex().toUpperCase());
            item = new ItemStack(m,amount);
            setMetaData(item,recipe.getResult()); //set result itemStack's metadata
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
                if(!matcher.find())return;
                int point = recipe.getResult().getMatchPoint();
                replaced.replace("{R}",matcher.group(point));
                materials.add(replaced);
            }
            Collections.sort(materials);

            Material material = Material.valueOf(list.get(0).toUpperCase());
            item = new ItemStack(material,amount);
        }

        //debug
        System.out.println(String.format("material : %s | amount : %d",item.getType().name(),item.getAmount()));

        whatMaking.put(player.getUniqueId(),item.getType());

        //debug
        if(inventory.getItem(craftingTableResultSlot) == null){
            // empty a result item's slot
            player.getWorld().dropItem(player.getLocation(),item);
        }else{
            if(item.getAmount() > item.getType().getMaxStackSize()){
                // over the max amount
                player.getWorld().dropItem(player.getLocation(),item);
            }else{
                // in the limit
                inventory.setItem(craftingTableResultSlot,item);
            }
        }

        new InventoryUtil().decrementMaterials(inventory,player,getMinimalAmount(recipe,input));

    }

    private void setMetaData(ItemStack item,Result result){
        Map<String,List<String>> metadata = result.getMetadata();
        if(metadata == null || metadata.isEmpty())return;
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
        if(recipe == null)return true;
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
        if(list.isEmpty())return -1;
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

        //debug
//        System.out.println(String.format("xGap : %d | yGap : %d | size(model) : %d | size (reals) : %d",xGap,yGap,models.size(),reals.size()));
//        models.forEach(s->System.out.println(String.format("models(X) : %d | models(Y) : %d",s.getX(),s.getY())));
//        reals.forEach(s->System.out.println(String.format("reals(X) : %d | reals(Y) : %d",s.getX(),s.getY())));

        if(models.size() != reals.size())return false;
        int size = models.size();
        for(int i=1;i<size;i++){

            //debug
            System.out.println(String.format("xGap* : %d | yGap* : %d",models.get(i).getX() - reals.get(i).getX(),models.get(i).getY() - reals.get(i).getY()));

            if(models.get(i).getX() - reals.get(i).getX() != xGap)return false;
            if(models.get(i).getY() - reals.get(i).getY() != yGap)return false;
        }
        return true;
    }

    private Recipe toRecipe(Inventory inventory){
        Recipe recipe = new Recipe();
        for(int y=0;y<craftingTableSize;y++){
            for(int x=0;x<craftingTableSize;x++){
                int i = x+y*9;
                Matter matter = inventory.getItem(i)==null
                        ? new Matter(Arrays.asList(Material.AIR),0)
                        : new Matter(Arrays.asList(inventory.getItem(i).getType()),inventory.getItem(i).getAmount());

                if(inventory.getItem(i) == null)continue;
                matter.setWarp(getEnchantWrap(inventory.getItem(i))); //set enchantments information
                recipe.addCoordinate(x,y,matter);
            }
        }
        return recipe;
    }
}
