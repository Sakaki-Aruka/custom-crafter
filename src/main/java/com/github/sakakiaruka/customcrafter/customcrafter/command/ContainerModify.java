package com.github.sakakiaruka.customcrafter.customcrafter.command;

import com.github.sakakiaruka.customcrafter.customcrafter.util.ContainerUtil;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.Arrays;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.github.sakakiaruka.customcrafter.customcrafter.CustomCrafter.getInstance;
import static com.github.sakakiaruka.customcrafter.customcrafter.SettingsLoad.LINE_SEPARATOR;
import static com.github.sakakiaruka.customcrafter.customcrafter.SettingsLoad.UPPER_ARROW;

public class ContainerModify {

    public static final String CONTAINER_KEY_PATTERN = "^([a-z0-9\\.\\-_]{1,255})$";
    public static final String CONTAINER_OPERATION_PATTERN = "^(\\+|\\-|/|\\*)$";
    public static final String CONTAINER_DATATYPE_PATTERN = "^(?i)(string|int|double)$";
    public static final String NUMBERS_PATTERN = "^([\\-0-9\\.]+)$";
    public static final String NUMBERS_ALPHABET = "^([\\-\\w\\d\\.]+)$";
    // this class provides add (set), remove, value-modify
    /*
    * all argument combinations:
    * /cc -container -add [key] [type] [value]
    * /cc -container -remove [key]
    * /cc -container -set [key] [type] [value]
    * /cc -container -value_modify [key] [type] [operation] [oped-key] [oped-value]
    *
    * arguments pattern:
    * [key] -> follow 'CONTAINER_KEY_PATTERN'
    * [type] -> follow '(?i)(string|int)'
    * [value] -> not null or empty string
    * [operation] -> follow '(\\+\\-*\\/)'
    * [oped-key] -> same with [key]
    *
    * patterns transition
    * [key] -> {ContainerKey}
    * [type] -> {ContainerDataType}
    * [value] -> {NumbersAlphabet}
    * [operation] -> {MathematicalOperators}
    * [oped-value] -> {ContainerKey} (same with [key])
     */

    // /cc -container -add [key] [type] [value]
    // /cc -container -set [key] [type] [value]
    public void addSet(String[] args, CommandSender sender) {
        String modifyType = args[1].replace("-","");
        Player player = (Player) sender;
        if (!checkMainHand(player, modifyType)) return;
        ItemStack item = player.getInventory().getItemInMainHand();

        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();
        String before = new ContainerUtil().containerValues(container);

        String keyName = args[2].toLowerCase();
        if (!getKeyCongruence(keyName, modifyType, sender)) return;

        NamespacedKey key = new NamespacedKey(getInstance(), keyName);
        PersistentDataType type;
        if ((type = new ContainerUtil().getDataType(args[3])) == null) {
            sender.sendMessage("Invalid NamespacedKey type.");
            return;
        }

        if (modifyType.equalsIgnoreCase("set")) {
            if (!container.has(key, type)) {
                sender.sendMessage("Container set > The specified key isn't contained.");
                return;
            }
        }

        String value = args[4];
        int intValue = 0;
        double doubleValue = 0d;
        if (type.equals(PersistentDataType.INTEGER)) {
            try{
                intValue = Integer.parseInt(value);
            }catch (Exception e) {
                sender.sendMessage("Container "+modifyType+" > The provided value is not a number.");
                return;
            }
        }

        if (type.equals(PersistentDataType.DOUBLE)) {
            try{
                doubleValue = Double.parseDouble(value);
            }catch (Exception e) {
                sender.sendMessage("Container "+modifyType+" > The provided value is not a number.");
                return;
            }
        }

        if (type.equals(PersistentDataType.STRING)) {
            if (5 < args.length) {
                value += String.join(" ", Arrays.asList(args).subList(5, args.length));
            }
            container.set(key, type, value);
        } else if (type.equals(PersistentDataType.INTEGER)) {
            container.set(key, type, intValue);
        } else if (type.equals(PersistentDataType.DOUBLE)) {
            container.set(key, type, doubleValue);
        }

        item.setItemMeta(meta);

        String after = new ContainerUtil().containerValues(container);
        sendDataDiff(before, after, modifyType, sender);
    }

    // /cc -container -remove [key] [type]
    public void remove(String[] args, CommandSender sender) {
        Player player = (Player) sender;
        if (!checkMainHand(player, "remove")) return;
        ItemStack item = player.getInventory().getItemInMainHand();

        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();
        String before = new ContainerUtil().containerValues(container);

        String keyName = args[2].toLowerCase();
        if (!getKeyCongruence(keyName, "remove", sender)) return;
        NamespacedKey key = new NamespacedKey(getInstance(), keyName);
        PersistentDataType type;
        if ((type = new ContainerUtil().getDataType(args[3])) == null) {
            sender.sendMessage("Container remove > Invalid NamespacedKey type.");
            return;
        }

        if (!container.has(key, type)) {
            sender.sendMessage("Container remove > The specified key isn't contained.");
            sender.sendMessage("Container remove > That has following data.");
            sender.sendMessage("Container remove > Your request nk -> "+key.toString());
            sender.sendMessage("Container remove > ");
            if (container.getKeys().isEmpty()) {
                sender.sendMessage("  Container remove > The container has not any keys.");
                return;
            }
            for (NamespacedKey k : container.getKeys()) {
                sender.sendMessage("  Container remove > "+k.toString());
            }
            return;
        }

        container.remove(key);
        item.setItemMeta(meta);
        String after = new ContainerUtil().containerValues(container);
        sendDataDiff(before, after, "remove", sender);
    }

    // /cc -container -value_modify [key] [type] [operator] [oped-key] [used-value]
    public void valueModify(String[] args, CommandSender sender) {
        String modifyType = "value_modify";
        Player player = (Player) sender;

        String keyName = args[2];
        String opedKeyName = args[5];

        NamespacedKey key = new NamespacedKey(getInstance(), keyName);
        NamespacedKey opedKey = new NamespacedKey(getInstance(), opedKeyName);

        if (!checkMainHand(player, modifyType)) return;
        ItemStack item = player.getInventory().getItemInMainHand();
        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();
        String before = new ContainerUtil().containerValues(container);

        if (!getKeyCongruence(keyName, modifyType, sender)) return;
        PersistentDataType type;
        if ((type = new ContainerUtil().getDataType(args[3])) == null) {
            sender.sendMessage("Invalid NamespacedKey type.");
            return;
        }

        Object targetObj = container.get(key, type);

        String operator = args[4];
        String usedValueString = args[6];
        // string + string
        if (!usedValueString.matches(NUMBERS_PATTERN) && usedValueString.matches(NUMBERS_ALPHABET) && type.equals(PersistentDataType.STRING)) {
            if (operator.equals("*") || operator.equals("/")) {
                sender.sendMessage("Container "+modifyType+" > The specified operator.");
                return;
            }
            if (operator.equals("-") && !String.valueOf(targetObj).contains(usedValueString)) {
                sender.sendMessage("Container "+modifyType+" > The system is not able to the specified operation.");
                return;
            }

            String target = String.valueOf(targetObj);
            String result;

            if (operator.equals("+")) result = target + usedValueString;
            else if (operator.equals("-")) result = target.replace(usedValueString,"");
            else return;

            container.set(key, PersistentDataType.STRING, result);
            item.setItemMeta(meta);

        // int + int
        }else if (usedValueString.matches(NUMBERS_PATTERN) && type.equals(PersistentDataType.INTEGER)){
            int target = Integer.parseInt(String.valueOf(targetObj));
            int usedValue = Integer.parseInt(args[6]);

            int result;
            if (operator.equals("+")) result = target + usedValue;
            else if (operator.equals("-")) result = target - usedValue;
            else if (operator.equals("*")) result = target * usedValue;
            else if (operator.equals("/")) result = Math.round(target / usedValue);
            else return;

            container.set(key, PersistentDataType.INTEGER, result);
            item.setItemMeta(meta);
        }

        String after = new ContainerUtil().containerValues(container);
        sendDataDiff(before, after, modifyType, sender);
    }

    // /cc -container -data -show
    public void data(CommandSender sender) {
        Player player = (Player) sender;
        if (!checkMainHand(player, "show")) {
            sender.sendMessage("Container data(show) > No container data found.");
            return;
        }
        ItemStack item = player.getInventory().getItemInMainHand();
        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();
        String result = new ContainerUtil().containerValues(container);
        sender.sendMessage(result);
        UUID uuid = UUID.randomUUID();
        sender.sendMessage(UPPER_ARROW +" Container Ticket: "+uuid);
    }

    // /cc -container -data -modifyShow [key] [operator] [key]
    public void modifyShow(String[] args, CommandSender sender) {
        Player player = (Player) sender;
        if (!checkMainHand(player, "modify-show")) {
            sender.sendMessage("Container modify-show > No container data found.");
            return;
        }

        NamespacedKey firstKey = new NamespacedKey(getInstance(), args[3]);
        NamespacedKey secondKey = new NamespacedKey(getInstance(), args[5]);
        ItemStack item = player.getInventory().getItemInMainHand();
        PersistentDataContainer container = item.getItemMeta().getPersistentDataContainer();
        String operator = args[4];

        PersistentDataType firstType = new ContainerUtil().getSpecifiedKeyType(container, firstKey);
        PersistentDataType secondType = new ContainerUtil().getSpecifiedKeyType(container, secondKey);
        if (firstType == null || secondType == null) {
            sender.sendMessage("Container modify-show > Container data type error.");
            sender.sendMessage("Container modify-show > Check the data type.");
            return;
        }

        if (operator.equals("+")) {
            //
        }
    }


    private void sendDataDiff(String before, String after, String modifyType, CommandSender sender) {
        StringBuilder builder = new StringBuilder();
        builder.append(before);
        builder.append(LINE_SEPARATOR +"The modify type is '"+modifyType+"'."+ LINE_SEPARATOR);
        builder.append(after);
        sender.sendMessage(builder.toString());
    }

    private boolean getKeyCongruence(String key, String operation, CommandSender sender) {
        Pattern pattern = Pattern.compile(CONTAINER_KEY_PATTERN);
        Matcher matcher = pattern.matcher(key);
        if (!matcher.matches()) {
            sender.sendMessage("Container "+operation+" > The specified key-name is invalid.");
            sender.sendMessage("Container "+operation+" > The key-name must follow the grammar. -> "+CONTAINER_KEY_PATTERN);
            return false;
        }
        return true;
    }

    private boolean checkMainHand(Player player, String opeType) {
        ItemStack item;
        if ((item = player.getInventory().getItemInMainHand()) == null) {
            player.sendMessage("Container "+opeType+" > Items that you have in your MainHand are empty.");
            return false;
        }

        if (item.getType().equals(Material.AIR)) {
            player.sendMessage("Container "+opeType+" > Items that you have in your MainHand are invalid type. (Material#AIR.)");
            return false;
        }

        return true;
    }
}
