package com.esmpfun.mobbuckets.config

import net.kyori.adventure.text.Component
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.EntityType
import org.bukkit.entity.Mob
import java.util.logging.Logger

enum class Tier { NORMAL, DANGEROUS }

/** One kind of mob bucket. [id] is the first mob's name and is what bucket items store. */
class MobEntry(val id: String, val types: List<EntityType>, val tier: Tier, val mobItem: ItemChoice) {
    val displayName: Component get() = ItemNames.mobName(types.first())
}

class MobCatalog private constructor(
    val entries: Map<String, MobEntry>,
    private val byType: Map<EntityType, MobEntry>,
    private val blocked: Set<EntityType>,
) {
    fun entry(id: String): MobEntry? = entries[id]

    fun entryFor(type: EntityType): MobEntry? = byType[type]

    fun isBlocked(type: EntityType): Boolean = type in blocked

    companion object {
        fun load(yaml: YamlConfiguration, log: Logger): MobCatalog {
            val notInThisVersion = mutableListOf<String>()
            val blocked = yaml.getStringList("cannot-be-caught").mapNotNull { ItemNames.entityType(it) }.toSet()

            val entries = linkedMapOf<String, MobEntry>()
            val byType = linkedMapOf<EntityType, MobEntry>()
            val usedItems = mutableMapOf<String, String>()

            for ((path, tier) in listOf("normal-mobs" to Tier.NORMAL, "dangerous-mobs" to Tier.DANGEROUS)) {
                val section = yaml.getConfigurationSection(path) ?: continue
                for (key in section.getKeys(false)) {
                    val itemId = section.getString(key)?.trim()?.lowercase() ?: continue
                    val types = mutableListOf<EntityType>()
                    for (name in key.split(',').map { it.trim().lowercase() }.filter { it.isNotEmpty() }) {
                        val type = ItemNames.entityType(name)
                        when {
                            type == null -> notInThisVersion += name
                            type.entityClass?.let { Mob::class.java.isAssignableFrom(it) } != true ->
                                log.warning("mobs.yml: \"$name\" isn't a mob, so it can't be caught. Skipped.")
                            type in blocked -> {}
                            type in byType -> log.warning("mobs.yml: \"$name\" is listed twice. Only the first line is used.")
                            else -> types += type
                        }
                    }
                    if (types.isEmpty()) continue

                    val item = ItemNames.choice(itemId)
                    if (item == null) {
                        log.warning("mobs.yml: \"$itemId\" (for $key) isn't an item in this Minecraft version. Skipped.")
                        continue
                    }
                    usedItems.put(itemId, key)?.let {
                        log.warning("mobs.yml: \"$key\" and \"$it\" both use $itemId, so only one of them can be crafted. Give each mob its own item.")
                    }

                    val entry = MobEntry(types.first().key.key, types, tier, item)
                    entries[entry.id] = entry
                    types.forEach { byType[it] = entry }
                }
            }

            if (notInThisVersion.isNotEmpty()) {
                log.info("Skipped mobs that aren't in this Minecraft version: ${notInThisVersion.joinToString(", ")}")
            }
            return MobCatalog(entries, byType, blocked)
        }
    }
}
