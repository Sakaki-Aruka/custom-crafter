package com.github.sakakiaruka.customcrafter.customcrafter.util;

import com.destroystokyo.paper.Namespaced;
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
import org.bukkit.FireworkEffect;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.BundleMeta;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.FireworkEffectMeta;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.Repairable;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.inventory.meta.SuspiciousStewMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.profile.PlayerTextures;

import io.papermc.paper.potion.SuspiciousEffectEntry;

import java.math.BigDecimal;
import java.math.MathContext;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ContainerUtil {

    public static Map<String, ItemStack> DEFINED_ITEMS = new HashMap<>();

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
            CONTAINER.accept(data, item, String.join(" ", Arrays.copyOfRange(args, 1, args.length, String[].class)));
        }
    }

    public static final BiFunction<Map<String, String>, String, Boolean> RANDOM = (data, predicate) -> {
        if (predicate.isEmpty()) predicate = "50.00"; // %
        predicate = getContent(data, predicate);
        final String pattern = "\\d+\\.?\\d*";
        if (!predicate.matches(pattern)) {
            sendIllegalTemplateWarn("random predicate", predicate, pattern);
            return false;
        }
        BigDecimal in = new BigDecimal(predicate, MathContext.DECIMAL128);
        if (in.compareTo(BigDecimal.ZERO) < 0) {
            sendOrdinalWarn("random predicate error. (The predicates probability is too small. It must be in range 0 ~ ).");
            return false;
        }
        else if (in.equals(BigDecimal.ZERO)) return false; // 0%
        else if (-1 < in.compareTo(BigDecimal.TEN.pow(2))) return true; // (over) 100%

        BigDecimal norm = BigDecimal.TEN.pow(in.scale() * -1, MathContext.DECIMAL128);
        BigDecimal times = in.divide(norm, MathContext.DECIMAL128);
        BigDecimal all = BigDecimal.TEN.pow(2).divide(norm, MathContext.DECIMAL128);
        BigDecimal MAX = new BigDecimal(Integer.MAX_VALUE);
        if (times.compareTo(MAX) + all.compareTo(MAX) != -2) {
            sendOrdinalWarn("random predicate error. (The required precision is too high.)");
            return false;
        }

        return new Random().nextInt(all.intValue()) > all.subtract(times).intValue();
    };

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
                String key = buffer.toString();
                if (key.startsWith("long:")) {
                    // {long:~~~~~}
                    result.append((long) Expression.eval(key.substring(5)).asDouble());
                } else if (key.startsWith("double:")) {
                    // {double:~~~~}
                    result.append(Expression.eval(key.substring(7)).asDouble());
                }else {
                    // {~~~~}
                    result.append(Expression.eval(key).asString());
                }
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
        formula = getContent(data, formula);
        List<Component> lore = new ArrayList<>();
        if (meta.lore() != null) lore.addAll(meta.lore());
        for (String s : formula.split(SettingsLoad.LINE_SEPARATOR)) {
            lore.add(Component.text(s));
        }
        meta.lore(lore);
        item.setItemMeta(meta);
    };

    public static final TriConsumer<Map<String, String>, ItemStack, String> ENCHANT = (data, item, formula) -> {
        // type: enchant, value: enchant=~~~,level=~~~
        ItemMeta meta = Objects.requireNonNull(item.getItemMeta());
        Map<Enchantment, Integer> contained = new HashMap<>(meta.getEnchants());
        contained.forEach((k, v) -> meta.removeEnchant(k));
        enchantInternal(contained, formula, data).forEach((k, v) -> meta.addEnchant(k, v, true));
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
            sendIllegalTemplateWarn("attribute modifier", formula, "attribute=([a-zA-Z_.]+),op=(?i)(add_number|add_scalar|multiply_scalar_1),value=(-?[0-9]*\\.?[0-9]+)");
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
        // type: attribute_modifier_equipment, value: attribute=~~~, op=~~~, value=~~~, slot=~~~
        // %ATTRIBUTE%, %OPE_TYPE%, %VALUE%, %SLOT%
        formula = getContent(data, formula);
        ItemMeta meta = Objects.requireNonNull(item.getItemMeta());
        Matcher matcher = Pattern.compile("attribute=([a-zA-Z_]+),op=(?i)(add_number|add_scalar|multiply_scalar_1),value=(-?[0-9]*\\.?[0-9]+),slot=([a-zA-Z_]+)").matcher(formula);
        if (!matcher.matches()) {
            sendIllegalTemplateWarn("attribute modifier equipment", formula, "attribute=([a-zA-Z_]+),op=(?i)(add_number|add_scalar|multiply_scalar_1),value=(-?[0-9]*\\.?[0-9]+),slot=([a-zA-Z_]+)");
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
        // duration == -1 -> INFINITY
        formula = getContent(data, formula);
        Matcher matcher = Pattern.compile("effect=([a-zA-Z_]+),level=([0-9]+),duration=(-*[0-9]+)").matcher(formula);
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
            meta.addPages(Component.text(element));
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
            Matcher u = Pattern.compile("[\"{a-zA-Z:]+(https*://[a-zA-Z0-9./%!*'();:@&=+$,?#\\[\\]]+)[}\"]+").matcher(preURL);
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

    public static final TriConsumer<Map<String, String>, ItemStack, String> RESULT_VALUE_SYNC = (data, item, formula) -> {
        // formula not used
        data.entrySet().removeIf(e -> e.getKey().startsWith("$result."));

        ItemMeta meta = Objects.requireNonNull(item.getItemMeta());
        Map<String, String> NEW_MAP = getData(meta.getPersistentDataContainer());
        NEW_MAP.forEach((k, v) -> data.put("$result." + k, v));
        data.put("$RESULT_MATERIAL$", item.getType().name());
        data.put("$RESULT_AMOUNT$", String.valueOf(item.getAmount()));
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

    public static final TriConsumer<Map<String, String>, ItemStack, String> AMOUNT = (data, item, formula) -> {
        // type: amount, value: limit=(true|false),amount=(\\d+)
        // "limit=true" means won't over the material's amount limit.
        // e.g. type: amount, value: limit=true,amount=10
        String pattern = "limit=(true|false),amount=([0-9]{1,4})";
        formula = getContent(data, formula);
        Matcher matcher = Pattern.compile(pattern).matcher(formula);
        if (!matcher.matches()) {
            sendIllegalTemplateWarn("amount", formula, pattern);
            return;
        }

        int amount = Integer.parseInt(matcher.group(2));
        if (Boolean.parseBoolean(matcher.group(1))) {
            // follow the material limit
            int limit = item.getType().getMaxStackSize();
            item.setAmount(Math.min(amount, limit));
        } else {
            item.setAmount(amount);
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

    public static final TriConsumer<Map<String, String>, ItemStack, String> ITEM_DEFINE = (data, item, formula) -> {
        /*
         * define:
         *   - name: [([a-zA-Z_0-9]+)]
         *     base: [([A-Z_]+)]
         *     value: - type: ~~~, value: ~~~
         *            - type: ~~~, value: ~~~
         *
         *
         * putting to the data whose key is "name" and value is ItemStack.
         *
         * format -> |name,base,value-1-length:value-1,value-2-length:value-2|name,base....
         * e.g.) test,stone,21:type:amount, value:10,~~~
         * e.g.) test_2,stone,0
         * e.g.) test_3,stone,0|test_4,dirt,0
         * item does not used.
         */

        formula = getContent(data, formula);
        // flag = 0 (separator = "|"), 1 = (name section), 2 = (base section), 3 = (element section)
        int flag = 0;
        // keys = name, base, values
        Map<String, String> map = new HashMap<>();
        StringBuilder buffer = new StringBuilder();
        for (int i = 0; i < formula.length(); i++) {
            char c = formula.charAt(i);
            if (c == '|') {
                flag = 0;
                buffer.setLength(0);
                map.clear();
                continue;
            }

            if (flag == 0) {
                flag = 1;
                buffer.append(c);
                continue;
            }

            if (flag == 1) {
                if (c != ',') buffer.append(c);
                else {
                    map.put("name", buffer.toString());
                    flag = 2;
                    buffer.setLength(0);
                }
                continue;
            }

            if (flag == 2) {
                if (c != ',') buffer.append(c);
                else {
                    map.put("base", buffer.toString().toUpperCase());
                    DEFINED_ITEMS.put("$" + data.get("$RECIPE_NAME$") + "." + map.get("name") + "$", new ItemStack(Material.valueOf(map.get("base").toUpperCase())));
                    if (i < formula.length() - 1) {
                        if (formula.charAt(i + 1) == '0') {
                            flag = 0;
                            map.clear();
                        }
                        else flag = 3;
                    }
                    buffer.setLength(0);
                }
            }

            if (flag == 3) {
                if (c != ':') {
                    buffer.append(c);
                    continue;
                }
                // ":"
                int len = Integer.parseInt(buffer.toString().replace(",", ""));
                buffer.setLength(0);
                String element = formula.substring(i + 1, i + 1 + len);
                i += len;

                Matcher parsed = Pattern.compile("type: ([a-zA-Z_0-9]+), value: (.+)").matcher(element);
                if (!parsed.matches()) {
                    sendIllegalTemplateWarn("item define", element, "type: ([a-zA-Z_0-9]+), value: (.+)");
                    return;
                }

                TriConsumer<Map<String, String>, ItemStack, String> consumer = SettingsLoad.getConsumer(parsed.group(1).toLowerCase());
                if (consumer == null) {
                    sendNoSuchTemplateWarn("item define", element);
                    return;
                }

                ItemStack defined = DEFINED_ITEMS.get("$" + data.get("$RECIPE_NAME$") + "." + map.get("name") + "$");
                consumer.accept(data, defined, parsed.group(2));
            }
        }
    };

    public static final TriConsumer<Map<String, String>, ItemStack, String> SET_STORAGE_ITEM = (data, item, formula) -> {
        // type: set_storage_item, value: name=([a-zA-Z_0-9]+),slot=(\\d+),amount=(\\d+)
        // bundle only

        if (!item.getType().equals(Material.BUNDLE)) {
            sendOrdinalWarn("(set storage item) Required BUNDLE.");
            return;
        }

        formula = getContent(data, formula);
        final String pattern = "name=([a-zA-Z_0-9]+)(,amount=(\\d+))?(,times=(\\d+))?";
        Matcher parsed = Pattern.compile(pattern).matcher(formula);
        if (!parsed.matches()) {
            sendIllegalTemplateWarn("set storage item", formula, pattern);
            return;
        }
        String name = "$" + data.get("$RECIPE_NAME$") + "." + parsed.group(1) + "$";
        if (!DEFINED_ITEMS.containsKey(name)) {
            sendNoSuchTemplateWarn("defined item", name);
            return;
        }
        BundleMeta meta = (BundleMeta) Objects.requireNonNull(item.getItemMeta());
        ItemStack defined = new ItemStack(DEFINED_ITEMS.get(name));
        if (parsed.group(3) != null) defined.setAmount(Integer.parseInt(parsed.group(3)));
        if (parsed.group(5) != null) {
            for (int i = 0; i < Integer.parseInt(parsed.group(5)); i++) {
                meta.addItem(new ItemStack(defined));
            }
        } else meta.addItem(defined);
        item.setItemMeta(meta);
    };

    public static final TriConsumer<Map<String, String>, ItemStack, String> CAN_PLACE_ON = (data, item, formula) -> {
        // type: can_place_on, value: ([A-Za-z_0-9,]+)
        // separate with ","
        formula = getContent(data, formula);
        final String pattern = "([A-Za-z0-9,_]+)";
        if (!formula.matches(pattern)) {
            sendIllegalTemplateWarn("can place on", formula, pattern);
            return;
        }
        ItemMeta meta = Objects.requireNonNull(item.getItemMeta());
        List<Namespaced> keys = new ArrayList<>();
        for (String block : formula.split(",")) {
            keys.add(NamespacedKey.minecraft(block.toLowerCase()));
        }
        meta.setPlaceableKeys(keys);
        item.setItemMeta(meta);
    };

    public static final TriConsumer<Map<String, String>, ItemStack, String> CAN_DESTROY = (data, item, formula) -> {
        // type: can_destroy, value: ([a-zA-Z0-9,_]+)
        // separate with ","
        formula = getContent(data, formula);
        final String pattern = "([a-zA-Z0-9,_]+)";
        if (!formula.matches(pattern)) {
            sendIllegalTemplateWarn("can destroy", formula, pattern);
            return;
        }
        ItemMeta meta = Objects.requireNonNull(item.getItemMeta());
        List<Namespaced> keys = new ArrayList<>();
        for (String block : formula.split(",")) {
            keys.add(NamespacedKey.minecraft(block.toLowerCase()));
        }
        meta.setDestroyableKeys(keys);
        item.setItemMeta(meta);
    };

    public static final TriConsumer<Map<String, String>, ItemStack, String> REPAIR_COST = (data, item, formula) -> {
        // type: repair_cost, value: (\\d+)
        formula = getContent(data, formula);
        if (!formula.matches("\\d+")) {
            sendIllegalTemplateWarn("repair cost", formula, "\\d+");
            return;
        }
        Repairable meta = (Repairable) Objects.requireNonNull(item.getItemMeta());
        meta.setRepairCost(Integer.parseInt(formula));
        item.setItemMeta(meta);
    };

    private static Color getRGBColor(String formula) {
        formula = formula.toLowerCase();
        final String RANDOM_PATTERN = "random\\[([0-9-]+)?:([0-9-]+)?]";
        final String RGB_PATTERN = "([0-9a-z-\\[\\]:]+)-([0-9a-z-\\[\\]:]+)-([0-9a-z-\\[\\]:]+)";
        Matcher parsed = Pattern.compile(RGB_PATTERN).matcher(formula);
        if (!parsed.matches()) {
            sendIllegalTemplateWarn("rgb", formula, RGB_PATTERN);
            return null;
        }
        final int under = 0;
        final int upper = 255;
        int red = parsed.group(1).matches(RANDOM_PATTERN) ? getRandomNumber(parsed.group(1), under, upper) : Integer.parseInt(parsed.group(1));
        int green = parsed.group(2).matches(RANDOM_PATTERN) ? getRandomNumber(parsed.group(2), under, upper) : Integer.parseInt(parsed.group(2));
        int blue = parsed.group(3).matches(RANDOM_PATTERN) ? getRandomNumber(parsed.group(3), under, upper) : Integer.parseInt(parsed.group(3));
        if (!inRGBRange(red, green, blue)) return null;
        return Color.fromRGB(red, green, blue);
    }

    private static FireworkEffect getFireworkEffect(String formula) {
        // enable to reverse : trail, flicker, color
        // enable to multi value : color, fade
        // exist or not : trail, flicker

        // each rgb element = 0 ~ 255
        FireworkEffect.Builder builder =  FireworkEffect.builder();
        Set<String> types = Set.of("trail", "flicker", "color", "fade", "shape");
        for (String element : formula.toLowerCase().split(",")) {
            boolean ignore = element.startsWith("!");
            Matcher parsed = Pattern.compile("!?([a-z]+)=(.+)").matcher(ignore ? element.substring(1) : element); // skipping first "!"
            if (!parsed.matches()) continue;
            if (!types.contains(parsed.group(1))) continue;
            switch (parsed.group(1)) {
                case "trail" -> builder.trail(ignore);
                case "flicker" -> builder.flicker(ignore);
                case "shape" -> builder.with(FireworkEffect.Type.valueOf(parsed.group(2).toUpperCase()));
                case "color" -> {
                    for (String colorStr : parsed.group(2).split("/")) {
                        Color color;
                        if (colorStr.startsWith("rgb=")) color = getRGBColor(colorStr.replace("rgb=", ""));
                        else color = InventoryUtil.getColor(colorStr);
                        if (color == null) continue;
                        builder.withColor(color);
                    }
                }
                case "fade" -> {
                    for (String fade : parsed.group(2).split("/")) {
                        Color color;
                        if (fade.startsWith("rgb=")) color = getRGBColor(fade.replace("rgb=", ""));
                        else color = InventoryUtil.getColor(fade);
                        if (color == null) continue;
                        builder.withFade(color);
                    }
                }
            }
        }
        return builder.build();
    }

    private static final TriConsumer<Map<String, String>, ItemStack, String> FIREWORK_STAR = (data, item, formula) -> {
        FireworkEffectMeta meta = (FireworkEffectMeta) Objects.requireNonNull(item.getItemMeta());
        formula = getContent(data, formula);
        if (formula.equalsIgnoreCase("clear")) meta.setEffect(null);
        else meta.setEffect(getFireworkEffect(formula));
        item.setItemMeta(meta);
    };

    private static final TriConsumer<Map<String, String>, ItemStack, String> FIREWORK_ROCKET = (data, item, formula) -> {
        // enable set "power"
        FireworkMeta meta = (FireworkMeta) Objects.requireNonNull(item.getItemMeta());
        formula = getContent(data, formula);
        if (formula.equalsIgnoreCase("clear")) meta.clearEffects();
        else meta.addEffect(getFireworkEffect(formula));

        if (formula.contains("power=")) {
            Matcher parsed = Pattern.compile(".*(power=[a-z0-9\\[\\]:]+).*").matcher(formula);
            if (parsed.matches()) {
                // power = 0 ~ 127
                int power = parsed.group(1).startsWith("random")
                        ? getRandomNumber(parsed.group(1).replace("power=", ""), 0, 127)
                        : Integer.parseInt(parsed.group(1).replace("power=", ""));
                if (!(0 <= power && power <= 127)) return;
                meta.setPower(power);
            }
        }
        item.setItemMeta(meta);
    };

    public static final TriConsumer<Map<String, String>, ItemStack, String> FIREWORK = (data, item, formula) -> {
        // type: firework, value: type=(rocket|star),value=(.+)
        // "trail", "flicker" don't have values. These are like anchor.
        // "withColor" is single value sector.
        // "withFade", "withColor" are enable to set multi values sectors. Those are separated with "/".
        // "power" is just for "ROCKET".
        // when use "!" before keys (trail, flicker, fade{need target color}, color {need target color}), remove the target element from a result.

        // clear, trail, flicker, power, color, shape

        // type is decision automatically from the material
        // e.g. type: firework, value: clear  # clear all effects
        // e.g. type: firework, value: power=10,color=green/rgb=100-100-100,shape=ball_large
        // e.g. type: firework, value: fade=green/white/yellow/rgb=100-100-100
        // e.g. type: firework, value: color=rgb=random[100:]-random[100:200]-random[:100]
        // e.g. type: firework, value: trail,flicker,color=green   # add trail, flicker, color(green)
        // e.g. type: firework, value: !trail,!flicker,!color=green   # remove trail, flicker, color(green)
        if (item.getType().equals(Material.FIREWORK_STAR)) FIREWORK_STAR.accept(data, item, formula);
        else if (item.getType().equals(Material.FIREWORK_ROCKET)) FIREWORK_ROCKET.accept(data, item, formula);
    };


    private static void potionInternal(List<PotionEffect> contained, String formula, Map<String, String> data) {
        // P.E.T. = PotionEffectType
        // e.g. value: random[self,!ambient,!particles]->random[beneficial,!self]:[a=1,d=1]
        // non arguments value: ambient, icon, particles
        // numeric arguments value: amplifier, duration
        // enum arguments value: type
        // PotionEffectCategory: beneficial, harmful, neutral
        // can use "!" that means ignore

        // e.g. value: random[self]->random[!self]:[amplifier=10,duration=200,ambient,icon,particles]
        // -> result effect is amplifier = 10, duration = 200 ticks, ambient, icon, particles
        // (need "amplifier"(alias "a") and "duration"(alias "d"))
        // can use random[:] in amplifier, duration. and can use random[] in ambient, icon and particles.
        // ambient, icon, particles random -> (ambient|icon|particles)=random[percentage] or =random[] (get random result from (BiFunction) this.RANDOM)

        // add potion effect -> "value: random[!self]->[amplifier=10,duration=200]"
        // when the target effect not contained the item, can skip to define the second effect.

        formula = getContent(data, formula);
        final String BASE_PATTERN = "([a-zA-Z\\[\\]!,0-9=_]+)->(.+)";
        final String RANDOM_BASE_PATTERN = "random\\[([a-zA-Z!,_]+)]";
        final String RANDOM_EFFECT_PATTERN = "random\\[([a-zA-Z!,_]+)]:\\[([a-zA-Z0-9:,!=\\[\\]]+)]";
        final String EFFECT_CREATE_PATTERN = "([a-zA-Z\\[\\]!,_]+):\\[([a-zA-Z0-9:,!=\\[\\]]+)]";
        final String TO_NUMERIC_PATTERN = "\\[([a-zA-Z0-9:,!=\\[\\]]+)]";
        Matcher parsed = Pattern.compile(BASE_PATTERN).matcher(formula);
        if (!parsed.matches()) {
            sendIllegalTemplateWarn("potion", formula, BASE_PATTERN);
            return;
        }
        PotionEffectType base;
        if (parsed.group(1).matches(RANDOM_BASE_PATTERN)) base = getRandomPotionEffectType(new HashSet<>(contained), parsed.group(1));
        else base = Registry.POTION_EFFECT_TYPE.get(NamespacedKey.minecraft(parsed.group(1).toLowerCase()));
        if (base == null) {
            sendNoSuchTemplateWarn("potion effect (base)", parsed.group(1));
            return;
        }

        data.put("$CURRENT_POTION_DURATION$", String.valueOf(getCurrentDuration(contained, base)));
        data.put("$CURRENT_POTION_AMPLIFIER$", String.valueOf(getCurrentAmplifier(contained, base)));
        formula = getContent(data, formula);
        removeCurrentVariables(data);
        Matcher reParsed = Pattern.compile(BASE_PATTERN).matcher(formula);
        if (!reParsed.matches()) return;
        boolean toNone = reParsed.group(2).equalsIgnoreCase("none");
        boolean toNumeric = reParsed.group(2).matches(TO_NUMERIC_PATTERN);
        boolean change = potionContained(contained, base) && reParsed.group(2).matches(EFFECT_CREATE_PATTERN);

        Matcher overrideParse = Pattern.compile("[a-zA-Z0-9:,=\\[\\]!_]*,?(o|override)=(true|false),?.*").matcher(reParsed.group(2));
        boolean override = !overrideParse.matches() || Boolean.parseBoolean(overrideParse.group(2));

        if (!potionContained(contained, base) && !toNone && toNumeric) {
            // add
            Matcher create = Pattern.compile(TO_NUMERIC_PATTERN).matcher(reParsed.group(2));
            if (!create.matches()) {
                sendIllegalTemplateWarn("potion",reParsed.group(2), TO_NUMERIC_PATTERN);
                return;
            }
            PotionEffect temporary = getBuiltPotionEffect(base, create.group(1));
            if (temporary == null || (!override && potionContained(contained, temporary.getType()))) return;
            contained.add(temporary);
        } else if (potionContained(contained, base) && !toNone && toNumeric) {
            // amplifier, duration change
            PotionEffect temporary = getBuiltPotionEffect(base, reParsed.group(2));
            if (temporary == null || (!override && potionContained(contained, temporary.getType()))) return;
            if (!base.equals(temporary.getType())) contained.remove(getSpecifiedPotionEffectIndex(contained, base));
            contained.add(temporary);
        } else if (!toNone && change) {
            // effect change (!toNone && potionContained && reParsed.group(2).matches(EFFECT_CREATE_PATTERN)
            Matcher create = Pattern.compile(EFFECT_CREATE_PATTERN).matcher(reParsed.group(2));
            if (!create.matches()) {
                sendIllegalTemplateWarn("potion",reParsed.group(2), TO_NUMERIC_PATTERN);
                return;
            }
            PotionEffectType type = reParsed.group(2).matches(RANDOM_EFFECT_PATTERN)
                    ? getRandomPotionEffectType(new HashSet<>(contained), create.group(1))
                    : Registry.POTION_EFFECT_TYPE.get(NamespacedKey.minecraft(create.group(1).toLowerCase()));
            if (type == null) {
                sendNoSuchTemplateWarn("potion effect (target)", create.group(1));
                return;
            } else if (!override && potionContained(contained, type)) return;
            PotionEffect temporary = getBuiltPotionEffect(type, reParsed.group(2));
            if (temporary == null) return;
            if (!base.equals(temporary.getType())) contained.remove(getSpecifiedPotionEffectIndex(contained, base));
            contained.add(temporary);
        }
    }

    private static int getSpecifiedPotionEffectIndex(List<PotionEffect> effects, PotionEffectType type) {
        for (int i = 0; i < effects.size(); i ++) {
            if (effects.get(i).getType().equals(type)) return i;
        }
        return -1;
    }

    private static PotionEffect getBuiltPotionEffect(PotionEffectType type, String formula) {
        Matcher a = Pattern.compile("[a-zA-Z0-9:,=\\[\\]!_]*,?(a|amplifier)=([0-9]+|random\\[[0-9]*:[0-9]*]),?.*").matcher(formula);
        Matcher d = Pattern.compile("[a-zA-Z0-9:,=\\[\\]!_]*,?(d|duration)=(-?[0-9]+|random\\[-?[0-9]*:-?[0-9]*]),?.*").matcher(formula);
        if (!a.matches() || !d.matches()) return null;
        int amplifier = a.group(2).matches("\\d+")
                ? Integer.parseInt(a.group(2))
                : getRandomNumber(a.group(2), 0, 255);
        int duration = d.group(2).matches("-?[0-9]+")
                ? Integer.parseInt(d.group(2))
                : getRandomNumber(d.group(2), 20, 20 * 60 * 60); // 20 * 60 * 60 ticks = 1 hour
        PotionEffect effect = type.createEffect(duration, amplifier);
        for (String element : formula.split(",")) {
            if (element.matches("!?ambient")) effect = effect.withAmbient(!(element.startsWith("!")));
            else if (element.matches("!?icon")) effect = effect.withIcon(!(element.startsWith("!")));
            else if (element.matches("!?particles")) effect = effect.withParticles(!(element.startsWith("!")));
        }
        return effect;
    }

    private static boolean potionContained(List<PotionEffect> list, PotionEffectType type) {
        return list.stream().anyMatch(e -> e.getType().equals(type));
    }

    private static int getCurrentDuration(List<PotionEffect> list, PotionEffectType type) {
        for (PotionEffect p : list) {
            if (p.getType().equals(type)) return p.getDuration();
        }
        return 0;
    }

    private static int getCurrentAmplifier(List<PotionEffect> list, PotionEffectType type) {
        for (PotionEffect p : list) {
            if (p.getType().equals(type)) return p.getAmplifier();
        }
        return 0;
    }

    private static PotionEffectType getRandomPotionEffectType(Set<PotionEffect> contained, String formula) {
        List<PotionEffectType> candidate = new ArrayList<>();
        Matcher parsed = Pattern.compile("random\\[([!a-zA-Z_,]+)]").matcher(formula);
        if (!parsed.matches()) return null;
        for (String element : parsed.group(1).split(",")) {
            if (element.equalsIgnoreCase("self")) contained.forEach(e -> candidate.add(e.getType()));
            else if (element.equalsIgnoreCase("!self")) contained.forEach(e -> candidate.remove(e.getType()));
            else if (element.equalsIgnoreCase("all")) candidate.addAll(Registry.POTION_EFFECT_TYPE.stream().collect(Collectors.toSet()));
            else if (element.equalsIgnoreCase("!all")) candidate.clear();
            else if (element.equalsIgnoreCase("beneficial")) candidate.addAll(getAllBeneficialEffects());
            else if (element.equalsIgnoreCase("!beneficial")) candidate.removeAll(getAllBeneficialEffects());
            else if (element.equalsIgnoreCase("harmful")) candidate.addAll(getAllHarmfulEffects());
            else if (element.equalsIgnoreCase("!harmful")) candidate.removeAll(getAllHarmfulEffects());
            else if (element.equalsIgnoreCase("neural")) candidate.addAll(getAllNeuralEffects());
            else if (element.equalsIgnoreCase("!neural")) candidate.removeAll(getAllNeuralEffects());
            else if (element.matches(getAllPotionEffectsRegexPattern())) candidate.add(Registry.POTION_EFFECT_TYPE.get(NamespacedKey.minecraft(element.toLowerCase())));
            else if (element.equalsIgnoreCase("ambient")) candidate.addAll(getAllAmbientEffects(contained));
            else if (element.equalsIgnoreCase("!ambient")) candidate.removeAll(getAllAmbientEffects(contained));
            else if (element.equalsIgnoreCase("icon")) candidate.addAll(getAllIconDisplayedEffects(contained));
            else if (element.equalsIgnoreCase("!icon")) candidate.removeAll(getAllIconDisplayedEffects(contained));
            else if (element.equalsIgnoreCase("particles")) candidate.addAll(getAllParticlesDisplayedEffects(contained));
            else if (element.equalsIgnoreCase("!particles")) candidate.removeAll(getAllParticlesDisplayedEffects(contained));
        }
        return candidate.isEmpty() ? null : candidate.get(new Random().nextInt(candidate.size()));
    }

    private static Set<PotionEffectType> getAllAmbientEffects(Set<PotionEffect> data) {
        return data
                .stream()
                .filter(PotionEffect::isAmbient)
                .map(PotionEffect::getType)
                .collect(Collectors.toSet());
    }

    private static Set<PotionEffectType> getAllIconDisplayedEffects(Set<PotionEffect> data) {
        return data
                .stream()
                .filter(PotionEffect::hasIcon)
                .map(PotionEffect::getType)
                .collect(Collectors.toSet());
    }

    private static Set<PotionEffectType> getAllParticlesDisplayedEffects(Set<PotionEffect> data) {
        return data
                .stream()
                .filter(PotionEffect::hasParticles)
                .map(PotionEffect::getType)
                .collect(Collectors.toSet());
    }

    private static Set<PotionEffectType> getAllBeneficialEffects() {
        return Registry.POTION_EFFECT_TYPE
                .stream()
                .filter(e -> e.getEffectCategory().equals(PotionEffectType.Category.BENEFICIAL))
                .collect(Collectors.toSet());
    }

    private static Set<PotionEffectType> getAllHarmfulEffects() {
        return Registry.POTION_EFFECT_TYPE
                .stream()
                .filter(e -> e.getEffectCategory().equals(PotionEffectType.Category.HARMFUL))
                .collect(Collectors.toSet());
    }

    private static Set<PotionEffectType> getAllNeuralEffects() {
        return Registry.POTION_EFFECT_TYPE
                .stream()
                .filter(e -> e.getEffectCategory().equals(PotionEffectType.Category.NEUTRAL))
                .collect(Collectors.toSet());
    }

    private static String getAllPotionEffectsRegexPattern() {
        StringBuilder builder = new StringBuilder("(");
        Registry.POTION_EFFECT_TYPE
                .stream()
                .forEach(e -> builder.append(Registry.POTION_EFFECT_TYPE.getKey(e).toString().replace("minecraft:", "")).append("|"));
        builder.deleteCharAt(builder.length() - 1); // remove last "|"
        builder.append(")");
        return builder.toString();
    }

    public static final TriConsumer<Map<String, String>, ItemStack, String> STEW = (data, item, formula) -> {
        // type: stew, value: {target}->(.+)
        // the formula rule looks like "enchant"
        SuspiciousStewMeta meta = (SuspiciousStewMeta) Objects.requireNonNull(item.getItemMeta());

        List<PotionEffect> contained = new ArrayList<>(meta.getCustomEffects());
        potionInternal(contained, formula, data);
        meta.clearCustomEffects();
        contained.forEach(e -> meta.addCustomEffect(SuspiciousEffectEntry.create(e.getType(), e.getDuration()), true));
        item.setItemMeta(meta);
    };

    private static Map<Enchantment, Integer> enchantInternal(Map<Enchantment, Integer> contained, String formula, Map<String, String> data) {
        // when detect illegal or invalid actions, this method returns the map that named "contained" contained in arguments.
        formula = getContent(data, formula);
        final String FORMULA_PATTERN = "type=(enchant|level),action=([a-zA-Z_\\[\\](),!]+)->(.+)";
        final String TARGET_PATTERN = "[a-zA-Z_]+|random(\\[!?\\([A-Za-z_,]+\\)])?";
        final String RANDOM_TARGET_PATTERN = "random(\\[!?\\([A-Za-z_,]+\\)])?";
        final String RANDOM_NUMBER_PATTERN = "[0-9]+|random(\\[([0-9-]+)?:([0-9-]+)?])?";
        Matcher parsed = Pattern.compile(FORMULA_PATTERN).matcher(formula);
        if (!parsed.matches()) {
            sendIllegalTemplateWarn("enchant", formula, FORMULA_PATTERN);
            return contained;
        }

        boolean isEnchant = parsed.group(1).equalsIgnoreCase("enchant");
        Matcher targetMatcher = Pattern.compile(TARGET_PATTERN).matcher(parsed.group(2));
        if (!targetMatcher.matches()) {
            sendIllegalTemplateWarn("enchant (source)", parsed.group(2), TARGET_PATTERN);
            return contained;
        }
        boolean isRandomTarget = parsed.group(2).matches(RANDOM_TARGET_PATTERN);
        Enchantment target;

        if (isRandomTarget) target = getRandomEnchantment(contained.keySet(), parsed.group(2));
        else target = Registry.ENCHANTMENT.get(NamespacedKey.minecraft(parsed.group(2).toLowerCase()));

        if (target == null) {
            sendNoSuchTemplateWarn("enchant element", parsed.group(2));
            return contained;
        }

        data.put("$CURRENT_ENCHANT_LEVEL$", contained.containsKey(target)
                ? String.valueOf(contained.get(target))
                : "0"
        );
        formula = getContent(data, formula);
        removeCurrentVariables(data);
        Matcher reMatch = Pattern.compile(FORMULA_PATTERN).matcher(formula);
        if (!reMatch.matches()) return contained;
        boolean toNone = reMatch.group(3).equalsIgnoreCase("None");
        boolean toNumeric = reMatch.group(3).matches(RANDOM_NUMBER_PATTERN);

        if (toNone) contained.remove(target);
        else if (toNumeric) {
            int level = reMatch.group(3).matches("\\d+")
                    ? Integer.parseInt(reMatch.group(3))
                    : getRandomNumber(reMatch.group(3), 1, 255);

            if (isEnchant && !contained.containsKey(target)) contained.put(target, level);
            else if (!isEnchant && contained.containsKey(target)) {
                contained.remove(target);
                contained.put(target, level);
            }
        }
        else if (!isEnchant) contained.remove(target); // !toNumeric && !isEnchant
        else {
            // !toNumeric && isEnchant
            if (!reMatch.group(3).matches(RANDOM_TARGET_PATTERN)) {
                // !toRandom
                int level = contained.get(target);
                contained.remove(target);
                contained.put(Registry.ENCHANTMENT.get(NamespacedKey.minecraft(reMatch.group(3).toLowerCase())), level);
            } else {
                Enchantment destination = getRandomEnchantment(contained.keySet(), reMatch.group(3));
                if (destination == null) contained.remove(target);
                else {
                    int level = contained.get(target);
                    contained.remove(target);
                    contained.put(destination, level);
                }
            }
        }
        return contained;
    }


    public static final TriConsumer<Map<String, String>, ItemStack, String> ENCHANT_BOOK = (data, item, formula) -> {
        // type: enchant_book, value: type=(enchant|level),action=([a-zA-Z_]+)->(.+)

        // e.g. type: enchant_book, value: type=enchant,action=fortune->5 (add fortune level 5)

        // e.g. type: enchant_book, value: type=enchant,action=fortune->None (remove fortune)
        // e.g. type: enchant_book, value: type=enchant,action=fortune->mending (fortune to mending)

        // e.g. type: enchant_book, value: type=enchant,action=random->random[!(fortune,smite,looting)] (random(contained) to random(without fortune, smite, looting))
        // e.g. type: enchant_book, value: type=enchant,action=random->random[(fortune, smite, looting)] (random(contained) to random(from fortune, smite, looting))

        // e.g. type: enchant_book, value: type=enchant,action=random[(fortune,smite)]->random (random(fortune or smite) to random(without fortune, smite))
        // e.g. type: enchant_book, value: type=enchant,action=random[(fortune,smite)]->random[(fortune,smite,looting,None)] (random(fortune or smite) to random(fortune, smite, looting) or remove)
        // e.g. type: enchant_book, value: type=enchant,action=random[(!self)]->fortune (random (all, but does not contain self) to fortune)
        // e.g. type: enchant_book, value: type=enchant,action=random[(self)]->random[!(self,fortune)] (random(contained) to random(without contained and fortune))
        // e.g. type: enchant_book, value: type=enchant,action=random[(all)]->fortune (random (all) to fortune)

        // -------------------------------------------------------------------------------------------------------------

        // e.g. type: enchant_book, value: type=level,action=fortune->None (remove fortune)

        // e.g. type: enchant_book, value: type=level,action=fortune->random[:] (fortune's level to 1 ~ 255)
        // e.g. type: enchant_book, value: type=level,action=random->random[:] (set random(contained)'s level to 1 ~ 255)

        // e.g. type: enchant_book, value: type=level,action=fortune->random[3:] (set fortune's level to 3 ~ 256)
        // e.g. type: enchant_book, value: type=level,action=fortune->random[:10] (set fortune's level 1 ~ 10)
        // e.g. type: enchant_book, value: type=level,action=fortune->random[10:20] (set fortune's level 10 ~ 20)
        // e.g. type: enchant_book, value: type=level,action=fortune->random[:] (1 ~ 255, same with "action=fortune->random")

        EnchantmentStorageMeta meta = (EnchantmentStorageMeta) Objects.requireNonNull(item.getItemMeta());
        Map<Enchantment, Integer> contained = new HashMap<>(meta.getStoredEnchants());
        contained.forEach((k, v) -> meta.removeStoredEnchant(k));
        enchantInternal(contained, formula, data).forEach((k, v) -> meta.addStoredEnchant(k, v, true));
        item.setItemMeta(meta);
    };

    public static int getRandomNumber(String formula, int underLimit, int upperLimit) {
        // e.g. [3:] (3 ~ upper limit)
        // e.g. [:10] (under limit ~ 10)
        // e.g. [5:20] (5 ~ 20)
        // e.g. [:] (under limit ~ upper limit)

        final String pattern = "random\\[([0-9-]+)?:([0-9-]+)?]";
        Matcher parsed = Pattern.compile(pattern).matcher(formula);
        if (!parsed.matches()) {
            return 0;
        }

        if (underLimit == upperLimit) return upperLimit;
        if (parsed.group(1) == null && parsed.group(2) == null) {
            // [:]
            return getInRange(underLimit, upperLimit + 1);
        } else if (parsed.group(1) != null && parsed.group(2) == null) {
            // [([0-9-]+):]
            return getInRange(Integer.parseInt(parsed.group(1)), upperLimit + 1);
        } else if (parsed.group(1) == null && parsed.group(2) != null) {
            // [:([0-9-]+)]
            return getInRange(underLimit, Integer.parseInt(parsed.group(2)) + 1);
        } else {
            // [([0-9-]+):([0-9-]+)]
            return getInRange(Integer.parseInt(parsed.group(1)), Integer.parseInt(parsed.group(2)) + 1);
        }
    }

    private static int getInRange(int under, int upper) {
        return new Random().ints(1, under, upper).toArray()[0];
    }

    private static Enchantment getRandomEnchantment(Set<Enchantment> enchants, String formula) {
        // e.g. random[!(self)] (from all, but does not contain self)
        // e.g. random[(all)] (from all)
        // e.g. random[(fortune,lure,looting)] (from these 3)
        // e.g. random[!(fortune,lure,looting)] (from all that does not contain these 3)

        final String pattern = "random(\\[!?\\([A-Za-z,_]+\\)])?";
        Matcher parsed = Pattern.compile(pattern).matcher(formula);
        if (!parsed.matches()) {
            return null;
        }

        Set<Enchantment> all = Registry.ENCHANTMENT.stream().collect(Collectors.toSet());
        String element = parsed.group(1).replaceAll("[()\\[\\]]", "");
        Set<String> enc = new HashSet<>(Arrays.asList(element.replace("!", "").toLowerCase().split(",")));
        if (element.startsWith("!")) {
            // without
            Set<Enchantment> remove = new HashSet<>();
            enc.forEach(s -> {
                switch (s) {
                    case "none" -> remove.add(null);
                    case "self" -> remove.addAll(enchants); // !self
                    case "all" -> remove.addAll(all);
                    default -> remove.add(Registry.ENCHANTMENT.get(NamespacedKey.minecraft(s)));
                }
            });
            all.removeAll(remove);
            if (all.isEmpty()) return null;
            return new ArrayList<>(all).get(new Random().nextInt(all.size()));
        }

        Set<Enchantment> set = new HashSet<>();
        enc.forEach(s -> {
            switch (s) {
                case "none" -> set.add(null);
                case "self" -> set.addAll(enchants); // self
                case "all" -> set.addAll(all); // all
                default -> set.add(Registry.ENCHANTMENT.get(NamespacedKey.minecraft(s)));
            }
        });
        if (!enc.contains("none")) set.remove(null);
        return new ArrayList<>(set).get(new Random().nextInt(set.size()));
    }


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
