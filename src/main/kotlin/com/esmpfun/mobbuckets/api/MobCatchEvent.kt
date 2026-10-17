package com.esmpfun.mobbuckets.api

import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.Cancellable
import org.bukkit.event.HandlerList
import org.bukkit.event.player.PlayerEvent
import org.bukkit.inventory.ItemStack

/**
 * A player is about to try catching [mob], after every built-in check passed and before the chance is rolled.
 * Cancelling keeps the bucket and sends no message, so the canceller should tell the player why.
 */
class MobCatchEvent(
    player: Player,
    val mob: LivingEntity,
    /** A copy of the bucket being used. */
    val bucket: ItemStack,
    val strengthened: Boolean,
    /** Catch chance in percent, 0 to 100. */
    chance: Int,
) : PlayerEvent(player), Cancellable {

    var chance: Int = chance
        set(value) { field = value.coerceIn(0, 100) }

    private var cancelled = false
    override fun isCancelled() = cancelled
    override fun setCancelled(cancel: Boolean) { cancelled = cancel }

    override fun getHandlers() = handlerList

    companion object {
        @JvmStatic
        val handlerList = HandlerList()
    }
}
