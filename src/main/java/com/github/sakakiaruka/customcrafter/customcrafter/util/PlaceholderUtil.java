package com.github.sakakiaruka.customcrafter.customcrafter.util;

import com.github.sakakiaruka.customcrafter.customcrafter.CustomCrafter;
import com.github.sakakiaruka.customcrafter.customcrafter.SettingsLoad;
import com.github.sakakiaruka.customcrafter.customcrafter.object.Matter.Matter;
import com.github.sakakiaruka.customcrafter.customcrafter.object.Recipe.Coordinate;
import com.github.sakakiaruka.customcrafter.customcrafter.object.Recipe.Recipe;
import com.github.sakakiaruka.customcrafter.customcrafter.object.Recipe.Tag;
import com.github.sakakiaruka.customcrafter.customcrafter.object.history.CraftHistoryQueryResult;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PlaceholderUtil extends PlaceholderExpansion {

    private static final Map<String, String> SERIALIZED_CUSTOM_RECIPES = new HashMap<>();
    private static final Map<String, String> OVERALL_INFO = new HashMap<>();
    private static final Map<String, String> ALL = new HashMap<>();

    @Override
    public @NotNull String getIdentifier() {
        return "custom-crafter";
    }

    @Override
    public @NotNull String getAuthor() {
        return "Sakaki-Aruka";
    }

    @Override
    @SuppressWarnings("UnstableApiUsage")
    public @NotNull String getVersion() {
        return CustomCrafter.getInstance().getPluginMeta().getVersion();
    }

    @Override
    public @Nullable String onRequest(OfflinePlayer player, @NotNull String params) {
        if (player != null) {
            if (params.equals("history:player-craft-history-unique")) {
                List<CraftHistoryQueryResult> histories = HistoryUtil.INSTANCE.getPlayerCreatedHistoryUnique(player.getUniqueId());
                if (!histories.isEmpty()) {
                    List<Map<String, Object>> list = new ArrayList<>();
                    histories.forEach(e -> list.add(CraftHistoryQueryResult.Companion.toObjectMap(e, true)));
                    return new Gson().toJson(list);
                }
            } else if (params.equals("history:player-craft-times")) {
                return new Gson().toJson(Map.of("times", HistoryUtil.INSTANCE.getPlayerCreatedHistoryUnique(player.getUniqueId())));
            } else if (params.equals("history:player-craft-history-all")) {
                List<CraftHistoryQueryResult> histories = HistoryUtil.INSTANCE.getPlayerCraftedHistoryAll(player.getUniqueId());
                if (!histories.isEmpty()) {
                    List<Map<String, Object>> list = new ArrayList<>();
                    histories.forEach(e -> list.add(CraftHistoryQueryResult.Companion.toObjectMap(e, true)));
                    return new Gson().toJson(list);
                }
            }
        }
        return ALL.get(params);
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public boolean canRegister() {
        return true;
    }

    public static void applyAll() {
        applyOverallCustomCrafterInfo();
        applySerializedCustomRecipes();

        ALL.putAll(SERIALIZED_CUSTOM_RECIPES);
        ALL.putAll(OVERALL_INFO);
    }

    private static String getJsonFormattedRecipeNameList() {
        return new Gson().toJson(SettingsLoad.NAMED_RECIPES_MAP.keySet());
    }

    private static void applyOverallCustomCrafterInfo() {
        OVERALL_INFO.put("info:recipes", String.format("{\"recipes\": %d}", SettingsLoad.NAMED_RECIPES_MAP.size()));
        OVERALL_INFO.put("info:base-block", String.format("{\"base-block\": \"%s\"}", SettingsLoad.BASE_BLOCK.name()));
        OVERALL_INFO.put("info:recipe-name-list", getJsonFormattedRecipeNameList());
    }

    private static void applySerializedCustomRecipes() {
        /*
         * contained fields
         *
         * name: String
         * recipe-type: String ("normal" or "amorphous")
         * side-length: Int (if their type is "amorphous", -1 otherwise sqrt(items-amount))
         * items-amount-total: Int
         * coordinates: List<Map<String, Object(String ,Int, List<String>)>> ("coordinate" : ('key' : 'value'))
         *   - name: String (Matter's name)
         *   - x, y, amount: Int
         *   - is-mass: Boolean
         *   - candidate: List<String(from Material.name())>
         * result: Map<String, Object(String or Int)> ("key" : "value")
         *   - type: String ("direct", "regex", "pass-through")
         *   - material: String
         * return-items?: Map<String, Map<String, Object(String or Int>> ("material-type" : ('key' : 'value'))
         *   - amount: Int
         *   - type: String
         *
         */

        for (Map.Entry<String, Recipe> entry : SettingsLoad.NAMED_RECIPES_MAP.entrySet()) {
            String name = entry.getKey();
            Recipe recipe = entry.getValue();

            JsonObject obj = new JsonObject();
            obj.addProperty("name", recipe.getName());
            obj.addProperty("recipe-type", recipe.getTag().name());
            obj.addProperty("side-length", recipe.getTag().equals(Tag.AMORPHOUS) ? -1 : (int) Math.sqrt(recipe.getCoordinate().size()));
            obj.addProperty("items-amount-total", recipe.getCoordinate().values().stream().mapToInt(Matter::getAmount).sum());

            List<Map<String, Object>> coordinates = new ArrayList<>();
            for (Map.Entry<Coordinate, Matter> required : recipe.getCoordinate().entrySet()) {
                Coordinate c = required.getKey();
                Matter m = required.getValue();
                Map<String, Object> values = new HashMap<>();
                values.put("name", m.getName());
                values.put("x", c.getX());
                values.put("y", c.getY());
                values.put("amount", m.getAmount());
                values.put("is-mass", m.isMass());
                values.put("candidate", m.getCandidate().stream().map(Material::name).collect(Collectors.toList()));
                coordinates.add(values);
            }
            obj.add("coordinates", new Gson().toJsonTree(coordinates));

//            String resultType;
//            if (!recipe.getResult().getNameOrRegex().contains("@") && recipe.getResult().getMatchPoint() == -1) resultType = "direct";
//            else if (recipe.getResult().getNameOrRegex().matches("^(?i)pass -> ([a-zA-Z_]+)$")) resultType = "pass-through";
//            else resultType = "regex";
//
//            Map<String, Object> result = new HashMap<>();
//            result.put("type", resultType);
//            result.put("material", recipe.getResult().getNameOrRegex());
//            result.put("name", recipe.getResult().getName());
//            obj.add("result", new Gson().toJsonTree(result));

            if (!recipe.getReturnItems().isEmpty()) {
                Map<String, Map<String, Object>> returns = new HashMap<>();
                for (Map.Entry<Material, ItemStack> returnEntry : recipe.getReturnItems().entrySet()) {
                    Map<String, Object> returnedItems = Map.of(
                            "amount", returnEntry.getValue().getAmount(),
                            "type", returnEntry.getValue().getType().name()
                    );
                    returns.put(returnEntry.getKey().name(), returnedItems);
                }
                obj.add("return-items", new Gson().toJsonTree(returns));
            }



            SERIALIZED_CUSTOM_RECIPES.put("recipe:" + name, new Gson().toJson(obj));
        }

    }
}
