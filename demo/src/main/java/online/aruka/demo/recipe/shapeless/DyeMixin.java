package online.aruka.demo.recipe.shapeless;

import com.destroystokyo.paper.MaterialTags;
import io.github.sakaki_aruka.customcrafter.api.interfaces.matter.CMatter;
import io.github.sakaki_aruka.customcrafter.api.interfaces.recipe.CRecipe;
import io.github.sakaki_aruka.customcrafter.api.interfaces.result.ResultSupplier;
import io.github.sakaki_aruka.customcrafter.impl.matter.CMatterImpl;
import io.github.sakaki_aruka.customcrafter.impl.recipe.CRecipeImpl;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.stream.Collectors;

public class DyeMixin {
    public static CRecipe mixWithoutBlack() {
        List<CMatter> colors = MaterialTags.DYES.getValues().stream()
                .filter(dye -> dye != Material.BLACK_DYE)
                .map(CMatterImpl::of)
                .collect(Collectors.toList());

        ResultSupplier supplier = ResultSupplier.timesSingle(ItemStack.of(Material.BLACK_DYE));

        return CRecipeImpl.shapeless(
                "Dye mixin without black",
                colors,
                null,
                List.of(supplier)
        );
    }
}
