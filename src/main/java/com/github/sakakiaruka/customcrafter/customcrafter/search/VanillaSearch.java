package com.github.sakakiaruka.customcrafter.customcrafter.search;

import com.github.sakakiaruka.customcrafter.customcrafter.SettingsLoad;
import com.github.sakakiaruka.customcrafter.customcrafter.object.Recipe.Coordinate;
import com.github.sakakiaruka.customcrafter.customcrafter.util.InventoryUtil;
import it.unimi.dsi.fastutil.ints.AbstractInt2ReferenceFunction;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.github.sakakiaruka.customcrafter.customcrafter.SettingsLoad.*;

public class VanillaSearch {
    public void main(Player player, Inventory inventory, boolean isOneCraft){
        List<Coordinate> coordinates = getCoordinateList(inventory);
        if(coordinates.isEmpty())return;
        if(coordinates.size() > VANILLA_CRAFTING_SLOTS)return;
        if(Search.getSquareSize(coordinates) > VANILLA_CRAFTING_SQUARE_SIZE)return;

        ItemStack[] itemStack = getItemStacks(inventory,coordinates.get(0));
        ItemStack[] itemStacks = itemStack.clone();

        org.bukkit.inventory.Recipe recipe;
        if ((recipe = Bukkit.getCraftingRecipe(itemStacks.clone(), player.getWorld())) == null) return;
        ItemStack result = recipe.getResult();

        if(result.getType().equals(Material.AIR))return;
        WHAT_MAKING.put(player.getUniqueId(),result.getType());

        if(!isOneCraft) {
            // needed batch process
            int minimal = getMinimalAmount(itemStacks);
            result.setAmount(result.getAmount() * minimal);
            decrementMaterials(inventory, minimal);

        } else {
            // not needed batch process
            decrementMaterials(inventory, 1);

        }

        ItemStack resultSlot = inventory.getItem(CRAFTING_TABLE_RESULT_SLOT);
        int resultAmount = result.getAmount();
        int resultStackMax = result.getType().getMaxStackSize();
        if (resultSlot == null || resultSlot.getType().equals(Material.AIR)) {
            // if result slot is empty
            if (result.getAmount() <= resultStackMax) {
                inventory.setItem(CRAFTING_TABLE_RESULT_SLOT, result);
            } else {
                ItemStack canSet = result.asQuantity(resultStackMax);
                ItemStack overflown = result.asQuantity(resultAmount - resultStackMax);
                inventory.setItem(CRAFTING_TABLE_RESULT_SLOT, canSet);
                InventoryUtil.safetyItemPlace(player, List.of(overflown));
            }
        } else {
            int resultSlotAmount = resultSlot.getAmount();
            if (resultSlot.asOne().equals(result.asOne())) {
                if (resultSlotAmount >= resultStackMax) {
                    // give old result to player
                    InventoryUtil.safetyItemPlace(player, List.of(resultSlot));
                    // remove result slot's items
                    inventory.setItem(CRAFTING_TABLE_RESULT_SLOT, new ItemStack(Material.AIR));
                    if (resultAmount <= resultStackMax) {
                        inventory.setItem(CRAFTING_TABLE_RESULT_SLOT, result);
                    } else {
                        ItemStack canSet = result.asQuantity(resultSlotAmount);
                        ItemStack overflown = result.asQuantity(resultAmount - resultStackMax);
                        inventory.setItem(CRAFTING_TABLE_RESULT_SLOT, canSet);
                        InventoryUtil.safetyItemPlace(player, List.of(overflown));
                    }
                } else {
                    if (resultSlotAmount + resultAmount <= resultStackMax) {
                        inventory.setItem(CRAFTING_TABLE_RESULT_SLOT, resultSlot.asQuantity(resultSlotAmount + resultAmount));
                    } else {
                        inventory.setItem(CRAFTING_TABLE_RESULT_SLOT, resultSlot.asQuantity(resultStackMax));
                        InventoryUtil.safetyItemPlace(player, List.of(result.asQuantity(resultAmount - (resultStackMax - resultSlotAmount))));
                    }
                }
            } else {
                InventoryUtil.safetyItemPlace(player, List.of(resultSlot));
                inventory.setItem(CRAFTING_TABLE_RESULT_SLOT, new ItemStack(Material.AIR));
                if (resultAmount <= resultStackMax) {
                    inventory.setItem(CRAFTING_TABLE_RESULT_SLOT, result);
                } else {
                    inventory.setItem(CRAFTING_TABLE_RESULT_SLOT, result.asQuantity(resultStackMax));
                    InventoryUtil.safetyItemPlace(player, List.of(result.asQuantity(resultAmount - resultStackMax)));
                }
            }
        }
    }

    private void decrementMaterials(Inventory inventory, int minus) {
        getCoordinateList(inventory).forEach(e -> {
            int slot = e.getX() + e.getY() * 9;
            int amount = Math.max(inventory.getItem(slot).getAmount() - minus, 0);
            inventory.getItem(slot).setAmount(amount);
        });
    }

    private int getMinimalAmount(ItemStack[] items){
        int result = Integer.MAX_VALUE;
        for (ItemStack item : items) {
            if (item != null && !item.getType().equals(Material.AIR) && item.getAmount() < result) {
                result = item.getAmount();
            }
        }
        return result;
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
        for(int i: InventoryUtil.getTableSlots(CRAFTING_TABLE_SIZE)){
            if(inventory.getItem(i) == null)continue;
            if(inventory.getItem(i).getType().equals(Material.AIR))continue;
            int x = i % 9;
            int y = i / 9;
            list.add(new Coordinate(x,y));
        }
        return list;
    }

}
