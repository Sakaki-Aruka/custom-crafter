package online.aruka.demo.ui;

import io.github.sakaki_aruka.customcrafter.api.interfaces.ui.CraftUIDesigner;
import io.github.sakaki_aruka.customcrafter.api.objects.recipe.CoordinateComponent;
import kotlin.Pair;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import online.aruka.demo.Demo;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CraftUI implements CraftUIDesigner {
    @Override
    public @NotNull Component title(@NotNull Context context) {
        return MiniMessage.miniMessage().deserialize("<rainbow>!!!!!Custom Crafter!!!!!");
    }

    @Override
    public @NotNull CoordinateComponent resultSlot(@NotNull Context context) {
        return CoordinateComponent.fromIndex(27);
    }

    @Override
    public @NotNull Pair<CoordinateComponent, ItemStack> makeButton(@NotNull Context context) {
        ItemStack button = ItemStack.of(Material.CRAFTING_TABLE);
        button.editMeta(meta -> meta.customName(MiniMessage.miniMessage().deserialize("<white>Click and Craft")));
        return new Pair<>(CoordinateComponent.fromIndex(18), button);
    }

    @Override
    public @NotNull Map<CoordinateComponent, ItemStack> blankSlots(@NotNull Context context) {
        Map<CoordinateComponent, ItemStack> map = new HashMap<>();
        for (int y = 0; y < 6; y++) {
            for (int x = 0; x < 3; x++) {
                var c = new CoordinateComponent(x, y);
                if (c.toIndex() == 18 || c.toIndex() == 27) continue;
                var blank = ItemStack.of(Material.BLACK_STAINED_GLASS_PANE);
                blank.editMeta(meta -> {
                    meta.customName(Component.text(""));
                    meta.addItemFlags(ItemFlag.values());
                    meta.getPersistentDataContainer().set(
                            new NamespacedKey(Demo.plugin, "blank_key"),
                            PersistentDataType.STRING,
                            UUID.randomUUID().toString()
                    );
                });
                map.put(c, blank);
            }
        }
        return map;
    }
}
