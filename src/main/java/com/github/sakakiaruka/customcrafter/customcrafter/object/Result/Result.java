package com.github.sakakiaruka.customcrafter.customcrafter.object.Result;

import com.github.sakakiaruka.customcrafter.customcrafter.util.AttributeModifierUtil;
import com.github.sakakiaruka.customcrafter.customcrafter.util.ContainerUtil;
import com.github.sakakiaruka.customcrafter.customcrafter.util.InventoryUtil;
import com.github.sakakiaruka.customcrafter.customcrafter.util.PotionUtil;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.*;

import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Result {
    private static final String NORMAL_ATTRIBUTE_MODIFIER_PATTERN = "^type:([\\w_]+)/operation:(?i)(add|multiply|add_scalar)/value:(\\-?\\d+(\\.\\d+)?)$";
    private static final String EQUIPMENT_ATTRIBUTE_MODIFIER_PATTERN = "^type:([\\w_]+)/operation:(?i)(add|multiply|add_scalar)/value:(\\-?\\d+(\\.\\d+)?)/slot:(\\w+)$";

    private static final String BOOK_FIELD_PATTERN = "^type:(?i)(author|title|add_page|pages|generation|add_long|add_long_extend)/value:(.+)$";

    private static final String LEATHER_ARMOR_COLOR_PATTERN = "type:(?i)(rgb|name|random)";



    private static final String PASS_THROUGH_MODE_TEMPLATE = "^mode=pass/type=([\\w\\d-_]+)/action=([\\w\\d-_]+)/value=(.+)$";
    private static final String PASS_THROUGH_MODE_ENCHANTMENT_ADD = "ACTION=add/VALUE=enchant=([\\w_]+),level=([\\d]+)";
    private static final String PASS_THROUGH_MODE_ENCHANTMENT_REMOVE = "ACTION=remove/VALUE=([\\w_]+)";
    //"mode=pass/type=enchant/action=(add|remove)/value=(\\w\\d_-)";
    private static final String PASS_THROUGH_MODE_ENCHANT_LEVEL_MODIFY = "ACTION=(minus|plus)/VALUE=enchant=([\\w_]+),change=([\\d]+)";
    //`"mode=pass/type=enchant_level/action=(?i)(minus|plus)/value=(\\+|-)(\\d+)";
    private static final String PASS_THROUGH_MODE_LORE_ADD = "ACTION=add/VALUE=(.+)";
    private static final String PASS_THROUGH_MODE_LORE_CLEAR = "ACTION=clear/VALUE=null";
    private static final String PASS_THROUGH_MODE_LORE_MODIFY = "ACTION=modify/VALUE=line=([\\d]+),lore=(.+)";
    //"mode=pass/type=lore/action=add/value=(.+)";
    //"mode=pass/type=lore/action=remove/value=null";
    //"mode=pass/type=lore/action=modify/value=(.+)";
    private static final String PASS_THROUGH_MODE_CONTAINER_MODIFY = "ACTION=modify/VALUE=(.+)";
    // modify (set, modify (+-/*^)) defined in InventoryUtil
    private static final String PASS_THROUGH_MODE_CONTAINER_REMOVE = "ACTION=remove/VALUE=([\\w\\d-_]+)";
    private static final String PASS_THROUGH_MODE_CONTAINER_ADD = "ACTION=add/VALUE=name=([\\w\\d-_]+),type=(string|double|int),init=(.+)";
    //"mode=pass/type=container/action=modify/value=(.+)";
    //"mode=pass/type=container/action=remove/value=(.+)";
    //"mode=pass/type=container/action=add/value=(.+)";
    private static final String PASS_THROUGH_MODE_DURABILITY_MODIFY = "ACTION=(minus|plus)/VALUE=([\\d]+)";
    //"mode=pass/type=durability/action=(?i)(minus|plus)/value=(\\d+)";


    private static final String PASS_THROUGH_MODE_LEATHER_ARMOR_COLOR_MODIFY_FROM_NAME = "ACTION=(?i)(name)/VALUE=([\\w_]+)";
    private static final String PASS_THROUGH_MODE_LEATHER_ARMOR_COLOR_MODIFY_FROM_RGB = "ACTION=(?i)(rgb)/VALUE=R=(\\d{1,3}),G=(\\d{1,3}),B=(\\d{1,3})";
    private static final String PASS_THROUGH_MODE_LEATHER_ARMOR_COLOR_MODIFY_FROM_RANDOM = "ACTION=(?i)(random)/VALUE=null";
    //  mode=pass/type=armor_color/action=name/value=([\w_]+)
    //  mode=pass/type=armor_color/action=rgb/value=R=(\d{1,3}),G=(\d{1,3}),B=(\d{1,3})
    //  mode=pass/type=armor_color/action=random/value=null
    // randoms last argument is a seed of Random classes constructor (default is empty string.)

    private static final String PASS_THROUGH_MODE_TEXTURE_ID_CLEAR = "ACTION=clear/VALUE=null";
    private static final String PASS_THROUGH_MODE_TEXTURE_ID_MODIFY = "ACTION=modify/VALUE=(\\d+)";
    //"mode=pass/type=texture_id/action=clear/value=null"
    //"mode=pass/type=texture_id/action=modify/value=(\\d+)"
    private static final String PASS_THROUGH_MODE_DISPLAY_NAME_MODIFY = "ACTION=item_name/VALUE=(.+)";
    private static final String PASS_THROUGH_MODE_DISPLAY_NAME_CLEAR = "ACTION=item_name/VALUE=null";
    //"mode=pass/type=item_name/action=modify/value=(.+)"
    //"mode=pass/type=item_name/action=clear/value=null"

    private static final String PASS_THROUGH_MODE_ITEM_FLAG_CLEAR = "ACTION=clear/VALUE=null";
    private static final String PASS_THROUGH_MODE_ITEM_FLAG_ADD = "ACTON=add/VALUE=([\\w_]+)";
    private static final String PASS_THROUGH_MODE_ITEM_FLAG_REMOVE = "ACTION=remove/VALUE=([\\d_]+)";
    //"mode=pass/type=item_flag/action=clear/value=null"
    //"mode=pass/type=item_flag/action=add/value=([\\w_]+)"
    //"mode=pass/type=item_flag/action=remove/value=([\\w_]+)"


    private String name;
    private Map<Enchantment,Integer> enchantsInfo;
    private int amount;
    private Map<MetadataType, List<String>> metadata;
    private String nameOrRegex;
    private int matchPoint;

    public Result(String name,Map<Enchantment,Integer> enchantsInfo,int amount,Map<MetadataType,List<String>> metadata,String nameOrRegex,int matchPoint){
        this.name = name;
        this.enchantsInfo = enchantsInfo;
        this.amount = amount;
        this.metadata = metadata;
        this.nameOrRegex = nameOrRegex;
        this.matchPoint = matchPoint;
    }

    public Result() {
    }

    public Result(String name) {
        // for pass-through
        this.name = name;
        this.enchantsInfo = null;
        this.amount = -1;
        this.metadata = null;
        this.nameOrRegex = "";
        this.matchPoint = Integer.MIN_VALUE;
    }

    public String getName() {
        return name;
    }

    public Result setName(String name) {
        this.name = name;
        return this;
    }

    public Map<Enchantment, Integer> getEnchantsInfo() {
        return enchantsInfo;
    }

    public Result setEnchantsInfo(Map<Enchantment, Integer> enchantsInfo) {
        this.enchantsInfo = enchantsInfo;
        return this;
    }

    public int getAmount() {
        return amount;
    }

    public Result setAmount(int amount) {
        this.amount = amount;
        return this;
    }

    public Map<MetadataType, List<String>> getMetadata() {
        return metadata;
    }

    public Result setMetadataValue(Map<MetadataType, List<String>> metadata) {
        this.metadata = metadata;
        return this;
    }

    public String getNameOrRegex() {
        return nameOrRegex;
    }

    public Result setNameOrRegex(String nameOrRegex) {
        this.nameOrRegex = nameOrRegex;
        return this;
    }

    public int getMatchPoint() {
        return matchPoint;
    }

    public Result setMatchPoint(int matchPoint) {
        this.matchPoint = matchPoint;
        return this;
    }

    public void setMetaData(ItemStack item){
        if(metadata==null || metadata.isEmpty()) return;
        ItemMeta meta = item.getItemMeta();

        for(Map.Entry<MetadataType,List<String>> entry : metadata.entrySet()){
            /*
            * kind of metadata -> lore, displayName, enchantment, itemFlag, unbreakable, customModelData, PotionData, PotionColor, Texture_id
            *
            * lore -> split with ","
            * displayName -> used directly itemName
            * enchantment -> "enchantment, level". These are separated with ",".
            * itemFlag -> "flagName, bool". These are separated with ",".
            * unbreakable -> "bool"
            * customModelData -> "modelNumber"
            * potionData -> "PotionEffectType, duration, amplifier(level)". These are separated with ",".
            * potionColor -> "red, green, blue" (RGB)
            * texture_id -> "([0-9]+)" only set the id that written at first on the list.
            * tool_durability -> "([0-9]+)" only. The item's remaining durability will set that the value.
            * attribute_modifier -> (Attribute)/operation:(add/multiply/add_scalar)/value:(double)/slot:(EquipmentSlot)
            *
            * book_field -> (setAuthor/setTitle)/value:(.+)
            * (deprecated) book_field -> (setPage)/page:(\\d+)/value:(.+) [page specify]
            * book_field -> (setPages)/value:(.+) [page un-specify]
            * book_field -> (setGeneration)/value:(original/tattered/copy_of_copy/copy_of_original)
            * book_field -> (addPage)/value:(.+)
            * book_field -> (addLong)/value:(.+) (auto divide pages)
            * book_field -> (addLongExtend)/value:(.+) (auto read data from a specified data and auto divide)
            *
            * leather_armor_color -> type:rgb/value:R->(\\d{1,3}),G->(\\d{1,3}),B->(\\d{1,3})
            * leather_armor_color -> type:name/value:([\\w_]+)  #color name
            * leather_armor_color -> type:random
            *
            * pass_through_mode_enchantment_modify -> mode=pass/type=enchant/action=(?i)(add|remove)/value=([\\w_]+)
            * pass_through_mode_enchant_level_modify -> mode=pass/type=enchant_level/action=(?i)(minus|plus)/value=(+|-)(\d+)
            * pass_through_mode_add_lore -> mode=pass/type=lore/action=add/value=(.+)
            * pass_through_mode_remove_lore -> mode=pass/type=lore/action=remove
            * pass_through_mode_lore_modify -> mode=pass/type=lore/action=modify/value=(.+)
            * pass_through_mode_container_modify -> mode=pass/type=container/action=modify/value=(.+)
            * pass_through_mode_durability -> mode=pass/type=durability/action=(?i)(minus|plus)/value=(\\d+)
            * pass_through_mode_armor_color -> mode=pass/type=armor_color/action=name/value=([\w_]+)
            * pass_through_mode_armor_color -> mode=pass/type=armor_color/action=rgb/value=R=(\d{1,3}),G=(\d{1,3}),B=(\d{1,3})
            * pass_through_mode_armor_color -> mode=pass/type=armor_color/action=random/value=null
            * pass_through_mode_texture_id -> mode=pass/type=texture_id/action=clear/value=null
            * pass_through_mode_texture_id -> mode=pass/type=texture_id/action=modify/value=(\\d+)
            * pass_through_mode_item_name -> mode=pass/type=item_name/action=modify/value=(.+)
            * pass_through_mode_item_name -> mode=pass/type=item_name/action=clear/value=null
            *
             */

            MetadataType type = entry.getKey();
            List<String> content = entry.getValue();

            if(type.equals(MetadataType.LORE)) meta.setLore(content);
            if(type.equals(MetadataType.DISPLAYNAME)) meta.setDisplayName(content.get(0));
            if(type.equals(MetadataType.ENCHANTMENT)) {
                for(String s : content){
                    List<String> enchants = Arrays.asList(s.split(","));
                    Enchantment enchant = Enchantment.getByKey(NamespacedKey.minecraft(enchants.get(0).toLowerCase()));
                    int level = Integer.parseInt(enchants.get(1));
                    meta.addEnchant(enchant,level,true);
                }
            }

            if(type.equals(MetadataType.ITEMFLAG)) content.forEach(s->meta.addItemFlags(ItemFlag.valueOf(s.toUpperCase())));
            if(type.equals(MetadataType.UNBREAKABLE)) meta.setUnbreakable(Boolean.parseBoolean(content.get(0)));
            if(type.equals(MetadataType.CUSTOMMODELDATA)) meta.setCustomModelData(Integer.parseInt(content.get(0)));
            if(type.equals(MetadataType.POTIONDATA)) {
                if(!new PotionUtil().isPotion(item.getType())) return;
                for(String s : content){
                    List<String> potionData = Arrays.asList(s.split(","));
                    PotionEffectType effectType = PotionEffectType.getByName(potionData.get(0).toUpperCase());
                    int duration = Math.max(Integer.parseInt(potionData.get(1)), 1);
                    int amplifier = Math.max(Integer.parseInt(potionData.get(2)), 1);
                    PotionEffect effect = new PotionEffect(effectType,duration,amplifier);
                    PotionMeta potionMeta = (PotionMeta)  meta;
                    potionMeta.addCustomEffect(effect,true);
                }
            }
            if(type.equals(MetadataType.POTIONCOLOR)) {
                for(String s : content){
                    List<String> colors = Arrays.asList(s.split(","));
                    int r = Integer.parseInt(colors.get(0));
                    int g = Integer.parseInt(colors.get(1));
                    int b = Integer.parseInt(colors.get(2));

                    PotionMeta potionMeta = (PotionMeta)meta;
                    potionMeta.setColor(Color.fromRGB(r,g,b));
                }
            }
            if (type.equals(MetadataType.TEXTURE_ID)) {
                if (content.isEmpty()) continue;
                String id = content.get(0);
                Matcher matcher = Pattern.compile("^([0-9]+)$").matcher(id);
                if (!matcher.matches()) continue;
                meta.setCustomModelData(Integer.parseInt(id));
            }

            if (type.equals(MetadataType.ATTRIBUTE_MODIFIER)) {
                // attribute_modifier,type:(Attribute)/operation:(add/multiply/add_scalar)/value:(double)
                // attribute_modifier,type:(Attribute)/operation:(add/multiply/add_scalar)/value:(double)/slot:(EquipmentSlot)
                for (String s : content) {
                    Matcher matcher;
                    boolean isNormal = false;
                    if (s.matches(NORMAL_ATTRIBUTE_MODIFIER_PATTERN)) {
                        matcher = Pattern.compile(NORMAL_ATTRIBUTE_MODIFIER_PATTERN).matcher(s);
                        isNormal = true;
                    } else if (s.matches(EQUIPMENT_ATTRIBUTE_MODIFIER_PATTERN)) {
                        matcher = Pattern.compile(EQUIPMENT_ATTRIBUTE_MODIFIER_PATTERN).matcher(s);
                    } else {
                        continue;
                    }
                    AttributeModifier modifier = AttributeModifierUtil.getAttributeModifier(matcher, isNormal);
                    if (modifier == null) continue;
                    Attribute attribute = Attribute.valueOf(matcher.group(1).toUpperCase());
                    meta.addAttributeModifier(attribute, modifier);
                }
            }

            if (type.equals(MetadataType.TOOL_DURABILITY)) {
                // tool_durability,value:([0-9]+)
                Damageable damageable;
                try {
                    damageable = (Damageable) meta;
                } catch (Exception e) {
                    Bukkit.getLogger().warning("[CustomCrafter] ToolDurability (Result) failed. (Illegal item.(Un-Damageable))");
                    continue;
                }
                String s = content.get(0);
                Matcher matcher = Pattern.compile("^value:([0-9]+)$").matcher(s);
                if (!matcher.matches()) {
                    Bukkit.getLogger().warning("[CustomCrafter] ToolDurability (Result) failed. (Illegal configuration format found.)");
                    continue;
                }
                int remaining = Integer.parseInt(matcher.group(1));
                int maxDurability = item.getType().getMaxDurability();
                if (remaining == 0) remaining = 1;
                if (maxDurability < remaining) remaining = maxDurability;
                damageable.setDamage(maxDurability - remaining);
            }

            if (type.equals(MetadataType.BOOK_FIELD)) {
                BookMeta bookMeta;
                try {
                    bookMeta = (BookMeta) meta;
                } catch (Exception e) {
                    Bukkit.getLogger().warning("[CustomCrafter] Book Elements (Result) failed. (Illegal item.(Not BookMeta))");
                    continue;
                }

                for (String s : content) {
                    Matcher matcher = Pattern.compile(BOOK_FIELD_PATTERN).matcher(s);
                    if (!matcher.matches()) {
                        Bukkit.getLogger().warning("[CustomCrafter] Book Elements (Result) failed. (Illegal book field pattern)");
                        continue;
                    }
                    String bookFieldType = matcher.group(1).toLowerCase();
                    String value = matcher.group(2);

                    if (bookFieldType.equals("author")) InventoryUtil.setAuthor(bookMeta, value);
                    if (bookFieldType.equals("title")) InventoryUtil.setTitle(bookMeta, value);
                    if (bookFieldType.equals("generation")) InventoryUtil.setGeneration(bookMeta, value);
                    if (bookFieldType.equals("add_page")) InventoryUtil.addPage(bookMeta, value);
                    if (bookFieldType.equals("pages")) InventoryUtil.setPages(bookMeta, value);
                    if (bookFieldType.equals("add_long")) InventoryUtil.addLong(bookMeta, value, false);
                    if (bookFieldType.equals("add_long_extend")) InventoryUtil.addLong(bookMeta, value, true);
                }
            }

            if (type.equals(MetadataType.LEATHER_ARMOR_COLOR)) {
                LeatherArmorMeta leatherArmorMeta;
                try {
                    leatherArmorMeta = (LeatherArmorMeta) meta;
                } catch (Exception e) {
                    Bukkit.getLogger().warning("[CustomCrafter] Leather Armor Color (Result) failed. (Illegal item. (Not LeatherArmorMeta))");
                    continue;
                }

                for (String s : content) {
                    Matcher matcher = Pattern.compile(LEATHER_ARMOR_COLOR_PATTERN).matcher(s);
                    if (!matcher.find()) {
                        Bukkit.getLogger().warning("[CustomCrafter] Leather Armor Color (Result) failed. (Illegal armor color pattern)");
                        continue;
                    }

                    String colorType = matcher.group(1).toLowerCase();

                    if (colorType.equals("random")) InventoryUtil.setLeatherArmorColorRandom(leatherArmorMeta);
                    if (colorType.equals("rgb")) InventoryUtil.setLeatherArmorColorFromRGB(leatherArmorMeta, s);
                    if (colorType.equals("name")) InventoryUtil.setLeatherArmorColorFromName(leatherArmorMeta, s);
                }
            }

            if (type.equals(MetadataType.PASS_THROUGH)) {
                // pass_through processor
                for (String s : entry.getValue()) {
                    s = s.toLowerCase();

                    Matcher template = Pattern.compile(PASS_THROUGH_MODE_TEMPLATE).matcher(s);
                    if (!template.matches()) {
                        Bukkit.getLogger().warning("[CustomCrafter] Pass-through mode failed. (Illegal pass through config found).");
                        continue;
                    }
                    String TYPE = template.group(1);
                    String ACTION = template.group(2);
                    String VALUE = template.group(3);

                    if (TYPE.equals("enchant") && (
                            isFollowPattern(PASS_THROUGH_MODE_ENCHANTMENT_ADD, ACTION, VALUE) ||
                            isFollowPattern(PASS_THROUGH_MODE_ENCHANTMENT_REMOVE, ACTION, VALUE))) {
                        // enchant
                        InventoryUtil.enchantModify(ACTION, VALUE, meta);
                    } else if (TYPE.equals("enchant_level") && isFollowPattern(PASS_THROUGH_MODE_ENCHANT_LEVEL_MODIFY, ACTION, VALUE)) {
                        // enchant level modify
                        InventoryUtil.enchantLevel(ACTION, VALUE, meta);

                    } else if (TYPE.equals("lore") && (
                            isFollowPattern(PASS_THROUGH_MODE_LORE_ADD, ACTION, VALUE) ||
                            isFollowPattern(PASS_THROUGH_MODE_LORE_CLEAR, ACTION, VALUE) ||
                            isFollowPattern(PASS_THROUGH_MODE_LORE_MODIFY, ACTION, VALUE))) {
                        // lore
                        InventoryUtil.loreModify(ACTION, VALUE, meta);

                    } else if (TYPE.equals("container") && (
                            isFollowPattern(PASS_THROUGH_MODE_CONTAINER_MODIFY, ACTION, VALUE) ||
                            isFollowPattern(PASS_THROUGH_MODE_CONTAINER_REMOVE, ACTION, VALUE) ||
                            isFollowPattern(PASS_THROUGH_MODE_CONTAINER_ADD, ACTION, VALUE))) {
                        // container
                        ContainerUtil.containerModify(ACTION, VALUE, meta);

                    } else if (TYPE.equals("durability") && isFollowPattern(PASS_THROUGH_MODE_DURABILITY_MODIFY, ACTION, VALUE)) {
                        // durability modify
                        InventoryUtil.durabilityModify(ACTION, VALUE, meta);

                    } else if (TYPE.equals("armor_color") && (
                            isFollowPattern(PASS_THROUGH_MODE_LEATHER_ARMOR_COLOR_MODIFY_FROM_NAME, ACTION, VALUE) ||
                            isFollowPattern(PASS_THROUGH_MODE_LEATHER_ARMOR_COLOR_MODIFY_FROM_RANDOM, ACTION, VALUE) ||
                            isFollowPattern(PASS_THROUGH_MODE_LEATHER_ARMOR_COLOR_MODIFY_FROM_RGB, ACTION, VALUE))) {
                        // armor_color
                        InventoryUtil.armorColor(ACTION, VALUE, meta);

                    } else if (TYPE.equals("texture_id") && (
                            isFollowPattern(PASS_THROUGH_MODE_TEXTURE_ID_MODIFY, ACTION, VALUE) ||
                            isFollowPattern(PASS_THROUGH_MODE_TEXTURE_ID_CLEAR, ACTION, VALUE))) {
                        // texture_id
                        InventoryUtil.textureIdModify(ACTION, VALUE, meta);

                    } else if (TYPE.equals("item_name") && (
                            isFollowPattern(PASS_THROUGH_MODE_DISPLAY_NAME_CLEAR, ACTION, VALUE) ||
                            isFollowPattern(PASS_THROUGH_MODE_DISPLAY_NAME_MODIFY, ACTION, VALUE))) {
                        // display_name
                        InventoryUtil.displayNameModify(ACTION, VALUE, meta);

                    } else if (TYPE.equals("item_flag") && (
                            isFollowPattern(PASS_THROUGH_MODE_ITEM_FLAG_CLEAR, ACTION, VALUE) ||
                            isFollowPattern(PASS_THROUGH_MODE_ITEM_FLAG_ADD, ACTION, VALUE) ||
                            isFollowPattern(PASS_THROUGH_MODE_ITEM_FLAG_REMOVE, ACTION, VALUE))) {
                        // item_flag
                        InventoryUtil.itemFlagModify(ACTION, VALUE, meta);

                    }
                }
            }

            item.setItemMeta(meta);
        }
    }

    private boolean isFollowPattern(String pattern, String actionInput, String valueInput) {
        Matcher template = Pattern.compile("ACTION=(.+)/VALUE=(.+)").matcher(pattern);
        if (!template.matches()) {
            Bukkit.getLogger().warning("[CustomCrafter] Result Pass-through error. (Not follow the pattern.)");
            return false;
        }

        Matcher action = Pattern.compile(template.group(1)).matcher(actionInput);
        Matcher value = Pattern.compile(template.group(2)).matcher(valueInput);
        return action.matches() && value.matches();
    }
}
