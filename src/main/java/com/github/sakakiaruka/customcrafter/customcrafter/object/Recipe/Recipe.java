package com.github.sakakiaruka.customcrafter.customcrafter.object.Recipe;

import com.github.sakakiaruka.customcrafter.customcrafter.object.Matter.EnchantStrict;
import com.github.sakakiaruka.customcrafter.customcrafter.object.Matter.EnchantWrap;
import com.github.sakakiaruka.customcrafter.customcrafter.object.Matter.Matter;
import com.github.sakakiaruka.customcrafter.customcrafter.object.Matter.Potions.Potions;
import com.github.sakakiaruka.customcrafter.customcrafter.object.Permission.RecipePermission;
import com.github.sakakiaruka.customcrafter.customcrafter.object.Recipe.Container.RecipeContainer;
import com.github.sakakiaruka.customcrafter.customcrafter.object.Result.Result;
import com.github.sakakiaruka.customcrafter.customcrafter.util.ContainerUtil;
import com.github.sakakiaruka.customcrafter.customcrafter.util.EnchantUtil;
import com.github.sakakiaruka.customcrafter.customcrafter.util.InventoryUtil;
import com.github.sakakiaruka.customcrafter.customcrafter.util.PotionUtil;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

import static com.github.sakakiaruka.customcrafter.customcrafter.search.Search.AMORPHOUS_NON_REQUIRED_ANCHOR;

public class Recipe {
    private String name;
    private Tag tag;
    private Map<Coordinate, Matter> coordinate;
    @Nullable private Map<Material, ItemStack> returnItems;
    @Nullable private RecipePermission permission;
    @Nullable private Result result;
    private List<RecipeContainer> containers;

    public Recipe(String name,String tag,Map<Coordinate,Matter> coordinate,Map<Material,ItemStack> returnItems,Result result, RecipePermission permission, List<RecipeContainer> containers){
        this.name = name;
        this.tag = Tag.valueOf(tag);
        this.coordinate = coordinate;
        this.returnItems = returnItems;
        this.result = result;
        this.permission = permission;
        this.containers = containers;
    }

    public Recipe(){ //only used for temporary (mainly real) -> tag is "Normal"
        this.tag = Tag.NORMAL;
        this.name = "";
        this.coordinate = new LinkedHashMap<>();
        this.returnItems = null;
        this.result = null;
        this.permission = null;
    }

    public void addCoordinate(int x,int y,Matter matter){
        this.coordinate.put(new Coordinate(x,y),matter);
    }

    public Tag getTag() {
        return tag;
    }

    public void setTag(Tag tag) {
        this.tag = tag;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<Coordinate, Matter> getCoordinate() {
        return coordinate;
    }

    public void setCoordinate(Map<Coordinate, Matter> coordinate) {
        this.coordinate = coordinate;
    }

    public @Nullable Map<Material, ItemStack> getReturnItems() {
        return returnItems;
    }

    public void setReturnItems(Map<Material, ItemStack> returnItems) {
        this.returnItems = returnItems;
    }

    public @Nullable Result getResult() {
        return result;
    }

    public void setResult(Result result) {
        this.result = result;
    }

    public @Nullable RecipePermission getPermission() {
        return permission;
    }

    public void setPermission(RecipePermission permission) {
        this.permission = permission;
    }

    public boolean hasPermission(){
        return permission != null;
    }

    public List<RecipeContainer> getContainers() {
        return containers;
    }

    public void setContainers(List<RecipeContainer> containers) {
        this.containers = containers;
    }

    public List<Matter> getContentsNoAir(){
        List<Matter> list = new ArrayList<>();
        coordinate.values().forEach(s->{
            if(!s.getCandidate().get(0).equals(Material.AIR))list.add(s);
        });
        return list;
    }

    public int normalRatioWith(Recipe recipe) {
        int minAmount = Integer.MAX_VALUE;
        int ratio = 1;
        List<Coordinate> recipeCoordinate = recipe.getCoordinateNoAir();
        List<Coordinate> inputCoordinate = this.getCoordinateNoAir();
        for (int i = 0; i < recipeCoordinate.size(); i++) {
            Matter currentRecipeMatter = recipe.getCoordinate().get(recipeCoordinate.get(i));
            if (currentRecipeMatter.isMass()) continue;
            int currentRecipeAmount = currentRecipeMatter.getAmount();
            int currentInputAmount = this.getCoordinate().get(inputCoordinate.get(i)).getAmount();
            if (currentInputAmount < minAmount) {
                ratio = currentInputAmount / currentRecipeAmount;
                minAmount = currentInputAmount;
            }
        }
        return ratio;
    }

    public int amorphousRatioWith(Recipe recipe) {
        Map<Coordinate, Coordinate> relate = this.getRelationWithRecipeAndInput(recipe);
        int ratio = 1;
        int minAmount = Integer.MAX_VALUE;
        for (Map.Entry<Coordinate, Coordinate> entry : relate.entrySet()) {
            Matter r = recipe.getCoordinate().get(entry.getKey());
            Matter i = this.getCoordinate().get(entry.getValue());
            if (r.isMass() || r.getCandidate().get(0).equals(Material.AIR)) continue;
            int recipeAmount = r.getAmount();
            int inputAmount = i.getAmount();
            if (inputAmount < minAmount) {
                minAmount = inputAmount;
                ratio = inputAmount / recipeAmount;
            }
        }
        return ratio;
    }

    public Map<Coordinate, Coordinate> getRelationWithRecipeAndInput(Recipe recipe) {
        List<Map<Coordinate, List<Coordinate>>> temp = new ArrayList<>();
        Map<Coordinate, List<Coordinate>> enchant = EnchantUtil.amorphous(recipe, this);
        Map<Coordinate, List<Coordinate>> container = ContainerUtil._amorphous(recipe, this);
        Map<Coordinate,List<Coordinate>> candidate = InventoryUtil.amorphous(recipe, this);
        Map<Coordinate, List<Coordinate>> potion = PotionUtil.amorphous(recipe, this);

        if (!enchant.equals(AMORPHOUS_NON_REQUIRED_ANCHOR)) temp.add(enchant);
        if (!container.equals(AMORPHOUS_NON_REQUIRED_ANCHOR)) temp.add(container);
        if (!potion.equals(AMORPHOUS_NON_REQUIRED_ANCHOR)) temp.add(potion);

        temp.add(candidate);
        // recipe, input
        return InventoryUtil.combination(temp);
    }



    public List<Coordinate> getCoordinateNoAir() {
        List<Coordinate> list = new ArrayList<>();
        coordinate.forEach((k, v) -> {
            if (!v.getCandidate().get(0).equals(Material.AIR)) list.add(k);
        });
        return list;
    }

    public List<Coordinate> getCoordinateList(){
        List<Coordinate> list = new ArrayList<>();
        coordinate.keySet().forEach(s->list.add(s));
        return list;
    }

    public List<Matter> getContentsNoDuplicate(){
        List<Matter> list = new ArrayList<>();
        List<List<Material>> temporary = new ArrayList<>();
        temporary.add(new ArrayList<>());

        for(Matter m:coordinate.values()){
            if(!temporary.contains(m.getCandidate())){
                list.add(m);
                temporary.add(m.getCandidate());
            }
        }
        return list;
    }

    public Map<Matter,Integer> getContentsNoDuplicateRelateAmount(){
        Map<Matter,Integer> map = new HashMap<>();
        for(Matter matter : coordinate.values()){
            if(!map.containsKey(matter)) {
                map.put(matter,1);
            }else {
                int i = map.get(matter) + matter.getAmount();
                map.put(matter,i);
            }
        }
        return map;
    }

    public Set<Material> getMassMaterialSet(){
        Set<Material> set = new HashSet<>();
        coordinate.values().forEach(s->{
            if(s.isMass())set.add(s.getCandidate().get(0));
        });
        return set;
    }

    public Matter getMatterFromCoordinate(Coordinate c){
        for(Map.Entry<Coordinate,Matter> entry : coordinate.entrySet()){
            if(entry.getKey().isSame(c))return entry.getValue();
        }
        return null;
    }


    public List<Coordinate> getEnchantedItemCoordinateList() {
        List<Coordinate> list = new ArrayList<>();
        for (Coordinate coordinate : getCoordinateNoAir()) {
            Matter matter = getMatterFromCoordinate(coordinate);
            if (!matter.hasWrap()) continue;
            list.add(coordinate);
        }
        return list;
    }


    public List<Coordinate> getHasContainersDataItemList() {
        List<Coordinate> list = new ArrayList<>();
        getCoordinateNoAir().forEach(c -> {
            if (getMatterFromCoordinate(c).hasContainers()) list.add(c);
        });
        return list;
    }

    public List<Coordinate> getHasPDCItemList() {
        List<Coordinate> list = new ArrayList<>();
        getCoordinateNoAir().forEach(c -> {
            if (getMatterFromCoordinate(c).hasPDC()) list.add(c);
        });
        return list;
    }

    public List<List<EnchantWrap>> getEnchantedItemList() {
        List<List<EnchantWrap>> list = new ArrayList<>();
        for (Matter matter : getContentsNoAir()) {
            if ((matter.getWrap() == null) || matter.getWrap().isEmpty()) continue;
            List<EnchantWrap> element = new ArrayList<>();
            for (EnchantWrap wrap : matter.getWrap()) {
                if (wrap.getStrict().equals(EnchantStrict.NOTSTRICT)) continue;
                element.add(wrap);
            }
            list.add(element);
        }
        return list;
    }

    public List<Coordinate> getPotionCoordinateList() {
        List<Coordinate> list = new ArrayList<>();
        for (Map.Entry<Coordinate, Matter> entry : this.coordinate.entrySet()) {
            if (!(entry.getValue() instanceof Potions)) continue;
            list.add(entry.getKey());
        }
        return list;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Recipe)) return false;
        Recipe recipe = (Recipe) o;
        return Objects.equals(name, recipe.name) && tag == recipe.tag && Objects.equals(coordinate, recipe.coordinate) && Objects.equals(returnItems, recipe.returnItems) && Objects.equals(permission, recipe.permission) && Objects.equals(result, recipe.result) && Objects.equals(containers, recipe.containers);
    }

    @Override
    public int hashCode() {
        int r = 17;
        r = r * 31 + name.hashCode();
        r = r * 31 + tag.hashCode();
        r = r * 31 + coordinate.hashCode();
        r = returnItems != null ? r * 31 + returnItems.hashCode() : r;
        r = permission != null ? r * 31 + permission.hashCode() : r;
        r = result != null ? r * 31 + result.hashCode() : r;
        return r;
    }
}
