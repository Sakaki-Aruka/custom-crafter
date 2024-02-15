package com.github.sakakiaruka.customcrafter.customcrafter.util;

import com.github.sakakiaruka.customcrafter.customcrafter.CustomCrafter;
import com.github.sakakiaruka.customcrafter.customcrafter.SettingsLoad;
import com.github.sakakiaruka.customcrafter.customcrafter.interfaces.TriConsumer;
import com.github.sakakiaruka.customcrafter.customcrafter.object.Matter.Container.MatterContainer;
import com.github.sakakiaruka.customcrafter.customcrafter.object.Matter.Matter;
import com.github.sakakiaruka.customcrafter.customcrafter.object.Recipe.Coordinate;
import com.github.sakakiaruka.customcrafter.customcrafter.object.Recipe.Recipe;
import com.github.sakakiaruka.customcrafter.customcrafter.search.Search;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;
import java.util.function.BiFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ContainerUtil {


    public static boolean isPass(ItemStack item, Matter matter) {
        // item -> target, matter -> source (recipe)
        // matter > item
        if (!matter.hasContainers()) return true;
        PersistentDataContainer pdc = Objects.requireNonNull(item.getItemMeta()).getPersistentDataContainer();
        List<MatterContainer> containers = matter.getContainers();
        if (containers.size() > pdc.getKeys().size()) return false;
        Map<String, String> data = getData(pdc);
        int count = 0;
        for (MatterContainer container : containers) {
            if (container.judge(data)) count++;
        }
        return count == containers.size();
    }
    // -------------------------------------

    public static Map<Coordinate, List<Coordinate>> _amorphous(Recipe recipe, Recipe input) {
        List<Coordinate> r = recipe.getHasContainersDataItemList();
        List<Coordinate> i = input.getHasPDCItemList();
        Map<Coordinate, List<Coordinate>> map = new HashMap<>();

        //debug
        for (Coordinate c : r) {
            System.out.print(c.toString() + ",");
        }
        System.out.println("\nrecipe ↑ | input ↓");
        for (Coordinate c : i) {
            System.out.print(c.toString() + ",");
        }
        System.out.println();

        // returns NON_REQUIRED = non require container elements
        // returns NULL         = an input does not matched
        if (r.isEmpty()) return Search.AMORPHOUS_NON_REQUIRED_ANCHOR;
        if (r.size() > i.size()) return Search.AMORPHOUS_NULL_ANCHOR;

        // debug (write here; search loop in full Input elements)
        for (Coordinate coordinate : r) {
            Matter R = recipe.getMatterFromCoordinate(coordinate);
            for (Coordinate value : i) {
                Matter I = input.getMatterFromCoordinate(value);
                Map<String, String> data = getData(I.getPDC());
                int judge = 0;
                for (MatterContainer container : R.getContainers()) {
                    if (container.judge(data)) judge++;
                }

                if (judge == R.getContainers().size()) {
                    if (!map.containsKey(coordinate)) map.put(coordinate, new ArrayList<>());
                    map.get(coordinate).add(value);
                }
            }
        }
        return map.isEmpty() ? Search.AMORPHOUS_NULL_ANCHOR : map;
    }

    public static final BiFunction<Map<String, String>, String, Boolean> NONE = (data, predicate) -> true;

    public static final BiFunction<Map<String, String>, String, Boolean> STRING_MATCH = (data, predicate) -> {
        // set split comma index (\\d+)(.+)
        // ~~:(source),(target)
        Matcher matcher = Pattern.compile("(\\d+):(.+)").matcher(predicate);
        if (!matcher.matches()) return false;
        int splitIndex = Integer.parseInt(matcher.group(1));
        String formula = matcher.group(2);
        int count = 0;
        for (int i = 0; i < formula.length(); i++) {
            if (formula.charAt(i) != ',') continue;
            count++;
            if (count != splitIndex) continue;
            String source = setEvalValue(setPlaceholderValue(data, formula.substring(0, i)));
            String target = setEvalValue(setPlaceholderValue(data, formula.substring(i + 1)));
            return target.matches(source);
        }
        return false;
    };

    public static final BiFunction<Map<String, String>, String, Boolean> VALUE_ALLOW = (data, predicate) -> {
        // type: (?i)(Allow)Value, predicate: ~~~~
        return Expression.eval(setEvalValue(setPlaceholderValue(data, predicate))).asBoolean();
    };

    public static final BiFunction<Map<String, String>, String, Boolean> VALUE_DENY = (data, predicate) -> {
        // type: (?i)(Deny)Value, predicate: ~~~~
        return !Expression.eval(setEvalValue(setPlaceholderValue(data, predicate))).asBoolean();
    };

    private static boolean getTagResult(Map<String, String> data, String predicate, boolean isAllow) {
        for (String key : predicate.replace(" ", "").split(",")) {
            if (!(data.containsKey(key) == isAllow)) return false;
        }
        return true;
    }

    public static final BiFunction<Map<String, String>, String, Boolean> TAG_ALLOW = (data, predicate) -> {
        // type: (?i)(Allow)Tag, predicate: ~~~,~~~,~~~
        // divided ",".
        return getTagResult(data, predicate, true);
    };

    public static final BiFunction<Map<String, String>, String, Boolean> TAG_DENY = (data, predicate) -> {
        // type: (?i)(Deny)Tag, predicate: ~~~,~~~,~~~
        // divided ",".
        return getTagResult(data, predicate, false);
    };

    // ====================================================================

    private static String getContent(Map<String, String> data, String key) {
        return setEvalValue(setPlaceholderValue(data, data.get(key)));
    }


    public static String setPlaceholderValue(Map<String, String> data, String formula) {
        // debug (in release, this method must be private)
        // %variableName% -> replace
        // \%variableName\% -> not replace
        StringBuilder result = new StringBuilder();
        StringBuilder buffer = new StringBuilder();
        int flag = 0;
        for (int i = 0; i < formula.length(); i++) {
            char c = formula.charAt(i);
            if (c != '%' && c != '\\' && flag == 0) {
                result.append(c);
            } else if (c == '\\' && i <= formula.length() - 2 && flag == 0) {
                if (formula.charAt(i + 1) != '%') {
                    result.append(c);
                } else {
                    // next char is '%'
                    result.append('%');
                    i++;
                }
            } else if (c == '%' && flag == 0) {
                flag = 1;
            } else if (c != '%' && flag == 1) {
                buffer.append(c);
            } else if (c == '%') {
                flag = 0;
                String key = buffer.toString();
                if (!data.containsKey(key)) result.append("None");
                else result.append(data.get(buffer.toString()));
                buffer.setLength(0);
            }
        }
        return result + (!buffer.isEmpty() ? buffer.toString() : "");
    }

    public static String setEvalValue(String formula) {
        // debug (in release, this method must be private)
        // {2+3} -> 5
        // {2^3} -> 8
        // setEvalValue(data, setPlaceholderValue(data, "{%TEST%+%TEST_2%}"))
        // - TEST=1, TEST=2 -> 3
        StringBuilder result = new StringBuilder();
        StringBuilder buffer = new StringBuilder();
        int flag = 0;
        for (int i = 0; i < formula.length(); i++) {
            char c = formula.charAt(i);
            if (c != '{' && c != '}' && c != '\\' && flag == 0) {
                result.append(c);
                continue;
            } else if (c == '\\' && i <= formula.length() - 2 && flag == 0) {
                char after = formula.charAt(i + 1);
                if (after != '{' && after != '}') {
                    result.append(c);
                } else {
                    // next char is '{' or '}'
                    result.append(after);
                    i++;
                }
            } else if (c == '{' && flag == 0) {
                flag = 1;
            } else if (c != '}' && flag == 1) {
                buffer.append(c);
            } else if (c == '}') {
                flag = 0;
                result.append(Expression.eval(buffer.toString()).asString());
                buffer.setLength(0);
            }
        }
        return result + (!buffer.isEmpty() ? buffer.toString() : "");
    }


    // === for RecipeContainer === //

    private static boolean inRGBRange(int... elements) {
        for (int i : elements) {
            if (!(0 <= i && i < 256)) return false;
        }
        return true;
    }

    public static final TriConsumer<Map<String, String>, ItemStack, String> LORE = (data, item, formula) -> {
        // type: lore, value: ~~~
        // formula -> ~~~
        ItemMeta meta = Objects.requireNonNull(item.getItemMeta());
        if (formula == null) {
            meta.setLore(null);
            item.setItemMeta(meta);
            return;
        }
        List<String> lore = new ArrayList<>();
        if (meta.getLore() != null) lore.addAll(meta.getLore());
        lore.add(getContent(data, formula));
        meta.setLore(lore);
        item.setItemMeta(meta);
    };

    public static final TriConsumer<Map<String, String>, ItemStack, String> ENCHANT = (data, item, formula) -> {
        // type: enchant, value: enchant=~~~,level=~~~
        // data.get("%ENCHANT%") -> enchantment name
        // data.get("%LEVEL%") -> enchantment level
        // formula -> enchant: ~~~, level: ~~~
        ItemMeta meta = Objects.requireNonNull(item.getItemMeta());
        Matcher matcher = Pattern.compile("enchant=(.+),level=(.+)").matcher(formula);
        if (!matcher.matches()) return;
        String enchantName = getContent(data, matcher.group(1));
        Enchantment enchantment = Enchantment.getByKey(NamespacedKey.minecraft(enchantName));
        if (enchantment == null) return;
        int level = (int) Double.parseDouble(getContent(data, matcher.group(2)));
        meta.addEnchant(enchantment, level, true);
        item.setItemMeta(meta);
    };

    public static final TriConsumer<Map<String, String>, ItemStack, String> POTION_COLOR_RGB = (data, item, formula) -> {
        // type: potion_color, value: r=100,g=100,b=100
        // %RED%, %GREEN%, %BLUE%
        ItemMeta meta = Objects.requireNonNull(item.getItemMeta());
        PotionMeta potionMeta = (PotionMeta) meta;
        Matcher matcher = Pattern.compile("red=(.+),green=(.+),blue=(.+)").matcher(formula);
        if (!matcher.matches()) return;
        int red = (int) Double.parseDouble(getContent(data, matcher.group(1)));
        int green = (int) Double.parseDouble(getContent(data, matcher.group(2)));
        int blue = (int) Double.parseDouble(getContent(data, matcher.group(3)));
        if (!inRGBRange(red, green, blue)) return;
        Color color = Color.fromRGB(red, green, blue);
        potionMeta.setColor(color);
        item.setItemMeta(meta);
    };

    public static final TriConsumer<Map<String, String>, ItemStack, String> POTION_COLOR_NAME = (data, item, formula) -> {
        // type: potion_color, value: white
        // %COLOR%
        ItemMeta meta = Objects.requireNonNull(item.getItemMeta());
        Matcher matcher = Pattern.compile("color=(.+)").matcher(formula);
        if (!matcher.matches()) return;
        Color color = InventoryUtil.getColor(getContent(data, matcher.group(1).toUpperCase()));
        if (color == null) return;
        ((PotionMeta) meta).setColor(color);
        item.setItemMeta(meta);
    };

    public static final TriConsumer<Map<String, String>, ItemStack, String> TEXTURE_ID = (data, item, formula) -> {
        // type: texture_id, value: 100
        // %TEXTURE_ID%
        ItemMeta meta = Objects.requireNonNull(item.getItemMeta());
        Matcher matcher = Pattern.compile("texture_id=(.+)").matcher(formula);
        if (!matcher.matches()) return;
        int id = (int) Double.parseDouble(getContent(data, matcher.group(1)));
        meta.setCustomModelData(id);
        item.setItemMeta(meta);
    };

    public static final TriConsumer<Map<String, String>, ItemStack, String> TOOL_DURABILITY_PERCENTAGE = (data, item, formula) -> {
        // type: tool_durability_percentage, value: 100%
        // %DAMAGE%, %MATERIAL%
        ItemMeta meta = Objects.requireNonNull(item.getItemMeta());
        Matcher matcher = Pattern.compile("durability_percentage=(.+)").matcher(formula);
        if (!matcher.matches()) return;
        Material material = item.getType();
        int lastOne = material.getMaxDurability() - 1;
        double percentage = Double.parseDouble(getContent(data, matcher.group(1)));
        int damage = Math.max(lastOne, (int) (material.getMaxDurability() * percentage));
        ((Damageable) meta).setDamage(damage);
    };

    public static final TriConsumer<Map<String, String>, ItemStack, String> TOOL_DURABILITY_REAL = (data, item, formula) -> {
        // type: tool_durability_real_number, value: 100
        // %DAMAGE%, %MATERIAL%
        ItemMeta meta = Objects.requireNonNull(item.getItemMeta());
        Matcher matcher = Pattern.compile("durability_real=(.+)").matcher(formula);
        if (!matcher.matches()) return;
        Material material = item.getType();
        int damage = (int) Double.parseDouble(getContent(data, matcher.group(1)));
        if (material.getMaxDurability() - damage <= 0) damage = material.getMaxDurability() - 1;
        ((Damageable) meta).setDamage(Math.min(damage, material.getMaxDurability()));
    };

    public static final TriConsumer<Map<String, String>, ItemStack, String> ITEM_NAME = (data, item, formula) -> {
        // type: item_name, value: this is an item
        // "" (empty string)
        // %NAME%
        ItemMeta meta = Objects.requireNonNull(item.getItemMeta());
        Matcher matcher = Pattern.compile("item_name=(.+)").matcher(formula);
        if (!matcher.matches()) return;
        meta.setDisplayName(getContent(data, matcher.group(1)));
        item.setItemMeta(meta);
    };

    public static final TriConsumer<Map<String, String>, ItemStack, String> ATTRIBUTE_MODIFIER = (data, item, formula) -> {
        ItemMeta meta = Objects.requireNonNull(item.getItemMeta());
        Matcher matcher = Pattern.compile("attribute=(.+),op=(.+),value=(.+)").matcher(formula);
        if (!matcher.matches()) return;
        Attribute attribute = Attribute.valueOf(getContent(data, matcher.group(1)).toUpperCase());
        AttributeModifier.Operation operation = AttributeModifier.Operation.valueOf(getContent(data, matcher.group(2)).toUpperCase());
        double value = Double.parseDouble(getContent(data, matcher.group(3)));
        AttributeModifier modifier = new AttributeModifier(UUID.randomUUID(), UUID.randomUUID().toString(), value, operation);
        meta.addAttributeModifier(attribute, modifier);
        item.setItemMeta(meta);
    };

    public static final TriConsumer<Map<String, String>, ItemStack, String> ATTRIBUTE_MODIFIER_EQUIPMENT = (data, item, formula) -> {
        // type: attribute_modifier_slot, value: attribute=~~~, ope=~~~, value=~~~, slot=~~~
        // %ATTRIBUTE%, %OPE_TYPE%, %VALUE%, %SLOT%
        ItemMeta meta = Objects.requireNonNull(item.getItemMeta());
        Matcher matcher = Pattern.compile("attribute=(.+),op=(.+),value=(.+),slot=(.+)").matcher(formula);
        if (!matcher.matches()) return;
        Attribute attribute = Attribute.valueOf(getContent(data, matcher.group(1)).toUpperCase());
        AttributeModifier.Operation operation = AttributeModifier.Operation.valueOf(getContent(data, matcher.group(2)).toUpperCase());
        double value = Double.parseDouble(getContent(data, matcher.group(3)));
        EquipmentSlot slot = EquipmentSlot.valueOf(getContent(data, matcher.group(4)).toUpperCase());
        AttributeModifier modifier = new AttributeModifier(UUID.randomUUID(), UUID.randomUUID().toString(), value, operation, slot);
        meta.addAttributeModifier(attribute, modifier);
        item.setItemMeta(meta);
    };

    public static final TriConsumer<Map<String, String>, ItemStack, String> ITEM_FLAG = (data, item, formula) -> {
        // type: item_flag, value: flag=~~~, action=(clear, remove, add)
        // %FLAG%, %ACTION%
        ItemMeta meta = Objects.requireNonNull(item.getItemMeta());
        Matcher matcher = Pattern.compile("flag=(.+),action=(.+)").matcher(formula);
        if (!matcher.matches()) return;
        String type = getContent(data, matcher.group(2));
        if (type.equalsIgnoreCase("clear")) {
            meta.getItemFlags().forEach(meta::removeItemFlags);
            return;
        }

        ItemFlag flag = ItemFlag.valueOf(getContent(data, matcher.group(1)).toUpperCase());
        if (type.equalsIgnoreCase("remove")) {
            meta.removeItemFlags(flag);
        } else if (type.equalsIgnoreCase("add")) {
            meta.addItemFlags(flag);
        }
    };

    public static final TriConsumer<Map<String, String>, ItemStack, String> UNBREAKABLE = (data, item, formula) -> {
        // type: unbreakable, value: ~~~
        // %BOOL%
        Matcher matcher = Pattern.compile("unbreak=(.+)").matcher(formula);
        if (!matcher.matches()) return;
        ItemMeta meta = Objects.requireNonNull(item.getItemMeta());
        meta.setUnbreakable(Boolean.parseBoolean(getContent(data, matcher.group(1))));
    };

    public static final TriConsumer<Map<String, String>, ItemStack, String> POTION_EFFECT = (data, item, formula) -> {
        // type: potion_effect, value: effect=~~~,level=~~~,duration=~~~
        Matcher matcher = Pattern.compile("effect=(.+),level=(.+),duration=(.+)").matcher(formula);
        if (!matcher.matches()) return;
        PotionMeta meta = (PotionMeta) Objects.requireNonNull(item.getItemMeta());
        PotionEffectType effect = PotionEffectType.getByName(getContent(data, matcher.group(1).toUpperCase()));
        if (effect == null) return;
        int level = (int) Double.parseDouble(getContent(data, matcher.group(2)));
        int duration = (int) Double.parseDouble(getContent(data, matcher.group(3)));
        PotionEffect potion = new PotionEffect(effect, duration, level);
        meta.addCustomEffect(potion, true);
        item.setItemMeta(meta);
    };

    public static final TriConsumer<Map<String, String>, ItemStack, String> LEATHER_ARMOR_COLOR = (data, item, formula) -> {
        // type: leather_armor_color, value: (r=~~,g=~~,b=~~|colorName|random)
        Matcher rgb = Pattern.compile("r=(.+),g=(.+),b=(.+)").matcher(formula);
        Matcher name = Pattern.compile("color=(.+)").matcher(formula);
        Matcher random = Pattern.compile("random").matcher(formula);
        if (!(rgb.matches() || name.matches() || random.matches())) return;
        LeatherArmorMeta meta = (LeatherArmorMeta) Objects.requireNonNull(item.getItemMeta());
        Color color;
        if (rgb.matches()) {
            int red = (int) Double.parseDouble(getContent(data, rgb.group(1)));
            int green = (int) Double.parseDouble(getContent(data, rgb.group(2)));
            int blue = (int) Double.parseDouble(getContent(data, rgb.group(3)));
            if (!inRGBRange(red, green, blue)) return;
            color = Color.fromRGB(red, green, blue);
        } else if (name.matches()) {
            color = InventoryUtil.getColor(getContent(data, name.group(1).toUpperCase()));
        } else if (random.matches()) {
            color = Color.fromRGB(new Random().nextInt(256));
        } else return;
        if (color == null) return;
        meta.setColor(color);
        item.setItemMeta(meta);
    };

    public static TriConsumer<Map<String, String>, ItemStack, String> BOOK = (data, item, formula) -> {
        // type: book, value: type=(?i)(author|title|add_page|from_file|long_field|gen), element=(.+)
        Matcher matcher = Pattern.compile("type=(.+),element=(.+)").matcher(formula);
        if (!matcher.matches()) return;
        String type = getContent(data, matcher.group(1));
        String element = getContent(data, matcher.group(2));
        if (!matcher.group(1).matches("(?i)(author|title|add_page|add_long|from_file|gen)")) return;
        BookMeta meta = (BookMeta) Objects.requireNonNull(item.getItemMeta());
        if (type.equalsIgnoreCase("author")) {
            meta.setAuthor(element);
        } else if (type.equalsIgnoreCase("title")) {
            meta.setTitle(element);
        } else if (type.equalsIgnoreCase("add_page")) {
            meta.addPage(element);
        } else if (type.equalsIgnoreCase("add_long")) {
            element = element.replace("\\n", SettingsLoad.LINE_SEPARATOR);
            InventoryUtil.addLong(meta, element, false);
        } else if (type.equalsIgnoreCase("from_file")) {
            InventoryUtil.addLong(meta, element, true);
        }else if (type.equalsIgnoreCase("gen")) {
            BookMeta.Generation gen = BookMeta.Generation.valueOf(element.toUpperCase());
            meta.setGeneration(gen);
        }
        item.setItemMeta(meta);
    };

    public static final TriConsumer<Map<String, String>, ItemStack, String> ENCHANT_MODIFY = (data, item, formula) -> {
        // type: enchant_modify, value: type=(enchant|level),action=(.+)->(.+)
        // e.g. type: enchant_modify, value: type=enchant,action=luck->mending (luck to mending)
        // e.g. type: enchant_modify, value: type=enchant,action=luck->None (remove enchant(luck))
        // e.g. type: enchant_modify, value: type=level,action=luck->2 (luck's level change (to 2))
        // e.g. type: enchant_modify, value: type=level,action=luck->None (remove enchant(luck))
        // a special variable that is named $CURRENT_LEVEL$ contains current enchants level. (in using "%$CURRENT_LEVEL$%")
        Matcher matcher = Pattern.compile("type=(.+),action=(.+)->(.+)").matcher(formula);
        if (!matcher.matches()) return;
        String type = matcher.group(1);
        if (!type.matches("(?i)(enchant|level)")) return;
        String left = matcher.group(2);
        String right = matcher.group(3);
        boolean toNone = right.equals("None");
        boolean changeEnchant = type.equalsIgnoreCase("enchant");
        ItemMeta meta = Objects.requireNonNull(item.getItemMeta());
        Enchantment enchant = Enchantment.getByKey(NamespacedKey.minecraft(getContent(data, left.toLowerCase())));
        if (enchant == null || !meta.getEnchants().containsKey(enchant)) return;
        data.put("$CURRENT_LEVEL$", String.valueOf(meta.getEnchantLevel(enchant)));
        if (toNone) {
            meta.removeEnchant(enchant);
        } else if (changeEnchant) {
            Enchantment replacer = Enchantment.getByKey(NamespacedKey.minecraft(right.toLowerCase()));
            if (replacer == null) return;
            int level = meta.getEnchantLevel(enchant);
            meta.removeEnchant(enchant);
            meta.addEnchant(replacer, level, true);
        } else {
            meta.removeEnchant(enchant);
            meta.addEnchant(enchant, Integer.parseInt(getContent(data, right)), true);
        }
        data.remove("$CURRENT_LEVEL$");
        item.setItemMeta(meta);
    };

    public static final TriConsumer<Map<String, String>, ItemStack, String> LORE_MODIFY = (data, item, formula) -> {
        // type: lore_modify, value: type=(clear|modify)(,value=(.+))?
        // e.g. type: lore_modify, value: type=clear (clear the current lore)
        //
        // type: lore_modify, value: type=modify,value=type=(insert|remove),element=line=((.+),value=(.+)|(.+))
        // type: lore_modify, value: type=modify,value=type=insert,element=line=(.+),value=(.+)
        // type: lore_modify, value: type=modify,value=type=remove,element=line=(.+)
        // e.g. type: lore_modify, value: type=modify,value=type=insert,element=line=1,value=A (add "A" to line 1 (second line).)
        // e.g. type: lore_modify, value: type=modify,value=type=remove,element=line=1 (remove line 1 (second line.))
        //
        // a special variable named %$LINES$% contains lore lines.
        ItemMeta meta = Objects.requireNonNull(item.getItemMeta());
        List<String> lore = new ArrayList<>();
        if (meta.getLore() != null) lore.addAll(meta.getLore());
        data.put("$LINES$", String.valueOf(lore.size()));
        formula = getContent(data, formula);
        data.remove("$LINES$");
        Matcher one = Pattern.compile("type=(clear|modify)(,value=type=(insert|remove),element=line=((\\d+),value=(.+)|(\\d+)))?").matcher(formula);
        if (!one.matches()) return;
        if (one.group(1).equalsIgnoreCase("clear")) {
            meta.setLore(null);
            return;
        }

        if (one.group(3).equalsIgnoreCase("remove")) {
            int line = (int) Double.parseDouble(one.group(4));
            lore.remove(line);
        } else if (one.group(3).equalsIgnoreCase("insert")) {
            int line = (int) Double.parseDouble(getContent(data, one.group(5)));
            if (line <= 0) return; // to small
            String element = one.group(6);
            lore.add(line, element);
        }
        meta.setLore(lore);
        item.setItemMeta(meta);
    };

    public static final TriConsumer<Map<String, String>, ItemStack, String> ATTRIBUTE_MODIFIER_MODIFY = (data, item, formula) -> {
        // type: attribute_modifier_modify, value: type=(clear|remove|modify)(,attribute=(.+)(,value=(.+))?)?
        // e.g. type: attribute_modifier_modify, value: type=clear
        // e.g. type: attribute_modifier_modify, value: type=remove,attribute=GENERIC_ARMOR (remove "GENERIC_ARMOR")
        // e.g. type: attribute_modifier_modify, value: type=modify,attribute=GENERIC_ARMOR,value=attribute=(.+),op=(.+),value=(.),slot=(.+) (redirect to the TriConsumer "ATTRIBUTE_MODIFIER")
        ItemMeta meta = Objects.requireNonNull(item.getItemMeta());
        formula = getContent(data, formula);
        Matcher matcher = Pattern.compile("type=(clear|remove|modify)(,attribute=([a-zA-Z_]+)(,value=(.+))?)?").matcher(formula);
        if (!matcher.matches()) return;
        String type = matcher.group(1);
        if (type.equals("clear")) {
            meta.getAttributeModifiers().keySet().forEach(meta::removeAttributeModifier);
            item.setItemMeta(meta);
            return;
        }
        Attribute attribute = Attribute.valueOf(matcher.group(3));
        if (type.equals("remove")) {
            meta.removeAttributeModifier(attribute);
            item.setItemMeta(meta);
        } else if (type.equals("modify")) {
            String element = matcher.group(5);
            if (element.matches("attribute=(.+),op=(.+),value=(.+),slot=(.+)")) {
                ATTRIBUTE_MODIFIER_EQUIPMENT.accept(data, item, matcher.group(5));
            } else if (element.matches("attribute=(.+),op=(.+),value=(.+)")) {
                ATTRIBUTE_MODIFIER.accept(data, item, matcher.group(5));
            }
        }
    };

    public static final TriConsumer<Map<String, String>, ItemStack, String> RESULT_VALUE_RELOAD = (data, item, formula) -> {
        // formula not used
        data.entrySet().iterator().forEachRemaining(e -> {
            if (e.getKey().startsWith("$result.")) data.remove(e.getKey());
        });

        ItemMeta meta = Objects.requireNonNull(item.getItemMeta());
        Map<String, String> NEW_MAP = getData(meta.getPersistentDataContainer());
        NEW_MAP.forEach((k, v) -> NEW_MAP.put("$result." + k, v));
        data.putAll(NEW_MAP);
    };

    public static final TriConsumer<Map<String, String>, ItemStack, String> CONTAINER = (data, item, formula) -> {
        // type: container, value: type=(add|remove|modify),target=([a-zA-Z_]+)(,value=(.+))?
        // e.g. type: container, value: type=add,target=variable-name.type,value=~~~
        // e.g. type: container, value: type=remove,target=variable-name.type
        // e.g. type: container, value: type=modify,target=variable-name.type,value=~~~
        // a special variable that is named "$CURRENT_VALUE$" contains current value.
        String pattern = "type=(add|remove|modify),target=([a-zA-Z0-9_.%$]*)(,value=(.+))?";
        formula = getContent(data, formula);
        Matcher m = Pattern.compile(pattern).matcher(formula);
        if (!m.matches()) return;
        ItemMeta meta = Objects.requireNonNull(item.getItemMeta());
        String type = m.group(1);
        String target = m.group(2);

        data.put("$CURRENT_VALUE$", data.getOrDefault(target, ""));
        Matcher matcher = Pattern.compile(pattern).matcher(formula);
        data.remove("$CURRENT_VALUES$");
        int index = target.lastIndexOf(".");
        PersistentDataType<?,?> pdt = getPersistentDataType(target.substring(index));
        NamespacedKey nk = new NamespacedKey(CustomCrafter.getInstance(), target.substring(0, index));
        if (pdt == null) return;
        if (type.equals("remove") || type.equals("modify")) {
            item.getItemMeta().getPersistentDataContainer().remove(nk);
        }
        if (type.equals("add") || type.equals("modify")) {
            String value = matcher.group(4);
            if (pdt.equals(PersistentDataType.STRING)) {
                item.getItemMeta().getPersistentDataContainer().set(
                        nk,
                        PersistentDataType.STRING,
                        value);
            } else if (pdt.equals(PersistentDataType.DOUBLE)) {
                item.getItemMeta().getPersistentDataContainer().set(
                        nk,
                        PersistentDataType.DOUBLE,
                        Double.parseDouble(value)
                );
            } else if (pdt.equals(PersistentDataType.LONG)) {
                item.getItemMeta().getPersistentDataContainer().set(
                        nk,
                        PersistentDataType.LONG,
                        Long.parseLong(value)
                );
            }
        }
        item.setItemMeta(meta);
        RESULT_VALUE_RELOAD.accept(data, item, "");
    };


    public static Map<String, String> getData(PersistentDataContainer container) {
        //e.g. test_container_1.double
        // in pdc: variableName does not contains "."
        // in map: variableName contains "."
        Map<String, String> result = new HashMap<>();
        for (NamespacedKey key : container.getKeys()) {
            String name = key.getKey();
            Matcher matcher = Pattern.compile("(.+)\\.(string|double|long|anchor)").matcher(name);
            if (!matcher.matches()) return new HashMap<>();
            String typeString = matcher.group(2);
            if (typeString.matches("(?i)(anchor)")) {
                result.put(name, "");
                continue;
            }
            PersistentDataType<?,?> type = getPersistentDataType(typeString);
            if (type == null) return new HashMap<>();

            result.put(name, String.valueOf(container.get(key, type)));
        }
        return result;
    }

    public static Map<String, String> getData(List<PersistentDataContainer> containers) {
        // variableName.type.index (String, double, long)
        // variableName.type.valueType (String(only "all"), double, long)
        // variableName.type."size" (String, double, long / e.g. test_value.double.size / get list size)
        Map<String, String> result = new HashMap<>();
        Map<String, List<Double>> doubleData = new HashMap<>();
        Map<String, List<Long>> longData = new HashMap<>();
        Map<String, List<String>> stringData = new HashMap<>();
        for (PersistentDataContainer pdc : containers) {
            if (!pdc.isEmpty()) continue;
            for (NamespacedKey key : pdc.getKeys()) {
                Matcher matcher = Pattern.compile("(.+)\\.(string|double|long|anchor)").matcher(key.getKey());
                if (!matcher.matches() || matcher.group(2).equals("anchor")) continue;
                String name = matcher.group(1);
                PersistentDataType<?,?> type = getPersistentDataType(matcher.group(2));
                if (type == null) continue;
                if (type.equals(PersistentDataType.DOUBLE)) {
                    if (!doubleData.containsKey(name)) doubleData.put(name, new LinkedList<>());
                    doubleData.get(name).add(pdc.get(key, PersistentDataType.DOUBLE));
                } else if (type.equals(PersistentDataType.LONG)) {
                    if (!longData.containsKey(name)) longData.put(name, new LinkedList<>());
                    longData.get(name).add(pdc.get(key, PersistentDataType.LONG));
                } else if (type.equals(PersistentDataType.STRING)) {
                    if (!stringData.containsKey(name)) stringData.put(name, new LinkedList<>());
                    stringData.get(name).add(pdc.get(key, PersistentDataType.STRING));
                }
            }
        }

        for (Map.Entry<String, List<String>> entry : stringData.entrySet()) {
            String key = entry.getKey();
            int index = 0;
            for (String element : entry.getValue()) {
                result.put(key + ".string." + index, element);
                index++;
            }
            result.put(key + ".string.size", String.valueOf(entry.getValue().size()));
            result.put(key + ".string.all", String.join(",", entry.getValue()));
        }

        for (Map.Entry<String, List<Double>> entry : doubleData.entrySet()) {
            String key = entry.getKey();
            int index = 0;
            for (double element : entry.getValue()) {
                result.put(key + ".double." + index, String.valueOf(element));
                index++;
            }
            result.put(key + ".double.size", String.valueOf(entry.getValue().size()));
            getDerivedDataDouble(entry.getValue()).forEach((k, v) ->
                    result.put(key + ".double." + k, String.valueOf(v)));

        }

        for (Map.Entry<String, List<Long>> entry : longData.entrySet()) {
            String key = entry.getKey();
            int index = 0;
            for (long element : entry.getValue()) {
                result.put(key + ".long." + index, String.valueOf(element));
                index++;
            }
            result.put(key + ".long.size", String.valueOf(entry.getValue().size()));
            getDerivedDataLong(entry.getValue()).forEach((k, v) ->
                    result.put(key + ".long." + k, String.valueOf(v)));
        }
        return result;
    }

    public static Map<String, Double> getDerivedDataDouble(List<Double> data) {
        Map<String, Double> result = new HashMap<>();
        double total = data.stream().mapToDouble(d -> d).sum();
        result.put("total", total);
        result.put("average", total / data.size());
        result.put("max", Collections.max(data));
        result.put("min", Collections.min(data));
        Collections.sort(data); // natural
        result.put("median", data.get(data.size() / 2 - (data.size() % 2 == 0 ? 1 : 0)));
        return result;
    }

    public static Map<String, Long> getDerivedDataLong(List<Long> data) {
        Map<String, Long> result = new HashMap<>();
        long total = data.stream().mapToLong(l -> l).sum();
        result.put("total", total);
        result.put("average", total / data.size());
        Collections.sort(data); // natural
        result.put("max", data.get(data.size() - 1));
        result.put("min", data.get(0));
        result.put("median", data.get(data.size() / 2 - (data.size() % 2 == 0 ? 1 : 0)));
        return result;
    }

    public static PersistentDataType<?,?> getPersistentDataType(String type) {
        if (type.equalsIgnoreCase("double")) return PersistentDataType.DOUBLE;
        if (type.equalsIgnoreCase("string")) return PersistentDataType.STRING;
        if (type.equalsIgnoreCase("long")) return PersistentDataType.LONG;
        return null;
    }

}
