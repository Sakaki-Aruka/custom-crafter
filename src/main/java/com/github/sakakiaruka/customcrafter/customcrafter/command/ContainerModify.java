package com.github.sakakiaruka.customcrafter.customcrafter.command;

import com.github.sakakiaruka.customcrafter.customcrafter.util.ContainerUtil;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.github.sakakiaruka.customcrafter.customcrafter.CustomCrafter.getInstance;
import static com.github.sakakiaruka.customcrafter.customcrafter.SettingsLoad.nl;

public class ContainerModify {

    public static final String CONTAINER_KEY_PATTERN = "^([a-z0-9\\.\\-_]{1,255})$";
    public static final String CONTAINER_OPERATION_PATTERN = "^(\\+|\\-|/|*)$";
    public static final String CONTAINER_DATATYPE_PATTERN = "^(?i)(string|int)$";
    // this class provides add (set), remove, value-modify
    /*
    * all argument combinations:
    * /cc -container -add [key] [type] [value]
    * /cc -container -remove [key]
    * /cc -container -set [key] [type] [value]
    * /cc -container -value_modify [key] [type] [operation] [oped-key]
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
    * [value] -> {NotNull}
    * [operation] -> {MathematicalOperators}
    * [oped-key] -> {ContainerKey} (same with [key])
     */

    // /cc -container -add [key] [type] [value]
    // /cc -container -set [key] [type] [value]
    public void addSet(String[] args, CommandSender sender) {
        String modifyType = args[1].replace("-","");
        Player player = (Player) sender;
        ItemStack item;
        if ((item = player.getInventory().getItemInMainHand()) == null) {
            sender.sendMessage("Container add > You have no items in your MainHand.");
            return;
        }

        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();
        String before = new ContainerUtil().containerValues(container);

        String keyName = args[2].toUpperCase();
        if (getKeyCongruence(keyName, modifyType, sender)) return;

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

        Object value = args[4];
        container.set(key, type, value);

        String after = new ContainerUtil().containerValues(container);
        sendDataDiff(before, after, modifyType, sender);
    }

    // /cc -container -remove [key]
    public void remove(String[] args, CommandSender sender) {
        Player player = (Player) sender;
        ItemStack item;
        if ((item = player.getInventory().getItemInMainHand()) == null) {
            sender.sendMessage("Container remove > You have no items in your MainHand.");
            return;
        }

        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();
        String before = new ContainerUtil().containerValues(container);

        String keyName = args[2].toUpperCase();
        if (getKeyCongruence(keyName, "remove", sender)) return;
        NamespacedKey key = new NamespacedKey(getInstance(), keyName);
        PersistentDataType type;
        if ((type = new ContainerUtil().getDataType(args[3])) == null) {
            sender.sendMessage("Container remove > Invalid NamespacedKey type.");
            return;
        }

        if (!container.has(key, type)) {
            sender.sendMessage("Container remove > The specified key isn't contained.");
            return;
        }

        container.remove(key);
        String after = new ContainerUtil().containerValues(container);
        sendDataDiff(before, after, "remove", sender);
    }

    // /cc -value_modify [key] [type] [operator] [oped-key]
    public void valueModify(String[] args, CommandSender sender) {
        //
    }


    private void sendDataDiff(String before, String after, String modifyType, CommandSender sender) {
        StringBuilder builder = new StringBuilder();
        builder.append(before);
        builder.append(nl+"The modify type is '"+modifyType+"'."+nl);
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
}
