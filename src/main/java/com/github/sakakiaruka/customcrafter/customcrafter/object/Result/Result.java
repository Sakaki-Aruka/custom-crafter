package com.github.sakakiaruka.customcrafter.customcrafter.object.Result;

import com.github.sakakiaruka.customcrafter.customcrafter.object.ContainerWrapper;
import com.github.sakakiaruka.customcrafter.customcrafter.util.AttributeModifierUtil;
import com.github.sakakiaruka.customcrafter.customcrafter.util.DataContainerUtil;
import com.github.sakakiaruka.customcrafter.customcrafter.util.InventoryUtil;
import com.github.sakakiaruka.customcrafter.customcrafter.util.PotionUtil;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;

import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Result {
    private static final String NORMAL_ATTRIBUTE_MODIFIER_PATTERN = "^type:([\\w_]+)/operation:(?i)(add|multiply|add_scalar)/value:(\\-?\\d+(\\.\\d+)?)$";
    private static final String EQUIPMENT_ATTRIBUTE_MODIFIER_PATTERN = "^type:([\\w_]+)/operation:(?i)(add|multiply|add_scalar)/value:(\\-?\\d+(\\.\\d+)?)/slot:(\\w+)$";
//    private static final String BOOK_FIELD_SET_AUTHOR_PATTERN = "^type:author/value:(.+)$";
//    private static final String BOOK_FIELD_SET_TITLE_PATTERN = "^type:title/value:(.+)$";
//    private static final String BOOK_FIELD_SET_PAGE_PATTERN = "^type:page/page:(\\d+)/value:(.+)$";
//    private static final String BOOK_FIELD_ADD_PAGE_PATTERN = "^type:add_page/value:(.+)$";
//    private static final String BOOK_FIELD_SET_PAGES_PATTERN = "^type:pages/value:(.+)$";
//    private static final String BOOK_FIELD_SET_GENERATION_PATTERN = "^type:generation/value:(?i)(original|tattered|copy_of_copy|copy_of_original)$";
//    private static final String BOOK_FIELD_ADD_LONG_PATTERN = "^type:add_long/value:(.+)$";
//    private static final String BOOK_FIELD_ADD_LONG_EXTEND_PATTERN = "^type:add_long_extend/value:(.+)$";
    private static final String BOOK_FIELD_PATTERN = "^type:(?i)(author|title|add_page|pages|generation|add_long|add_long_extend)/value:(.+)$";
//    private static final List<String> BOOK_FIELD_PATTERN_LIST = new ArrayList<>(Arrays.asList(
//            BOOK_FIELD_SET_AUTHOR_PATTERN,
//            BOOK_FIELD_SET_TITLE_PATTERN,
//            BOOK_FIELD_ADD_PAGE_PATTERN,
//            BOOK_FIELD_SET_PAGES_PATTERN,
//            BOOK_FIELD_SET_GENERATION_PATTERN,
//            BOOK_FIELD_ADD_LONG_PATTERN,
//            BOOK_FIELD_ADD_LONG_EXTEND_PATTERN
//    ));

    private String name;
    private Map<Enchantment,Integer> enchantsInfo;
    private int amount;
    private Map<MetadataType, List<String>> metadata;
    private String nameOrRegex;
    private int matchPoint;
    private List<ContainerWrapper> dataContainer;

    public Result(String name,Map<Enchantment,Integer> enchantsInfo,int amount,Map<MetadataType,List<String>> metadata,String nameOrRegex,int matchPoint, List<ContainerWrapper> dataContainer){
        this.name = name;
        this.enchantsInfo = enchantsInfo;
        this.amount = amount;
        this.metadata = metadata;
        this.nameOrRegex = nameOrRegex;
        this.matchPoint = matchPoint;
        this.dataContainer = dataContainer;
    }

    public List<ContainerWrapper> getDataContainer() {
        return dataContainer;
    }

    public void setDataContainer(List<ContainerWrapper> dataContainer) {
        this.dataContainer = dataContainer;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<Enchantment, Integer> getEnchantsInfo() {
        return enchantsInfo;
    }

    public void setEnchantsInfo(Map<Enchantment, Integer> enchantsInfo) {
        this.enchantsInfo = enchantsInfo;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public Map<MetadataType, List<String>> getMetadata() {
        return metadata;
    }

    public void setMetadataValue(Map<MetadataType, List<String>> metadata) {
        this.metadata = metadata;
    }

    public String getNameOrRegex() {
        return nameOrRegex;
    }

    public void setNameOrRegex(String nameOrRegex) {
        this.nameOrRegex = nameOrRegex;
    }

    public int getMatchPoint() {
        return matchPoint;
    }

    public void setMatchPoint(int matchPoint) {
        this.matchPoint = matchPoint;
    }

    public void setMetaData(ItemStack item){
        if(metadata==null || metadata.isEmpty()) return;
        ItemMeta meta = item.getItemMeta();

        new DataContainerUtil().addAllData(item, this.dataContainer);

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
             */

            MetadataType type = entry.getKey();
            List<String> content = entry.getValue();

            if(type.equals(MetadataType.LORE)) meta.setLore(content);
            if(type.equals(MetadataType.DISPLAYNAME)) meta.setDisplayName(content.get(0));
            if(type.equals(MetadataType.ENCHANTMENT)) {
                for(String s : content){
                    List<String> enchants = Arrays.asList(s.split(","));
                    Enchantment enchant = Enchantment.getByName(enchants.get(0).toUpperCase());
                    int level = Integer.valueOf(enchants.get(1));
                    meta.addEnchant(enchant,level,true);
                }
            }

            if(type.equals(MetadataType.ITEMFLAG)) content.forEach(s->meta.addItemFlags(ItemFlag.valueOf(s.toUpperCase())));
            if(type.equals(MetadataType.UNBREAKABLE)) meta.setUnbreakable(Boolean.valueOf(content.get(0)));
            if(type.equals(MetadataType.CUSTOMMODELDATA)) meta.setCustomModelData(Integer.valueOf(content.get(0)));
            if(type.equals(MetadataType.POTIONDATA)) {
                if(!new PotionUtil().isPotion(item.getType())) return;
                for(String s : content){
                    List<String> potionData = Arrays.asList(s.split(","));
                    PotionEffectType effectType = PotionEffectType.getByName(potionData.get(0).toUpperCase());
                    int duration = Integer.valueOf(potionData.get(1)) < 1 ? 1 : Integer.valueOf(potionData.get(1));
                    int amplifier = Integer.valueOf(potionData.get(2)) < 1 ? 1 : Integer.valueOf(potionData.get(2));
                    PotionEffect effect = new PotionEffect(effectType,duration,amplifier);
                    PotionMeta potionMeta = (PotionMeta)  meta;
                    potionMeta.addCustomEffect(effect,true);
                }
            }
            if(type.equals(MetadataType.POTIONCOLOR)) {
                for(String s : content){
                    List<String> colors = Arrays.asList(s.split(","));
                    int r = Integer.valueOf(colors.get(0));
                    int g = Integer.valueOf(colors.get(1));
                    int b = Integer.valueOf(colors.get(2));

                    PotionMeta potionMeta = (PotionMeta)meta;
                    potionMeta.setColor(Color.fromRGB(r,g,b));
                }
            }
            if (type.equals(MetadataType.TEXTURE_ID)) {
                if (content.isEmpty()) continue;
                String id = content.get(0);
                Matcher matcher = Pattern.compile("^([0-9]+)$").matcher(id);
                if (!matcher.matches()) continue;
                meta.setCustomModelData(Integer.valueOf(id));
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
                    AttributeModifier modifier = new AttributeModifierUtil().getAttributeModifier(matcher, isNormal);
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
                int remaining = Integer.valueOf(matcher.group(1));
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

                InventoryUtil util = new InventoryUtil();

                for (String s : content) {
                    Matcher matcher = Pattern.compile(BOOK_FIELD_PATTERN).matcher(s);
                    if (!matcher.matches()) {
                        Bukkit.getLogger().warning("[CustomCrafter] Book Elements (Result) failed. (Illegal book field pattern)");
                        continue;
                    }
                    String bookFieldType = matcher.group(1).toLowerCase();
                    String value = matcher.group(2);

                    if (bookFieldType.equals("author")) util.setAuthor(bookMeta, value);
                    if (bookFieldType.equals("title")) util.setTitle(bookMeta, value);
                    if (bookFieldType.equals("generation")) util.setGeneration(bookMeta, value);
                    if (bookFieldType.equals("add_page")) util.addPage(bookMeta, value);
                    if (bookFieldType.equals("pages")) util.setPages(bookMeta, value);
                    if (bookFieldType.equals("add_long")) util.addLong(bookMeta, value, false);
                    if (bookFieldType.equals("add_long_extend")) util.addLong(bookMeta, value, true);
                }
            }

            item.setItemMeta(meta);
        }
    }
}
