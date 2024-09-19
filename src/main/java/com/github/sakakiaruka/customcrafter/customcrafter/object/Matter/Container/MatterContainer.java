package com.github.sakakiaruka.customcrafter.customcrafter.object.Matter.Container;

import java.util.Map;
import java.util.Objects;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MatterContainer)) return false;
        MatterContainer that = (MatterContainer) o;
        return Objects.equals(predicate, that.predicate) && type == that.type && Objects.equals(predicateFormula, that.predicateFormula);
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = result * 31 + predicate.hashCode();
        result = result * 31 + type.hashCode();
        result = result * 31 + predicateFormula.hashCode();
        return result;
    }
}
