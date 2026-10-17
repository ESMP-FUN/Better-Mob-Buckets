package com.esmpfun.mobbuckets.integrations

import io.github.darkstarworks.apm.api.MetricDefinition
import io.github.darkstarworks.apm.api.PlayerMetrics
import org.bukkit.Bukkit
import java.util.UUID

/**
 * Sends catch numbers to Advanced Player Metrics, so they show in /stats and on its website leaderboards.
 * No APM class is touched unless APM is installed.
 */
class ApmBridge private constructor() {

    private fun metrics(): PlayerMetrics? {
        if (Bukkit.getPluginManager().getPlugin("APM")?.isEnabled != true) return null
        return Bukkit.getServicesManager().load(PlayerMetrics::class.java)
    }

    fun caught(player: UUID, mobId: String) {
        val m = metrics() ?: return
        // Registered on every use (a map write), so the numbers survive an APM restart.
        m.register(MetricDefinition(CAUGHT, CATEGORY, "Mobs caught in mob buckets", board = true, icon = "BUCKET"))
        m.increment(player, CAUGHT)
        val child = "$CAUGHT.$mobId"
        m.register(MetricDefinition(child, CATEGORY, readable(mobId), parent = CAUGHT))
        m.increment(player, child)
    }

    fun missed(player: UUID) {
        val m = metrics() ?: return
        m.register(MetricDefinition(MISSED, CATEGORY, "Mob buckets broken by a missed catch", icon = "BARRIER"))
        m.increment(player, MISSED)
    }

    fun released(player: UUID) {
        val m = metrics() ?: return
        m.register(MetricDefinition(RELEASED, CATEGORY, "Mobs let out of mob buckets", icon = "AXOLOTL_BUCKET"))
        m.increment(player, RELEASED)
    }

    private fun readable(mobId: String) =
        mobId.split('_').joinToString(" ") { word -> word.replaceFirstChar { it.uppercase() } }

    companion object {
        private const val CATEGORY = "misc"
        private const val CAUGHT = "misc.mob_buckets_caught"
        private const val MISSED = "misc.mob_buckets_missed"
        private const val RELEASED = "misc.mob_buckets_released"

        fun createIfPresent(): ApmBridge? =
            if (Bukkit.getPluginManager().getPlugin("APM") != null) ApmBridge() else null
    }
}
