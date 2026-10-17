package com.esmpfun.mobbuckets.integrations

import com.esmpfun.mobbuckets.MobBucketsPlugin
import com.esmpfun.mobbuckets.stats.CatchStats.Count
import me.clip.placeholderapi.expansion.PlaceholderExpansion
import org.bukkit.OfflinePlayer

/**
 * `%mobbuckets_caught%`, `_missed%`, `_released%`, `_catch_rate%` for the player (online only, else 0),
 * and `_chance%`, `_strengthened_chance%` for the server's settings.
 */
class MobBucketsPlaceholders(private val plugin: MobBucketsPlugin) : PlaceholderExpansion() {

    override fun getIdentifier() = "mobbuckets"
    override fun getAuthor() = "darkstarworks"
    override fun getVersion(): String = plugin.pluginMeta.version
    override fun persist() = true

    override fun onRequest(player: OfflinePlayer?, params: String): String? {
        when (params.lowercase()) {
            "chance" -> return plugin.settings.chance.toString()
            "strengthened_chance" -> return plugin.settings.strengthenedChance.toString()
        }
        val online = player?.player
        val stats = plugin.stats
        return when (params.lowercase()) {
            "caught" -> (online?.let { stats.get(it, Count.CAUGHT) } ?: 0).toString()
            "missed" -> (online?.let { stats.get(it, Count.MISSED) } ?: 0).toString()
            "released" -> (online?.let { stats.get(it, Count.RELEASED) } ?: 0).toString()
            "catch_rate" -> (online?.let { stats.catchRate(it) } ?: 0).toString()
            else -> null
        }
    }
}
