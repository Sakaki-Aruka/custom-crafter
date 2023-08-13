package com.github.sakakiaruka.customcrafter.customcrafter.util;

import com.github.sakakiaruka.customcrafter.customcrafter.object.Matter.Matter;
import com.github.sakakiaruka.customcrafter.customcrafter.object.Recipe.*;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.*;

import static com.github.sakakiaruka.customcrafter.customcrafter.SettingsLoad.*;

public class InventoryUtil {
    public List<Integer> getTableSlots(int size){
        List<Integer> list = new ArrayList<>();
        for(int i=0;i<size;i++){
            for(int j=0;j<size;j++){
                int result = i*9+j;
                list.add(result);
            }
        }

        return list;
    }

    public List<Integer> getBlankCoordinates(int size){
        List<Integer> list = new ArrayList<>();
        for(int i=0;i<craftingTableTotalSize;i++){
            list.add(i);
        }
        list.removeAll(getTableSlots(size));
        list.removeAll(Arrays.asList(craftingTableMakeButton));
        list.removeAll(Arrays.asList(craftingTableResultSlot));
        return list;
    }

    public void decrementMaterials(Inventory inventory, int amount){
        // decrement crafting tables material
        // amount -> decrement amount
        List<Integer> slots = getTableSlots(craftingTableSize);
        for(int i:slots){
            if(inventory.getItem(i) == null)continue;
            int oldAmount = inventory.getItem(i).getAmount();
            int newAmount = oldAmount - amount < 0 ? 0 : oldAmount - amount;
            inventory.getItem(i).setAmount(newAmount);
        }
    }

    public void decrementResult(Inventory inventory,Player player){
        if(inventory.getItem(craftingTableResultSlot) == null)return;
        World world = player.getWorld();
        Location location = player.getLocation();
        ItemStack item = inventory.getItem(craftingTableResultSlot);
        world.dropItem(location,item); // drop
        inventory.setItem(craftingTableResultSlot,new ItemStack(Material.AIR));
    }

    public void returnItems(Recipe recipe,Inventory inventory, int removeAmount,Player player){
        if(recipe.getReturnItems().isEmpty())return;
        List<Material> isMassList = new ArrayList<>();
        recipe.getContentsNoAir().forEach(s->{
            if(s.isMass())isMassList.add(s.getCandidate().get(0));
        });

        for(ItemStack item:inventory){
            if(item == null)continue;
            if(!recipe.getReturnItems().containsKey(item.getType()))continue;
            int returnAmount = recipe.getReturnItems().get(item.getType()).getAmount();
            if(!isMassList.contains(item.getType())) returnAmount *= removeAmount;
            ItemStack itemStack = recipe.getReturnItems().get(item.getType()).clone();
            if(!itemStack.getType().equals(Material.AIR)) {
                drop(itemStack,returnAmount,player);
                continue;
            }

            // pass through return
            drop(item,returnAmount,player);
        }
    }

    private void drop(ItemStack item, int returnAmount, Player player) {
        item.setAmount(returnAmount);
        World world = player.getWorld();
        Location location = player.getLocation();
        world.dropItem(location,item);
    }

    public void snatchFromVirtual(Map<Matter, Integer> virtual, List<Matter> list, boolean mass) {
        Map<Matter, Integer> buf = new HashMap<>();
        A:for(Map.Entry<Matter, Integer> entry : virtual.entrySet()) {
            B:for(Matter matter : list) {
                if (!matter.sameCandidate(entry.getKey())) continue;
                int ii = (buf.containsKey(entry.getKey()) ? entry.getValue() : 0)  - (mass ? 1 : matter.getAmount());
                buf.put(entry.getKey(),ii);
            }
        }

        for(Map.Entry<Matter, Integer> entry : buf.entrySet()) {
            virtual.put(entry.getKey(),virtual.get(entry.getKey()) + entry.getValue());
        }
    }

    public List<ItemStack> getItemStackFromCraftingMenu(Inventory inventory) {
        List<ItemStack> result = new ArrayList<>();
        for (int i : getTableSlots(craftingTableSize)) {
            if (inventory.getItem(i) == null || inventory.getItem(i).getType().equals(Material.AIR)) continue;
            result.add(inventory.getItem(i));
        }
        return result;
    }
}
