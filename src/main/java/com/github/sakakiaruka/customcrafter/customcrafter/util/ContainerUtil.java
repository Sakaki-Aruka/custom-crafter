package com.github.sakakiaruka.customcrafter.customcrafter.util;

import com.github.sakakiaruka.customcrafter.customcrafter.SettingsLoad;
import com.github.sakakiaruka.customcrafter.customcrafter.interfaces.TriConsumer;
import com.github.sakakiaruka.customcrafter.customcrafter.object.ContainerWrapper;
import com.github.sakakiaruka.customcrafter.customcrafter.object.Matter.Container.MatterContainer;
import com.github.sakakiaruka.customcrafter.customcrafter.object.Matter.Matter;
import com.github.sakakiaruka.customcrafter.customcrafter.object.Recipe.Coordinate;
import com.github.sakakiaruka.customcrafter.customcrafter.object.Recipe.Recipe;
import com.github.sakakiaruka.customcrafter.customcrafter.search.Search;
import com.google.common.collect.Multimap;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
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

import javax.management.ObjectName;
import java.io.ObjectStreamException;
import java.nio.file.Path;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.github.sakakiaruka.customcrafter.customcrafter.CustomCrafter.getInstance;

public class ContainerUtil {
//    public static Map<String, Map<Integer, ContainerWrapper>> containers = new HashMap<>();
//    public static final String ALLOW_TAG = "allow_tag";
//    public static final String ALLOW_VALUE = "allow_value";
//    public static final String DENY_TAG = "deny_tag";
//    public static final String DENY_VALUE = "deny_value";
//    public static final String STORE_ONLY = "store_only";
//
//    //---
//    private static final String ARROW_RANGE_PATTERN = "^([0-9a-zA-Z+\\-*/()$_]+)<-->([0-9a-zA-Z+\\-*/()$_]+)$";
//    private static final String LARGER_PATTERN = "^([0-9a-zA-Z+\\-*/()$_]+)<$";
//    private static final String SMALLER_PATTERN = "<([0-9a-zA-Z+\\-*/()$_]+)";
//    private static final String NUMBER_EQUALS_PATTERN = "==([0-9a-zA-Z+\\-*/()$_]+)";
//    private static final String STRING_EQUALS_PATTERN = "==\\[(.+)]";
//    private static final String STRING_IGNORE_EQUALS_PATTERN = "\\?=\\[(.+)]";
//    private static final String FOLLOW_REGEX_PATTERN = "r=\\[(.+)]";
//    private static final String CONTAINER_OPERATION_PATTERN = "([+\\-/*^])";
//
//    private static final String RECIPE_CONTAINER_ARROW_RANGE_PATTERN = "^([0-9a-zA-Z+\\-*/()$_\\[\\]]+)<--\\[(maximum|minimum|median|mode|average|random)\\]-->([0-9a-zA-Z+\\-*/()$_\\[\\]]+)$";
//    private static final String RECIPE_CONTAINER_LARGER_PATTERN = "^([0-9a-zA-Z+\\-*/()$_\\[\\]]+)<\\[(maximum|minimum|median|mode|average|random)\\]$";
//    private static final String RECIPE_CONTAINER_SMALLER_PATTERN = "^\\[(maximum|minimum|median|mode|average|random)\\]<([0-9a-zA-Z+\\-*/()$_\\[\\]]+)$";
//    private static final String RECIPE_CONTAINER_EQUAL_PATTERN = "^\\[(maximum|minimum|median|mode|average|random)\\]==([0-9a-zA-Z+\\-*/()$_\\[\\]]+)$";
//
//    private static final String MULTI_VALUE_PATTERN = "^\\(multi\\)\\(types:(.+)\\)(.+)$";
//    private static final String MULTI_VALUE_CLASS_PATTERN = "([\\w]+)\\*([\\d]+)";
//
//    private static final String USING_CONTAINER_VALUES_LORE_PATTERN = "^using_container_values_lore -> (.+)$";
//    private static final String USING_CONTAINER_VALUES_ENCHANTMENT_PATTERN = "^using_container_values_enchantment -> enchantment:([$a-zA-Z0-9\\-_]+)/level:(\\$[a-z0-9\\-_]+|[0-9]+)$";
//    private static final String USING_CONTAINER_VALUES_POTION_COLOR_RGB_PATTERN = "^using_container_values_potion_color -> type:(?i)(rgb)/value:R->([$a-z0-9\\-_]+),G->([$a-z0-9\\-_]+),B->([$a-z0-9\\-_]+)$";
//    private static final String USING_CONTAINER_VALUES_POTION_COLOR_RANDOM_PATTERN = "^using_container_values_potion_color -> type:(?i)(random)$";
//    private static final String USING_CONTAINER_VALUES_TOOL_DURABILITY_ABSOLUTE_PATTERN = "^using_container_values_tool_durability -> type:absolute/value:([$a-z0-9\\-_]+)$";
//    private static final String USING_CONTAINER_VALUES_TOOL_DURABILITY_PERCENTAGE_PATTERN = "^using_container_values_tool_durability -> type:percentage/value:([$a-z0-9\\-_]+)$";
//    private static final String USING_CONTAINER_VALUES_TEXTURE_ID_PATTERN = "^using_container_values_texture_id -> ([a-z0-9\\-_]+)$";
//    private static final String USING_CONTAINER_VALUES_ITEM_NAME_PATTERN = "^using_container_values_item_name -> (.+)$";
//    private static final int ENCHANTMENT_MAX_LEVEL = 255;
//
//
//    public static PersistentDataType getDataType(String input) {
//        if (input.equalsIgnoreCase("string")) return PersistentDataType.STRING;
//        if (input.equalsIgnoreCase("int")) return PersistentDataType.INTEGER;
//        if (input.equalsIgnoreCase("double")) return PersistentDataType.DOUBLE;
//        return null;
//    }

//
//    public static Map<Integer, ContainerWrapper> mattersLoader(Path path) {
//        Map<Integer, ContainerWrapper> map = new LinkedHashMap<>();
//        FileConfiguration config = YamlConfiguration.loadConfiguration(path.toFile());
//        int current = 0;
//        while (true) {
//            if (!config.contains("terms."+current)) break;
//            String thumb =  "terms."+current+".";
//
//            String tag = config.getString(thumb+"tag");
//            int order = config.getInt(thumb+"order");
//            NamespacedKey key = new NamespacedKey(getInstance(), config.getString(thumb+"key"));
//            PersistentDataType type = getDataType(config.getString(thumb+"type"));
//            String value = "";
//            if (config.contains(thumb+"value")) {
//                value = config.getString(thumb+"value");
//            }
//
//            ContainerWrapper wrapper = new ContainerWrapper(key, type, value,order,tag);
//
//            if (!map.containsKey(order)) map.put(order, wrapper);
//            current++;
//        }
//
//        String name = config.getString("name");
//
//        containers.put(name, map);
//        return map;
//    }


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
//
//    private static double getFormulaValue(String input, PersistentDataContainer container) {
//        List<String> list = new ArrayList<>();
//        List<String> buffer = new ArrayList<>();
//        input = input.replace(" ","");
//        for (int i=0;i<input.length();i++) {
//            String s = String.valueOf(input.charAt(i));
//            if (i == input.length()-1 && !buffer.isEmpty()) {
//                String variableName = String.join("",buffer).replace("$","") + (s.equals(")") ? "" : s);
//                buffer.clear();
//                NamespacedKey key = new NamespacedKey(getInstance(), variableName);
//                PersistentDataType type = PersistentDataType.DOUBLE;
//                String value = container.has(key, type) ? container.get(key, type).toString() : "0";
//                list.add(value);
//                if (s.equals(")")) list.add(s);
//                break;
//            }
//
//            if (s.equals("$")) {
//                buffer.add(s);
//                continue;
//            }
//
//            if (!buffer.isEmpty() && s.matches(CONTAINER_OPERATION_PATTERN)) {
//                String variableName = String.join("", buffer).replace("$","");
//                buffer.clear();
//                NamespacedKey key = new NamespacedKey(getInstance(), variableName);
//                PersistentDataType type = PersistentDataType.DOUBLE;
//                String value = container.has(key, type) ? container.get(key, type).toString() : "0";
//                list.add(value);
//                list.add(s);
//                continue;
//            }
//
//            if (!buffer.isEmpty()) {
//                buffer.add(s);
//                continue;
//            }
//
//            list.add(s);
//        }
//
//        String formula = String.join("", list);
//        return calc(formula);
//
//    }
//
//
//    private static double calc(String input) {
//
//        if (input.matches("^(\\d*)(\\.?)(\\d)+$")) return Double.parseDouble(input);
//
//        List<String> outQueue = new ArrayList<>();
//        List<String> stack = new ArrayList<>();
//
//        String buffer = "";
//
//        for (int i=0;i<input.length();i++) {
//            String s = String.valueOf(input.charAt(i));
//            if (s.matches("\\d") || s.equals("~") || s.equals(".")) buffer += s;
//            if (s.equals("(")) {
//                if (!buffer.isEmpty()) {
//                    if (buffer.contains("~")) buffer = buffer.replace("~","-");
//                    outQueue.add(buffer);
//                    buffer = "";
//                }
//
//                stack.add(0,s);
//            }
//            if (s.equals(")")) {
//                if (!buffer.isEmpty()) {
//                    if (buffer.contains("~")) buffer = buffer.replace("~","-");
//                    outQueue.add( buffer);
//                    buffer = "";
//                }
//                int start = stack.indexOf("(");
//                outQueue.addAll(stack.subList(0, start));
//                while (start > -1) {
//                    stack.remove(0);
//                    start--;
//                }
//                continue;
//            }
//            if (s.matches("(\\+|\\-|\\*|/|\\^)")) {
//                if (!buffer.isEmpty()) {
//                    if (buffer.contains("~")) buffer = buffer.replace("~","-");
//                    outQueue.add( buffer);
//                    buffer = "";
//                }
//
//                if (stack.size() == 0) {
//                    stack.add(0,s);
//                    continue;
//                }
//
//                int _newPriority = getPriority(s);
//                int _collectedPriority = getPriority(stack.get(0));
//                if (_newPriority > _collectedPriority) {
//                    stack.add(0, s);
//                    continue;
//                }
//
//                if (!s.equals("^")) {
//                    if (!buffer.isEmpty()) {
//                        if (buffer.contains("~")) buffer = buffer.replace("~","-");
//                        outQueue.add( buffer);
//                        buffer = "";
//                    }
//                    outQueue.add(stack.get(0));
//                    stack.remove(0);
//                }
//                stack.add(0, s);
//            }
//        }
//        if (!buffer.isEmpty()) outQueue.add(buffer);
//        outQueue.addAll(stack);
//        return rpnCalc(outQueue);
//    }
//
//    private static int getPriority(String s) {
//        if (s.equals("^")) return 4;
//        if (s.equals("*")) return 3;
//        if (s.equals("/")) return 3;
//        if (s.equals("+")) return 2;
//        if (s.equals("-")) return 2;
//        return -1;
//    }
//
//    private static double rpnCalc(List<String> list) {
//        double accumlator = 0;
//        List<Double> stacks = new ArrayList<>();
//        for (String s : list) {
//            if (!s.matches("(\\+|-|\\*|/|^)")) {
//                String t = s.contains("~") ? s.replace("~", "-") : s ;
//                stacks.add(Double.parseDouble(t));
//                continue;
//            }
//            double element1 = stacks.get(stacks.size()-1);
//            double element2 = stacks.get(stacks.size()-2);
//            if (s.equals("+")) accumlator = element2 + element1;
//            if (s.equals("-")) accumlator = element2 - element1;
//            if (s.equals("*")) accumlator = element2 * element1;
//            if (s.equals("/")) accumlator = element2 / element1;
//            if (s.equals("^")) accumlator = Math.pow(element2, element1);
//
//            stacks.remove(element1);
//            stacks.remove(element2);
//            stacks.add(accumlator);
//        }
//        return accumlator;
//    }
//
//
//    public static void setContainerDataItemStackToMatter(ItemStack item, Matter matter) {
//        ItemMeta meta = item.getItemMeta();
//        PersistentDataContainer container = meta.getPersistentDataContainer();
//        Map<Integer, ContainerWrapper> wrappers = new LinkedHashMap<>();
//
//        List<NamespacedKey> keys = new ArrayList<>(container.getKeys());
//        for (int i=0;i<keys.size();i++) {
//            NamespacedKey key = keys.get(i);
//            PersistentDataType type;
//            if ((type = getSpecifiedKeyType(container, key)) == null) return;
//            // order: i
//            // tag: STORE_ONLY
//            // value: value
//            String value = String.valueOf(container.get(key, type));
//            ContainerWrapper wrapper = new ContainerWrapper(key, type, value, i, STORE_ONLY);
//            wrappers.put(i, wrapper);
//        }
//        matter.setContainerWrappers(wrappers);
//    }
//
//    public static PersistentDataType getSpecifiedKeyType(PersistentDataContainer container, NamespacedKey key) {
//        for (PersistentDataType type : getDefaultDataTypes()) {
//            try{
//                container.get(key, type);
//                return type;
//            }catch (Exception e) {
//                continue;
//            }
//        }
//        return null;
//    }
//
//    private static List<PersistentDataType> getDefaultDataTypes() {
//        List<PersistentDataType> types = new ArrayList<>();
//        types.add(PersistentDataType.STRING);
//        types.add(PersistentDataType.INTEGER);
//        types.add(PersistentDataType.DOUBLE);
//        return types;
//    }
//
//    public static String containerValues(PersistentDataContainer container) {
//        StringBuilder builder = new StringBuilder();
//        for (NamespacedKey key : container.getKeys()) {
//            builder.append(BAR + LINE_SEPARATOR);
//            builder.append("key: "+key.toString()+ LINE_SEPARATOR);
//            PersistentDataType type = getSpecifiedKeyType(container, key);
//            builder.append("type: "+type.getComplexType().getSimpleName()+ LINE_SEPARATOR);
//            builder.append("value: "+container.get(key, type).toString()+ LINE_SEPARATOR);
//            builder.append(BAR + LINE_SEPARATOR);
//        }
//        return builder.toString();
//    }

//    public static void setRecipeDataContainerToResultItem(ItemStack item, Recipe input, Recipe recipe) {
//        // String -> NK, List<String> -> each containers data
//        Map<String, List<String>> stringTypeData = new HashMap<>();
//        Map<String, List<Double>> numericTypeData = new HashMap<>();
//        for (Matter matter : input.getContentsNoAir()) {
//            if (!matter.hasContainer()) continue;
//            for (Map.Entry<Integer, ContainerWrapper> entry : matter.getContainerWrappers().entrySet()) {
//                String key = entry.getValue().getKey().toString();
//                String value = entry.getValue().getValue();
//                PersistentDataType type = entry.getValue().getType();
//                if (type.equals(PersistentDataType.STRING)) {
//                    if (!stringTypeData.containsKey(key)) stringTypeData.put(key, new ArrayList<>());
//                    stringTypeData.get(key).add(value);
//                } else if (type.equals(PersistentDataType.INTEGER) || type.equals(PersistentDataType.DOUBLE)) {
//                    if (!numericTypeData.containsKey(key)) numericTypeData.put(key, new ArrayList<>());
//                    numericTypeData.get(key).add(Double.parseDouble(value));
//                }
//            }
//        }
//        //---
//        Map<String, Map<String, Double>> derivedMap = getDerivedValues(numericTypeData);
//        ItemMeta meta = item.getItemMeta();
//        PersistentDataContainer container = meta.getPersistentDataContainer();
//        for (Map.Entry<NamespacedKey, List<RecipeDataContainer>> entry : recipe.getContainer().entrySet()) {
//            NamespacedKey key = entry.getKey();
//            for (RecipeDataContainer rdc : entry.getValue()) {
//                PersistentDataType type = rdc.getDataType();
//                RecipeDataContainerModifyType modifyType = rdc.getModifyType();
//                String term = rdc.getTerm();
//                String action = rdc.getAction();
//                boolean isEnd = rdc.isEnd();
//                boolean isNumeric = type.equals(PersistentDataType.INTEGER) || type.equals(PersistentDataType.DOUBLE);
//
//                if (modifyType.equals(RecipeDataContainerModifyType.MAKE)) {
//                    if (type.equals(PersistentDataType.STRING)) {
//                        String value = action.isEmpty() ? "" : action;
//                        container.set(key, type, value);
//                    } else if (type.equals(PersistentDataType.DOUBLE)) {
//                        double value = Double.parseDouble(action.isEmpty() ? "0" : action);
//                        container.set(key, type, value);
//                    } else if (type.equals(PersistentDataType.INTEGER)) {
//                        int value = Integer.parseInt(action.isEmpty() ? "0" : action);
//                        container.set(key, type, value);
//                    }
//                }
//
//                if (!isNumeric) {
//                    Set<Boolean> resultSet = new HashSet<>();
//                    boolean isAllMatch = term.equalsIgnoreCase("[all_match]");
//                    stringTypeData.get(key.toString()).forEach(e-> resultSet.add(e.matches(term)));
//                    if ((!isAllMatch && resultSet.contains(true) || (isAllMatch && !resultSet.contains(false)))) {
//                        //success
//                        container.set(key, type, action);
//                    }
//                }
//
//                Matcher arrow = Pattern.compile(RECIPE_CONTAINER_ARROW_RANGE_PATTERN).matcher(term);
//                if (isNumeric && arrow.matches()) {
//                    double start = getValueFromString(arrow.group(1), derivedMap);
//                    double value = derivedMap.get(key.toString()).get(arrow.group(2));
//                    double end = getValueFromString(arrow.group(3), derivedMap);
//                    if (start < value && value < end) {
//                        setValuesToContainer(key, type, getValueFromString(action, derivedMap), container);
//                    }
//                }
//
//                Matcher larger = Pattern.compile(RECIPE_CONTAINER_LARGER_PATTERN).matcher(term);
//                if (isNumeric && larger.matches()) {
//                    double small = getValueFromString(larger.group(1), derivedMap);
//                    double value = derivedMap.get(key.toString()).get(larger.group(2));
//                    if (small < value) setValuesToContainer(key, type, getValueFromString(action, derivedMap), container);
//
//                }
//
//                Matcher smaller = Pattern.compile(RECIPE_CONTAINER_SMALLER_PATTERN).matcher(term);
//                if (isNumeric && smaller.matches()) {
//                    double value = derivedMap.get(key.toString()).get(smaller.group(1));
//                    double large = getValueFromString(smaller.group(2), derivedMap);
//                    if (value < large) setValuesToContainer(key, type, getValueFromString(action, derivedMap), container);
//
//                }
//
//                Matcher equal = Pattern.compile(RECIPE_CONTAINER_EQUAL_PATTERN).matcher(term);
//                if (isNumeric && equal.matches()) {
//                    double value = derivedMap.get(key.toString()).get(equal.group(1));
//                    double comparison = getValueFromString(equal.group(2), derivedMap);
//                    if (value == comparison) setValuesToContainer(key, type, getValueFromString(action, derivedMap), container);
//                }
//
//                if (isEnd) {
//                    item.setItemMeta(meta);
//                    return;
//                }
//            }
//        }
//        item.setItemMeta(meta);
//    }
//
//    private static void setValuesToContainer(NamespacedKey key, PersistentDataType type, double value, PersistentDataContainer container) {
//        if (type.equals(PersistentDataType.INTEGER)) container.set(key, type, (int) value);
//        if (type.equals(PersistentDataType.DOUBLE)) container.set(key, type, value);
//    }
//    private static double getValueFromString(String formula, Map<String, Map<String, Double>> derived) {
//        List<String> queue = new ArrayList<>();
//        List<String> buffer = new ArrayList<>();
//        for (int i=0; i<formula.length(); i++) {
//            String s = String.valueOf(formula.charAt(i));
//            if (s.equals("$")) {
//                buffer.add(s);
//                continue;
//            }
//
//            if (s.matches("(\\+|-|/|\\*|\\(|\\)|\\^|\\[|\\])")) {
//                if (buffer.size() == 0) {
//                    queue.add(s);
//                    continue;
//                }
//                String joined = String.join("", buffer);
//                Matcher matcher = Pattern.compile("^\\$([a-z0-9_.\\-]+)\\[(.+)\\]$").matcher(joined);
//                if (!matcher.matches()) continue;
//                String key = matcher.group(1);
//                String type = matcher.group(2);
//                double value = derived.get(key).get(type);
//                queue.add(String.valueOf(value));
//
//                buffer.clear();
//                continue;
//            }
//
//            queue.add(s);
//        }
//        return calc(String.join("", queue));
//    }
//
//    private static Map<String, Map<String, Double>> getDerivedValues(Map<String, List<Double>> source) {
//        Map<String, Map<String, Double>> result = new HashMap<>();
//        for (Map.Entry<String, List<Double>> entry : source.entrySet()) {
//            String key = entry.getKey();
//            List<Double> doubleList = entry.getValue();
//            if (!result.containsKey(key)) result.put(key, new HashMap<>());
//            if (doubleList.size() == 1) {
//                result.get(key).put("maximum", doubleList.get(0));
//                result.get(key).put("minimum", doubleList.get(0));
//                result.get(key).put("median", doubleList.get(0));
//                result.get(key).put("mode", doubleList.get(0));
//                result.get(key).put("average", doubleList.get(0));
//                result.get(key).put("random", doubleList.get(0));
//                continue;
//            }
//
//            Collections.sort(doubleList);
//            result.get(key).put("maximum", Collections.max(doubleList));
//            result.get(key).put("minimum", Collections.min(doubleList));
//            double median;
//            if (doubleList.size() % 2 == 0) {
//                median = (doubleList.get(doubleList.size() / 2) + doubleList.get(doubleList.size() / 2) - 1) / 2;
//            } else {
//                median = doubleList.get(doubleList.size() / 2);
//            }
//            result.get(key).put("median", median);
//
//            double mode = 0d;
//            double sum = 0d;
//            Map<Double, Integer> counter = new HashMap<>();
//            for (double d : doubleList) {
//                int current = counter.containsKey(d) ? counter.get(d) : 0;
//                counter.put(d, current + 1);
//                sum += d;
//            }
//            int biggest = Collections.max(counter.values());
//            int times = 0;
//            for (Map.Entry<Double, Integer> e : counter.entrySet()) {
//                if (e.getValue().equals(biggest)) {
//                    mode += e.getKey();
//                    times++;
//                }
//            }
//            mode /= times;
//            result.get(key).put("mode", mode);
//
//            double average = sum / doubleList.size();
//            result.get(key).put("average", average);
//
//            double random = doubleList.get(new Random().nextInt(doubleList.size()));
//            result.get(key).put("random", random);
//
//        }
//        return result;
//    }
//
//
//    private static PersistentDataType getSpecifiedDataType(PersistentDataContainer container, NamespacedKey key) {
//        List<PersistentDataType> types = Arrays.asList(PersistentDataType.STRING, PersistentDataType.INTEGER, PersistentDataType.DOUBLE);
//        for (PersistentDataType type : types) {
//            try{
//                if (!container.has(key, type)) continue;
//                return type;
//            } catch (Exception e) {
//                continue;
//            }
//        }
//        return null;
//    }
//
//    private static ItemStack getCorrespondenceItemStack(Inventory inventory, Matter matter) {
//        for (ItemStack item : InventoryUtil.getItemStackFromCraftingMenu(inventory)) {
//            if (matter.getCandidate().contains(item.getType())) return item;
//        }
//        return null;
//    }
//
//
//    public static void setRecipeUsingContainerValueMetadata(Inventory inventory, Recipe recipe, ItemStack item) {
//        ItemMeta resultMeta = item.getItemMeta();
//
//        if (!recipe.hasUsingContainerValuesMetadata()) return;
//        for (Matter matter : recipe.getContentsNoAir()) {
//            if (!recipe.getUsingContainerValuesMetadata().containsKey(matter)) continue;
//            ItemStack relate;
//            if ((relate = getCorrespondenceItemStack(inventory, matter)) == null) continue;
//            List<String> orders = recipe.getUsingContainerValuesMetadata().get(matter);
//            if (orders == null || orders.isEmpty()) continue;
//
//            for (String order : orders) {
//                PersistentDataContainer source = relate.getItemMeta().getPersistentDataContainer();
//
//                if (order.matches(USING_CONTAINER_VALUES_LORE_PATTERN)) setUsingContainerValuesLore(resultMeta, source, order);
//                else if (order.matches(USING_CONTAINER_VALUES_ENCHANTMENT_PATTERN)) setUsingContainerValuesEnchantment(resultMeta, source, order);
//                else if (order.matches(USING_CONTAINER_VALUES_POTION_COLOR_RGB_PATTERN)) setUsingContainerValuesPotionColor(resultMeta, source, order);
//                else if (order.matches(USING_CONTAINER_VALUES_POTION_COLOR_RANDOM_PATTERN)) setUsingContainerValuesPotionColor(resultMeta, source, order);
//                else if (order.matches(USING_CONTAINER_VALUES_TOOL_DURABILITY_ABSOLUTE_PATTERN)) setUsingContainerValuesToolDurability(item.getType(), resultMeta, source, order);
//                else if (order.matches(USING_CONTAINER_VALUES_TOOL_DURABILITY_PERCENTAGE_PATTERN)) setUsingContainerValuesToolDurability(item.getType(), resultMeta, source, order);
//                else if (order.matches(USING_CONTAINER_VALUES_TEXTURE_ID_PATTERN)) setUsingContainerValuesTextureId(resultMeta, source, order);
//                else if (order.matches(USING_CONTAINER_VALUES_ITEM_NAME_PATTERN)) setUsingContainerValuesItemName(resultMeta, source, order);
//                else if (order.matches(USING_CONTAINER_VALUES_ATTRIBUTE_MODIFIER_PATTERN)) AttributeModifierUtil.setAttributeModifierToResult(resultMeta, source, order);
//                else if (order.matches(USING_CONTAINER_VALUES_ATTRIBUTE_MODIFIER_EQUIPMENT_SLOT_PATTERN)) AttributeModifierUtil.setAttributeModifierToResult(resultMeta, source, order);
//                else Bukkit.getLogger().warning("[CustomCrafter] USING_CONTAINER_VALUES Metadata failed. (Illegal configuration format found.)");
//            }
//        }
//        item.setItemMeta(resultMeta);
//    }
//
//    private static String getOrderElement(PersistentDataContainer source, String order, String pattern, int locate) {
//        String buffer = "";
//        StringBuilder result = new StringBuilder();
//        Matcher matcher = Pattern.compile(pattern).matcher(order);
//        if (!matcher.matches()) return "None";
//        String parsed = matcher.group(locate);
//        for (int i=0; i<parsed.length(); i++) {
//            String s = String.valueOf(parsed.charAt(i));
//            if (s.equals("{") && (i ==0 || !buffer.equals("\\"))) {
//                int start = i + 1;
//                int end = parsed.substring(i, parsed.length()).indexOf("}");
//                String formula = parsed.substring(start, i + end);
//                i += formula.length() + 1;
//                if (formula.matches("^\\$([a-z0-9\\-_]+)$")) {
//                    result.append(getContent(source, formula));
//                    continue;
//                }
//                double value = getFormulaValue(formula, source);
//                result.append(String.valueOf(value));
//                buffer = String.valueOf(parsed.charAt(i));
//                continue;
//            }
//
//            if (s.equals("{") && i != 0 && buffer.equals("\\")) {
//                // delete char
//                result.deleteCharAt(result.length()-1);
//            }
//            result.append(s);
//            buffer = s;
//        }
//        return result.toString();
//    }
//
//    private static void setUsingContainerValuesLore(ItemMeta meta, PersistentDataContainer source, String order) {
//        meta.setLore(Arrays.asList(getOrderElement(source, order, USING_CONTAINER_VALUES_LORE_PATTERN, 1)));
//    }
//
//    private static void setUsingContainerValuesEnchantment(ItemMeta meta, PersistentDataContainer source, String order) {
//        Matcher matcher = Pattern.compile(USING_CONTAINER_VALUES_ENCHANTMENT_PATTERN).matcher(order);
//        if (!matcher.matches()) return;
//        Enchantment enchant;
//        try{
//            String matched = matcher.group(1);
//            enchant = Enchantment.getByKey(NamespacedKey.minecraft((matched.contains("$") ? getContent(source, matched) : matched).toLowerCase()));
//        } catch (Exception e) {
//            Bukkit.getLogger().warning("[CustomCrafter] USING_CONTAINER_VALUES_ENCHANTMENT failed. (Illegal EnchantmentName)");
//            return;
//        }
//
//        int level;
//        try{
//            double preLevel = Double.parseDouble(matcher.group(2).contains("$") ? getContent(source, matcher.group(2)) : matcher.group(2));
//            level = (int) Math.round(ENCHANTMENT_MAX_LEVEL < preLevel ? ENCHANTMENT_MAX_LEVEL : preLevel);
//            if (level < 1) {
//                Bukkit.getLogger().warning("[CustomCrafter] USING_CONTAINER_VALUES_ENCHANTMENT failed. (Illegal Level Range. x < 1)");
//                return;
//            }
//        } catch (Exception e) {
//            Bukkit.getLogger().warning("[CustomCrafter] USING_CONTAINER_VALUES_ENCHANTMENT failed. (Illegal Integer pattern. Does not follow ([0-9]+))");
//            return;
//        }
//        meta.addEnchant(enchant, level, true);
//
//    }
//
//    private static void setUsingContainerValuesPotionColor(ItemMeta meta, PersistentDataContainer source, String order) {
//        Matcher rgb = Pattern.compile(USING_CONTAINER_VALUES_POTION_COLOR_RGB_PATTERN).matcher(order);
//        Matcher random = Pattern.compile(USING_CONTAINER_VALUES_POTION_COLOR_RANDOM_PATTERN).matcher(order);
//        if (rgb.matches()) {
//            // rgb
//            String red = rgb.group(1);
//            String green = rgb.group(2);
//            String blue = rgb.group(3);
//            int RED ,GREEN, BLUE;
//            try {
//                RED = (int) Math.round(Double.parseDouble(red.contains("$") ? getContent(source, red) : red));
//                GREEN = (int) Math.round(Double.parseDouble(green.contains("$") ? getContent(source, green) : green));
//                BLUE = (int) Math.round(Double.parseDouble(blue.contains("$") ? getContent(source, blue) : blue));
//            } catch (Exception e) {
//                Bukkit.getLogger().warning("[CustomCrafter] USING_CONTAINER_VALUES_POTION_COLOR failed. (Returned None Value or String type-value.)");
//                return;
//            }
//
//            if (RED < 0 || 255 < RED) RED = RED < 0 ? 0 : 255;
//            if (GREEN < 0 || 255 < GREEN) GREEN = GREEN < 0 ? 0 : 255;
//            if (BLUE < 0 || 255 < BLUE) BLUE = BLUE < 0 ? 0 : 255;
//            try{
//                PotionMeta potionMeta = (PotionMeta) meta;
//                potionMeta.setColor(Color.fromRGB(RED, GREEN, BLUE));
//            } catch (Exception e) {
//                Bukkit.getLogger().warning("[CustomCrafter] USING_CONTAINER_VALUES_POTION_COLOR failed. (PotionMeta cast failed.)");
//                return;
//            }
//
//
//        } else if (random.matches()) {
//            // random
//            Random dice = new Random();
//            int RED = dice.nextInt(256);
//            int GREEN = dice.nextInt(256);
//            int BLUE = dice.nextInt(256);
//            try{
//                PotionMeta potionMeta = (PotionMeta) meta;
//                potionMeta.setColor(Color.fromRGB(RED, GREEN, BLUE));
//            } catch (Exception e) {
//                Bukkit.getLogger().warning("[CustomCrafter] USING_CONTAINER_VALUES_POTION_COLOR failed. (PotionMeta cast failed.)");
//                return;
//            }
//
//        } else {
//            Bukkit.getLogger().warning("[CustomCrafter] USING_CONTAINER_VALUES_POTION_COLOR failed. (Illegal pattern.)");
//            return;
//        }
//    }
//
//    private static void setUsingContainerValuesTextureId(ItemMeta meta, PersistentDataContainer source, String order) {
//        int id;
//        try{
//            id = (int) Math.round(Double.parseDouble(getContent(source, order)));
//        } catch (Exception e) {
//            Bukkit.getLogger().warning("[CustomCrafter] USING_CONTAINER_VALUES_TEXTURE_ID failed. (Illegal texture id.)");
//            return;
//        }
//        meta.setCustomModelData(id);
//    }
//
//    private static void setUsingContainerValuesToolDurability(Material material,ItemMeta meta, PersistentDataContainer source, String order) {
//        Matcher absolute = Pattern.compile(USING_CONTAINER_VALUES_TOOL_DURABILITY_ABSOLUTE_PATTERN).matcher(order);
//        Matcher percentage = Pattern.compile(USING_CONTAINER_VALUES_TOOL_DURABILITY_PERCENTAGE_PATTERN).matcher(order);
//        if (absolute.matches()) {
//            // absolute
//            int LAST_ONE = material.getMaxDurability() - 1;
//            String variable = absolute.group(1);
//            double value;
//            try {
//                value = Double.parseDouble(getContent(source, variable));
//            } catch (Exception e) {
//                Bukkit.getLogger().warning("[CustomCrafter] USING_CONTAINER_VALUES_TOOL_DURABILITY failed. (Returned None Value or String type-value found.)");
//                return;
//            }
//            Damageable damageable;
//            try{
//                damageable = (Damageable) meta;
//            } catch (Exception e) {
//                Bukkit.getLogger().warning("[CustomCrafter] USING_CONTAINER_VALUES_TOOL_DURABILITY failed. (Illegal item.(Un-Damageable))");
//                return;
//            }
//            int durability = material.getMaxDurability() - (int) Math.round(value);
//            if (durability < 1 || material.getMaxDurability() < durability) durability = durability < 0 ? LAST_ONE : 0;
//            damageable.setDamage(durability);
//
//        } else if (percentage.matches()) {
//            // percentage
//            int LAST_ONE = material.getMaxDurability() - 1;
//            String variable = percentage.group(1);
//            double value;
//            try {
//                value = Double.parseDouble(getContent(source, variable));
//            } catch (Exception e) {
//                Bukkit.getLogger().warning("[CustomCrafter] USING_CONTAINER_VALUES_TOOL_DURABILITY failed. (Returned None Value or String type-value found.)");
//                return;
//            }
//            Damageable damageable;
//            try {
//                damageable = (Damageable) meta;
//            } catch (Exception e) {
//                Bukkit.getLogger().warning("[CustomCrafter] USING_CONTAINER_VALUES_TOOL_DURABILITY failed. (Illegal item.(Un-Damageable))");
//                return;
//            }
//            // value -> (%)
//            if (value < 1 || 100 < value) value = value < 1 ? 99 : 0;
//            int durability = (int) Math.round((100 - value) * 0.01 * material.getMaxDurability());
//            if (durability < 1) durability = LAST_ONE;
//            damageable.setDamage(durability);
//
//        } else {
//            Bukkit.getLogger().warning("[CustomCrafter] USING_CONTAINER_VALUES_TOOL_DURABILITY failed. (Illegal pattern.)");
//            return;
//        }
//
//    }
//
//    private static void setUsingContainerValuesItemName(ItemMeta meta, PersistentDataContainer source, String order) {
//        // modify item-display-name
//        meta.setDisplayName(getOrderElement(source, order, USING_CONTAINER_VALUES_ITEM_NAME_PATTERN, 1));
//    }
//
//    public static String getContent(PersistentDataContainer container, String order) {
//        if (!order.contains("$")) return order;
//        order = order.replace("$","");
//        NamespacedKey key = new NamespacedKey(getInstance(), order);
//        PersistentDataType type;
//        try{
//            type = getSpecifiedDataType(container, key);
//        } catch (Exception e) {
//            return "None";
//        }
//        if (type == null) return "None";
//        if (!container.has(key, type)) return "None";
//        return String.valueOf(container.get(key, type));
//    }
//
//    public static  void containerModify(String action, String value, ItemMeta meta) {
//        PersistentDataContainer container = meta.getPersistentDataContainer();
//        if (action.equals("add")) {
//            // non override
//            Matcher v = Pattern.compile("type=(string|double|int),init=(.+)").matcher(value);
//            if (!v.matches()) return;
//            String name = v.group(1);
//            String type = v.group(2);
//            String init = v.group(3);
//            NamespacedKey key = new NamespacedKey(getInstance(), name);
//            PersistentDataType T = getDataType(type);
//            if (container.has(key, T)) return;
//            if (type.equals("string")) container.set(key, T, init);
//            else if (type.equals("double")) container.set(key, T, Double.parseDouble(init));
//            else if (type.equals("int")) container.set(key, T, Integer.parseInt(init));
//        } else if (action.equals("remove")) {
//            NamespacedKey key = new NamespacedKey(getInstance(), value);
//            container.remove(key);
//        } else if (action.equals("modify")) {
//            //
//        }
//    }
//
//
//    public static Map<Coordinate, List<Coordinate>> amorphous(Recipe recipe, Recipe input) {
//        List<Coordinate> r = recipe.getHasContainerDataItemList();
//        List<Coordinate> i = input.getHasContainerDataItemList();
//        Map<Coordinate, List<Coordinate>> map = new HashMap<>();
//
//
//        // returns NON_REQUIRED = non required container elements
//        // returns NULL         = an input does not matched
//        if (r.isEmpty()) return Search.AMORPHOUS_NON_REQUIRED_ANCHOR;
//        if (r.size() > i.size()) return Search.AMORPHOUS_NULL_ANCHOR;
//
//        // container wrapper's "value" is value. (sometimes when a wrapper has the tag "allow_tag" or "deny tag", this variable maybe empty.)
//
//        for (int j = 0; j < r.size(); j++) {
//            Matter R = recipe.getMatterFromCoordinate(r.get(j));
//            Matter I = input.getMatterFromCoordinate(i.get(j));
//            int judge = 0;
//            for (int k = 0; k < R.getContainerWrappers().size(); k++) {
//                ContainerWrapper rContainerWrapper = R.getContainerWrappers().get(k);
//                String TAG = rContainerWrapper.getTag();
//                String VALUE = rContainerWrapper.getValue();
//                String KEY = rContainerWrapper.getKey().getKey();
//                String TYPE = rContainerWrapper.getValueType().toGenericString().toLowerCase();
//                if (TAG.equals(STORE_ONLY)) judge += 1;
//                else if (TAG.equals(ALLOW_TAG)) {
//                    if (!VALUE.contains(",") && !VALUE.startsWith("(multi)")) {
//                        if (hasKey(I, KEY)) judge += 1;
//                    } else if (multiKeys(I, VALUE, true)) judge += 1;
//
//                } else if (TAG.equals(DENY_TAG)) {
//                    if (!VALUE.contains(",") && !VALUE.startsWith("(multi)")) {
//                        if (!hasKey(I, KEY)) judge += 1;
//                    } else if (multiKeys(I, VALUE, false)) judge += 1;
//                }
//
//                if (TAG.equals(ALLOW_VALUE) && hasValue(I, KEY, VALUE, TYPE)) judge += 1;
//                if (TAG.equals(DENY_VALUE) && !hasValue(I, KEY, VALUE, TYPE)) judge += 1;
//            }
//
//            if (judge == R.getContainerWrappers().size()) {
//                if (!map.containsKey(r.get(j))) map.put(r.get(j), new ArrayList<>());
//                map.get(r.get(j)).add(i.get(j));
//            }
//        }
//
//        //debug
//        Bukkit.getLogger().info("[ContainerUtil]r size=" + r.size() + ", i size=" + i.size());
//        map.forEach((key, value) -> System.out.printf("[ContainerUtil] index=%s, list=%s%n", key.toString(), value.toString()));
//
//        return map.isEmpty() ? Search.AMORPHOUS_NULL_ANCHOR : map;
//    }
//
//    private static boolean multiKeys(Matter matter, String multiKeys, boolean needed) {
//        // (multi)(types:int*3,string*3,double*1)a,b,c,d,e,f,g
//        // -> a,b,c = type int
//        // -> d,e,f = type string
//        // -> g = type double
//        // when these keys exist and the variable "needed" is true, this returns true.
//        // when these keys not exist and the variable "needed" is false, this return true.
//        // otherwise, this returns false.
//        // this method only for "ALLOW_TAG" and "DENY_TAG". Not "ALLOW_VALUE" and "DENY_VALUE".
//        Matcher matcher = Pattern.compile(MULTI_VALUE_PATTERN).matcher(multiKeys);
//        if (!matcher.matches()) return false;
//        List<String> variableNames = new ArrayList<>(Arrays.asList(matcher.group(2).split(",")));
//        for (String variableName : variableNames) {
//            int judge = 0;
//            for (Map.Entry<Integer, ContainerWrapper> entry : matter.getContainerWrappers().entrySet()) {
//                String name = entry.getValue().getKey().getKey();
//                if (variableName.equals(name)) {
//                    judge += 1;
//                    break;
//                }
//            }
//            if ((!needed && judge != 0) || (needed && judge == 0)) return false;
//        }
//        return true;
//    }
//
//    private static boolean hasKey(Matter matter, String keyName) {
//        for (int i = 0; i < matter.getContainerWrappers().size(); i++) {
//            ContainerWrapper wrapper = matter.getContainerWrappers().get(i);
//            if (wrapper.getKey().getKey().equals(keyName)) return true;
//        }
//        return false;
//    }
//
//    private static boolean hasValue(Matter matter, String keyName, String value, String type) {
//        Map<String, ContainerWrapper> data = new HashMap<>();
//        for (Map.Entry<Integer, ContainerWrapper> entry : matter.getContainerWrappers().entrySet()) {
//            String key = entry.getValue().getKey().getKey();
//            data.put(key, entry.getValue());
//        }
//
//        for (int i = 0; i < matter.getContainerWrappers().size(); i++) {
//            ContainerWrapper wrapper = matter.getContainerWrappers().get(i);
//            String typeStr = wrapper.getValueType().toGenericString().toLowerCase();
//            if (!typeStr.equals(type) || !keyName.equals(wrapper.getKey().getKey())) continue;
//            int judge = 0;
//            if (type.equals("int") || type.equals("double")) {
//                judge += isInArrowRange(value, keyName,data) ? 1 : 0;
//                judge += isInSmallerRange(value, keyName, data) ? 1 : 0;
//                judge += isInLargerRange(value, keyName, data) ? 1 : 0;
//            } else if (type.equals("string")) {
//                judge += isEqualsString(value, keyName, data) ? 1 : 0;
//                judge += isEqualsIgnoreCase(value, keyName, data) ? 1 : 0;
//                judge += isFollowRegexPattern(value, keyName, data) ? 1 : 0;
//            }
//
//            if (judge == 0) return false;
//        }
//        return true;
//    }
//
//    private static boolean isFollowRegexPattern(String source, String key, Map<String, ContainerWrapper> data) {
//        Matcher matcher = Pattern.compile(FOLLOW_REGEX_PATTERN).matcher(source);
//        if (!matcher.matches()) return false;
//        String element = getFormulaValueString(matcher.group(1), data);
//        String value = getFormulaValueString(data.get(key).getValue(), data);
//        return Pattern.compile(value).matcher(element).matches();
//    }
//
//    private static boolean isEqualsIgnoreCase(String source, String key, Map<String, ContainerWrapper> data) {
//        Matcher matcher = Pattern.compile(STRING_IGNORE_EQUALS_PATTERN).matcher(source);
//        if (!matcher.matches()) return false;
//        String element = getFormulaValueString(matcher.group(1), data);
//        String value = getFormulaValueString(data.get(key).getValue(), data);
//        return element.equalsIgnoreCase(value);
//    }
//
//    private static boolean isEqualsString(String source, String key, Map<String, ContainerWrapper> data) {
//        Matcher matcher = Pattern.compile(STRING_EQUALS_PATTERN).matcher(source);
//        if (!matcher.matches()) return false;
//        String element = getFormulaValueString(matcher.group(1), data);
//        String value = getFormulaValueString(data.get(key).getValue(), data);
//        return element.equals(value);
//    }
//
//
//    private static boolean isInArrowRange(String source, String key,Map<String, ContainerWrapper> data) {
//        Matcher matcher = Pattern.compile(ARROW_RANGE_PATTERN).matcher(source);
//        if (!matcher.matches()) return false;
//        double start = getFormulaValue(matcher.group(1), data);
//        double end = getFormulaValue(matcher.group(2), data);
//        double value = getFormulaValue(data.get(key).getValue(), data);
//
//        return start < value && value < end;
//    }
//
//    private static boolean isInSmallerRange(String source, String key,Map<String, ContainerWrapper> data) {
//        Matcher matcher = Pattern.compile(SMALLER_PATTERN).matcher(source);
//        if (!matcher.matches()) return false;
//        double large = getFormulaValue(matcher.group(1), data);
//        double value = getFormulaValue(data.get(key).getValue(), data);
//
//        return value < large;
//    }
//
//    private static boolean isInLargerRange(String source, String key, Map<String, ContainerWrapper> data) {
//        Matcher matcher = Pattern.compile(LARGER_PATTERN).matcher(source);
//        if (!matcher.matches()) return false;
//        double small = getFormulaValue(matcher.group(1), data);
//        double value = getFormulaValue(data.get(key).getValue(), data);
//
//        return small < value;
//    }
//
//    private static double getFormulaValue(String formula, Map<String, ContainerWrapper> map) {
//        Map<String, Double> data = new HashMap<>();
//        for (Map.Entry<String, ContainerWrapper> entry : map.entrySet()) {
//            if (entry.getValue().getValueType().equals(String.class)) continue;
//            String key = entry.getKey();
//            double v = Double.parseDouble(entry.getValue().getValue());
//            data.put(key, v);
//        }
//
//        StringBuilder stack = new StringBuilder();
//        for (int i = 0; i < formula.length(); i++) {
//            String c = String.valueOf(formula.charAt(i));
//            if (!c.equals("$") && stack.isEmpty()) continue;
//            if (c.matches("[-+/*^~()]")) {
//                String replacer = String.valueOf(data.get(stack.toString()));
//                formula = formula.replace(stack.toString(), replacer);
//                stack.setLength(0);
//            }
//            stack.append(c);
//        }
//        return calc(formula);
//    }
//
//    private static String getFormulaValueString(String formula, Map<String, ContainerWrapper> map) {
//        Map<String, String> data = new HashMap<>();
//        for (Map.Entry<String, ContainerWrapper> entry : map.entrySet()) {
//            if (!entry.getValue().getValueType().equals(String.class)) continue;
//            String key = entry.getKey();
//            String v = entry.getValue().getValue();
//            data.put(key, v);
//        }
//
//        StringBuilder value = new StringBuilder();
//        for (int i = 0; i < formula.length(); i++) {
//            String c = String.valueOf(formula.charAt(i));
//            if (c.matches("[^0-9a-zA-Z_]")) {
//                String buffer = value.toString();
//                int start = buffer.lastIndexOf("$");
//                String key = buffer.substring(start + 1, i);
//                if (start == -1 || !data.containsKey(key)) {
//                    value.append(c);
//                    continue;
//                }
//                value.replace(start + 1, i, data.get(key));
//            }
//            value.append(c);
//        }
//        return value.toString();
//    }
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
                Matcher matcher = Pattern.compile("(.+)\\.(string|double|long)").matcher(key.getKey());
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
