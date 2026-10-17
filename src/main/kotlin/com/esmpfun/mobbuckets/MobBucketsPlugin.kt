package com.esmpfun.mobbuckets

import com.esmpfun.mobbuckets.claims.ClaimCheck
import com.esmpfun.mobbuckets.command.MobBucketsCommand
import com.esmpfun.mobbuckets.config.MobCatalog
import com.esmpfun.mobbuckets.config.Settings
import com.esmpfun.mobbuckets.integrations.MobBucketsPlaceholders
import com.esmpfun.mobbuckets.item.BucketItems
import com.esmpfun.mobbuckets.listener.BrewingListener
import com.esmpfun.mobbuckets.listener.CatchListener
import com.esmpfun.mobbuckets.listener.DebugListener
import com.esmpfun.mobbuckets.listener.GuardListener
import com.esmpfun.mobbuckets.listener.ReleaseListener
import com.esmpfun.mobbuckets.messages.Messages
import com.esmpfun.mobbuckets.recipe.LooseCraftListener
import com.esmpfun.mobbuckets.recipe.RecipeRegistrar
import com.esmpfun.mobbuckets.stats.CatchStats
import org.bukkit.Bukkit
import org.bukkit.configuration.InvalidConfigurationException
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerInteractEntityEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.java.JavaPlugin
import java.io.File
import java.io.IOException
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class MobBucketsPlugin : JavaPlugin() {

    lateinit var settings: Settings
        private set
    lateinit var catalog: MobCatalog
        private set
    lateinit var claims: ClaimCheck
        private set

    val messages = Messages(this)
    val items = BucketItems(this)
    val recipes = RecipeRegistrar(this)
    lateinit var stats: CatchStats
        private set

    override fun onEnable() {
        saveDefaultConfig()
        claims = ClaimCheck.create(this)
        reloadEverything()

        stats = CatchStats(this)
        listOf(CatchListener(this), ReleaseListener(this), GuardListener(this), BrewingListener(this), LooseCraftListener(this), DebugListener(this), stats)
            .forEach { server.pluginManager.registerEvents(it, this) }
        if (server.pluginManager.isPluginEnabled("PlaceholderAPI")) MobBucketsPlaceholders(this).register()
        getCommand("mobbuckets")?.let {
            val command = MobBucketsCommand(this)
            it.setExecutor(command)
            it.tabCompleter = command
        }
        logger.info("${catalog.entries.size} mobs can be caught.")
    }

    override fun onDisable() {
        recipes.unregisterAll()
    }

    fun reloadEverything() {
        reloadConfig()
        messages.load()
        settings = Settings.load(config, logger)
        catalog = MobCatalog.load(loadMobsFile(), logger)
        recipes.registerAll()
    }

    private fun loadMobsFile(): YamlConfiguration {
        val file = File(dataFolder, "mobs.yml")
        if (!file.exists()) saveResource("mobs.yml", false)
        val yaml = YamlConfiguration()
        try {
            yaml.load(file)
        } catch (e: IOException) {
            logger.severe("mobs.yml couldn't be read (${e.message}). No mobs can be caught until it is fixed.")
        } catch (e: InvalidConfigurationException) {
            logger.severe("mobs.yml has a mistake in it (${e.message}). No mobs can be caught until it is fixed.")
        }
        return yaml
    }

    private class Click(val event: PlayerInteractEntityEvent, val entityId: Int, val hand: EquipmentSlot, val tick: Int, val repeat: Boolean)

    private val lastClicks = ConcurrentHashMap<UUID, Click>()

    /** True for the second report of one right-click on a mob: older servers send two, 26.3 one. */
    fun isRepeatClick(event: PlayerInteractEntityEvent): Boolean {
        val last = lastClicks[event.player.uniqueId]
        if (last != null && last.event === event) return last.repeat
        val tick = Bukkit.getCurrentTick()
        val repeat = last != null && last.entityId == event.rightClicked.entityId && last.hand == event.hand && last.tick == tick
        lastClicks[event.player.uniqueId] = Click(event, event.rightClicked.entityId, event.hand, tick, repeat)
        return repeat
    }

    fun forget(player: Player) {
        lastClicks.remove(player.uniqueId)
    }

    fun debug(message: String) {
        if (settings.debug) logger.info("[debug] $message")
    }

    /** Takes one bucket from the player's hand and puts [replacement] in its place, or in their inventory. */
    fun useOne(player: Player, hand: EquipmentSlot, replacement: ItemStack?) {
        val inventory = player.inventory
        val stack = inventory.getItem(hand)
        if (stack.amount <= 1) {
            inventory.setItem(hand, replacement)
            return
        }
        stack.amount -= 1
        inventory.setItem(hand, stack)
        if (replacement != null) {
            inventory.addItem(replacement).values.forEach { player.world.dropItemNaturally(player.location, it) }
        }
    }
}
