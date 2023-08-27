package com.github.sakakiaruka.customcrafter.customcrafter.util;

import com.github.sakakiaruka.customcrafter.customcrafter.command.File;
import com.github.sakakiaruka.customcrafter.customcrafter.object.Matter.Matter;
import com.github.sakakiaruka.customcrafter.customcrafter.object.Recipe.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import java.awt.print.Book;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
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

    // === book field set ===
    public void setAuthor(BookMeta meta, String value) {
        meta.setAuthor(value);
    }

    public void setTitle(BookMeta meta, String value) {
        meta.setTitle(value);
    }

    public void setPage(BookMeta meta, int page, String value) {
        // page specified
        if (page < 0 || 100 < page) {
            Bukkit.getLogger().warning("[CustomCrafter] Set result metadata (setPage) failed. (Illegal insert page.)");
            return;
        }
        if (!isValidPage(meta, "setPage")) return;
        meta.setPage(page, value);
    }

    public void setPages(BookMeta meta, String value) {
        // set page un-specified page
        meta.setPages(value);
    }

    public void setGeneration(BookMeta meta, String value) {
        // set book-generation
        BookMeta.Generation generation;
        try {
            generation = BookMeta.Generation.valueOf(value.toUpperCase());
        } catch (Exception e) {
            Bukkit.getLogger().warning("[CustomCrafter] Set result metadata (setGeneration) failed. (Illegal BOOK_GENERATION)");
            return;
        }
        meta.setGeneration(generation);
    }

    public void addPage(BookMeta meta, String value) {
        // add page
        String section = "addPage";
        if (!isValidPage(meta, section)) return;
        if (!isValidCharacters(value, section)) return;
        if (!isValidCharacters(meta, value, section)) return;
        meta.addPage(value);
    }

    private boolean isValidPage(BookMeta meta, String section) {
        if (100 <= meta.getPageCount()) {
            Bukkit.getLogger().warning("[CustomCrafter] Set result metadata ("+section+") failed. (Over 50 pages.)");
            return false;
        }
        return true;
    }

    private boolean isValidCharacters(String value, String section) {
        if (256 < value.length()) {
            Bukkit.getLogger().warning("[CustomCrafter] Set result metadata ("+section+") failed. (Over 256 characters.)");
            return false;
        }
        return true;
    }

    private boolean isValidCharacters(BookMeta meta, String value, String section) {
        if (25600 < (meta.getPageCount() * 256) + value.length()) {
            Bukkit.getLogger().warning("[CustomCrafter] Set result metadata ("+section+") failed. (Over 25600 characters.)");
            return false;
        }
        return true;
    }

    public void addLong(BookMeta meta, String value, boolean extend) {
        // 320 -> the characters limit that about one page.
        // 25600 -> the characters limit that about one book.
        // 14 -> the lines limit that about one page.
        int ONE_BOOK_CHAR_LIMIT = 25600;
        String PATTERN = "[a-zA-Z0-9\\-.+*/=%'\"#@_(),;:?!|{}<>\\[\\]$]";
        String section = "addLong";

        if (extend) {
            try {
                value = String.join(nl, Files.readAllLines(Paths.get(value), StandardCharsets.UTF_8));
            } catch (Exception e) {
                Bukkit.getLogger().warning("[CustomCrafter] Set result metadata (addLong - extend) failed. (About a file read.)");
                Bukkit.getLogger().warning(e.getMessage());
                return;
            }
        }

        if (!isValidCharacters(meta, value, section)) return;

        if (ONE_BOOK_CHAR_LIMIT < value.length()) {
            Bukkit.getLogger().warning("[CustomCrafter] Set result metadata (addLong) failed. (Over 25600 characters.)");
            meta.addPage("Overflown");
            return;
        }

        int count = 0;
        int horizontal = 0;
        int vertical = 0;
        StringBuilder element = new StringBuilder();
        StringBuilder buffer = new StringBuilder();

        // 22 -> horizontal limit
        // 14 -> vertical limit

        for (int i=0;i<value.length();i++) {
            String target = String.valueOf(value.charAt(i));
            int evaluation = target.matches(PATTERN) ? 1 : 2;

            if ((22 <= (horizontal + evaluation) || target.equals(nl)) && vertical == 14) {
                // make a new page

                if (meta.getPageCount() == 100) {
                    Bukkit.getLogger().warning("[CustomCrafter] Set result metadata (addLong) failed. (Over 100 pages.)");
                    Bukkit.getLogger().warning("[CustomCrafter] Remaining "+(element.capacity() + (value.length() - i)) + " characters.");
                    return;
                }

                meta.addPage(element.toString());
                element.setLength(0);

                horizontal = evaluation;
                element.append(target);

                vertical = 0;

            } else if ((22 <= (horizontal + evaluation) || target.equals(nl)) && vertical < 14) {
                // make a new line
                horizontal = evaluation;
                element.append(target);

                vertical++;

            } else {
                // a character add
                element.append(target);
                horizontal += evaluation;
            }
        }

        meta.addPage(element.toString()); // add remaining string
    }
}
