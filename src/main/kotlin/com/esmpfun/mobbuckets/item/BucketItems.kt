package com.esmpfun.mobbuckets.item

import com.esmpfun.mobbuckets.MobBucketsPlugin
import com.esmpfun.mobbuckets.config.ItemNames
import com.esmpfun.mobbuckets.config.MobEntry
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.entity.Ageable
import org.bukkit.entity.Entity
import org.bukkit.entity.Tameable
import org.bukkit.entity.Villager
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

/**
 * What a mob bucket item holds. Empty buckets store their [MobEntry.id]; filled ones store the
 * caught mob's own type name (a llama bucket can hold a trader llama) and its saved data.
 */
class BucketData(val mobId: String, val strengthened: Boolean, val entityData: ByteArray?) {
    val isFilled: Boolean get() = entityData != null
}

class BucketItems(private val plugin: MobBucketsPlugin) {

    private val mobKey = NamespacedKey(plugin, "mob")
    private val strengthenedKey = NamespacedKey(plugin, "strengthened")
    private val entityKey = NamespacedKey(plugin, "entity")

    fun read(item: ItemStack?): BucketData? {
        if (item == null || item.type != Material.BUCKET || !item.hasItemMeta()) return null
        val pdc = item.itemMeta.persistentDataContainer
        val mob = pdc.get(mobKey, PersistentDataType.STRING) ?: return null
        return BucketData(
            mob,
            pdc.has(strengthenedKey, PersistentDataType.BYTE),
            pdc.get(entityKey, PersistentDataType.BYTE_ARRAY),
        )
    }

    /** A fresh, empty bucket of this type that hasn't been strengthened yet. */
    fun isFreshEmpty(item: ItemStack?, mobId: String): Boolean =
        read(item)?.let { !it.isFilled && !it.strengthened && it.mobId == mobId } == true

    fun empty(entry: MobEntry, strengthened: Boolean, amount: Int = 1): ItemStack {
        val settings = plugin.settings
        val messages = plugin.messages
        val mob = entry.displayName
        val chance = if (strengthened) settings.strengthenedChance else settings.chance
        val item = ItemStack(Material.BUCKET, amount)
        item.editMeta { meta ->
            meta.itemName(
                messages.itemText(if (strengthened) "item.empty-strengthened-name" else "item.empty-name", "mob" to mob)
            )
            val lore = messages.itemLines("item.empty-lore", "mob" to mob, "chance" to chance).toMutableList()
            val strengthenItem = settings.strengthenItem
            if (!strengthened && strengthenItem != null) {
                lore += messages.itemLines(
                    "item.empty-lore-strengthen-hint",
                    "item" to Component.translatable(strengthenItem.translationKey()),
                    "strengthened-chance" to settings.strengthenedChance,
                )
            }
            meta.lore(lore)
            if (strengthened) meta.setEnchantmentGlintOverride(true)
            meta.persistentDataContainer.set(mobKey, PersistentDataType.STRING, entry.id)
            if (strengthened) meta.persistentDataContainer.set(strengthenedKey, PersistentDataType.BYTE, 1)
        }
        return item
    }

    fun filled(entity: Entity, data: ByteArray): ItemStack {
        val messages = plugin.messages
        val mob = ItemNames.mobName(entity.type)
        val item = ItemStack(Material.BUCKET)
        item.editMeta { meta ->
            meta.itemName(messages.itemText("item.filled-name", "mob" to mob))
            val lore = mutableListOf<Component>()
            entity.customName()?.let { lore += messages.itemText("item.filled-lore-name", "name" to it) }
            if (entity is Ageable && !entity.isAdult) lore += messages.itemText("item.filled-lore-baby")
            if (entity is Tameable && entity.isTamed) {
                entity.owner?.name?.let { lore += messages.itemText("item.filled-lore-owner", "owner" to it) }
            }
            if (entity is Villager && entity.profession != Villager.Profession.NONE) {
                lore += messages.itemText(
                    "item.filled-lore-job",
                    "job" to Component.translatable("entity.minecraft.villager.${entity.profession.key.key}"),
                    "level" to entity.villagerLevel,
                )
            }
            lore += messages.itemLines("item.filled-lore")
            meta.lore(lore)
            meta.setMaxStackSize(1)
            meta.setEnchantmentGlintOverride(true)
            meta.persistentDataContainer.set(mobKey, PersistentDataType.STRING, entity.type.key.key)
            meta.persistentDataContainer.set(entityKey, PersistentDataType.BYTE_ARRAY, data)
        }
        return item
    }
}
