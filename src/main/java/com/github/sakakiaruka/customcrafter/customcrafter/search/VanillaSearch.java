package com.github.sakakiaruka.customcrafter.customcrafter.search;

import com.github.sakakiaruka.customcrafter.customcrafter.object.Recipe.Coordinate;
import com.github.sakakiaruka.customcrafter.customcrafter.util.InventoryUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.github.sakakiaruka.customcrafter.customcrafter.SettingsLoad.*;

public class VanillaSearch {
    public void main(Player player, Inventory inventory,boolean batchBool){
        List<Coordinate> coordinates = getCoordinateList(inventory);
        if(coordinates.isEmpty())return;
        if(coordinates.size() > VANILLA_CRAFTING_SLOTS)return;
        if(getSquareSize(coordinates) > VANILLA_CRAFTING_SQUARE_SIZE)return;

        ItemStack[] itemStack = getItemStacks(inventory,coordinates.get(0));
        ItemStack[] itemStacks = itemStack.clone();

        org.bukkit.inventory.Recipe recipe = Bukkit.getCraftingRecipe(itemStacks.clone(), player.getWorld());
        if (recipe == null) return; // the system could not find the recipe
        ItemStack result = recipe.getResult();


        if(result == null)return;
        if(result.getType().equals(Material.AIR))return;
        WHAT_MAKING.put(player.getUniqueId(),result.getType());

        InventoryUtil util = new InventoryUtil();
        if(batchBool) {
            // needed batch process
            int minimal = getMinimalAmount(itemStacks);
            result.setAmount(result.getAmount() * minimal);
            util.decrementMaterials(inventory,minimal);

        }else{
            // not needed batch process
            util.decrementMaterials(inventory,1);

        }

        if(result.getAmount() > result.getType().getMaxStackSize()){
            // amount over
            InventoryUtil.safetyItemDrop(player, Collections.singletonList(result));
            inventory.setItem(CRAFTING_TABLE_RESULT_SLOT,new ItemStack(Material.AIR));
        }else{
            inventory.setItem(CRAFTING_TABLE_RESULT_SLOT,result);
        }

    }

    private int getMinimalAmount(ItemStack[] items){
        List<Integer> list = new ArrayList<>();
        Arrays.stream(items).forEach(s->{

            if(s != null && !s.getType().equals(Material.AIR)){
                list.add(s.getAmount());
            }
        });
        if(list.isEmpty())return -1;
        Collections.sort(list);
        return list.get(0);
    }


    private ItemStack[] getItemStacks(Inventory inventory,Coordinate start){
        ItemStack[] items = new ItemStack[VANILLA_CRAFTING_SLOTS];
        int counter = 0;
        for(int y=start.getY();y < start.getY()+3;y++){
            for(int x=start.getX();x < start.getX()+3;x++){
                if(y >= CRAFTING_TABLE_SIZE || x >= CRAFTING_TABLE_SIZE){
                    // over the crafting tables coordinates
                    items[counter] = new ItemStack(Material.AIR);
                    counter++;
                    continue;
                }
                int slot = x + y* VANILLA_CRAFTING_SLOTS;
                items[counter] = inventory.getItem(slot) == null ? new ItemStack(Material.AIR) : inventory.getItem(slot);
                counter++;
            }
        }
        return items;
    }

    private List<Coordinate> getCoordinateList(Inventory inventory){
        List<Coordinate> list = new ArrayList<>();
        for(int i:new InventoryUtil().getTableSlots(CRAFTING_TABLE_SIZE)){
            if(inventory.getItem(i) == null)continue;
            if(inventory.getItem(i).getType().equals(Material.AIR))continue;
            int x = i % 9;
            int y = i / 9;
            list.add(new Coordinate(x,y));
        }
        return list;
    }

    private int getSquareSize(List<Coordinate> list){
        List<Integer> xBuffer = new ArrayList<>();
        List<Integer> yBuffer = new ArrayList<>();
        list.forEach(s->{
            xBuffer.add(s.getX());
            yBuffer.add(s.getY());
        });
        Collections.sort(xBuffer);
        Collections.sort(yBuffer);
        int width = xBuffer.get(xBuffer.size()-1) - xBuffer.get(0) + 1;
        int height = yBuffer.get(yBuffer.size()-1) - yBuffer.get(0) + 1;
        return Math.max(width,height);
    }
}
