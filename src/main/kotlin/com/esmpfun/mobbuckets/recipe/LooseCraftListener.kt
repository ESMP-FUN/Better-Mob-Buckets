package com.esmpfun.mobbuckets.recipe

import com.esmpfun.mobbuckets.MobBucketsPlugin
import com.esmpfun.mobbuckets.config.MobEntry
import com.esmpfun.mobbuckets.config.Tier
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryType
import org.bukkit.event.inventory.PrepareItemCraftEvent
import org.bukkit.inventory.CraftingInventory
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.RecipeChoice

/**
 * Lets mob buckets be crafted from stacked ingredients in any layout, e.g. a full stack of slime balls
 * in one slot. Vanilla shapeless recipes only take one item per slot, so this fills in when they don't match.
 */
class LooseCraftListener(private val plugin: MobBucketsPlugin) : Listener {

    private class Match(val entry: MobEntry, val take: IntArray)

    @EventHandler(priority = EventPriority.NORMAL)
    fun onPrepare(event: PrepareItemCraftEvent) {
        if (event.recipe != null) return
        val match = match(event.inventory.matrix) ?: return
        event.inventory.result = plugin.items.empty(match.entry, strengthened = false)
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun onTakeResult(event: InventoryClickEvent) {
        val inventory = event.inventory as? CraftingInventory ?: return
        if (event.slotType != InventoryType.SlotType.RESULT || inventory.recipe != null) return
        val player = event.whoClicked as? Player ?: return
        if (match(inventory.matrix) == null) return
        event.isCancelled = true

        when (event.click) {
            ClickType.LEFT, ClickType.RIGHT -> {
                val match = match(inventory.matrix) ?: return
                val result = plugin.items.empty(match.entry, strengthened = false)
                val cursor = event.view.cursor
                when {
                    cursor.isEmpty -> event.view.setCursor(result)
                    cursor.isSimilar(result) && cursor.amount < cursor.maxStackSize -> cursor.amount += 1
                    else -> return
                }
                consume(inventory, match)
            }
            ClickType.SHIFT_LEFT, ClickType.SHIFT_RIGHT -> {
                for (i in 0 until MAX_SHIFT_CRAFTS) {
                    val match = match(inventory.matrix) ?: break
                    val result = plugin.items.empty(match.entry, strengthened = false)
                    if (!hasRoom(player, result)) break
                    player.inventory.addItem(result)
                    consume(inventory, match)
                }
            }
            ClickType.NUMBER_KEY -> {
                val match = match(inventory.matrix) ?: return
                val target = player.inventory.getItem(event.hotbarButton)
                if (target != null && !target.isEmpty) return
                player.inventory.setItem(event.hotbarButton, plugin.items.empty(match.entry, strengthened = false))
                consume(inventory, match)
            }
            ClickType.DROP, ClickType.CONTROL_DROP -> {
                if (!event.view.cursor.isEmpty) return
                val match = match(inventory.matrix) ?: return
                player.world.dropItem(player.eyeLocation, plugin.items.empty(match.entry, strengthened = false))
                consume(inventory, match)
            }
            else -> return
        }
        plugin.server.scheduler.runTask(plugin, Runnable { player.updateInventory() })
    }

    private fun consume(inventory: CraftingInventory, match: Match) {
        val matrix = inventory.matrix
        for (i in matrix.indices) {
            val item = matrix[i] ?: continue
            val left = item.amount - match.take[i]
            matrix[i] = if (left <= 0) null else item.clone().also { it.amount = left }
        }
        inventory.matrix = matrix
        inventory.result = match(matrix)?.let { plugin.items.empty(it.entry, strengthened = false) }
    }

    private fun hasRoom(player: Player, item: ItemStack): Boolean =
        player.inventory.storageContents.any {
            it == null || it.isEmpty || (it.isSimilar(item) && it.amount < it.maxStackSize)
        }

    private fun match(matrix: Array<ItemStack?>): Match? {
        if (matrix.none { it != null && it.isSimilar(PLAIN_BUCKET) }) return null
        val settings = plugin.settings
        for (entry in plugin.catalog.entries.values) {
            val base = when (entry.tier) {
                Tier.NORMAL -> settings.normalRecipe
                Tier.DANGEROUS -> settings.dangerousRecipe
            } ?: continue
            // Single items before item groups, so a group doesn't use up items a single-item slot needs.
            val needs = buildList {
                add(Triple(BUCKET_CHOICE, 1, false))
                base.extras.forEach { (item, amount) -> add(Triple(item.choice, amount, item.isGroup)) }
                add(Triple(entry.mobItem.choice, base.mobItemAmount, entry.mobItem.isGroup))
            }.sortedBy { it.third }
            val take = allocate(matrix.toList(), { if (it.isEmpty) 0 else it.amount }, needs.map { (choice, amount, _) -> choice::test to amount })
                ?: continue
            return Match(entry, take)
        }
        return null
    }

    private companion object {
        val PLAIN_BUCKET = ItemStack(Material.BUCKET)
        val BUCKET_CHOICE = RecipeChoice.ExactChoice(PLAIN_BUCKET)
        const val MAX_SHIFT_CRAFTS = 64
    }
}

/**
 * How many to take from each slot to cover [needs], in order, or null when the grid is short of something
 * or holds an item none of the needs accept.
 */
internal fun <T : Any> allocate(slots: List<T?>, amountOf: (T) -> Int, needs: List<Pair<(T) -> Boolean, Int>>): IntArray? {
    val take = IntArray(slots.size)
    for ((accepts, amount) in needs) {
        var missing = amount
        for (i in slots.indices) {
            if (missing == 0) break
            val item = slots[i] ?: continue
            if (amountOf(item) <= 0 || !accepts(item)) continue
            val used = minOf(missing, amountOf(item) - take[i])
            take[i] += used
            missing -= used
        }
        if (missing > 0) return null
    }
    val stray = slots.any { item -> item != null && amountOf(item) > 0 && needs.none { it.first(item) } }
    return if (stray) null else take
}
