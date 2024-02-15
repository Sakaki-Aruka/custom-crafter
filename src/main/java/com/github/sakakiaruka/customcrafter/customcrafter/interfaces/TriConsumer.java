package com.github.sakakiaruka.customcrafter.customcrafter.interfaces;

@FunctionalInterface
public interface TriConsumer<T, U, V> {
    public void accept(T t, U u, V v);
}
