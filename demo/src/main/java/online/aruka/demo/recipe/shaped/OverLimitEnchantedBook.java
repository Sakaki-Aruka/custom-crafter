package online.aruka.demo.recipe.shaped;

import io.github.sakaki_aruka.customcrafter.api.interfaces.matter.CEnchantmentStoreMatter;
import io.github.sakaki_aruka.customcrafter.api.interfaces.matter.CMatter;
import io.github.sakaki_aruka.customcrafter.api.interfaces.recipe.CRecipe;
import io.github.sakaki_aruka.customcrafter.api.interfaces.result.ResultSupplier;
import io.github.sakaki_aruka.customcrafter.api.objects.matter.enchant.CEnchantComponent;
import io.github.sakaki_aruka.customcrafter.api.objects.matter.enchant.EnchantStrict;
import io.github.sakaki_aruka.customcrafter.api.objects.recipe.CRecipeType;
import io.github.sakaki_aruka.customcrafter.api.objects.recipe.CoordinateComponent;
import io.github.sakaki_aruka.customcrafter.impl.matter.CMatterImpl;
import io.github.sakaki_aruka.customcrafter.impl.matter.enchant.CEnchantmentStoreMatterImpl;
import io.github.sakaki_aruka.customcrafter.impl.recipe.CRecipeImpl;
import io.github.sakaki_aruka.customcrafter.impl.recipe.GroupRecipe;
import io.github.sakaki_aruka.customcrafter.impl.result.ResultSupplierImpl;
import kotlin.Pair;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class OverLimitEnchantedBook {
    public static CRecipe onlyEfficiency() {
        CEnchantmentStoreMatter enchantMatter = new CEnchantmentStoreMatterImpl(
                "Lv.5 Efficiency Enchanted Book",
                Set.of(Material.ENCHANTED_BOOK),
                Set.of(new CEnchantComponent(5, Enchantment.EFFICIENCY, EnchantStrict.STRICT)),
                1,
                false,
                null
        );

        GroupRecipe.Matter book = GroupRecipe.Matter.Companion.of(enchantMatter, true);
        GroupRecipe.Matter goldBlock = GroupRecipe.Matter.Companion.of(
                CMatterImpl.Companion.of(Material.GOLD_BLOCK),
                false
        );

        /*
         * # -> Gold Block
         * + -> Efficiency Lv.5 Enchanted Book or Air
         *
         * #+#
         * +#+
         * #+#
         *
         * Returns:
         *   - Enchanted Book Amount = 2: x1, Efficiency Lv.6 Enchanted Book
         *   - Enchanted Book Amount = 3: x1, Efficiency Lv.6 Enchanted Book / x1, Efficiency Lv.5 Enchanted Book
         *   - Enchanted Book Amount = 4: x1, Efficiency Lv.7 Enchanted Book
         */

        Map<CoordinateComponent, CMatter> items = CoordinateComponent.Companion.squareFill(3, 0, 0, false)
                .stream().map(c -> new Pair<CoordinateComponent, CMatter>(
                        c, c.toIndex() % 2 == 0 ? goldBlock : book)
                ).collect(Collectors.toMap(Pair::component1, Pair::component2));

        Set<GroupRecipe.Context> groups = Set.of(
                // Gold Block Group
                GroupRecipe.Context.Companion.of(
                        items.keySet().stream().filter(c -> c.toIndex() % 2 == 0)
                                .collect(Collectors.toSet()),
                        5,
                        "Gold Block Context"
                ),

                // Enchanted Book Group
                GroupRecipe.Context.Companion.of(
                        items.keySet().stream().filter(c -> c.toIndex() % 2 == 1)
                                .collect(Collectors.toSet()),
                        2, // min = 2, coordinates = 4 -> requires book placed on 2 ~ 4 slots
                        "Efficiency Lv.5 Enchanted Book Context"
                )
        );

        ResultSupplier supplier = new ResultSupplierImpl(ctx -> {
            List<ItemStack> results = new ArrayList<>();
            int bookCount = (int) ctx.getMapped().values().stream()
                    .filter(item -> item.getType() == Material.ENCHANTED_BOOK)
                    .count();
            if (bookCount / 2 == 1) {
                // 2, 3
                ItemStack lv6 = ItemStack.of(Material.ENCHANTED_BOOK);
                lv6.editMeta(meta -> {
                    ((EnchantmentStorageMeta) meta).addStoredEnchant(
                            Enchantment.EFFICIENCY, 6, true
                    );
                });
                results.add(lv6);

                if (bookCount == 3) {
                    results.add(ctx.getMapped().values().stream().filter(item ->
                            item.getType() == Material.ENCHANTED_BOOK
                    ).collect(Collectors.toList()).getFirst());
                }
            } else {
                ItemStack lv7 = ItemStack.of(Material.ENCHANTED_BOOK);
                lv7.editMeta(meta -> {
                    ((EnchantmentStorageMeta) meta).addStoredEnchant(
                            Enchantment.EFFICIENCY, 7, true
                    );
                });
                results.add(lv7);
            }
            return results;
        });

        return new GroupRecipe(
                "Over Limit Enchanted Book Recipe (Efficiency only)",
                items,
                groups,
                GroupRecipe.Companion.createFilters(CRecipeImpl.Companion.getDefaultFilters()),
                null,
                List.of(supplier),
                CRecipeType.NORMAL
        );
    }
}
