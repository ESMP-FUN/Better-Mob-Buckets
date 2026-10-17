package com.esmpfun.mobbuckets.listener

import com.esmpfun.mobbuckets.MobBucketsPlugin
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractAtEntityEvent
import org.bukkit.event.player.PlayerInteractEntityEvent

/** With debug on, shows in the console how a right-click on a mob with a mob bucket travels through every plugin. */
class DebugListener(private val plugin: MobBucketsPlugin) : Listener {

    @EventHandler(priority = EventPriority.LOWEST)
    fun lowest(event: PlayerInteractEntityEvent) = probe(event, EventPriority.LOWEST)

    @EventHandler(priority = EventPriority.LOW)
    fun low(event: PlayerInteractEntityEvent) = probe(event, EventPriority.LOW)

    @EventHandler(priority = EventPriority.NORMAL)
    fun normal(event: PlayerInteractEntityEvent) = probe(event, EventPriority.NORMAL)

    @EventHandler(priority = EventPriority.MONITOR)
    fun monitor(event: PlayerInteractEntityEvent) = probe(event, EventPriority.MONITOR)

    private fun probe(event: PlayerInteractEntityEvent, stage: EventPriority) {
        if (!plugin.settings.debug || event is PlayerInteractAtEntityEvent) return
        val held = event.player.inventory.getItem(event.hand)
        if (plugin.items.read(held) == null) {
            if (stage == EventPriority.LOWEST && held.type == Material.BUCKET) {
                plugin.debug("${event.player.name} right-clicked a ${event.rightClicked.type.key.key} with a bucket that isn't a mob bucket.")
            }
            return
        }
        if (stage == EventPriority.LOWEST) {
            plugin.debug("${event.player.name} right-clicked a ${event.rightClicked.type.key.key} with a mob bucket (${event.hand}).")
            val others = event.handlers.registeredListeners
                .filter { it.plugin != plugin && it.priority.slot <= EventPriority.HIGH.slot }
                .joinToString { "${it.plugin.name} (${it.priority})" }
            plugin.debug("Plugins that see this click before Mob Buckets: ${others.ifEmpty { "none" }}")
        }
        plugin.debug("  after the $stage plugins: ${if (event.isCancelled) "BLOCKED" else "allowed"}")
    }
}
