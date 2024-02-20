package com.github.sakakiaruka.customcrafter.customcrafter.util;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.github.sakakiaruka.customcrafter.customcrafter.CustomCrafter;
import com.github.sakakiaruka.customcrafter.customcrafter.SettingsLoad;
import com.github.sakakiaruka.customcrafter.customcrafter.interfaces.TriConsumer;
import com.github.sakakiaruka.customcrafter.customcrafter.object.AnchorTagType;
import com.github.sakakiaruka.customcrafter.customcrafter.object.Matter.Container.MatterContainer;
import com.github.sakakiaruka.customcrafter.customcrafter.object.Matter.Matter;
import com.github.sakakiaruka.customcrafter.customcrafter.object.Recipe.Coordinate;
import com.github.sakakiaruka.customcrafter.customcrafter.object.Recipe.Recipe;
import com.github.sakakiaruka.customcrafter.customcrafter.search.Search;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.profile.PlayerTextures;

import java.net.URL;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.UUID;
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

    public static void commandMain(Player player, String[] args) {
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item.getType().equals(Material.AIR)) {
            player.sendMessage("[Custom Crafter] No items in your main hand.");
            return;
        }

        if (args[1].equals("show")) {
            // /cc container (show container in main hand)
            getData(item.getItemMeta().getPersistentDataContainer())
                    .forEach((k, v) -> player.sendMessage("key=" + k + ", value=" + v));
        } else {
            Map<String, String> data = getData(item.getItemMeta().getPersistentDataContainer());
            args[0] = "";
            CONTAINER.accept(data, item, String.join(" ", args));
        }
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
            String left = setEvalValue(setPlaceholderValue(data, formula.substring(0, i)));
            String right = setEvalValue(setPlaceholderValue(data, formula.substring(i + 1)));
            return right.matches(left);
        }
        return false;
    };

    public static final BiFunction<Map<String, String>, String, Boolean> ALLOW_VALUE = (data, predicate) -> {
        // type: (?i)(Allow)Value, predicate: ~~~~
        return Expression.eval(setEvalValue(setPlaceholderValue(data, predicate))).asBoolean();
    };

    public static final BiFunction<Map<String, String>, String, Boolean> DENY_VALUE = (data, predicate) -> {
        // type: (?i)(Deny)Value, predicate: ~~~~
        return !Expression.eval(setEvalValue(setPlaceholderValue(data, predicate))).asBoolean();
    };

    private static boolean anyContained(Map<String, String> data, String key) {
        int count = 0;
        if (data.containsKey(key + "long")) count++;
        if (data.containsKey(key + "double")) count++;
        if (data.containsKey(key + "string")) count++;
        if (data.containsKey(key + "anchor")) count++;
        return count != 0;
    }

    public static final BiFunction<Map<String, String>, String, Boolean> ALLOW_TAG = (data, predicate) -> {
        // type: (?i)(Allow)Tag, predicate: ~~~,~~~,~~~
        // divided ",".
        int count = 0;
        String[] sources = predicate.replace(" ", "").split(",");
        for (String key : sources) {
            if (key.endsWith("*")) {
                String removed = key.substring(0, key.length() - 1);
                count += anyContained(data, removed) ? 1 : 0;
            } else if (data.containsKey(key)) count++;
        }
        return count == sources.length;
    };

    public static final BiFunction<Map<String, String>, String, Boolean> DENY_TAG = (data, predicate) -> {
        // type: (?i)(Deny)Tag, predicate: ~~~,~~~,~~~
        // divided ",".
        int count = 0;
        String[] sources = predicate.replace(" ", "").split(",");
        for (String key : sources) {
            if (key.endsWith("*")) {
                String removed = key.substring(0, key.length() - 1);
                count += anyContained(data, removed) ? 0 : 1;
            } else if (!data.containsKey(key)) count++;
        }
        return count == sources.length;
    };

    // ====================================================================

    private static String getContent(Map<String, String> data, String formula) {
        return setEvalValue(setPlaceholderValue(data, formula));
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

    private static void sendIllegalTemplateWarn(String type, String source, String pattern) {
        Bukkit.getLogger().warning(String.format("%s===%s[Custom Crafter] Illegal %s pattern. (%s)", SettingsLoad.LINE_SEPARATOR,SettingsLoad.LINE_SEPARATOR, type, source));
        Bukkit.getLogger().warning(String.format("[Custom Crafter] The source pattern is %s.%s===", pattern, SettingsLoad.LINE_SEPARATOR));
    }

    private static void sendNoSuchTemplateWarn(String type, String source) {
        Bukkit.getLogger().warning(String.format("%s===%s[Custom Crafter] No such %s. (%s)%s===", SettingsLoad.LINE_SEPARATOR,SettingsLoad.LINE_SEPARATOR, type, source, SettingsLoad.LINE_SEPARATOR));
    }

    private static void sendOrdinalWarn(String warn) {
        Bukkit.getLogger().warning(String.format("%s===%s[Custom Crafter] %s%s===", SettingsLoad.LINE_SEPARATOR, SettingsLoad.LINE_SEPARATOR, warn, SettingsLoad.LINE_SEPARATOR));
    }

    public static final TriConsumer<Map<String, String>, ItemStack, String> LORE = (data, item, formula) -> {
        // type: lore, value: ~~~
        // formula -> ~~~
        ItemMeta meta = Objects.requireNonNull(item.getItemMeta());
        if (formula == null) {
            meta.lore(null);
            item.setItemMeta(meta);
            return;
        }
        List<Component> lore = new ArrayList<>();
        if (meta.lore() != null) lore.addAll(meta.lore());
        lore.add(Component.text(getContent(data, formula)));
        meta.lore(lore);
        item.setItemMeta(meta);
    };

    public static final TriConsumer<Map<String, String>, ItemStack, String> ENCHANT = (data, item, formula) -> {
        // type: enchant, value: enchant=~~~,level=~~~
        ItemMeta meta = Objects.requireNonNull(item.getItemMeta());
        formula = getContent(data, formula);
        Matcher matcher = Pattern.compile("enchant=([a-zA-Z_]+),level=([0-9]+)").matcher(formula);
        if (!matcher.matches()) {
            sendIllegalTemplateWarn("enchant", formula, "enchant=([a-zA-Z_]+),level=([0-9]+)");
            return;
        }
        Enchantment enchantment = Enchantment.getByKey(NamespacedKey.minecraft(matcher.group(1).toLowerCase()));
        if (enchantment == null) {
            sendNoSuchTemplateWarn("enchant", matcher.group(1));
            return;
        }
        int level = (int) Double.parseDouble(matcher.group(2));
        meta.addEnchant(enchantment, level, true);
        item.setItemMeta(meta);
    };

    public static final TriConsumer<Map<String, String>, ItemStack, String> POTION_COLOR_RGB = (data, item, formula) -> {
        // type: potion_color, value: r=100,g=100,b=100
        // %RED%, %GREEN%, %BLUE%
        formula = getContent(data, formula);
        ItemMeta meta = Objects.requireNonNull(item.getItemMeta());
        PotionMeta potionMeta = (PotionMeta) meta;
        Matcher matcher = Pattern.compile("red=([0-9]+),green=([0-9]+),blue=([0-9]+)").matcher(formula);
        if (!matcher.matches()) {
            sendIllegalTemplateWarn("potion color rgb", formula, "red=([0-9]+),green=([0-9]+),blue=([0-9]+)");
            return;
        }
        int red = (int) Double.parseDouble(matcher.group(1));
        int green = (int) Double.parseDouble(matcher.group(2));
        int blue = (int) Double.parseDouble(matcher.group(3));
        if (!inRGBRange(red, green, blue)) {
            sendOrdinalWarn("Illegal rgb element. (Out of 0 ~ 255)");
            return;
        }
        Color color = Color.fromRGB(red, green, blue);
        potionMeta.setColor(color);
        item.setItemMeta(meta);
    };

    public static final TriConsumer<Map<String, String>, ItemStack, String> POTION_COLOR_NAME = (data, item, formula) -> {
        // type: potion_color, value: white
        // %COLOR%
        ItemMeta meta = Objects.requireNonNull(item.getItemMeta());
        Color color = InventoryUtil.getColor(getContent(data, formula.toUpperCase()));
        if (color == null) {
            sendNoSuchTemplateWarn("color name", formula);
            return;
        }
        ((PotionMeta) meta).setColor(color);
        item.setItemMeta(meta);
    };

    public static final TriConsumer<Map<String, String>, ItemStack, String> TEXTURE_ID = (data, item, formula) -> {
        // type: texture_id, value: 100
        // %TEXTURE_ID%
        formula = getContent(data, formula);
        ItemMeta meta = Objects.requireNonNull(item.getItemMeta());
        Matcher matcher = Pattern.compile("([0-9]+)").matcher(formula);
        if (!matcher.matches()) {
            sendIllegalTemplateWarn("texture id", formula, "([0-9]+)");
            return;
        }
        int id = (int) Double.parseDouble(matcher.group(1));
        meta.setCustomModelData(id);
        item.setItemMeta(meta);
    };

    public static final TriConsumer<Map<String, String>, ItemStack, String> TOOL_DURABILITY_PERCENTAGE = (data, item, formula) -> {
        // type: tool_durability_percentage, value: 100%
        // %DAMAGE%, %MATERIAL%
        formula = getContent(data, formula);
        ItemMeta meta = Objects.requireNonNull(item.getItemMeta());
        Matcher matcher = Pattern.compile("([0-9]*)\\.?([0-9]+)").matcher(formula);
        if (!matcher.matches()) {
            sendIllegalTemplateWarn("tool durability percentage", formula, "([0-9]*)\\.?([0-9]+)");
            return;
        }
        Material material = item.getType();
        int lastOne = material.getMaxDurability() - 1;
        double percentage = Double.parseDouble(formula);
        int damage = Math.max(lastOne, (int) (material.getMaxDurability() * percentage));
        ((Damageable) meta).setDamage(damage);
    };

    public static final TriConsumer<Map<String, String>, ItemStack, String> TOOL_DURABILITY_REAL = (data, item, formula) -> {
        // type: tool_durability_real_number, value: 100
        formula = getContent(data, formula);
        ItemMeta meta = Objects.requireNonNull(item.getItemMeta());
        Matcher matcher = Pattern.compile("([0-9]+)").matcher(formula);
        if (!matcher.matches()) {
            sendIllegalTemplateWarn("tool durability real", formula, "([0-9]+)");
            return;
        }
        Material material = item.getType();
        int damage = (int) Double.parseDouble(formula);
        if (material.getMaxDurability() - damage <= 0) damage = material.getMaxDurability() - 1;
        ((Damageable) meta).setDamage(Math.min(damage, material.getMaxDurability()));
    };

    public static final TriConsumer<Map<String, String>, ItemStack, String> ITEM_NAME = (data, item, formula) -> {
        // type: item_name, value: this is an item
        // "" (empty string)
        ItemMeta meta = Objects.requireNonNull(item.getItemMeta());
        meta.displayName(Component.text(getContent(data,formula)));
        item.setItemMeta(meta);
    };

    public static final TriConsumer<Map<String, String>, ItemStack, String> ATTRIBUTE_MODIFIER = (data, item, formula) -> {
        ItemMeta meta = Objects.requireNonNull(item.getItemMeta());
        formula = getContent(data, formula);
        Matcher matcher = Pattern.compile("attribute=([a-zA-Z_]+),op=(?i)(add_number|add_scalar|multiply_scalar_1),value=(-?[0-9]*\\.?[0-9]+)").matcher(formula);
        if (!matcher.matches()) {
            sendIllegalTemplateWarn("attribute modifier", formula, "attribute=([a-zA-Z_]+),op=(?i)(add_number|add_scalar|multiply_scalar_1),value=(-?[0-9]*\\.?[0-9]+)");
            return;
        }
        Attribute attribute = Attribute.valueOf(matcher.group(1).toLowerCase());
        AttributeModifier.Operation operation = AttributeModifier.Operation.valueOf(matcher.group(2).toUpperCase());
        double value = Double.parseDouble(matcher.group(3));
        AttributeModifier modifier = new AttributeModifier(UUID.randomUUID(), UUID.randomUUID().toString(), value, operation);
        meta.addAttributeModifier(attribute, modifier);
        item.setItemMeta(meta);
    };

    public static final TriConsumer<Map<String, String>, ItemStack, String> ATTRIBUTE_MODIFIER_EQUIPMENT = (data, item, formula) -> {
        // type: attribute_modifier_equipment, value: attribute=~~~, ope=~~~, value=~~~, slot=~~~
        // %ATTRIBUTE%, %OPE_TYPE%, %VALUE%, %SLOT%
        formula = getContent(data, formula);
        ItemMeta meta = Objects.requireNonNull(item.getItemMeta());
        Matcher matcher = Pattern.compile("attribute=([a-zA-Z_]+),op=(?i)(add_number|add_scalar|multiply_scalar_1),value=(-?[0-9]*\\.?[0-9]+),slot=([a-zA-Z_]+)").matcher(formula);
        if (!matcher.matches()) {
            sendIllegalTemplateWarn("attribute modifier equipment", formula, "attribute=([a-zA-Z_]+),op=([a-zA-Z_]+),op=(?i)(add_number|add_scalar|multiply_scalar_1),value=(-?[0-9]*\\.?[0-9]+),slot=([a-zA-Z_]+)");
            return;
        }
        Attribute attribute = Attribute.valueOf(matcher.group(1).toUpperCase());
        AttributeModifier.Operation operation = AttributeModifier.Operation.valueOf(matcher.group(2).toUpperCase());
        double value = Double.parseDouble(matcher.group(3));
        EquipmentSlot slot = EquipmentSlot.valueOf(matcher.group(4).toUpperCase());
        AttributeModifier modifier = new AttributeModifier(UUID.randomUUID(), UUID.randomUUID().toString(), value, operation, slot);
        meta.addAttributeModifier(attribute, modifier);
        item.setItemMeta(meta);
    };

    public static final TriConsumer<Map<String, String>, ItemStack, String> ITEM_FLAG = (data, item, formula) -> {
        // type: item_flag, value: flag=~~~, action=(clear, remove, add)
        // %FLAG%, %ACTION%
        formula = getContent(data, formula);
        ItemMeta meta = Objects.requireNonNull(item.getItemMeta());
        Matcher matcher = Pattern.compile("flag=([a-zA-Z_]+),action=(?i)(clear|remove|add)").matcher(formula);
        if (!matcher.matches()) {
            sendIllegalTemplateWarn("item flag", formula, "flag=([a-zA-Z_]+),action=(?i)(clear|remove|add)");
            return;
        }
        String type = matcher.group(2);
        if (type.equalsIgnoreCase("clear")) {
            meta.getItemFlags().forEach(meta::removeItemFlags);
            return;
        }

        ItemFlag flag = ItemFlag.valueOf(matcher.group(1).toUpperCase());
        if (type.equalsIgnoreCase("remove")) {
            meta.removeItemFlags(flag);
        } else if (type.equalsIgnoreCase("add")) {
            meta.addItemFlags(flag);
        }
    };

    public static final TriConsumer<Map<String, String>, ItemStack, String> UNBREAKABLE = (data, item, formula) -> {
        // type: unbreakable, value: ~~~
        ItemMeta meta = Objects.requireNonNull(item.getItemMeta());
        meta.setUnbreakable(Boolean.parseBoolean(getContent(data, formula)));
    };

    public static final TriConsumer<Map<String, String>, ItemStack, String> POTION_EFFECT = (data, item, formula) -> {
        // type: potion_effect, value: effect=~~~,level=~~~,duration=~~~
        // duration is game tick
        formula = getContent(data, formula);
        Matcher matcher = Pattern.compile("effect=([a-zA-Z_]+),level=([0-9]+),duration=([0-9]+)").matcher(formula);
        if (!matcher.matches()) {
            sendIllegalTemplateWarn("potion effect", formula, "effect=([a-zA-Z_]+),level=([0-9]+),duration=([0-9]+)");
            return;
        }
        PotionMeta meta = (PotionMeta) Objects.requireNonNull(item.getItemMeta());
        PotionEffectType effect = PotionEffectType.getByName(matcher.group(1).toUpperCase());
        if (effect == null) {
            sendNoSuchTemplateWarn("potion effect", matcher.group(1));
            return;
        }
        int level = (int) Double.parseDouble(matcher.group(2));
        int duration = (int) Double.parseDouble(matcher.group(3));
        PotionEffect potion = new PotionEffect(effect, duration, level);
        meta.addCustomEffect(potion, true);
        item.setItemMeta(meta);
    };

    public static final TriConsumer<Map<String, String>, ItemStack, String> LEATHER_ARMOR_COLOR = (data, item, formula) -> {
        // type: leather_armor_color, value: (r=~~,g=~~,b=~~|colorName|random)
        LeatherArmorMeta meta = (LeatherArmorMeta) Objects.requireNonNull(item.getItemMeta());
        data.put("$CURRENT_RED$", String.valueOf(meta.getColor().getRed()));
        data.put("$CURRENT_GREEN$", String.valueOf(meta.getColor().getGreen()));
        data.put("$CURRENT_BLUE$", String.valueOf(meta.getColor().getBlue()));
        data.put("$CURRENT_RGB$", String.valueOf(meta.getColor().asRGB()));
        formula = getContent(data, formula);
        removeCurrentVariables(data);

        Matcher rgb = Pattern.compile("r=([0-9]+),g=([0-9]+),b=([0-9]+)").matcher(formula);
        Matcher name = Pattern.compile("([a-zA-Z_]+)").matcher(formula);
        Matcher random = Pattern.compile("(?i)random").matcher(formula);
        if (!rgb.matches() || !name.matches() || !random.matches()) return;
        Color color;
        if (rgb.matches()) {
            int red = (int) Double.parseDouble(rgb.group(1));
            int green = (int) Double.parseDouble(rgb.group(2));
            int blue = (int) Double.parseDouble(rgb.group(3));
            if (!inRGBRange(red, green, blue)) return;
            color = Color.fromRGB(red, green, blue);
        } else if (name.matches() && !random.matches()) {
            color = InventoryUtil.getColor(name.group(1));
        } else if (random.matches()) {
            color = Color.fromRGB(new Random().nextInt(256));
        } else return;
        if (color == null) return;
        meta.setColor(color);
        item.setItemMeta(meta);
    };

    public static TriConsumer<Map<String, String>, ItemStack, String> BOOK = (data, item, formula) -> {
        // type: book, value: type=(?i)(author|title|add_page|from_file|long_field|gen), element=(.+)
        formula = getContent(data, formula);
        Matcher matcher = Pattern.compile("type=(author|title|add_page|add_long|from_file|gen),element=(.+)").matcher(formula);
        if (!matcher.matches()) return;
        String type = matcher.group(1);
        String element = matcher.group(2);
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

    public static final TriConsumer<Map<String, String>, ItemStack, String> HEAD = (data, item, formula) -> {
        // type: head, value: type=(name|url),value=(.+)
        // e.g. type: head, value:  type=name,value=notch (notch's head)
        // e.g. type: head, value: type=url,value=eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMjRhODgyY2MyNjczM2Q5ZGEyYjVmYjYyMTE4ZDdlMzdkMDNjM2Q2YWY3ZWI1MzczYmIwYjE5N2Y5OTc5YjliOCJ9fX0= (BLT sandwich head)
        if (!(item.getItemMeta() instanceof SkullMeta)) {
            sendOrdinalWarn("The subject item cannot retain data on the skull.");
            return;
        }
        SkullMeta meta = (SkullMeta) Objects.requireNonNull(item.getItemMeta());
        formula = getContent(data, formula);
        String pattern = "type=(name|url),value=(.+)";
        Matcher formulaParse = Pattern.compile(pattern).matcher(formula);
        if (!formulaParse.matches()) {
            sendIllegalTemplateWarn("head", formula, pattern);
            return;
        }
        String type = formulaParse.group(1);
        String value = formulaParse.group(2);
        if (type.equals("name")) {
            meta.setOwningPlayer(Bukkit.getOfflinePlayer(value));
            item.setItemMeta(meta);
            return;
        }
        PlayerProfile profile = Bukkit.createProfile(null, UUID.randomUUID().toString());
        PlayerTextures textures = profile.getTextures();
        URL url;
        try {
            String preURL = new String(Base64.getDecoder().decode(value));
            Matcher u = Pattern.compile("[\"{a-zA-Z:]+(http://[a-zA-Z0-9./]+)[}\"]+").matcher(preURL);
            if (!u.matches()) {
                sendOrdinalWarn("head url parse error. (internal error)");
                return;
            }
            url = new URL(u.group(1));
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        textures.setSkin(url);
        profile.setTextures(textures);

        meta.setPlayerProfile(profile);
        item.setItemMeta(meta);
    };

    public static final TriConsumer<Map<String, String>, ItemStack, String> ENCHANT_MODIFY = (data, item, formula) -> {
        // type: enchant_modify, value: type=(enchant|level),action=(.+)->(.+)
        // e.g. type: enchant_modify, value: type=enchant,action=luck->mending (luck to mending)
        // e.g. type: enchant_modify, value: type=enchant,action=luck->None (remove enchant(luck))
        // e.g. type: enchant_modify, value: type=level,action=luck->2 (luck's level change (to 2))
        // e.g. type: enchant_modify, value: type=level,action=luck->None (remove enchant(luck))
        // a special variable that is named $CURRENT_LEVEL$ contains current enchants level. (in using "%$CURRENT_LEVEL$%")
        ItemMeta meta = Objects.requireNonNull(item.getItemMeta());
        formula = getContent(data, formula);
        Matcher matcher = Pattern.compile("type=(enchant|level),action=([a-zA-Z_]+)->([{}+\\-*/\\\\^%$a-zA-Z0-9_]+)").matcher(formula);
        if (!matcher.matches()) return;
        String type = matcher.group(1);
        String left = matcher.group(2);
        String right = matcher.group(3);
        boolean toNone = right.equalsIgnoreCase("None");
        boolean changeEnchant = type.equals("enchant");

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
        List<Component> lore = new ArrayList<>();
        if (meta.hasLore()) {
            lore.addAll(meta.lore());
            for (int i = 0; i < meta.lore().size(); i++) {
                data.put("$CURRENT_LINE."+ i +"$", ((TextComponent) meta.lore().get(i)).content());
            }
        }
        data.put("$CURRENT_LINES$", String.valueOf(lore.size()));
        formula = getContent(data, formula);
        removeCurrentVariables(data);
        Matcher one = Pattern.compile("type=(clear|modify)(,value=type=(remove|insert),line=([0-9]+)(,value=(.+))*)?").matcher(formula);
        if (!one.matches()) {
            sendIllegalTemplateWarn("lore modify", formula, "type=(clear|modify)(,value=type=(remove|insert),line=([0-9]+)(,value=(.+))*)?");
            return;
        }
        if (one.group(1).equalsIgnoreCase("clear")) {
            meta.lore(null);
            item.setItemMeta(meta);
            return;
        }

        int line = Integer.parseInt(one.group(4));
        if (line < 0) {
            sendOrdinalWarn("A specified line index is out of the valid range.");
            return;
        }
        if (one.group(3).equalsIgnoreCase("remove")) {
            lore.remove(line);
        } else if (one.group(3).equalsIgnoreCase("insert")) {
            String element = one.group(6);
            lore.add(line, Component.text(element));
        }
        meta.lore(lore);
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
        Attribute attribute = Attribute.valueOf(matcher.group(3).toUpperCase());
        if (type.equals("remove")) {
            meta.removeAttributeModifier(attribute);
            item.setItemMeta(meta);
        } else if (type.equals("modify")) {
            String element = matcher.group(5);
            if (element.matches("attribute=([a-zA-Z_]+),op=([a-zA-Z_]+),value=([\\d.-]+),slot=([a-zA-Z_]+)")) {
                ATTRIBUTE_MODIFIER_EQUIPMENT.accept(data, item, matcher.group(5));
            } else if (element.matches("attribute=([a-zA-Z_]+),op=([a-zA-Z_]+),value=([\\d.-]+)")) {
                ATTRIBUTE_MODIFIER.accept(data, item, matcher.group(5));
            }
        }
    };

    public static final TriConsumer<Map<String, String>, ItemStack, String> RESULT_VALUE_RELOAD = (data, item, formula) -> {
        // formula not used
        data.entrySet().removeIf(e -> e.getKey().startsWith("$result."));

        ItemMeta meta = Objects.requireNonNull(item.getItemMeta());
        Map<String, String> NEW_MAP = getData(meta.getPersistentDataContainer());
        NEW_MAP.forEach((k, v) -> data.put("$result." + k, v));
    };

    public static final TriConsumer<Map<String, String>, ItemStack, String> CONTAINER = (data, item, formula) -> {
        // type: container, value: type=(add|remove|modify),target=([a-zA-Z_]+)(,value=(.+))?
        // e.g. type: container, value: type=add,target=variable-name.type,value=~~~
        // e.g. type: container, value: type=remove,target=variable-name.type
        // e.g. type: container, value: type=modify,target=variable-name.type,value=~~~
        // a special variable that is named "$CURRENT_VALUE$" contains current value.
        formula = getContent(data, formula);
        String pattern = "type=(add|remove|modify),target=([a-zA-Z0-9_.%$]*)(,value=(.+))?";
        Matcher m = Pattern.compile(pattern).matcher(formula);
        if (!m.matches()) {
            sendIllegalTemplateWarn("container", formula, pattern);
            return;
        }
        ItemMeta meta = Objects.requireNonNull(item.getItemMeta());
        String type = m.group(1);
        String target = m.group(2);

        data.put("$CURRENT_VALUE$", data.getOrDefault(target, ""));
        Matcher matcher = Pattern.compile(pattern).matcher(formula);
        removeCurrentVariables(data);
        if (!matcher.matches()) {
            sendIllegalTemplateWarn("container", formula, pattern);
            return;
        }

        int index = target.lastIndexOf(".");
        PersistentDataType<?,?> pdt = getPersistentDataType(target.substring(index + 1));
        NamespacedKey nk = new NamespacedKey(CustomCrafter.getInstance(), target);
        if (pdt == null) {
            sendNoSuchTemplateWarn("persistent data type", target.substring(index + 1));
            return;
        }
        if (type.equals("remove") || type.equals("modify")) {
            if (meta.getPersistentDataContainer().has(nk)) meta.getPersistentDataContainer().remove(nk);
        }
        if (type.equals("add") || type.equals("modify")) {
            String value = matcher.group(4);
            if (pdt.equals(PersistentDataType.STRING)) {
                meta.getPersistentDataContainer().set(
                        nk,
                        PersistentDataType.STRING,
                        value);
            } else if (pdt.equals(PersistentDataType.DOUBLE)) {
                meta.getPersistentDataContainer().set(
                        nk,
                        PersistentDataType.DOUBLE,
                        Double.parseDouble(value)
                );
            } else if (pdt.equals(PersistentDataType.LONG)) {
                meta.getPersistentDataContainer().set(
                        nk,
                        PersistentDataType.LONG,
                        Long.parseLong(value)
                );
            } else if (pdt.getPrimitiveType().equals(UUID.class) && pdt.getComplexType().equals(UUID.class)) {
                // AnchorTagType
                meta.getPersistentDataContainer().set(
                        nk,
                        AnchorTagType.TYPE,
                        UUID.randomUUID()
                );
            }
        }
        item.setItemMeta(meta);
        RESULT_VALUE_RELOAD.accept(data, item, "");
    };

    public static final TriConsumer<Map<String, String>, ItemStack, String> MATERIAL = (data, item, formula) -> {
        // type: material, value: ~~~
        // e.g. type: material, value: air
        formula = getContent(data, formula).toUpperCase();
        try {
            item.setType(Material.valueOf(formula));
        } catch (Exception e) {
            sendOrdinalWarn("Failed to set a material.");
        }
    };

    public static final TriConsumer<Map<String, String>, ItemStack, String> RUN_COMMAND_AS_CONSOLE = (data, item, formula) -> {
        // type: run_command_as_player, value: ~~~
        formula = getContent(data, formula);
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), formula);
    };

    public static final TriConsumer<Map<String, String>, ItemStack, String> RUN_COMMAND_AS_PLAYER = (data, item, formula) -> {
        // type: run_command_as_player, value: ~~~
        formula = getContent(data, formula);
        Player player = Bukkit.getPlayer(UUID.fromString(data.get("$PLAYER_UUID$")));
        if (player == null) {
            sendNoSuchTemplateWarn("player (from UUID)", formula);
            return;
        }
        Bukkit.dispatchCommand(player, formula);
    };


    private static void removeCurrentVariables(Map<String, String> data) {
        data.entrySet().removeIf(element -> element.getKey().matches("\\$CURRENT_[A-Z0-9_.]+\\$"));
    }

    public static Map<String, String> getData(PersistentDataContainer container) {
        //e.g. test_container_1.double
        // in pdc: variableName does not contain "."
        // in map: variableName contains "."
        // the types overview "long, double, string, anchor, *"
        // -> anchor is only used to "tag".
        // -> "*" is a wildcard. (it means all types.)
        Map<String, String> result = new HashMap<>();
        for (NamespacedKey key : container.getKeys()) {
            String name = key.getKey();
            Matcher matcher = Pattern.compile("([a-zA-Z0-9_]+)\\.(string|double|long|anchor)").matcher(name);
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
            if (pdc.isEmpty()) continue;
            for (NamespacedKey key : pdc.getKeys()) {
                Matcher matcher = Pattern.compile("([a-zA-Z0-9_]+)\\.(string|double|long|anchor)").matcher(key.getKey());
                if (!matcher.matches()) continue;
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
                } else if (type.equals(AnchorTagType.TYPE)) {
                    result.put(name, "");
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
        int size = data.size();
        result.put("total", total);
        result.put("average", total / data.size());
        Collections.sort(data); // natural
        result.put("max", data.get(data.size() - 1));
        result.put("min", data.get(0));
        if (size < 3) {
            result.put("median", result.get("average"));
            return result;
        }
        double median = size % 2 == 0 ? (data.get(size / 2 - 1) + data.get(size / 2)) / 2 : data.get(size / 2);
        result.put("median", median);
        return result;
    }

    public static Map<String, Long> getDerivedDataLong(List<Long> data) {
        Map<String, Long> result = new HashMap<>();
        int size = data.size();
        long total = data.stream().mapToLong(l -> l).sum();
        result.put("total", total);
        result.put("average", total / size);
        Collections.sort(data); // natural
        result.put("max", data.get(size - 1));
        result.put("min", data.get(0));
        if (size < 3) {
            result.put("median", result.get("average"));
            return result;
        }
        long median = size % 2 == 0 ? (data.get(size / 2 - 1) + data.get(size / 2)) / 2 : data.get(size / 2);
        result.put("median", median);
        return result;
    }

    public static PersistentDataType<?,?> getPersistentDataType(String type) {
        if (type.equalsIgnoreCase("double")) return PersistentDataType.DOUBLE;
        if (type.equalsIgnoreCase("string")) return PersistentDataType.STRING;
        if (type.equalsIgnoreCase("long")) return PersistentDataType.LONG;
        if (type.equals("anchor")) return AnchorTagType.TYPE;
        return null;
    }

}
