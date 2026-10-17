package com.esmpfun.mobbuckets.api

import org.bukkit.entity.Mob
import org.bukkit.entity.Player
import org.bukkit.event.HandlerList
import org.bukkit.event.player.PlayerEvent

/** [mob] was let out of a bucket and is now in the world. */
class MobReleasedEvent(
    player: Player,
    val mob: Mob,
    /** Mob bucket id from mobs.yml, such as "zombie". */
    val mobId: String,
) : PlayerEvent(player) {

    override fun getHandlers() = handlerList

    companion object {
        @JvmStatic
        val handlerList = HandlerList()
    }
}
