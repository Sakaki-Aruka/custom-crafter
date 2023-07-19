package com.github.sakakiaruka.customcrafter.customcrafter.util;

import com.github.sakakiaruka.customcrafter.customcrafter.command.ContainerModify;
import com.github.sakakiaruka.customcrafter.customcrafter.object.ContainerWrapper;
import com.github.sakakiaruka.customcrafter.customcrafter.object.Matter.Matter;
import com.github.sakakiaruka.customcrafter.customcrafter.object.Recipe.Recipe;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.nio.file.Path;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.github.sakakiaruka.customcrafter.customcrafter.CustomCrafter.getInstance;
import static com.github.sakakiaruka.customcrafter.customcrafter.SettingsLoad.*;

public class ContainerUtil {
    public static Map<String, Map<Integer, ContainerWrapper>> containers = new HashMap<>();
    public static final String ALLOW_TAG = "allow_tag";
    public static final String ALLOW_VALUE = "allow_value";
    public static final String DENY_TAG = "deny_tag";
    public static final String DENY_VALUE = "deny_value";
    public static final String STORE_ONLY = "store_only";

    //---
    private static final String OPERATORS_PATTERN = "\\+|-|\\*|/|\\(|\\)";
    private static final String ORDER_NUMBER_PATTERN = "_\\d{1,2}";
    private static final String ARROW_RANGE_PATTERN = "([_|\\d|\\+|-|\\*|/|\\(|\\)]+)<-->([_|\\d|\\+|-|\\*|/|\\(|\\)]+)";
    private static final String LARGER_PATTERN = "([_|\\d|\\+|-|\\*|/|\\(|\\)]+)<";
    private static final String SMALLER_PATTERN = "<([_|\\d|\\+|-|\\*|/|\\(|\\)]+)";

    public PersistentDataType getDataType(String input) {
        if (input.equalsIgnoreCase("string")) return PersistentDataType.STRING;
        if (input.equalsIgnoreCase("int")) return PersistentDataType.INTEGER;
        return null;
    }

    public Class getClassFromType(PersistentDataType type) {
        if (type.equals(PersistentDataType.INTEGER)) return Integer.class;
        if (type.equals(PersistentDataType.STRING)) return String.class;
        return null;
    }

    public Map<Integer, ContainerWrapper> mattersLoader(Path path) {
        Map<Integer, ContainerWrapper> map = new LinkedHashMap<>();
        FileConfiguration config = YamlConfiguration.loadConfiguration(path.toFile());
        int current = 0;
        while (true) {
            if (!config.contains("terms."+current)) break;
            String thumb =  "terms."+current+".";

            String tag = config.getString(thumb+"tag");
            int order = config.getInt(thumb+"order");
            NamespacedKey key = new NamespacedKey(getInstance(), config.getString(thumb+"key"));
            PersistentDataType type = getDataType(config.getString(thumb+"type"));
            String value = "";
            if (config.contains(thumb+"value")) {
                value = config.getString(thumb+"value");
            }

            ContainerWrapper wrapper = new ContainerWrapper(key, type, value,order,tag);

            if (!map.containsKey(order)) map.put(order, wrapper);
            current++;
        }

        String name = config.getString("name");

        containers.put(name, map);
        return map;
    }

    public boolean isPassTargetEmpty(Matter source) {
        // target = always empty
        if (!source.hasContainer()) return true;
        Map<Integer, ContainerWrapper> wrappers = source.getContainerWrappers();
        for (Map.Entry<Integer, ContainerWrapper> entry : wrappers.entrySet()) {
            String tag = entry.getValue().getTag();
            if (tag.equals(ALLOW_TAG) || tag.equals(ALLOW_VALUE)) return false;
        }
        return true;
    }

    public boolean isPass(ItemStack item, Matter matter) {
        // item -> target, matter -> source (recipe)
        // matter > item

        //debug
        if (matter.getContainerWrappers() != null) {
            matter.getContainerWrappers().entrySet().forEach(s->System.out.println(s.getValue().info()));
            System.out.println(bar);
            PersistentDataContainer container = item.getItemMeta().getPersistentDataContainer();
            if (!container.isEmpty()) {
                for (NamespacedKey key : container.getKeys()) {
                    System.out.println("exist key: "+key.toString());
                }
            }
            System.out.println(bar);
        }



        if (!matter.hasContainer()) return true;

        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();

        for (Map.Entry<Integer, ContainerWrapper> entry : matter.getContainerWrappers().entrySet()) {
            String tag = entry.getValue().getTag();
            if (tag.equals(STORE_ONLY)) continue;

            ContainerWrapper wrapper = entry.getValue();
            String value = entry.getValue().getValue();
            NamespacedKey key = wrapper.getKey();
            PersistentDataType type = wrapper.getType();

            //debug
            System.out.println("current: "+entry.getKey());

            if (tag.equals(ALLOW_TAG)) {
                if (!container.has(key, type)) return false;
                continue;
            }

            if (tag.equals(DENY_TAG)) {

                //debug
                System.out.println("key: "+key.toString());
                System.out.println("tag: "+tag);
                System.out.println("has: "+container.has(key, type));
                System.out.println("!has: "+!container.has(key, type));

                if (!container.has(key, type)) continue;
                if (container.has(key, type)) return false;
            }

            //debug
            System.out.println("no tags");

            if (value.matches(ARROW_RANGE_PATTERN) && !arrowPatternOperation(value, matter, container, wrapper)) return false;
            else if (value.matches(ARROW_RANGE_PATTERN) && arrowPatternOperation(value, matter, container, wrapper)) continue;

            if (value.matches(SMALLER_PATTERN) && !smallerPatternOperation(value, matter, container, wrapper)) return false;
            else if (value.matches(SMALLER_PATTERN) && smallerPatternOperation(value, matter, container, wrapper)) continue;

            if (value.matches(LARGER_PATTERN) && !largerPatternOperation(value, matter, container, wrapper)) return false;
            else if (value.matches(LARGER_PATTERN) && largerPatternOperation(value, matter, container, wrapper)) continue;


            //TODO: write 'allow_value' and 'deny_value'
            //if (!tag.equals(ALLOW_VALUE) && !tag.equals(DENY_VALUE)) continue;
            boolean isAllow = tag.equals(ALLOW_VALUE);
            // isAllow = true -> ALLOW_VALUE
            // isAllow = false -> DENY_VALUE
            if (!isAllow && container.getKeys().isEmpty()) continue;
            if (isAllow && container.getKeys().isEmpty()) return false;

            //debug
            System.out.println("tag: "+tag+" / "+value);

            if (type.equals(PersistentDataType.STRING)) {

                Pattern pattern = Pattern.compile(value);
                Matcher matcher = pattern.matcher(String.valueOf(container.get(key, type)));
                if (matcher.matches() == isAllow) continue;
                return false;

            } else if (type.equals(PersistentDataType.INTEGER)) {
                PersistentDataType valueType;
                if ((valueType = getSpecifiedKeyType(container, key)) == null) return false;
                if (!valueType.equals(PersistentDataType.INTEGER)) return false;
                String element = String.valueOf(container.get(key, type));
                if (element.matches(ContainerModify.NUMBERS_PATTERN)) return false;

                int current = Integer.valueOf(value);
                int containerHas = Integer.valueOf(element);
                if ((current == containerHas) == isAllow) continue;
                return false;
            }
        }
        return true;
    }



    public boolean isPassRecipe(Recipe recipe, Recipe input) {
        // r -> source
        // m -> target
        // r > m

        int inputCount = 0;
        int recipeCount = recipe.getContentsNoAir().size();
        //if (recipe.getContainerHasAmount() != input.getContainerHasAmount()) return false;
        A:for (Matter x : recipe.getContentsNoAir()) {
            //if (!x.hasContainer()) continue;
            B:for (Matter m : input.getContentsNoAir()) {
                if (!m.hasContainer()) {
                    if (!isPassTargetEmpty(x)) return false;
                    inputCount++;
                    continue;
                }
                ItemStack item = new ItemStack(m.getCandidate().get(0));
                ItemMeta meta = item.getItemMeta();
                PersistentDataContainer container = meta.getPersistentDataContainer();
                for (Map.Entry<Integer, ContainerWrapper> entry : m.getContainerWrappers().entrySet()) {
                    NamespacedKey key = entry.getValue().getKey();
                    PersistentDataType type = entry.getValue().getType();
                    Object value = entry.getValue().getValue();
                    container.set(key, type, value);
                }
                item.setItemMeta(meta);
                if (!isPass(item, x)) continue;
                inputCount++;
            }
        }

        //debug
        System.out.println("input: "+inputCount+" / recipe: "+recipeCount);

        return inputCount == recipeCount;
    }


    private boolean arrowPatternOperation(String source, Matter matter, PersistentDataContainer container, ContainerWrapper wrapper) {
        NamespacedKey key = wrapper.getKey();
        PersistentDataType type = wrapper.getType();
        Pattern pattern = Pattern.compile(ARROW_RANGE_PATTERN);
        Matcher matcher = pattern.matcher(source);
        String tag = wrapper.getTag();

        int element;
        try{
            element = Integer.valueOf(String.valueOf(container.get(key,type)));
        }catch (Exception e) {
            return false;
        }
        /*
        * examples
        * _1+1<-->10
        * _1<-->_3
         */
        int start = getFormulaValue(matcher.group(1), matter, container);
        int end = getFormulaValue(matcher.group(2), matter, container);

        if (tag.equals(ALLOW_VALUE)) {
            return start < element && element < end;
        } else if (tag.equals(DENY_VALUE)) {
            return element < start || end < element;
        }
        return false;
    }

    private boolean smallerPatternOperation(String source, Matter matter, PersistentDataContainer container, ContainerWrapper wrapper) {
        NamespacedKey key = wrapper.getKey();
        PersistentDataType type = wrapper.getType();
        String formula = source.substring(1, source.length());
        String tag = wrapper.getTag();

        int element;
        try{
            element = Integer.valueOf(String.valueOf(container.get(key, type)));
        }catch (Exception e) {
            return false;
        }
        int target = getFormulaValue(formula, matter, container);

        if (tag.equals(ALLOW_VALUE)) {
            return element < target;
        } else if (tag.equals(DENY_VALUE)) {
            return target < element;
        }
        return false;
    }

    private boolean largerPatternOperation(String source, Matter matter, PersistentDataContainer container, ContainerWrapper wrapper) {
        NamespacedKey key = wrapper.getKey();
        PersistentDataType type = wrapper.getType();
        String formula = source.substring(0,source.length()-1);
        String tag = wrapper.getTag();

        int element;
        try{
            element = Integer.valueOf(String.valueOf(container.get(key, type)));
        }catch (Exception e) {
            return false;
        }
        int target = getFormulaValue(formula, matter, container);

        if (tag.equals(ALLOW_VALUE)) {
            return target < element;
        } else if (tag.equals(DENY_VALUE)) {
            return element < target;
        }
        return false;
    }

    private int getFormulaValue(String input, Matter matter, PersistentDataContainer container) {
        List<String> list = new ArrayList<>();
        List<String> buffer = new ArrayList<>();
        input = input.replace(" ","");
        for (int i=0;i<input.length();i++) {
            String s = String.valueOf(input.charAt(i));
            if (s.matches(OPERATORS_PATTERN) && 0 < buffer.size()) {
                String t = String.join("",buffer);
                if (t.matches("\\d+")) {
                    list.add(t);
                    list.add(s);
                    buffer.clear();
                    continue;
                }
                list.add(String.valueOf(getSpecifiedElementValue(input, matter, container)));
                list.add(s);
                buffer.clear();
            }
            list.add(s);
        }

        String source = String.join("",list);
        return calc(source);
    }

    private int getSpecifiedElementValue(String input, Matter matter, PersistentDataContainer container) {
        if (input.matches("\\d+")) return Integer.valueOf(input);
        int order = Integer.valueOf(input.replace("_",""));
        NamespacedKey key = matter.getContainerWrappers().get(order).getKey();
        PersistentDataType type = matter.getContainerWrappers().get(order).getType();
        return Integer.valueOf(container.get(key, type).toString());
    }

    private int calc(String input) {
        List<String> output = new ArrayList<>();
        List<String> buffer = new ArrayList<>();
        List<String> intBuffer = new ArrayList<>();

        boolean inBracket = false;
        for (String s : input.split("")) {
            if (s.matches("\\d")) {
                intBuffer.add(s);
                continue;
            }

            if (s.matches(OPERATORS_PATTERN) && buffer.size() != 0) {
                if (s.equals("(")) {
                    buffer.add(s);
                    inBracket = true;
                } else if (s.equals(")")) {
                    inBracket = false;
                    int firstLeftBracket = buffer.indexOf("(");
                    List<String> push = buffer.subList(0,firstLeftBracket);
                    output.addAll(push);
                    buffer.remove(buffer.indexOf("("));
                } else {
                    output.add(String.join("",intBuffer));
                    intBuffer.clear();
                    int current = getPriority(s);
                    int previous = getPriority(buffer.get(0));
                    if (current <= previous) {
                        output.add(buffer.get(0));
                        buffer.add(s);
                    } else buffer.add(s);
                }
            }
        }
        if (intBuffer.size() != 0) output.add(String.join("",intBuffer));
        if (buffer.size() != 0) output.addAll(buffer);
        return rpnCalc(output);
    }

    private int getPriority(String s) {
        if (s.equals("^")) return 4;
        if (s.equals("*")) return 3;
        if (s.equals("/")) return 3;
        if (s.equals("+")) return 2;
        if (s.equals("-")) return 2;
        return -1;
    }

    private int rpnCalc(List<String> list) {
        // rpn -> Reverse Polish Nation
        int accumulator;
        List<Integer> stack = new ArrayList<>();

        for (int i=0;i<list.size();i++) {
            String s = list.get(i);
            if (s.matches("\\d+")) stack.add(Integer.valueOf(s));
            else if (s.equals("+")) {
                accumulator = Integer.valueOf(stack.get(i-2));
                accumulator += Integer.valueOf(stack.get(i-1));
                stack.add(accumulator);
            } else if (s.equals("-")) {
                accumulator = Integer.valueOf(stack.get(i-2));
                accumulator = Integer.valueOf(stack.get(i-1)) - accumulator;
                stack.add(accumulator);
            } else if (s.equals("*")) {
                accumulator = Integer.valueOf(stack.get(i-2));
                accumulator *= Integer.valueOf(stack.get(i-1));
                stack.add(accumulator);
            }else if (s.equals("/")) {
                accumulator = Integer.valueOf(stack.get(i-2));
                accumulator = Integer.valueOf(stack.get(i-1)) / accumulator;
                stack.add(accumulator);
            }
        }
        return stack.get(0);
    }


    public void setContainerDataItemStackToMatter(ItemStack item, Matter matter) {
        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();
        Map<Integer, ContainerWrapper> wrappers = new LinkedHashMap<>();

        List<NamespacedKey> keys = new ArrayList<>(container.getKeys());
        for (int i=0;i<keys.size();i++) {
            NamespacedKey key = keys.get(i);
            PersistentDataType type;
            if ((type = getSpecifiedKeyType(container, key)) == null) return;
            // order: i
            // tag: STORE_ONLY
            // value: value
            String value = String.valueOf(container.get(key, type));
            ContainerWrapper wrapper = new ContainerWrapper(key, type, value, i, STORE_ONLY);
            wrappers.put(i, wrapper);
        }
        matter.setContainerWrappers(wrappers);
    }

    private PersistentDataType getSpecifiedKeyType(PersistentDataContainer container, NamespacedKey key) {
        for (PersistentDataType type : getDefaultDataTypes()) {
            try{
                container.get(key, type);
                return type;
            }catch (Exception e) {
                continue;
            }
        }
        return null;
    }

    private List<PersistentDataType> getDefaultDataTypes() {
        List<PersistentDataType> types = new ArrayList<>();
        types.add(PersistentDataType.STRING);
        types.add(PersistentDataType.INTEGER);
        return types;
    }

    public String containerValues(PersistentDataContainer container) {
        StringBuilder builder = new StringBuilder();
        for (NamespacedKey key : container.getKeys()) {
            builder.append(bar+nl);
            builder.append("key: "+key.toString()+nl);
            PersistentDataType type = getSpecifiedKeyType(container, key);
            builder.append("type: "+type.getComplexType().getSimpleName()+nl);
            builder.append("value: "+container.get(key, type).toString()+nl);
            builder.append(bar+nl);
        }
        return builder.toString();
    }
}
