package com.github.sakakiaruka.customcrafter.customcrafter.util;

import com.github.sakakiaruka.customcrafter.customcrafter.object.Matter.Matter;
import com.github.sakakiaruka.customcrafter.customcrafter.object.Recipe.*;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.github.sakakiaruka.customcrafter.customcrafter.SettingsLoad.*;

public class InventoryUtil {

    private static final String LEATHER_ARMOR_COLOR_RGB_PATTERN = "^type:(?i)(RGB)/value:R->(\\d{1,3}),G->(\\d{1,3}),B->(\\d{1,3})$";
    private static final String LEATHER_ARMOR_COLOR_NAME_PATTERN = "^type:(?i)(NAME)/value:([\\w_]+)$";
    private static final String LEATHER_ARMOR_COLOR_RANDOM_PATTERN = "^type:(?i)(RANDOM)$";

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
        for(int i = 0; i< CRAFTING_TABLE_TOTAL_SIZE; i++){
            list.add(i);
        }
        list.removeAll(getTableSlots(size));
        list.removeAll(Arrays.asList(CRAFTING_TABLE_MAKE_BUTTON));
        list.removeAll(Arrays.asList(CRAFTING_TABLE_RESULT_SLOT));
        return list;
    }

    public void decrementMaterials(Inventory inventory, int amount){
        // decrement crafting tables material
        // amount -> decrement amount
        List<Integer> slots = getTableSlots(CRAFTING_TABLE_SIZE);
        for(int i:slots){
            if(inventory.getItem(i) == null)continue;
            int oldAmount = inventory.getItem(i).getAmount();
            int newAmount = Math.max(oldAmount - amount, 0);
            inventory.getItem(i).setAmount(newAmount);
        }
    }

    public void decrementResult(Inventory inventory,Player player){
        if(inventory.getItem(CRAFTING_TABLE_RESULT_SLOT) == null)return;
        ItemStack item = inventory.getItem(CRAFTING_TABLE_RESULT_SLOT);
        InventoryUtil.safetyItemDrop(player, Collections.singletonList(item));
        inventory.setItem(CRAFTING_TABLE_RESULT_SLOT,new ItemStack(Material.AIR));
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
        InventoryUtil.safetyItemDrop(player, Collections.singletonList(item));
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
        for (int i : getTableSlots(CRAFTING_TABLE_SIZE)) {
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
        if (320 < value.length()) {
            Bukkit.getLogger().warning("[CustomCrafter] Set result metadata ("+section+") failed. (Over 256 characters.)");
            return false;
        }
        return true;
    }

    private boolean isValidCharacters(BookMeta meta, String value, String section) {
        if (25600 < (meta.getPageCount() * 320) + value.length()) {
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
        String PATTERN = "[a-zA-Z0-9\\-.+*/=%'\"#@_(),;:?!|{}<>§\\[\\]$]";
        String section = "addLong";

        if (extend) {
            try {
                value = String.join(LINE_SEPARATOR, Files.readAllLines(Paths.get(value), StandardCharsets.UTF_8));
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

        int horizontal = 0;
        int vertical = 0;
        StringBuilder element = new StringBuilder();

        // 22 -> horizontal limit
        // 14 -> vertical limit

        for (int i=0;i<value.length();i++) {
            String target = String.valueOf(value.charAt(i));
            int evaluation = target.matches(PATTERN) ? 1 : 2;

            if ((22 <= (horizontal + evaluation) || target.equals(LINE_SEPARATOR)) && vertical == 14) {
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

            } else if ((22 <= (horizontal + evaluation) || target.equals(LINE_SEPARATOR)) && vertical < 14) {
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


    // leather_armor_color modify
    private Color getColor(String value) {
        // if the value is not a color-name, returns null.
        Color color;
        value = value.toUpperCase();
        if (value.equals("AQUA")) color = Color.AQUA;
        else if (value.equals("BLACK")) color = Color.BLACK;
        else if (value.equals("BLUE")) color = Color.BLUE;
        else if (value.equals("FUCHSIA")) color = Color.FUCHSIA;
        else if (value.equals("GRAY")) color = Color.GRAY;
        else if (value.equals("GREEN")) color = Color.GREEN;
        else if (value.equals("LIME")) color = Color.LIME;
        else if (value.equals("MAROON")) color = Color.MAROON;
        else if (value.equals("NAVY")) color = Color.NAVY;
        else if (value.equals("OLIVE")) color = Color.OLIVE;
        else if (value.equals("ORANGE")) color = Color.ORANGE;
        else if (value.equals("PURPLE")) color = Color.PURPLE;
        else if (value.equals("RED")) color = Color.RED;
        else if (value.equals("SILVER")) color = Color.SILVER;
        else if (value.equals("TEAL")) color = Color.TEAL;
        else if (value.equals("WHITE")) color = Color.WHITE;
        else if (value.equals("YELLOW")) color = Color.YELLOW;
        else {
            Bukkit.getLogger().warning("[CustomCrafter] Set result metadata (ColorName) failed. Input -> "+value);
            return null;
        }
        return color;
    }

    private String getLeatherArmorColorWarningUnMatchPattern() {
        String result =
                "[CustomCrafter] Set result metadata (LeatherArmorColor) failed. (Illegal data format.)" + LINE_SEPARATOR
                + "Follow the patterns." + LINE_SEPARATOR
                + "- " + LEATHER_ARMOR_COLOR_RGB_PATTERN + LINE_SEPARATOR
                + "- " + LEATHER_ARMOR_COLOR_NAME_PATTERN + LINE_SEPARATOR
                + "- " + LEATHER_ARMOR_COLOR_RANDOM_PATTERN + LINE_SEPARATOR;
        return result;
    }

    public void setLeatherArmorColorFromRGB(LeatherArmorMeta meta, String value) {
        Matcher matcher = Pattern.compile(LEATHER_ARMOR_COLOR_RGB_PATTERN).matcher(value);
        if (!matcher.matches()) {
            Bukkit.getLogger().warning(getLeatherArmorColorWarningUnMatchPattern());
            return;
        }

        int RED = Integer.parseInt(matcher.group(2));
        int GREEN = Integer.parseInt(matcher.group(3));
        int BLUE = Integer.parseInt(matcher.group(4));
        Color color;
        try {
            color = Color.fromRGB(RED, GREEN, BLUE);
        } catch (Exception e) {
            Bukkit.getLogger().warning("[CustomCrafter] Set result metadata (LeatherArmorColor) failed. (Illegal RGB elements.)");
            e.printStackTrace();
            return;
        }
        meta.setColor(color);
    }

    public void setLeatherArmorColorFromName(LeatherArmorMeta meta, String value) {
        Matcher matcher = Pattern.compile(LEATHER_ARMOR_COLOR_NAME_PATTERN).matcher(value);
        if (!matcher.matches()) {
            Bukkit.getLogger().warning(getLeatherArmorColorWarningUnMatchPattern());
            return;
        }
        Color color;
        if ((color = getColor(matcher.group(2))) == null) {
            Bukkit.getLogger().warning("[CustomCrafter] Set result metadata (LeatherArmorColor) failed. (Illegal color-name.)" + LINE_SEPARATOR
            + "You can use 'AQUA', 'BLACK', 'BLUE', 'FUCHSIA', 'GRAY', 'GREEN', 'LIME', 'MAROON', 'NAVY', 'OLIVE', 'ORANGE', 'PURPLE', 'RED', 'SILVER', 'TEAL', 'WHITE' and 'YELLOW'."
            + LINE_SEPARATOR);
            return;
        }
        meta.setColor(color);
    }

    public void setLeatherArmorColorRandom(LeatherArmorMeta meta) {
        Random random = new Random();
        int RED = random.nextInt(256);
        int GREEN = random.nextInt(256);
        int BLUE = random.nextInt(256);
        Color color = Color.fromRGB(RED, GREEN, BLUE);
        meta.setColor(color);
    }

    public static void safetyItemDrop(Player player, List<ItemStack> items) {
        for (ItemStack item : items) {
            Item dropped = player.getWorld().dropItem(player.getLocation(), item);
            dropped.setOwner(player.getUniqueId());
            dropped.setGravity(false);
        }
    }

    public static void enchantModify(String action, String value, ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (!meta.hasEnchants()) return;
        if (action.equals("add")) {
            Matcher v = Pattern.compile("enchant=([\\w_]+),level=([\\d]+)").matcher(value);
            if (!v.matches()) return;
            Enchantment enchant = Enchantment.getByName(v.group(1).toUpperCase());
            int level = Integer.parseInt(v.group(2));
            meta.addEnchant(enchant, level, false);
        } else if (action.equals("remove")) {
            Enchantment enchant = Enchantment.getByName(value.toUpperCase());
            meta.removeEnchant(enchant);
        }
        item.setItemMeta(meta);
    }

    public static void enchantLevel(String action, String value, ItemStack item) {
        // if the specified enchantment is not contained the item-stack, this method does nothing.
        ItemMeta meta = item.getItemMeta();
        if (!meta.hasEnchants()) return;
        Matcher v = Pattern.compile("enchant=([\\w_]),change=([\\d]+)").matcher(value);
        if (!v.matches()) return;
        Enchantment enchant = Enchantment.getByName(v.group(1).toUpperCase());
        int change = Integer.parseInt(v.group(2));
        if (!meta.hasEnchant(enchant)) return;

        int oldLevel = meta.getEnchantLevel(enchant);
        meta.removeEnchant(enchant);
        int newLevel = 1;
        if (action.equals("minus")) newLevel = Math.max(1, oldLevel - change);
        else if (action.equals("plus")) newLevel = Math.min(255, oldLevel + change);
        meta.addEnchant(enchant, newLevel, false);
    }

    public static void loreModify(String action, String value, ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (action.equals("add")) {
            List<String> lore = meta.getLore();
            meta.setLore(null); // clear the lore
            lore.add(value);
            meta.setLore(lore);
        } else if (action.equals("clear")) {
            meta.setLore(null);
        } else if (action.equals("modify")) {
            List<String> lore = meta.getLore();
            meta.setLore(null); // clear the lore

            Matcher v = Pattern.compile("line=([\\d]+),lore=(.+)").matcher(value);
            if (!v.matches()) return;
            int line = Integer.parseInt(v.group(1));
            String add = v.group(2);

            if (lore.size()< line+1) return;
            lore.add(line+1, add);
            meta.setLore(lore);
        }
        item.setItemMeta(meta);
    }

    public static void durabilityModify(String action, String value, ItemStack item) {
        Damageable damageable;
        try {
            damageable = (Damageable) item.getItemMeta();
        } catch (Exception e) {
            return;
        }

        double oldHealth = damageable.getHealth();
        List<AttributeModifier> modifiers = (List<AttributeModifier>) item.getItemMeta().getAttributeModifiers(Attribute.GENERIC_MAX_HEALTH);
        double maxHealth = modifiers.get(0).getAmount();
        double lastOne = maxHealth - 1;
        double change = Double.parseDouble(value);

        if (action.equals("minus")) {
            // minus
            double damage = Math.min(lastOne, change);
            damageable.damage(damage);
        } else if (action.equals("plus")) {
            // plus
            double newHealth = Math.min(maxHealth, oldHealth + change);
            damageable.setHealth(newHealth);
        }
    }
}
