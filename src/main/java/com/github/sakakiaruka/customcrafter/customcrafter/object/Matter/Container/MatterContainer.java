package com.github.sakakiaruka.customcrafter.customcrafter.object.Matter.Container;

import java.util.Map;
import java.util.function.BiFunction;

public class MatterContainer {
    private final BiFunction<Map<String, String>, String, Boolean> predicate;
    private final ContainerType type;
    private final String predicateFormula;


    public MatterContainer(BiFunction<Map<String, String>, String, Boolean> predicate, ContainerType type, String predicateFormula) {
        this.predicate = predicate;
        this.type = type;
        this.predicateFormula = predicateFormula;
    }

    public BiFunction<Map<String, String>, String, Boolean> getPredicate() {
        return predicate;
    }


    public ContainerType getType() {
        return type;
    }

    public String getPredicateFormula() {
        return predicateFormula;
    }

    public boolean judge(Map<String, String> data) {
        // shortcut of "A.getPredicate().apply(data, A.getPredicateFormula());"
        return predicate.apply(data, predicateFormula);
    }

}
