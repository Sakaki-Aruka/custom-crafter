package com.github.sakakiaruka.customcrafter.customcrafter.search;

import com.github.sakakiaruka.customcrafter.customcrafter.object.ContainerWrapper;
import com.github.sakakiaruka.customcrafter.customcrafter.object.Matter.EnchantStrict;
import com.github.sakakiaruka.customcrafter.customcrafter.object.Matter.EnchantWrap;
import com.github.sakakiaruka.customcrafter.customcrafter.object.Matter.Matter;
import com.github.sakakiaruka.customcrafter.customcrafter.object.Matter.Potions.PotionStrict;
import com.github.sakakiaruka.customcrafter.customcrafter.object.Matter.Potions.Potions;
import com.github.sakakiaruka.customcrafter.customcrafter.object.Permission.RecipePermission;
import com.github.sakakiaruka.customcrafter.customcrafter.object.Recipe.Coordinate;
import com.github.sakakiaruka.customcrafter.customcrafter.object.Recipe.Recipe;
import com.github.sakakiaruka.customcrafter.customcrafter.object.Recipe.Tag;
import com.github.sakakiaruka.customcrafter.customcrafter.util.*;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;


import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.github.sakakiaruka.customcrafter.customcrafter.SettingsLoad.*;


public class Search {

    private static final String PASS_THROUGH_PATTERN = "^(?i)pass -> ([a-zA-Z_]+)$";

    public void massSearch(Player player,Inventory inventory, boolean isOneCraft){
        // mass (in batch)
        Recipe result = null;
        int massAmount = 0;
        Recipe input = toRecipe(inventory);
        List<ItemStack> interestedItems = getInterestedAreaItems(inventory);

        Top:for(Recipe recipe: RECIPE_LIST){

            if(recipe.hasPermission()){ // permission check
                RecipePermission source = recipe.getPermission();
                if(!new RecipePermissionUtil().containsPermission(player, source)) continue;
            }

            if(recipe.getTag().equals(Tag.NORMAL)){
                //normal
                if(getSquareSize(recipe) != getSquareSize(input))continue;
                if(!isSameShape(getCoordinateNoAir(recipe),getCoordinateNoAir(input)))continue;
                if(!isAllCandidateContains(recipe,input))continue;

                // check mass matter is one
                for(int i=0;i<recipe.getContentsNoAir().size();i++){
                    Matter recipeMatter = recipe.getContentsNoAir().get(i);
                    Matter inputMatter = input.getContentsNoAir().get(i);

                    if (!new ContainerUtil().isPass(interestedItems.get(i), recipeMatter)) continue Top;

                    //(amount one virtual test)
                    Matter recipeOne = recipeMatter.oneCopy();
                    Matter inputOne = inputMatter.oneCopy();

                    if(!isSameMatter(recipeOne,inputOne)) continue Top;
                    if(!(recipeOne.getClass().equals(Potions.class) && inputOne.getClass().equals(Potions.class))) continue;
                    if(!new PotionUtil().isSamePotion((Potions)recipeOne,(Potions) inputOne)) continue Top;

                    //end (amount one virtual test end)

                    if(recipe.getContentsNoAir().get(i).isMass()){
                        if(inputMatter.getAmount() != 1)continue Top;
                    }

                    if(inputMatter.getAmount() < recipeMatter.getAmount())continue Top;
                    if(!getEnchantWrapCongruence(recipeMatter,inputMatter))continue Top; // enchant check
                }


            }else{
                //amorphous
                if(!searchAmorphous(recipe,inventory)) continue;
                if(!getEnchantWrapCongruenceAmorphousWrap(recipe,input))continue;
                if(! new PotionUtil().getPotionsCongruence(recipe, input)) continue;
            }
            result = recipe;
            massAmount  = getMinimalAmount(result,input);
            break;
        }


        if(result != null){
            // custom recipe found
            new InventoryUtil().returnItems(result,inventory,massAmount,player);
            int quantity = (isOneCraft ? 1 : massAmount) * result.getResult().getAmount();
            setResultItem(inventory,result,input,player,quantity,isOneCraft);
        }else{
            // not found
            new VanillaSearch().main(player,inventory,true);
        }
    }

    private boolean searchAmorphous(Recipe recipe, Inventory inventory) {
        Recipe input = toRecipe(inventory);
        if(recipe.getContentsNoAir().size() != input.getContentsNoAir().size()) return false;
        if(!getAllCandidateNoDuplicate(recipe).containsAll(getAllCandidateNoDuplicate(input))) return false;

        List<Matter> massList = getMassOrNotList(recipe,true);
        List<Matter> normalList = getMassOrNotList(recipe,false);
        Map<Matter, Integer> virtual = getVirtual(input);

        int vTotal = 0;
        for (int i : virtual.values()) {
            vTotal += i;
        }

        if (!new ContainerUtil().amorphousContainerCongruence(recipe, input)) return false;

        new InventoryUtil().snatchFromVirtual(virtual,massList,true);
        new InventoryUtil().snatchFromVirtual(virtual,normalList,false);
        if(containsMinus(virtual)) return false;

        int normalListTotal = 0;
        for(Matter matter : normalList) {
            normalListTotal += matter.getAmount();
        }

        return (vTotal - (massList.size() + normalListTotal) == getTotal(input));

    }


    private boolean containsMinus(Map<Matter,Integer> virtual) {
        for(int i : virtual.values()) {
            if(i < 0) return true;
        }
        return false;
    }

    private List<Matter> getMassOrNotList(Recipe recipe, boolean mass) {
        List<Matter> result = new ArrayList<>();
        recipe.getContentsNoAir().forEach(s->{
            if(mass && s.isMass()) result.add(s);
            if(!mass && !s.isMass()) result.add(s);
        });
        return result;
    }

    private Map<Matter, Integer> getVirtual(Recipe recipe) {
        Map<Matter, Integer> result = new HashMap<>();
        for(Matter matter : recipe.getContentsNoAir()) {
            for(Material material : matter.getCandidate()) {
                // virtual data -> amount 0.
                Matter mass = new Matter(Arrays.asList(material),0,true);
                Matter normal = new Matter(Arrays.asList(material),0,false);
                int m = (result.containsKey(mass) ? result.get(mass) : 0) + 1;
                int n = (result.containsKey(normal) ? result.get(normal) : 0) + matter.getAmount();

                // about container data
                Map<Integer, ContainerWrapper> containerElements = matter.containerElementsDeepCopy();
                mass.setContainerWrappers(containerElements); // set container data
                normal.setContainerWrappers(containerElements); // set container data

                result.put(mass,m);
                result.put(normal,n);
            }
        }
        return result;
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

        if(list.isEmpty())return -1;
        Collections.sort(list);

        return list.get(0);
    }

    private Set<Material> getAllCandidateNoDuplicate(Recipe recipe){
        Set<Material> set = new HashSet<>();
        recipe.getContentsNoAir().forEach(s->{
            set.addAll(s.getCandidate());
        });
        return set;
    }


    private void setResultItem(Inventory inventory, Recipe recipe, Recipe input, Player player, int amount, boolean oneCraft){
        ItemStack item = null;
        if (ALL_MATERIALS.contains(recipe.getResult().getNameOrRegex())
        || recipe.getResult().getMatchPoint() == -1
        || !recipe.getResult().getNameOrRegex().contains("@")){
            // result has defined material
            Material m = Material.valueOf(recipe.getResult().getNameOrRegex().toUpperCase());
            item = new ItemStack(m,amount);
            recipe.getResult().setMetaData(item);
            //setMetaData(item,recipe.getResult()); //set result itemStack's metadata
        } else if (recipe.getResult().getNameOrRegex().matches(PASS_THROUGH_PATTERN)) {
            // pass through mode
            // nameOrRegex: pass -> material name (there are only one in the inventory.)
            // example): nameOrRegex: pass -> cobblestone
            Material target;
            try {
                target = Material.valueOf(recipe.getResult().getNameOrRegex().toUpperCase());
            } catch (Exception e) {
                Bukkit.getLogger().warning("[CustomCrafter] pass-through mode failed. (Illegal Material name.)");
                return;
            }
            List<ItemStack> items = new ArrayList<>();
            for (int i=0;i<inventory.getSize();i++) {
                if (inventory.getItem(i) == null) continue;
                if (inventory.getItem(i).getType().equals(target)) items.add(inventory.getItem(i));
            }
            if (items.size() != 1) {
                Bukkit.getLogger().warning("[CustomCrafter] pass-through mode failed. (Same material some where.) ");
                return;
            }

            item = items.get(0);


        }else {
            // not contains -> A result has written by regex pattern.
            List<String> list = Arrays.asList(recipe.getResult().getNameOrRegex().split("@"));

            String p = list.get(0);
            String replaced = list.get(1);
            Pattern pattern = Pattern.compile(p);
            List<String> materials = new ArrayList<>();
            for(Material m:getContainsMaterials(input)){
                String name = m.name();
                Matcher matcher = pattern.matcher(name);

                //if(!matcher.find())continue;
                int point = recipe.getResult().getMatchPoint();
                if(!matcher.find(0))continue;
                if(replaced.contains("{R}"))replaced = replaced.replace("{R}",matcher.group(point));
                materials.add(replaced);
            }
            Collections.sort(materials);

            Material material = Material.valueOf(materials.get(0).toUpperCase());
            item = new ItemStack(material,amount);
            recipe.getResult().setMetaData(item);
            //setMetaData(item,recipe.getResult());
        }

        if(item == null)return;
        if(item.getType().equals(Material.AIR))return;

        WHAT_MAKING.put(player.getUniqueId(),item.getType());

        new ContainerUtil().setRecipeDataContainerToResultItem(item, input, recipe);
        if (recipe.hasUsingContainerValuesMetadata()) new ContainerUtil().setRecipeUsingContainerValueMetadata(inventory, recipe, item);

        if(inventory.getItem(CRAFTING_TABLE_RESULT_SLOT) == null){
            // empty a result item's slot
            InventoryUtil.safetyItemDrop(player, Collections.singletonList(item));
        }else{
            if(item.getAmount() > item.getType().getMaxStackSize()){
                // over the max amount
                InventoryUtil.safetyItemDrop(player, Collections.singletonList(item));
            }else{
                // in the limit
                inventory.setItem(CRAFTING_TABLE_RESULT_SLOT,item);
            }
        }

        new InventoryUtil().decrementMaterials(inventory,oneCraft ? 1 : getMinimalAmount(recipe,input));

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


    public boolean isSameMatter(Matter recipe,Matter input){

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


        // collation with a recipe
        for(Matter matter : recipe.getContentsNoAir()){
            if(!matter.hasWrap())continue;

            int exitCode = 0;
            for(Material material : matter.getCandidate()){
                if(inputVirtual.get(material) == null)continue;
                if(inputVirtual.get(material).isEmpty())continue;
                List<List<EnchantWrap>> list = inputVirtual.get(material);
                if(!new EnchantUtil().containsFromDoubleList(list,matter))continue;

                //debug
                exitCode = 1;
                break;
            }
            if(exitCode == 0)return false;
        }


        return true;
    }

    public boolean getEnchantWrapCongruence(Matter recipe,Matter input){

        if(recipe.getCandidate().get(0).equals(Material.ENCHANTED_BOOK)){
            if(!input.getCandidate().get(0).equals(Material.ENCHANTED_BOOK)) return false;

            for(EnchantWrap wrap : recipe.getWrap()){
                if(wrap.getStrict().equals(EnchantStrict.NOTSTRICT)) continue;
                if(!input.contains(wrap.getEnchant())) return false;
                if(wrap.getStrict().equals(EnchantStrict.ONLYENCHANT)) continue;
                if(wrap.getLevel() != input.getEnchantLevel(wrap.getEnchant())) return false;
            }
            return true;
        }

        if(!input.hasWrap() && recipe.hasWrap())return false;
        if(!recipe.hasWrap())return true; // no target

        for(EnchantWrap wrap : recipe.getWrap()){
            if(wrap.getStrict().equals(EnchantStrict.NOTSTRICT))continue; // not have to check

            Enchantment recipeEnchant = wrap.getEnchant();
            List<Enchantment> enchantList = new ArrayList<>();
            input.getWrap().forEach(s->enchantList.add(s.getEnchant()));
            if(!enchantList.contains(recipeEnchant))return false;

            if(wrap.getStrict().equals(EnchantStrict.ONLYENCHANT))continue; //enchant contains check OK

            int recipeLevel = wrap.getLevel();
            int inputLevel = input.getEnchantLevel(wrap.getEnchant());
            if(recipeLevel != inputLevel)return false; // level check failed
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

            if(models.get(i).getX() - reals.get(i).getX() != xGap)return false;
            if(models.get(i).getY() - reals.get(i).getY() != yGap)return false;
        }
        return true;
    }

    private List<ItemStack> getInterestedAreaItems(Inventory inventory) {
        List<ItemStack> list = new ArrayList<>();
        for (int y = 0; y< CRAFTING_TABLE_SIZE; y++) {
            for (int x = 0; x< CRAFTING_TABLE_SIZE; x++) {
                int i = x+y*9;
                if (inventory.getItem(i) == null) continue;
                list.add(inventory.getItem(i));
            }
        }
        return list;
    }

    private Recipe toRecipe(Inventory inventory){
        Recipe recipe = new Recipe();
        for(int y = 0; y< CRAFTING_TABLE_SIZE; y++){
            for(int x = 0; x< CRAFTING_TABLE_SIZE; x++){
                int i = x+y*9;
                Matter matter = toMatter(inventory,i);
                if(inventory.getItem(i) == null)continue;
                if(inventory.getItem(i).getItemMeta().hasEnchants()) {
                    matter.setWrap(getEnchantWrap(inventory.getItem(i))); //set enchantments information
                }
                // enchanted_book pattern
                if(inventory.getItem(i).getType().equals(Material.ENCHANTED_BOOK)){
                    ItemStack item = inventory.getItem(i);
                    if(((EnchantmentStorageMeta)item.getItemMeta()).getStoredEnchants().isEmpty()) continue;
                    for(Map.Entry<Enchantment,Integer> entry : ((EnchantmentStorageMeta)item.getItemMeta()).getStoredEnchants().entrySet()){
                        Enchantment enchant = entry.getKey();
                        int level = entry.getValue();
                        EnchantStrict strict = EnchantStrict.INPUT;
                        EnchantWrap wrap = new EnchantWrap(level,enchant,strict);
                        matter.addWrap(wrap);
                    }
                }
                new ContainerUtil().setContainerDataItemStackToMatter(inventory.getItem(i), matter);

                recipe.addCoordinate(x,y,matter);
            }
        }
        return recipe;
    }

    private Matter toMatter(Inventory inventory,int slot){
        Matter matter;
        if(inventory.getItem(slot) == null){
            matter = new Matter(Arrays.asList(Material.AIR),0);
        }else if(new PotionUtil().isPotion(inventory.getItem(slot).getType())){
            matter = new Potions(inventory.getItem(slot), PotionStrict.INPUT);
        }else{
            matter = new Matter(inventory.getItem(slot));
        }
        return matter;
    }
}
