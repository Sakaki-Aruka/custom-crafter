package com.github.sakakiaruka.customcrafter.customcrafter.search;

import com.github.sakakiaruka.customcrafter.customcrafter.object.Recipe.Coordinate;
import com.github.sakakiaruka.customcrafter.customcrafter.util.InventoryUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.github.sakakiaruka.customcrafter.customcrafter.SettingsLoad.*;

public class VanillaSearch {
    public void main(Player player, Inventory inventory){
        List<Coordinate> coordinates = getCoordinateList(inventory);
        if(coordinates.isEmpty())return;
        if(coordinates.size() > vanillaCraftingSlots)return;
        if(getSquareSize(coordinates) > vanillaCraftingSquareSize)return;

        ItemStack result = Bukkit.craftItem(getItemStacks(inventory,coordinates.get(0)),player.getWorld(),player);
        if(result == null)return;
        if(result.getType().equals(Material.AIR))return;

        if(result.getAmount() > result.getType().getMaxStackSize()){
            // amount over
            player.getWorld().dropItem(player.getLocation(),result);
        }else{
            inventory.setItem(craftingTableResultSlot,result);
        }

    }

    private ItemStack[] getItemStacks(Inventory inventory,Coordinate start){
        ItemStack[] items = new ItemStack[vanillaCraftingSlots];
        int counter = 0;
        for(int y=start.getY();y < start.getY()+3;y++){
            for(int x=start.getX();x < start.getX()+3;x++){
                int slot = x + y*vanillaCraftingSlots;
                items[counter] = inventory.getItem(slot) == null ? new ItemStack(Material.AIR) : inventory.getItem(slot);
                counter++;

                //debug
                System.out.println(String.format("itemStack[9] -> slot[%d] : %s",counter,inventory.getItem(slot).getType().name()));


            }
        }
        return items;
    }

    private List<Coordinate> getCoordinateList(Inventory inventory){
        List<Coordinate> list = new ArrayList<>();
        for(int i:new InventoryUtil().getTableSlots(craftingTableSize)){
            if(inventory.getItem(i) == null)continue;
            if(inventory.getItem(i).getType().equals(Material.AIR))continue;
            int x = i % vanillaCraftingSlots;
            int y = i / vanillaCraftingSlots;
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
