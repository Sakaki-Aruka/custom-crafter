package com.github.sakakiaruka.customcrafter.customcrafter.object;

import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataType;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class AnchorTagType implements PersistentDataType<UUID, UUID> {
    @org.jetbrains.annotations.NotNull
    @Override
    public Class<UUID> getPrimitiveType() {
        return UUID.class;
    }

    @org.jetbrains.annotations.NotNull
    @Override
    public Class<UUID> getComplexType() {
        return UUID.class;
    }

    @org.jetbrains.annotations.NotNull
    @Override
    public UUID toPrimitive(@org.jetbrains.annotations.NotNull UUID complex, @org.jetbrains.annotations.NotNull PersistentDataAdapterContext context) {
        return UUID.randomUUID();
    }

    @org.jetbrains.annotations.NotNull
    @Override
    public UUID fromPrimitive(@org.jetbrains.annotations.NotNull UUID primitive, @org.jetbrains.annotations.NotNull PersistentDataAdapterContext context) {
        return UUID.randomUUID();
    }
}
