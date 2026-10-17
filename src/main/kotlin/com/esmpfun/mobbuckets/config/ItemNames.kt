package com.esmpfun.mobbuckets.config

import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.Registry
import org.bukkit.Tag
import org.bukkit.entity.EntityType
import org.bukkit.inventory.RecipeChoice

/** A recipe ingredient read from the config. [isGroup] means any item of [example]'s kind. */
class ItemChoice(val choice: RecipeChoice, val example: Material, val isGroup: Boolean)

/** Turns the item and mob names server owners type into game objects. */
object ItemNames {

    // Renamed between Minecraft versions; either spelling works in the config.
    private val aliases = mapOf("iron_chain" to "chain", "chain" to "iron_chain")

    fun material(id: String): Material? {
        val clean = clean(id)
        return find(clean) ?: aliases[clean]?.let { find(it) }
    }

    /** `rotten_flesh`, or `#wool_carpets` for any item in that group. */
    fun choice(id: String): ItemChoice? {
        val clean = clean(id)
        if (clean.startsWith("#")) {
            val key = NamespacedKey.fromString(clean.removePrefix("#")) ?: return null
            val tag = Bukkit.getTag(Tag.REGISTRY_ITEMS, key, Material::class.java) ?: return null
            val first = tag.values.minByOrNull { it.name } ?: return null
            return ItemChoice(RecipeChoice.MaterialChoice(tag), first, true)
        }
        val material = material(clean) ?: return null
        return ItemChoice(RecipeChoice.MaterialChoice(material), material, false)
    }

    fun entityType(id: String): EntityType? {
        val key = NamespacedKey.fromString(clean(id)) ?: return null
        return Registry.ENTITY_TYPE.get(key)?.takeIf { it != EntityType.UNKNOWN }
    }

    fun mobName(type: EntityType): Component = Component.translatable(type.translationKey())

    fun mobName(id: String): Component = entityType(id)?.let { mobName(it) } ?: Component.text(id)

    private fun find(id: String): Material? = Material.matchMaterial(id)?.takeIf { it.isItem && !it.isAir }

    private fun clean(id: String) = id.trim().lowercase().removePrefix("minecraft:")
}
