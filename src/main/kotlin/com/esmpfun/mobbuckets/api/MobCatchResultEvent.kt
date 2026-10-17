package com.esmpfun.mobbuckets.api

import org.bukkit.Location
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.event.HandlerList
import org.bukkit.event.player.PlayerEvent
import org.bukkit.inventory.ItemStack

/** A catch attempt finished. On a catch the mob is already gone from the world and [filledBucket] is set. */
class MobCatchResultEvent(
    player: Player,
    val mobType: EntityType,
    /** Mob bucket id from mobs.yml, such as "zombie". */
    val mobId: String,
    val location: Location,
    val caught: Boolean,
    val filledBucket: ItemStack?,
) : PlayerEvent(player) {

    override fun getHandlers() = handlerList

    companion object {
        @JvmStatic
        val handlerList = HandlerList()
    }
}
