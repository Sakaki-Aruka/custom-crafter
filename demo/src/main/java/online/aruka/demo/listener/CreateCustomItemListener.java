package online.aruka.demo.listener;

import io.github.sakaki_aruka.customcrafter.api.event.CreateCustomItemEvent;
import io.github.sakaki_aruka.customcrafter.api.interfaces.recipe.CRecipe;
import io.github.sakaki_aruka.customcrafter.api.objects.MappedRelation;
import io.github.sakaki_aruka.customcrafter.api.search.Search;
import kotlin.Pair;
import online.aruka.demo.Demo;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public final class CreateCustomItemListener implements Listener {
    @EventHandler
    public void onCreateCustomItem(CreateCustomItemEvent event) {
        Search.SearchResult result = event.getResult();
        String playerName = event.getPlayer().getName();
        if (result == null || result.size() == 0) {
            Demo.plugin.getLogger().info(String.format("[Create Item Listener] Player: %s, Result: Null or Empty", playerName));
            return;
        }

        StringBuilder builder = new StringBuilder();
        builder.append(System.lineSeparator());
        for (Pair<CRecipe, MappedRelation> pair : result.customs()) {
            builder.append("  - Recipe Name: ");
            builder.append(pair.component1().getName().isEmpty() ? "(Empty Recipe Name)" : pair.component1().getName());
            builder.append(System.lineSeparator());
        }
        Demo.plugin.getLogger().info(String.format("[Create Item Listener] Player: %s, Result: %s", playerName, builder.toString()));
    }
}
