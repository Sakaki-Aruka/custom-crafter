package com.github.sakakiaruka.customcrafter.customcrafter.object.Recipe;

import com.github.sakakiaruka.customcrafter.customcrafter.object.Matter.Matter;
import com.github.sakakiaruka.customcrafter.customcrafter.object.Permission.RecipePermission;
import com.github.sakakiaruka.customcrafter.customcrafter.object.Result.Result;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.Permission;

import java.util.*;

public class Recipe {
    private String name;
    private Tag tag;
    private Map<Coordinate, Matter> coordinate;
    private Map<Material, ItemStack> returnItems;
    private RecipePermission permission;
    private Result result;
    public Recipe(String name,String tag,Map<Coordinate,Matter> coordinate,Map<Material,ItemStack> returnItems,Result result, RecipePermission permission){
        this.name = name;
        this.tag = Tag.valueOf(tag);
        this.coordinate = coordinate;
        this.returnItems = returnItems;
        this.result = result;
        this.permission = permission;
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
}
