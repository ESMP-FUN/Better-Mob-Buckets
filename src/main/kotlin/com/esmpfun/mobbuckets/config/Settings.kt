package com.esmpfun.mobbuckets.config

import org.bukkit.Material
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.configuration.file.FileConfiguration
import java.util.logging.Logger

/** The extra items and mob-item amount for one recipe tier. */
class RecipeBase(val extras: List<Pair<ItemChoice, Int>>, val mobItemAmount: Int)

class Settings(
    val chance: Int,
    val strengthenedChance: Int,
    val onlyOwnerCatchesPets: Boolean,
    val protectOtherPluginsMobs: Boolean,
    val spawnerMobs: Boolean,
    val releasedNeverDespawn: Boolean,
    val strengthenItem: Material?,
    val respectClaims: Boolean,
    val showInRecipeBook: Boolean,
    val normalRecipe: RecipeBase?,
    val dangerousRecipe: RecipeBase?,
    val debug: Boolean,
) {
    companion object {
        private const val GRID_SIZE = 9

        fun load(config: FileConfiguration, log: Logger): Settings {
            val strengthenItem = if (config.getBoolean("strengthening.enabled", true)) {
                val id = config.getString("strengthening.item", "redstone") ?: "redstone"
                ItemNames.material(id).also {
                    if (it == null) log.warning("strengthening.item \"$id\" isn't an item. Strengthening is off until it is fixed.")
                }
            } else {
                null
            }
            return Settings(
                chance = config.getInt("catching.chance", 50).coerceIn(0, 100),
                strengthenedChance = config.getInt("catching.strengthened-chance", 75).coerceIn(0, 100),
                onlyOwnerCatchesPets = config.getBoolean("catching.only-owner-catches-pets", true),
                protectOtherPluginsMobs = config.getBoolean("catching.protect-other-plugins-mobs", true),
                spawnerMobs = config.getBoolean("catching.spawner-mobs", true),
                releasedNeverDespawn = config.getBoolean("releasing.mobs-never-despawn", true),
                strengthenItem = strengthenItem,
                respectClaims = config.getBoolean("claims.respect-claims", true),
                showInRecipeBook = config.getBoolean("crafting.show-in-recipe-book", true),
                normalRecipe = recipe(config.getConfigurationSection("crafting.normal-mobs"), "crafting.normal-mobs", log),
                dangerousRecipe = recipe(config.getConfigurationSection("crafting.dangerous-mobs"), "crafting.dangerous-mobs", log),
                debug = config.getBoolean("debug", false),
            )
        }

        private fun recipe(section: ConfigurationSection?, path: String, log: Logger): RecipeBase? {
            if (section == null) {
                log.warning("$path is missing from config.yml, so those mobs can't be crafted.")
                return null
            }
            val mobItemAmount = section.getInt("mob-item-amount", 3)
            val extras = mutableListOf<Pair<ItemChoice, Int>>()
            section.getConfigurationSection("extra-items")?.let { items ->
                for (id in items.getKeys(false)) {
                    val amount = items.getInt(id)
                    if (amount <= 0) continue
                    val item = ItemNames.choice(id)
                    if (item == null) {
                        log.warning("$path.extra-items: \"$id\" isn't an item. Those mobs can't be crafted until it is fixed.")
                        return null
                    }
                    extras += item to amount
                }
            }
            val total = 1 + mobItemAmount + extras.sumOf { it.second }
            if (mobItemAmount < 1 || total > GRID_SIZE) {
                log.warning(
                    "$path needs $total items (the bucket, $mobItemAmount mob items and the extra items), " +
                        "but a crafting grid only fits $GRID_SIZE, and mob-item-amount must be at least 1. " +
                        "Those mobs can't be crafted until it is fixed."
                )
                return null
            }
            return RecipeBase(extras, mobItemAmount)
        }
    }
}
