package com.esmpfun.mobbuckets.listener

import com.esmpfun.mobbuckets.MobBucketsPlugin
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.BrewerInventory
import org.bukkit.inventory.ItemStack

/**
 * Minecraft only lets potions and bottles into a brewing stand's three bottle spots, so a mob
 * bucket is placed there by hand. The brewing itself is a registered potion mix.
 */
class BrewingListener(private val plugin: MobBucketsPlugin) : Listener {

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun onClick(event: InventoryClickEvent) {
        val stand = event.view.topInventory as? BrewerInventory ?: return
        if (plugin.settings.strengthenItem == null) return

        if (event.clickedInventory === stand && event.slot in BOTTLE_SLOTS &&
            (event.click == ClickType.LEFT || event.click == ClickType.RIGHT)
        ) {
            val cursor = event.cursor
            if (!canStrengthen(cursor) || !isEmpty(stand.getItem(event.slot))) return
            event.isCancelled = true
            stand.setItem(event.slot, cursor.asOne())
            event.view.setCursor(cursor.subtract())
            return
        }

        if (event.clickedInventory === event.view.bottomInventory && event.isShiftClick) {
            val item = event.currentItem ?: return
            if (!canStrengthen(item)) return
            val free = BOTTLE_SLOTS.firstOrNull { isEmpty(stand.getItem(it)) } ?: return
            event.isCancelled = true
            stand.setItem(free, item.asOne())
            event.currentItem = item.clone().subtract().takeIf { it.amount > 0 }
        }
    }

    private fun canStrengthen(item: ItemStack?): Boolean {
        val data = plugin.items.read(item) ?: return false
        return !data.isFilled && !data.strengthened && plugin.catalog.entry(data.mobId) != null
    }

    private fun isEmpty(item: ItemStack?) = item == null || item.type.isAir

    private companion object {
        val BOTTLE_SLOTS = 0..2
    }
}
