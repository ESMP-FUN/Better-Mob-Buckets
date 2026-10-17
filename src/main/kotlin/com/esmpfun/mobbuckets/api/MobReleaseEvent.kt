package com.esmpfun.mobbuckets.api

import org.bukkit.Location
import org.bukkit.entity.Mob
import org.bukkit.entity.Player
import org.bukkit.event.Cancellable
import org.bukkit.event.HandlerList
import org.bukkit.event.player.PlayerEvent

/**
 * A player is about to let [mob] out at [location]. The mob isn't in the world yet.
 * Cancelling keeps it in the bucket and sends no message, so the canceller should tell the player why.
 */
class MobReleaseEvent(
    player: Player,
    val mob: Mob,
    val location: Location,
) : PlayerEvent(player), Cancellable {

    private var cancelled = false
    override fun isCancelled() = cancelled
    override fun setCancelled(cancel: Boolean) { cancelled = cancel }

    override fun getHandlers() = handlerList

    companion object {
        @JvmStatic
        val handlerList = HandlerList()
    }
}
