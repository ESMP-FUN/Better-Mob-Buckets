package com.esmpfun.mobbuckets.listener

import com.esmpfun.mobbuckets.MobBucketsPlugin
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockDispenseEvent
import org.bukkit.event.block.CauldronLevelChangeEvent
import org.bukkit.event.inventory.PrepareItemCraftEvent
import org.bukkit.event.player.PlayerBucketEntityEvent
import org.bukkit.event.player.PlayerBucketFillEvent
import org.bukkit.event.player.PlayerJoinEvent

/** Mob buckets are real buckets underneath; this stops them acting like one. */
class GuardListener(private val plugin: MobBucketsPlugin) : Listener {

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    fun onFill(event: PlayerBucketFillEvent) {
        if (plugin.items.read(event.player.inventory.getItem(event.hand)) != null) event.isCancelled = true
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    fun onScoopFish(event: PlayerBucketEntityEvent) {
        if (plugin.items.read(event.originalBucket) != null) event.isCancelled = true
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    fun onCauldron(event: CauldronLevelChangeEvent) {
        if (event.reason != CauldronLevelChangeEvent.ChangeReason.BUCKET_FILL) return
        val player = event.entity as? Player ?: return
        val main = player.inventory.itemInMainHand
        val usedHand = if (main.type == Material.BUCKET) main else player.inventory.itemInOffHand
        if (plugin.items.read(usedHand) != null) event.isCancelled = true
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    fun onDispense(event: BlockDispenseEvent) {
        if (plugin.items.read(event.item) != null) event.isCancelled = true
    }

    /** Other plugins' recipes that take any bucket must not eat a mob bucket. */
    @EventHandler(priority = EventPriority.HIGH)
    fun onPrepareCraft(event: PrepareItemCraftEvent) {
        if (event.inventory.matrix.any { plugin.items.read(it) != null }) event.inventory.result = null
    }

    @EventHandler
    fun onJoin(event: PlayerJoinEvent) {
        plugin.recipes.showTo(event.player)
    }
}
