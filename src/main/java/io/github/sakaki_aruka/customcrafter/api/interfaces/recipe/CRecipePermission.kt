package io.github.sakaki_aruka.customcrafter.api.interfaces.recipe

import org.bukkit.configuration.serialization.ConfigurationSerializable
import org.bukkit.configuration.serialization.ConfigurationSerialization

interface CRecipePermission: ConfigurationSerializable {
    /**
     * Represents a recipe-permission interface.
     * You must register this class with `[ConfigurationSerialization.registerClass]`.
     * Register example in Kotlin:
     * `ConfigurationSerialization.registerClass(this.javaClass)`
     *
     */

    val parent: String?
    val name: String

    fun hasParent(): Boolean = parent != null
    override fun serialize(): MutableMap<String, Any>
    fun deserialize(): MutableMap<String, Any>
    fun register()
    fun same(permission: CRecipePermission): Boolean {
        return (this.name == permission.name && this.parent == permission.parent )
    }
}