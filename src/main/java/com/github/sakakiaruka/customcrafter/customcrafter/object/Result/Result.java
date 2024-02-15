package com.github.sakakiaruka.customcrafter.customcrafter.object.Result;

import org.bukkit.enchantments.Enchantment;

import java.util.List;
import java.util.Map;

public class Result {

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

//    public void setMetaData(ItemStack item){
//        if(metadata==null || metadata.isEmpty()) return;
//        ItemMeta meta = item.getItemMeta();
//
//        for(Map.Entry<MetadataType,List<String>> entry : metadata.entrySet()){
//            /*
//            * kind of metadata -> lore, displayName, enchantment, itemFlag, unbreakable, customModelData, PotionData, PotionColor, Texture_id
//            *
//            * lore -> split with ","
//            * displayName -> used directly itemName
//            * enchantment -> "enchantment, level". These are separated with ",".
//            * itemFlag -> "flagName, bool". These are separated with ",".
//            * unbreakable -> "bool"
//            * customModelData -> "modelNumber"
//            * potionData -> "PotionEffectType, duration, amplifier(level)". These are separated with ",".
//            * potionColor -> "red, green, blue" (RGB)
//            * texture_id -> "([0-9]+)" only set the id that written at first on the list.
//            * tool_durability -> "([0-9]+)" only. The item's remaining durability will set that the value.
//            * attribute_modifier -> (Attribute)/operation:(add/multiply/add_scalar)/value:(double)/slot:(EquipmentSlot)
//            *
//            * book_field -> (setAuthor/setTitle)/value:(.+)
//            * (deprecated) book_field -> (setPage)/page:(\\d+)/value:(.+) [page specify]
//            * book_field -> (setPages)/value:(.+) [page un-specify]
//            * book_field -> (setGeneration)/value:(original/tattered/copy_of_copy/copy_of_original)
//            * book_field -> (addPage)/value:(.+)
//            * book_field -> (addLong)/value:(.+) (auto divide pages)
//            * book_field -> (addLongExtend)/value:(.+) (auto read data from a specified data and auto divide)
//            *
//            * leather_armor_color -> type:rgb/value:R->(\\d{1,3}),G->(\\d{1,3}),B->(\\d{1,3})
//            * leather_armor_color -> type:name/value:([\\w_]+)  #color name
//            * leather_armor_color -> type:random
//            *
//            * pass_through_mode_enchantment_modify -> mode=pass/type=enchant/action=(?i)(add|remove)/value=([\\w_]+)
//            * pass_through_mode_enchant_level_modify -> mode=pass/type=enchant_level/action=(?i)(minus|plus)/value=(+|-)(\d+)
//            * pass_through_mode_add_lore -> mode=pass/type=lore/action=add/value=(.+)
//            * pass_through_mode_remove_lore -> mode=pass/type=lore/action=remove
//            * pass_through_mode_lore_modify -> mode=pass/type=lore/action=modify/value=(.+)
//            * pass_through_mode_container_modify -> mode=pass/type=container/action=modify/value=(.+)
//            * pass_through_mode_durability -> mode=pass/type=durability/action=(?i)(minus|plus)/value=(\\d+)
//            * pass_through_mode_armor_color -> mode=pass/type=armor_color/action=name/value=([\w_]+)
//            * pass_through_mode_armor_color -> mode=pass/type=armor_color/action=rgb/value=R=(\d{1,3}),G=(\d{1,3}),B=(\d{1,3})
//            * pass_through_mode_armor_color -> mode=pass/type=armor_color/action=random/value=null
//            * pass_through_mode_texture_id -> mode=pass/type=texture_id/action=clear/value=null
//            * pass_through_mode_texture_id -> mode=pass/type=texture_id/action=modify/value=(\\d+)
//            * pass_through_mode_item_name -> mode=pass/type=item_name/action=modify/value=(.+)
//            * pass_through_mode_item_name -> mode=pass/type=item_name/action=clear/value=null
//            *
//             */

}
