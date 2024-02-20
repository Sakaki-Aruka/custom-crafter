package com.github.sakakiaruka.customcrafter.customcrafter.object.Recipe.Container;

import com.github.sakakiaruka.customcrafter.customcrafter.interfaces.TriConsumer;
import com.github.sakakiaruka.customcrafter.customcrafter.util.ContainerUtil;
import org.bukkit.inventory.ItemStack;

import java.util.Map;
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
}
