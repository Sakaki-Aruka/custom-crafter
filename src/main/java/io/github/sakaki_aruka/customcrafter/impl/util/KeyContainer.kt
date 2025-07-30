package io.github.sakaki_aruka.customcrafter.impl.util

import org.bukkit.persistence.PersistentDataType

data class KeyContainer<T, U>(
    val key: String,
    val type: PersistentDataType<T, U>
)
