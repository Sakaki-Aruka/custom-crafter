package com.github.sakakiaruka.customcrafter.customcrafter.object.Recipe.Container;

import com.github.sakakiaruka.customcrafter.customcrafter.interfaces.TriConsumer;
import com.github.sakakiaruka.customcrafter.customcrafter.util.ContainerUtil;
import org.bukkit.inventory.ItemStack;

import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;

public class RecipeContainer {
    private final BiFunction<Map<String, String>, String, Boolean> predicate;
    //private final BiConsumer<Map<String, String>, ItemStack> action;
    private final TriConsumer<Map<String, String>, ItemStack, String> action;
    private final String predicateFormula;
    private final String actionFormula;
    public RecipeContainer(BiFunction<Map<String, String>, String, Boolean> predicate, TriConsumer<Map<String, String>, ItemStack, String> action, String predicateFormula, String actionFormula) {
        this.predicate = predicate;
        this.action = action;
        this.predicateFormula = predicateFormula;
        this.actionFormula = actionFormula;
    }

    public RecipeContainer(TriConsumer<Map<String, String>, ItemStack, String> action, String actionFormula) {
        this.predicate = ContainerUtil.NONE; // always returns true
        this.action = action;
        this.predicateFormula = "";
        this.actionFormula = actionFormula;
    }

    public BiFunction<Map<String, String>, String, Boolean> getPredicate() {
        return predicate;
    }

    public TriConsumer<Map<String, String>, ItemStack, String> getAction() {
        return action;
    }

    public String getPredicateFormula() {
        return predicateFormula;
    }

    public String getActionFormula() {
        return actionFormula;
    }

    public boolean hasPredicate() {
        return !(predicate.equals(ContainerUtil.NONE) && predicateFormula.isEmpty());
    }

    public void run(Map<String, String> data, ItemStack item) {
        if (!predicate.apply(data, predicateFormula)) return;
        action.accept(data, item, actionFormula);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RecipeContainer)) return false;
        RecipeContainer that = (RecipeContainer) o;
        return Objects.equals(predicate, that.predicate) && Objects.equals(action, that.action) && Objects.equals(predicateFormula, that.predicateFormula) && Objects.equals(actionFormula, that.actionFormula);
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = result * 31 + predicate.hashCode();
        result = result * 31 + action.hashCode();
        result = result * 31 + predicateFormula.hashCode();
        result = result * 31 + actionFormula.hashCode();
        return result;
    }
}
