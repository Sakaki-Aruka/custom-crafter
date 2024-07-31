package com.github.sakakiaruka.customcrafter.customcrafter.search;

import com.github.sakakiaruka.customcrafter.customcrafter.CustomCrafter;
import com.github.sakakiaruka.customcrafter.customcrafter.object.Matter.EnchantStrict;
import com.github.sakakiaruka.customcrafter.customcrafter.object.Matter.EnchantWrap;
import com.github.sakakiaruka.customcrafter.customcrafter.object.Matter.Matter;
import com.github.sakakiaruka.customcrafter.customcrafter.object.Matter.Potions.PotionStrict;
import com.github.sakakiaruka.customcrafter.customcrafter.object.Matter.Potions.Potions;
import com.github.sakakiaruka.customcrafter.customcrafter.object.Permission.RecipePermission;
import com.github.sakakiaruka.customcrafter.customcrafter.object.Recipe.Container.RecipeContainer;
import com.github.sakakiaruka.customcrafter.customcrafter.object.Recipe.Coordinate;
import com.github.sakakiaruka.customcrafter.customcrafter.object.Recipe.Recipe;
import com.github.sakakiaruka.customcrafter.customcrafter.object.Recipe.Tag;
import com.github.sakakiaruka.customcrafter.customcrafter.util.*;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.github.sakakiaruka.customcrafter.customcrafter.SettingsLoad.*;


public class Search {

    private static final String PASS_THROUGH_PATTERN = "^(?i)pass -> ([a-zA-Z_]+)$";
    public static final Map<Coordinate, List<Coordinate>> AMORPHOUS_NULL_ANCHOR = new HashMap<>() {{
        put(Coordinate.NULL_ANCHOR, Collections.emptyList());
    }};

    public static final Map<Coordinate, List<Coordinate>> AMORPHOUS_NON_REQUIRED_ANCHOR = new HashMap<>() {{
        put(Coordinate.NON_REQUIRED_ANCHOR, Collections.emptyList());
    }};

    public void massSearch(Player player,Inventory inventory, boolean isOneCraft){
        // mass (in batch)
        Recipe result = null;
        int massAmount = 0;
        Recipe input = toRecipe(inventory);
        List<ItemStack> interestedItems = getInterestedAreaItems(inventory);
        int itemContainedSlots = input.getContentsNoAir().size();
        if (itemContainedSlots == 0) return;
        if (ITEM_PLACED_SLOTS_RECIPE_MAP.get(itemContainedSlots) == null || ITEM_PLACED_SLOTS_RECIPE_MAP.get(itemContainedSlots).isEmpty()) {
            new VanillaSearch().main(player, inventory, isOneCraft);
            return;
        }

        int judge = 0;

        for (Recipe recipe: ITEM_PLACED_SLOTS_RECIPE_MAP.get(itemContainedSlots)){

            if(recipe.hasPermission()){ // permission check
                RecipePermission source = recipe.getPermission();
                if(!RecipePermissionUtil.containsPermission(player, source)) continue;
            }

            if (recipe.getTag().equals(Tag.NORMAL) && isMatchNormal(interestedItems ,recipe, input)) judge++;
            if (recipe.getTag().equals(Tag.AMORPHOUS) && isMatchAmorphous(recipe, input)) judge++;

            if (judge == 0) continue;
            result = recipe;
            massAmount = getMinimalAmount(result, input);

            break;
        }


        if (result != null) {
            // custom recipe found
            InventoryUtil.returnItems(result,inventory,massAmount,player);
            int quantity;
            if (result.getTag().equals(Tag.NORMAL)) {
                quantity = (isOneCraft ? 1 : input.normalRatioWith(result)) * result.getResult().getAmount();
            } else {
                quantity = (isOneCraft ? 1 : input.amorphousRatioWith(result)) * result.getResult().getAmount();
            }
            setResultItem(inventory,result,input,player,quantity,isOneCraft);
        } else {
            // not found
            new VanillaSearch().main(player,inventory,isOneCraft);
        }
    }

    private static boolean containsMoreThanOneAmountMatter(Recipe recipe) {
        for (Matter m : recipe.getContentsNoAir()) {
            if (1 < m.getAmount()) return true;
        }
        return false;
    }

    private boolean isMatchNormal(List<ItemStack> interestedItems,Recipe recipe, Recipe input) {
        if (getSquareSize(recipe.getCoordinateList()) != getSquareSize(input.getCoordinateList())) return false;
        if (!isSameShape(getCoordinateNoAir(recipe), getCoordinateNoAir(input))) return false;
        if (!isAllCandidateContains(recipe,input)) return false;

        // check mass matter is one
        for (int i = 0; i<recipe.getContentsNoAir().size(); i++){
            Matter recipeMatter = recipe.getContentsNoAir().get(i);
            Matter inputMatter = input.getContentsNoAir().get(i);

            //debug
            if (recipeMatter.getAmount() != 1 && recipeMatter.getAmount() != inputMatter.getAmount()) return false;

            if (!ContainerUtil.isPass(interestedItems.get(i), recipeMatter)) return false;
            if (inputMatter.getAmount() < recipeMatter.getAmount()) return false;
            //(amount one virtual test)
            Matter recipeOne = recipeMatter.oneCopy();
            Matter inputOne = inputMatter.oneCopy();

            if (!isSameMatter(recipeOne,inputOne)) return false;
            if (recipeOne.getClass().equals(Potions.class)) {
                if (!inputOne.getClass().equals(Potions.class)) return false;
                if(!PotionUtil.isSamePotion((Potions)recipeOne, (Potions) inputOne)) return false;
            }
            //end (amount one virtual test end)

            if (recipe.getContentsNoAir().get(i).isMass()) {
                if(inputMatter.getAmount() != 1) return false;
            }
            if (!getEnchantWrapCongruence(recipeMatter, inputMatter)) return false; // enchant check
        }
        return true;
    }

    private boolean isMatchAmorphous(Recipe recipe, Recipe input) {
        List<Map<Coordinate, List<Coordinate>>> temp = new ArrayList<>();
        Map<Coordinate, List<Coordinate>> enchant = EnchantUtil.amorphous(recipe, input);
        Map<Coordinate, List<Coordinate>> container = ContainerUtil._amorphous(recipe, input);
        Map<Coordinate,List<Coordinate>> candidate = InventoryUtil.amorphous(recipe, input);
        Map<Coordinate, List<Coordinate>> potion = PotionUtil.amorphous(recipe, input);

        Map<Coordinate, Map<String, Boolean>> rStatus = InventoryUtil.getEachMatterStatus(recipe);
        Map<Coordinate, Map<String, Boolean>> iStatus = InventoryUtil.getEachMatterStatus(input);

        if (!enchant.equals(AMORPHOUS_NON_REQUIRED_ANCHOR)) temp.add(enchant);
        if (!container.equals(AMORPHOUS_NON_REQUIRED_ANCHOR)) temp.add(container);
        if (!potion.equals(AMORPHOUS_NON_REQUIRED_ANCHOR)) temp.add(potion);

        temp.add(candidate);
        Map<Coordinate, Coordinate> relate;
        if ((relate = InventoryUtil.combination(temp)).isEmpty()) return false;

        for (Map.Entry<Coordinate, Coordinate> entry : relate.entrySet()) {
            Coordinate r = entry.getKey();
            Coordinate i = entry.getValue();
            if (!rStatus.get(r).equals(iStatus.get(i))) return false;
        }

        return true;
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


    private void setResultItem(Inventory inventory, Recipe recipe, Recipe input, Player player, int amount, boolean oneCraft){
        ItemStack item = null;
        List<PersistentDataContainer> pdcList = new ArrayList<>();
        for (Coordinate coordinate : input.getHasPDCItemList()) {
            int slot = coordinate.getY() * 9 + coordinate.getX();
            pdcList.add(inventory.getItem(slot).getItemMeta().getPersistentDataContainer());
        }
        Map<String, String> inputContainerData = ContainerUtil.getData(pdcList);
        inputContainerData.put("$PLAYER_NAME$", player.getName());
        inputContainerData.put("$PLAYER_UUID$", player.getUniqueId().toString());
        inputContainerData.put("$PLAYER_CURRENT_WORLD$", player.getWorld().getName());
        inputContainerData.put("$PLAYER_CURRENT_X$", String.valueOf(player.getLocation().getX())); // double
        inputContainerData.put("$PLAYER_CURRENT_Y$", String.valueOf(player.getLocation().getY())); // double
        inputContainerData.put("$PLAYER_CURRENT_Z$", String.valueOf(player.getLocation().getZ())); // double
        inputContainerData.put("$PLAYER_CURRENT_Xi$", String.valueOf(player.getLocation().getBlockX())); // int
        inputContainerData.put("$PLAYER_CURRENT_Yi$", String.valueOf(player.getLocation().getBlockY())); // int
        inputContainerData.put("$PLAYER_CURRENT_Zi$", String.valueOf(player.getLocation().getBlockZ())); // int
        inputContainerData.put("$PLAYER_CURRENT_PITCH$", String.valueOf(player.getLocation().getPitch())); // float
        inputContainerData.put("$PLAYER_CURRENT_YAW$", String.valueOf(player.getLocation().getYaw())); // float
        inputContainerData.put("$PLAYER_IN_WATER$", String.valueOf(player.isInWater())); // true|false
        inputContainerData.put("$PLAYER_CURRENT_FOOD_LEVEL$", String.valueOf(player.getFoodLevel()));
        inputContainerData.put("$PLAYER_PING$", String.valueOf(player.getPing()));
        inputContainerData.put("$PLAYER_EXP$", String.valueOf(player.getExp()));
        inputContainerData.put("$PLAYER_EXP_LEVEL$", String.valueOf(player.getLevel()));
        inputContainerData.put("$PLAYER_DISPLAYED_NAME$", ((TextComponent) player.displayName()).content());
        inputContainerData.put("$PLAYER_MAXIMUM_NO_DAMAGE_TICKS$", String.valueOf(player.getMaximumNoDamageTicks()));
        inputContainerData.put("$PLAYER_CLIENT_BRAND_NAME$", Objects.toString(player.getClientBrandName()));
        inputContainerData.put("$PLAYER_IS_FLYING$", String.valueOf(player.isFlying()));
        inputContainerData.put("$PLAYER_CURRENT_GAME_MODE$", player.getGameMode().name());
        inputContainerData.put("$PLAYER_IN_RAIN$", String.valueOf(player.isInRain()));
        inputContainerData.put("$PLAYER_IN_LAVA$", String.valueOf(player.isInLava()));
        inputContainerData.put("$PLAYER_FACING$", player.getFacing().name());
        inputContainerData.put("$PLAYER_CURRENT_HEALTH$", String.valueOf(player.getHealth()));
        inputContainerData.put("$RECIPE_NAME$", recipe.getName());

        if (recipe.getTag().equals(Tag.NORMAL)) {
            List<ItemStack> items = getInterestedAreaItems(inventory);
            // key: "slot#plugin_name#variable_name"
            // slot: 0 ~ 36
            // plugin_name: lowercase alphanumeric
            // variable_name: variable_name
            int index = 0;

            List<PersistentDataType<?, ?>> pList = List.of(
                    PersistentDataType.DOUBLE,
                    PersistentDataType.FLOAT,
                    PersistentDataType.STRING,
                    PersistentDataType.LONG,
                    PersistentDataType.INTEGER,
                    PersistentDataType.SHORT,
                    PersistentDataType.BYTE,
                    PersistentDataType.BOOLEAN
            );

            for (ItemStack i : items) {
                if (i.getType().equals(Material.AIR) || i.getItemMeta() == null) continue;  // PersistentDataHolder check
                PersistentDataContainer container = i.getItemMeta().getPersistentDataContainer();
                for (Plugin plugin : Bukkit.getPluginManager().getPlugins()) {
                    final String pluginName = plugin.getName();
                    for (NamespacedKey namespacedKey : container.getKeys()) {
                        if (!namespacedKey.getNamespace().equals(pluginName.toLowerCase())) continue;
                        for (PersistentDataType<?, ?> pdt : pList) {
                            if (!container.has(namespacedKey, pdt) || !container.has(namespacedKey, pdt)) continue;
                            final String containerContent = Objects.requireNonNull(container.get(namespacedKey, pdt)).toString();
                            final String containerKey = String.format("$%d#%s#%s$", index, pluginName.toLowerCase(), namespacedKey.getKey());
                            inputContainerData.put(containerKey, containerContent);
                        }
                    }
                }
                index++;
            }
        }

        if (ALL_MATERIALS.contains(recipe.getResult().getNameOrRegex())
        && recipe.getResult().getMatchPoint() == -1
        && !recipe.getResult().getNameOrRegex().contains("@")) {
            // result has defined material
            Material m = Material.valueOf(recipe.getResult().getNameOrRegex().toUpperCase());
            item = new ItemStack(m, amount);
        } else if (recipe.getResult().getNameOrRegex().matches(PASS_THROUGH_PATTERN)) {
            // pass through mode
            // nameOrRegex: pass -> material name (there are only one in the inventory.)
            // example): nameOrRegex: pass -> cobblestone
            Material target;
            try {
                Matcher m = Pattern.compile(PASS_THROUGH_PATTERN).matcher(recipe.getResult().getNameOrRegex());
                if (!m.matches()) {
                    CustomCrafter.getInstance().getLogger().warning("pass-through mode failed. (Illegal Material name.)");
                    return;
                }
                target = Material.valueOf(m.group(1).toUpperCase());
            } catch (Exception e) {
                CustomCrafter.getInstance().getLogger().warning("pass-through mode failed. (Illegal Material name.)");
                return;
            }
            List<ItemStack> items = new ArrayList<>();
            for (int i=0;i<inventory.getSize();i++) {
                if (inventory.getItem(i) == null) continue;
                if (inventory.getItem(i).getType().equals(target)) items.add(inventory.getItem(i));
            }
            if (items.size() != 1) {
                CustomCrafter.getInstance().getLogger().warning("pass-through mode failed. (Same material some where.) ");
                return;
            }

            item = items.get(0);

        } else {
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
        }

        if(item == null)return;
        if(item.getType().equals(Material.AIR))return;

        WHAT_MAKING.put(player.getUniqueId(),item.getType());
        inputContainerData.put("$RESULT_MATERIAL$", item.getType().name());
        inputContainerData.put("$RESULT_AMOUNT$", String.valueOf(item.getAmount()));
        for (RecipeContainer container : recipe.getContainers()) {
            container.run(inputContainerData, item);
        }

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

        if (recipe.getTag().equals(Tag.NORMAL)) InventoryUtil.decrementMaterialsForNormalRecipe(inventory, input, recipe, oneCraft);
        else InventoryUtil.decrementMaterials(inventory, input, recipe, oneCraft);

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

    public boolean getEnchantWrapCongruence(Matter recipe,Matter input){


        if(!input.hasWrap() && recipe.hasWrap()) return false;
        if(!recipe.hasWrap())return true; // no target

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



    public static int getSquareSize(List<Coordinate> list){
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
                if (inventory.getItem(i) == null || inventory.getItem(i).getType().equals(Material.AIR)) continue;
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
                recipe.addCoordinate(x,y,matter);
            }
        }
        return recipe;
    }

    private Matter toMatter(Inventory inventory,int slot){
        Matter matter;
        ItemStack item = inventory.getItem(slot);
        if(item == null || item.getType().equals(Material.AIR)){
            return new Matter(List.of(Material.AIR),0);
        }else if(PotionUtil.isPotion(item.getType())){
            matter = new Potions(inventory.getItem(slot), PotionStrict.INPUT);
        }else{
            matter = new Matter(item);
        }

        if (!item.getItemMeta().getPersistentDataContainer().isEmpty()) return matter;

        PersistentDataContainer pdc = item.getItemMeta().getPersistentDataContainer();
        matter.setPDC(pdc);

        return matter;
    }
}
