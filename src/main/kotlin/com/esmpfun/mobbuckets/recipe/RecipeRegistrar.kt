package com.esmpfun.mobbuckets.recipe

import com.esmpfun.mobbuckets.MobBucketsPlugin
import com.esmpfun.mobbuckets.config.Tier
import io.papermc.paper.potion.PotionMix
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.RecipeChoice
import org.bukkit.inventory.ShapelessRecipe
import org.bukkit.inventory.recipe.CraftingBookCategory

// Paper 26.2 deprecated these calls in favour of BrewingRecipe and batch adds, which 1.21 doesn't have.
/** Adds a crafting recipe and a brewing mix for every mob bucket, and removes them again on reload. */
@Suppress("DEPRECATION")
class RecipeRegistrar(private val plugin: MobBucketsPlugin) {

    private val craftKeys = mutableListOf<NamespacedKey>()
    private val brewKeys = mutableListOf<NamespacedKey>()

    fun registerAll() {
        unregisterAll()
        val settings = plugin.settings
        val brewer = Bukkit.getPotionBrewer()

        for (entry in plugin.catalog.entries.values) {
            val base = when (entry.tier) {
                Tier.NORMAL -> settings.normalRecipe
                Tier.DANGEROUS -> settings.dangerousRecipe
            } ?: continue

            val key = NamespacedKey(plugin, "bucket_${entry.id}")
            val recipe = ShapelessRecipe(key, plugin.items.empty(entry, strengthened = false))
            // Exact match, so a mob bucket can't stand in for the plain bucket.
            recipe.addIngredient(RecipeChoice.ExactChoice(ItemStack(Material.BUCKET)))
            for ((item, amount) in base.extras) repeat(amount) { recipe.addIngredient(item.choice.clone()) }
            repeat(base.mobItemAmount) { recipe.addIngredient(entry.mobItem.choice.clone()) }
            recipe.group = "mobbuckets"
            recipe.category = CraftingBookCategory.MISC
            if (Bukkit.addRecipe(recipe, false)) {
                craftKeys += key
            } else {
                plugin.logger.warning("Couldn't add the recipe for the ${entry.id} bucket; another recipe already uses that name.")
            }

            val strengthenItem = settings.strengthenItem ?: continue
            val brewKey = NamespacedKey(plugin, "strengthen_${entry.id}")
            brewer.addPotionMix(
                PotionMix(
                    brewKey,
                    plugin.items.empty(entry, strengthened = true),
                    PotionMix.createPredicateChoice { plugin.items.isFreshEmpty(it, entry.id) },
                    RecipeChoice.MaterialChoice(strengthenItem),
                )
            )
            brewKeys += brewKey
        }

        Bukkit.updateRecipes()
        Bukkit.getOnlinePlayers().forEach { showTo(it) }
    }

    fun unregisterAll() {
        if (craftKeys.isEmpty() && brewKeys.isEmpty()) return
        craftKeys.forEach { Bukkit.removeRecipe(it, false) }
        brewKeys.forEach { Bukkit.getPotionBrewer().removePotionMix(it) }
        craftKeys.clear()
        brewKeys.clear()
        Bukkit.updateRecipes()
    }

    fun showTo(player: Player) {
        if (plugin.settings.showInRecipeBook && craftKeys.isNotEmpty()) player.discoverRecipes(craftKeys)
    }
}
