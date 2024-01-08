package com.github.sakakiaruka.customcrafter.customcrafter.object.Recipe;

import com.github.sakakiaruka.customcrafter.customcrafter.object.Matter.EnchantStrict;
import com.github.sakakiaruka.customcrafter.customcrafter.object.Matter.EnchantWrap;
import com.github.sakakiaruka.customcrafter.customcrafter.object.Matter.Matter;
import com.github.sakakiaruka.customcrafter.customcrafter.object.Matter.Potions.Potions;
import com.github.sakakiaruka.customcrafter.customcrafter.object.Permission.RecipePermission;
import com.github.sakakiaruka.customcrafter.customcrafter.object.Recipe.Container.RecipeDataContainer;
import com.github.sakakiaruka.customcrafter.customcrafter.object.Result.Result;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class Recipe {
    private String name;
    private Tag tag;
    private Map<Coordinate, Matter> coordinate;
    private Map<Material, ItemStack> returnItems;
    private RecipePermission permission;
    private Result result;
    private Map<NamespacedKey, List<RecipeDataContainer>> container;
    private Map<Matter, List<String>> usingContainerValuesMetadata;

    public Recipe(String name,String tag,Map<Coordinate,Matter> coordinate,Map<Material,ItemStack> returnItems,Result result, RecipePermission permission, Map<NamespacedKey, List<RecipeDataContainer>> container, Map<Matter, List<String>> usingContainerValuesMetadata){
        this.name = name;
        this.tag = Tag.valueOf(tag);
        this.coordinate = coordinate;
        this.returnItems = returnItems;
        this.result = result;
        this.permission = permission;
        this.container = container;
        this.usingContainerValuesMetadata = usingContainerValuesMetadata;
    }

    public Recipe(){ //only used for temporary (mainly real) -> tag is "Normal"
        this.tag = Tag.NORMAL;
        this.name = "";
        this.coordinate = new LinkedHashMap<>();
        this.returnItems = null;
        this.result = null;
        this.permission = null;
        this.container = new HashMap<>();
        this.usingContainerValuesMetadata = new HashMap<>();
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

    public Map<Material, ItemStack> getReturnItems() {
        return returnItems;
    }

    public void setReturnItems(Map<Material, ItemStack> returnItems) {
        this.returnItems = returnItems;
    }

    public Result getResult() {
        return result;
    }

    public void setResult(Result result) {
        this.result = result;
    }

    public RecipePermission getPermission() {
        return permission;
    }

    public void setPermission(RecipePermission permission) {
        this.permission = permission;
    }

    public boolean hasPermission(){
        return permission != null;
    }


    public List<Matter> getContentsNoAir(){
        List<Matter> list = new ArrayList<>();
        coordinate.values().forEach(s->{
            if(!s.getCandidate().get(0).equals(Material.AIR))list.add(s);
        });
        return list;
    }

    public List<Coordinate> getCoordinateNoAir() {
        List<Coordinate> list = new ArrayList<>();
        coordinate.entrySet().forEach(s -> {
            if (!s.getValue().getCandidate().get(0).equals(Material.AIR)) list.add(s.getKey());
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

    public int getContainerHasAmount() {
        int result = 0;
        for (Matter matter : coordinate.values()) {
            if (matter.hasContainer()) result++;
        }
        return result;
    }

    public Map<NamespacedKey, List<RecipeDataContainer>> getContainer() {
        return container;
    }

    public void setContainer(Map<NamespacedKey, List<RecipeDataContainer>> container) {
        this.container = container;
    }

    public boolean hasContainer() {
        if (this.container == null) return false;
        if (this.container.isEmpty()) return false;
        return true;
    }

    public Map<Matter, List<String>> getUsingContainerValuesMetadata() {
        return usingContainerValuesMetadata;
    }

    public void setUsingContainerValuesMetadata(Map<Matter, List<String>> usingContainerValuesMetadata) {
        this.usingContainerValuesMetadata = usingContainerValuesMetadata;
    }

    public boolean hasUsingContainerValuesMetadata() {
        return !(this.usingContainerValuesMetadata == null || this.usingContainerValuesMetadata.isEmpty());
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

    public List<Coordinate> getHasContainerDataItemList() {
        List<Coordinate> list = new ArrayList<>();
        for (Coordinate coordinate : getCoordinateNoAir()) {
            Matter matter = getMatterFromCoordinate(coordinate);
            if (!matter.hasContainer()) continue;
            list.add(coordinate);
        }
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

}
