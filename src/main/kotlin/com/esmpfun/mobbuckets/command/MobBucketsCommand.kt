package com.esmpfun.mobbuckets.command

import com.esmpfun.mobbuckets.MobBucketsPlugin
import com.esmpfun.mobbuckets.config.Tier
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.JoinConfiguration
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabExecutor

class MobBucketsCommand(private val plugin: MobBucketsPlugin) : TabExecutor {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        when (args.firstOrNull()?.lowercase()) {
            "recipe" -> recipe(sender, args)
            "give" -> give(sender, args)
            "reload" -> reload(sender)
            else -> plugin.messages.send(sender, "command.usage")
        }
        return true
    }

    private fun recipe(sender: CommandSender, args: Array<out String>) {
        val messages = plugin.messages
        if (!sender.hasPermission("mobbuckets.recipe")) return messages.send(sender, "command.no-permission")
        val id = args.getOrNull(1) ?: return messages.send(sender, "command.usage")
        val entry = plugin.catalog.entry(id.lowercase()) ?: return messages.send(sender, "command.unknown-mob", "mob" to id)
        val settings = plugin.settings
        val base = if (entry.tier == Tier.NORMAL) settings.normalRecipe else settings.dangerousRecipe

        val parts = mutableListOf<Component>(Component.translatable(Material.BUCKET.translationKey()))
        base?.extras?.forEach { (item, amount) -> parts += amountOf(amount, messages.itemName(item)) }
        parts += amountOf(base?.mobItemAmount ?: 0, messages.itemName(entry.mobItem))
        messages.send(
            sender,
            "command.recipe",
            "mob" to entry.displayName,
            "ingredients" to Component.join(JoinConfiguration.commas(true), parts),
        )
        settings.strengthenItem?.let {
            messages.send(
                sender,
                "command.recipe-strengthen",
                "item" to Component.translatable(it.translationKey()),
                "chance" to settings.chance,
                "strengthened-chance" to settings.strengthenedChance,
            )
        }
    }

    private fun amountOf(amount: Int, name: Component) = Component.text("${amount}x ").append(name)

    private fun give(sender: CommandSender, args: Array<out String>) {
        val messages = plugin.messages
        if (!sender.hasPermission("mobbuckets.give")) return messages.send(sender, "command.no-permission")
        if (args.size < 3) return messages.send(sender, "command.usage")
        val target = Bukkit.getPlayerExact(args[1]) ?: return messages.send(sender, "command.unknown-player", "player" to args[1])
        val entry = plugin.catalog.entry(args[2].lowercase()) ?: return messages.send(sender, "command.unknown-mob", "mob" to args[2])
        val amount = args.getOrNull(3)?.toIntOrNull()?.coerceIn(1, 64) ?: 1
        val strengthened = args.getOrNull(4)?.lowercase() in setOf("strengthened", "true", "yes")

        val leftover = target.inventory.addItem(plugin.items.empty(entry, strengthened, amount))
        leftover.values.forEach { target.world.dropItemNaturally(target.location, it) }
        messages.send(sender, "command.given", "player" to target.name, "amount" to amount, "mob" to entry.displayName)
        if (sender != target) messages.send(target, "command.received", "amount" to amount, "mob" to entry.displayName)
    }

    private fun reload(sender: CommandSender) {
        if (!sender.hasPermission("mobbuckets.reload")) return plugin.messages.send(sender, "command.no-permission")
        plugin.reloadEverything()
        plugin.messages.send(sender, "command.reloaded", "count" to plugin.catalog.entries.size)
    }

    override fun onTabComplete(sender: CommandSender, command: Command, label: String, args: Array<out String>): List<String> {
        val options: List<String> = when (args.size) {
            1 -> listOf("recipe" to "mobbuckets.recipe", "give" to "mobbuckets.give", "reload" to "mobbuckets.reload")
                .filter { sender.hasPermission(it.second) }.map { it.first }
            2 -> when (args[0].lowercase()) {
                "recipe" -> plugin.catalog.entries.keys.toList()
                "give" -> Bukkit.getOnlinePlayers().map { it.name }
                else -> emptyList()
            }
            3 -> if (args[0].equals("give", true)) plugin.catalog.entries.keys.toList() else emptyList()
            4 -> if (args[0].equals("give", true)) listOf("1", "16") else emptyList()
            5 -> if (args[0].equals("give", true)) listOf("strengthened") else emptyList()
            else -> emptyList()
        }
        val typed = args.lastOrNull()?.lowercase() ?: ""
        return options.filter { it.lowercase().startsWith(typed) }
    }
}
