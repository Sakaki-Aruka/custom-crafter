package com.github.sakakiaruka.customcrafter.customcrafter.search;

import com.github.sakakiaruka.customcrafter.customcrafter.object.Matter.EnchantStrict;
import com.github.sakakiaruka.customcrafter.customcrafter.object.Matter.EnchantWrap;
import com.github.sakakiaruka.customcrafter.customcrafter.object.Matter.Matter;
import com.github.sakakiaruka.customcrafter.customcrafter.object.Recipe.Coordinate;
import com.github.sakakiaruka.customcrafter.customcrafter.object.Recipe.Recipe;
import com.github.sakakiaruka.customcrafter.customcrafter.object.Recipe.Tag;
import com.github.sakakiaruka.customcrafter.customcrafter.object.Result.MetadataType;
import com.github.sakakiaruka.customcrafter.customcrafter.object.Result.Result;
import com.github.sakakiaruka.customcrafter.customcrafter.util.EnchantUtil;
import com.github.sakakiaruka.customcrafter.customcrafter.util.InventoryUtil;
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

import static com.github.sakakiaruka.customcrafter.customcrafter.SettingsLoad.*;


public class Search {

    public void main(Player player, Inventory inventory){
        // normal
        Recipe r = null;
        int amount = 0;

        Recipe input = toRecipe(inventory);
        Recipe:for(Recipe recipe : recipes){

//            //debug
//            recipe.getContentsNoAir().forEach(s->System.out.println(s.info()));
//            input.getContentsNoAir().forEach(s->System.out.println(s.info()));

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

                amount = recipe.getResult().getAmount();
                r = recipe;
                break;

            }else{
                //amorphous
                if(getTotal(recipe) != getTotal(input))continue;
                if(!getAllCandidateNoDuplicate(recipe).containsAll(getAllCandidateNoDuplicate(input)))continue;


                //debug
                if(!getEnchantWrapCongruenceAmorphousWrap(recipe,input))continue;
                //debug finish

                int inputTotal = 0;
                Map<Material,Integer> relation = new HashMap<>();
                //TODO fix here

                for(Matter matter : input.getContentsNoAir()){
                    Material material = matter.getCandidate().get(0);
                    if(!relation.containsKey(material)) relation.put(material,0);
                    int i = relation.get(material) + matter.getAmount();
                    inputTotal+=i;

                    //debug
                    System.out.println(String.format("input total : %d | i : %d",inputTotal,i));

                }

                int virtualTotal = 0;
                Map<Material,Integer> virtual = new HashMap<>();

//                //debug
//                recipe.getContentsNoAir().forEach(s->{
//                    System.out.println(String.format("matter : %s",s.info()));
//                });

                for(Matter matter : recipe.getContentsNoAir()){
                    final boolean mass = matter.isMass();
                    final int quantity = matter.getAmount();
                    for(Material material : matter.getCandidate()){
                        int i = mass ? 1 : quantity;
                        if(virtual.containsKey(material)) i+= virtual.get(material);
                        virtual.put(material,i);
                        virtualTotal+=i;
                    }
                }

                int ideal = virtualTotal - inputTotal;
                for(Matter matter:input.getContentsNoAir()){
                    Material material = matter.getCandidate().get(0);
                    int i = virtual.get(material) - matter.getAmount();
                    if(i < 0)continue Recipe;
                    virtual.put(material,i);
                }

                int temp = 0;
                for(int i:virtual.values())temp+=i;
                if(ideal != temp)continue;

                amount = recipe.getResult().getAmount();
                r = recipe;
                break;
            }
        }

        if(r != null){
            // custom recipe found
            Map<Coordinate,Integer> remove = new HashMap<>();
            for(Map.Entry<Coordinate,Matter> entry:r.getCoordinate().entrySet()){
                remove.put(entry.getKey(),1);
            }

            new InventoryUtil().returnItems(r,inventory,1,player);
            setResultItem(inventory,r,input,player,amount);
        }else{
            // no custom recipe found -> search from vanilla recipes
            new VanillaSearch().main(player,inventory,false);
        }


    }

    public void massSearch(Player player,Inventory inventory){
        // mass (in batch)
        Recipe result = null;
        int massAmount = 0;
        Recipe input = toRecipe(inventory);
        Top:for(Recipe recipe:recipes){
            if(recipe.getTag().equals(Tag.NORMAL)){
                //normal
                if(getSquareSize(recipe) != getSquareSize(input))continue;
                if(!isSameShape(getCoordinateNoAir(recipe),getCoordinateNoAir(input)))continue;
                if(!isAllCandidateContains(recipe,input))continue;

                // check mass matter is one
                for(int i=0;i<recipe.getContentsNoAir().size();i++){
                    Matter recipeMatter = recipe.getContentsNoAir().get(i);
                    Matter inputMatter = input.getContentsNoAir().get(i);

                    //debug
                    System.out.println(String.format("recipe : %b | input : %b",recipeMatter.hasWrap(),inputMatter.hasWrap()));

                    if(recipe.getContentsNoAir().get(i).isMass()){
                        if(inputMatter.getAmount() != 1)continue Top;
                    }

                    if(inputMatter.getAmount() < recipeMatter.getAmount())continue Top;
                    if(!getEnchantWrapCongruence(recipeMatter,inputMatter))continue Top; // enchant check
                }

                result = recipe;
                massAmount  = getMinimalAmount(result,input);

                //debug
                System.out.println("mass amount : "+massAmount);

                break;

            }else{
                //amorphous

                if(recipe.getContentsNoAir().size() != input.getContentsNoAir().size())continue;
                if(!getAllCandidateNoDuplicate(recipe).containsAll(getAllCandidateNoDuplicate(input)))continue;

                int inputTotal = 0;
                Map<Material,Integer> relation = new HashMap<>();
                input.getContentsNoAir().forEach(s->{
                    Material material = s.getCandidate().get(0);
                    if(!relation.containsKey(s.getCandidate().get(0))) relation.put(material,0);
                    int i= relation.get(s.getCandidate().get(0)) + s.getAmount();
                    relation.put(material,i);
                    increment(inputTotal,s.getAmount());
                });

                int virtualTotal = 0;
                int massVirtualTotal = 0;
                Map<Material,Integer> virtual = new HashMap<>();
                Map<Material,Integer> massVirtual = new HashMap<>();
                recipe.getContentsNoAir().forEach(s->{
                    s.getCandidate().forEach(t->{
                        if(!s.isMass()){
                            // no mass
                            int i = s.getAmount();
                            if(virtual.containsKey(t)) i += virtual.get(t);
                            virtual.put(t,i);
                            increment(virtualTotal,i);
                        }else{
                            // mass
                            int i = 1;
                            if(massVirtual.containsKey(t)) i += massVirtual.get(t);
                            massVirtual.put(t,i);
                            increment(massVirtualTotal,1);
                        }
                    });
                });

                if(inputTotal < virtualTotal)continue;

                for(Matter matter:input.getContentsNoAir()){
                    Material material = matter.getCandidate().get(0);
                    if(massVirtual.containsKey(material)){
                        massVirtual.put(material,massVirtual.get(material) - 1);
                        continue;
                    }
                    int i = virtual.get(material) - matter.getAmount();
                    virtual.put(material,i);
                }

                for(int i:massVirtual.values()) if(i != 0)continue Top;

                int recipeTotal = 0;
                for(Matter m : recipe.getContentsNoAir()){
                    if(m.isMass())continue;
                    recipeTotal += m.getAmount();
                }

                int inputAmount = inputTotal - massVirtualTotal;
                if(inputAmount % recipeTotal != 0)continue;

                //debug
                System.out.println("mass amount (amorphous) : "+getMinimalAmount(recipe,input));

                //debug
                if(!getEnchantWrapCongruenceAmorphousWrap(recipe,input))continue;

                result = recipe;
                massAmount = getMinimalAmount(result,input);
                break;

            }
        }

        //debug
        String name = result == null ? "null" : result.getName() ;
        int amount = massAmount == 0 ? -1 : massAmount;
        System.out.println(String.format("recipe name : %s | mass Amount : %d",name,amount));

        if(result != null){
            // custom recipe found
            new InventoryUtil().returnItems(result,inventory,massAmount,player);
            setResultItem(inventory,result,input,player,massAmount*result.getResult().getAmount());
        }else{
            // not found
            new VanillaSearch().main(player,inventory,true);
        }
    }

    private boolean isAllCandidateContains(Recipe recipe,Recipe input){
        for(int i=0;i<recipe.getContentsNoAir().size();i++){
            List<Material> matters = recipe.getContentsNoAir().get(i).getCandidate();
            Material material = input.getContentsNoAir().get(i).getCandidate().get(0);
            if(!matters.contains(material))return false;
        }
        return true;
    }

    private int getMinimalAmount(Recipe recipe,Recipe input){
        Set<Material> set = recipe.getMassMaterialSet();
        List<Integer> list = new ArrayList<>();
        for(Matter matter : input.getContentsNoAir()){
            if(set.contains(matter.getCandidate().get(0)))continue; // input matter is Mass
            list.add(matter.getAmount());
        }

        //debug
        System.out.println(String.format("amount list (amorphous) : %s",list));
        System.out.println(String.format("mass material set : %s",set));

        if(list.isEmpty())return -1;
        Collections.sort(list);

        //debug
        System.out.println(String.format("amount list : %s | minimal amount : %d",list,list.get(0)));

        return list.get(0);
    }

    private Set<Material> getAllCandidateNoDuplicate(Recipe recipe){
        Set<Material> set = new HashSet<>();
        recipe.getContentsNoAir().forEach(s->{
            set.addAll(s.getCandidate());
        });
        return set;
    }


    private void setResultItem(Inventory inventory,Recipe recipe,Recipe input,Player player,int amount){
        ItemStack item = null;
        if(allMaterials.contains(recipe.getResult().getNameOrRegex())
        || recipe.getResult().getMatchPoint() == -1
        || !recipe.getResult().getNameOrRegex().contains("@")){
            // result has defined material
            Material m = Material.valueOf(recipe.getResult().getNameOrRegex().toUpperCase());
            item = new ItemStack(m,amount);
            setMetaData(item,recipe.getResult()); //set result itemStack's metadata
        }else{
            // not contains -> A result has written by regex pattern.
            List<String> list = Arrays.asList(recipe.getResult().getNameOrRegex().split("@"));

            //debug
//            System.out.println(String.format("list : %s",list));

            String p = list.get(0);
            String replaced = list.get(1);
            Pattern pattern = Pattern.compile(p);
            List<String> materials = new ArrayList<>();
            for(Material m:getContainsMaterials(input)){
                String name = m.name();
                Matcher matcher = pattern.matcher(name);

//                //debug
//                System.out.println(String.format("replaced : %s | name : %s | matcher : %b | point : %d",replaced,name,matcher.find(),recipe.getResult().getMatchPoint()));

                //if(!matcher.find())continue;
                int point = recipe.getResult().getMatchPoint();

//                //debug
//                System.out.println("point : "+point);
//                System.out.println("materials : "+materials);

                if(!matcher.find(0))continue;

                if(replaced.contains("{R}"))replaced = replaced.replace("{R}",matcher.group(point));

                materials.add(replaced);
            }
            Collections.sort(materials);

//            //debug
//            System.out.println(String.format("materials : %s",materials));

            Material material = Material.valueOf(materials.get(0).toUpperCase());
            item = new ItemStack(material,amount);
            setMetaData(item,recipe.getResult());
        }

//        //debug
//        System.out.println(String.format("material : %s | amount : %d",item.getType().name(),item.getAmount()));
        if(item == null)return;
        if(item.getType().equals(Material.AIR))return;

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
        Map<MetadataType,List<String>> metadata = result.getMetadata();

        //debug
        System.out.println(String.format("metadata : %s",metadata == null ? "null" : metadata));

        if(metadata == null || metadata.isEmpty())return;
        ItemMeta meta = item.getItemMeta();
        for(Map.Entry<MetadataType,List<String>> e:metadata.entrySet()){
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

//            //debug
//            System.out.println(String.format("key : %s | value : %s | value size : %d",e.getKey(),e.getValue(),e.getValue().size()));

            MetadataType type = e.getKey();
            List<String> content = e.getValue();

            if(type.equals(MetadataType.LORE))meta.setLore(content);
            if(type.equals(MetadataType.DISPLAYNAME))meta.setDisplayName(content.get(0));
            if(type.equals(MetadataType.ENCHANTMENT)){
                for(String s:content){
                    List<String> l = Arrays.asList(s.split(","));
                    Enchantment enchant = Enchantment.getByName(l.get(0).toUpperCase());
                    int level = Integer.valueOf(l.get(1));
                    meta.addEnchant(enchant,level,true);
                }

//                //debug
//                System.out.println(String.format("map type : %s",meta.getEnchants().getClass().getSimpleName()));

            }
            if(type.equals(MetadataType.ITEMFLAG)) content.forEach(s->meta.addItemFlags(ItemFlag.valueOf(s.toUpperCase())));
            if(type.equals(MetadataType.UNBREAKABLE))meta.setUnbreakable(Boolean.valueOf(content.get(0)));
            if(type.equals(MetadataType.CUSTOMMODELDATA))meta.setCustomModelData(Integer.valueOf(content.get(0)));
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


    private boolean isSameMatter(Matter recipe,Matter input){

//        //debug
//        System.out.println(String.format("candidate : %b | amount : %d & %d | wrap : %b",recipe.getCandidate().containsAll(input.getCandidate()),recipe.getAmount(),input.getAmount(),getEnchantWrapCongruence(recipe,input)));

        if(!recipe.getCandidate().containsAll(input.getCandidate()))return false;
        if(recipe.getAmount() != input.getAmount())return false;
        if(!getEnchantWrapCongruence(recipe,input))return false;
        return true;
    }


    private boolean getEnchantWrapCongruenceAmorphousWrap(Recipe recipe,Recipe input){
        Map<Material,List<List<EnchantWrap>>> inputVirtual = new HashMap<>();
        for(Matter matter : input.getContentsNoAir()){
            if(!matter.hasWrap())continue;
            Material material = matter.getCandidate().get(0);
            if(inputVirtual.get(material) == null)inputVirtual.put(material,new ArrayList<>());
            List<EnchantWrap> wrap = matter.getWrap();
            inputVirtual.get(material).add(wrap);
        }

        //if(inputVirtual.isEmpty())return false;
        //debug
        System.out.println("=");
        System.out.println(String.format("inputVirtual : %s",inputVirtual));
        inputVirtual.entrySet().forEach(s->{
            System.out.println("==");
            System.out.println(String.format("key material : %s",s.getKey().name()));
            s.getValue().forEach(t->{
                System.out.println("===");
                t.forEach(u->System.out.println(String.format("u loop : %s",u.info())));
            });
        });

        // collation with a recipe
        for(Matter matter : recipe.getContentsNoAir()){
            if(!matter.hasWrap())continue;

            int exitCode = 0;
            for(Material material : matter.getCandidate()){
                if(inputVirtual.get(material) == null)continue;
                if(inputVirtual.get(material).isEmpty())continue;
                List<List<EnchantWrap>> list = inputVirtual.get(material);
                if(!new EnchantUtil().containsFromDoubleList(list,matter))continue;
                //TODO : Write here

                //debug
                System.out.println(String.format("inputV : %s | list : %s | congruence : %b",inputVirtual,list,new EnchantUtil().containsFromDoubleList(list,matter)));

                //debug
                exitCode = 1;
                break;
            }
            if(exitCode == 0)return false;
        }

        //debug
        return true;
    }

    public boolean getEnchantWrapCongruence(Matter recipe,Matter input){

        //debug
        if(recipe.hasWrap())recipe.getWrap().forEach(s->System.out.println(s.info()));
        else System.out.println("recipe has not EnchantWrap");
        System.out.println("recipe ↑ --- input ↓");
        if(input.hasWrap())input.getWrap().forEach(s->System.out.println(s.info()));
        else System.out.println("input has not EnchantWrap");

        if(!input.hasWrap() && recipe.hasWrap())return false;
        if(!recipe.hasWrap())return true; // no target

        for(EnchantWrap wrap : recipe.getWrap()){
            if(wrap.getStrict().equals(EnchantStrict.NOTSTRICT))continue; // not have to check

//            //debug
//            System.out.println("not strict : OK");

            Enchantment recipeEnchant = wrap.getEnchant();
            List<Enchantment> enchantList = new ArrayList<>();
            input.getWrap().forEach(s->enchantList.add(s.getEnchant()));
            if(!enchantList.contains(recipeEnchant))return false;

//            //debug
//            System.out.println("contains : OK");

            if(wrap.getStrict().equals(EnchantStrict.ONLYENCHANT))continue; //enchant contains check OK

//            //debug
//            System.out.println("only enchant : OK");

            int recipeLevel = wrap.getLevel();
            int inputLevel = input.getEnchantLevel(wrap.getEnchant());
            if(recipeLevel != inputLevel)return false; // level check failed

//            //debug
//            System.out.println("strict : OK");

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
        EnchantStrict strict = EnchantStrict.INPUT;
        for(Map.Entry<Enchantment,Integer> entry:map.entrySet()){
            Enchantment enchant = entry.getKey();
            int level = entry.getValue();
            EnchantWrap wrap = new EnchantWrap(level,enchant,strict);
            list.add(wrap);

            //debug
            System.out.println("enchant info -> "+wrap.info());

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
                if(inventory.getItem(i).getItemMeta().hasEnchants()) {
                    matter.setWarp(getEnchantWrap(inventory.getItem(i))); //set enchantments information
                }
                recipe.addCoordinate(x,y,matter);
            }
        }
        return recipe;
    }

    private void increment(int input,int plus){
        input = input + plus;
    }
}
