package com.esmpfun.mobbuckets.stats

import com.esmpfun.mobbuckets.MobBucketsPlugin
import com.esmpfun.mobbuckets.api.MobCatchResultEvent
import com.esmpfun.mobbuckets.api.MobReleasedEvent
import com.esmpfun.mobbuckets.integrations.ApmBridge
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.persistence.PersistentDataType

/** Counts each player's catches, misses and releases on the player, and passes them on to APM when it is installed. */
class CatchStats(private val plugin: MobBucketsPlugin) : Listener {

    enum class Count { CAUGHT, MISSED, RELEASED }

    private val keys = Count.entries.associateWith { NamespacedKey(plugin, "stat_${it.name.lowercase()}") }
    private val apm = ApmBridge.createIfPresent()

    fun get(player: Player, count: Count): Int =
        player.persistentDataContainer.getOrDefault(keys.getValue(count), PersistentDataType.INTEGER, 0)

    /** Caught out of all tries, in whole percent; 0 before the first try. */
    fun catchRate(player: Player): Int {
        val caught = get(player, Count.CAUGHT)
        val tries = caught + get(player, Count.MISSED)
        return if (tries == 0) 0 else Math.round(caught * 100.0 / tries).toInt()
    }

    @EventHandler(priority = EventPriority.MONITOR)
    fun onCatch(event: MobCatchResultEvent) {
        add(event.player, if (event.caught) Count.CAUGHT else Count.MISSED)
        if (event.caught) apm?.caught(event.player.uniqueId, event.mobId) else apm?.missed(event.player.uniqueId)
    }

    @EventHandler(priority = EventPriority.MONITOR)
    fun onRelease(event: MobReleasedEvent) {
        add(event.player, Count.RELEASED)
        apm?.released(event.player.uniqueId)
    }

    private fun add(player: Player, count: Count) {
        player.persistentDataContainer.set(keys.getValue(count), PersistentDataType.INTEGER, get(player, count) + 1)
    }
}
