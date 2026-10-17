package com.esmpfun.mobbuckets.messages

import com.esmpfun.mobbuckets.MobBucketsPlugin
import com.esmpfun.mobbuckets.config.ItemChoice
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import org.bukkit.command.CommandSender
import org.bukkit.configuration.InvalidConfigurationException
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File
import java.io.IOException
import java.io.InputStreamReader

/**
 * Loads `messages.yml` and renders its MiniMessage templates. `{name}` placeholders take
 * strings, numbers or components. Keys added by an update are written into the owner's file.
 */
class Messages(private val plugin: MobBucketsPlugin) {

    private val defaults: YamlConfiguration by lazy {
        val bundled = plugin.getResource("messages.yml") ?: error("messages.yml is missing from the plugin jar")
        YamlConfiguration.loadConfiguration(InputStreamReader(bundled, Charsets.UTF_8))
    }

    private val mm = MiniMessage.miniMessage()

    @Volatile
    private var messages = YamlConfiguration()

    @Volatile
    private var prefix: Component = Component.empty()

    fun load() {
        val file = File(plugin.dataFolder, "messages.yml")
        if (!file.exists()) plugin.saveResource("messages.yml", false)
        val loaded = YamlConfiguration()
        try {
            loaded.load(file)
        } catch (e: IOException) {
            useBuiltIn(e.message)
            return
        } catch (e: InvalidConfigurationException) {
            useBuiltIn(e.message)
            return
        }

        val missing = defaults.getKeys(true).filter { !defaults.isConfigurationSection(it) && !loaded.contains(it) }
        if (missing.isNotEmpty()) {
            missing.forEach { loaded.set(it, defaults.get(it)) }
            try {
                loaded.save(file)
            } catch (e: IOException) {
                plugin.logger.warning("Couldn't add ${missing.size} new messages to messages.yml: ${e.message}")
            }
        }
        messages = loaded
        prefix = text("prefix")
    }

    private fun useBuiltIn(reason: String?) {
        plugin.logger.warning("messages.yml couldn't be read ($reason). The built-in messages are used until it is fixed.")
        messages = YamlConfiguration()
        prefix = text("prefix")
    }

    fun text(key: String, vararg placeholders: Pair<String, Any>): Component =
        render(messages.getString(key) ?: defaults.getString(key) ?: key, placeholders)

    /** Item names and lore: same as [text], minus the italics Minecraft adds to custom item text. */
    fun itemText(key: String, vararg placeholders: Pair<String, Any>): Component =
        text(key, *placeholders).decoration(TextDecoration.ITALIC, false)

    fun itemLines(key: String, vararg placeholders: Pair<String, Any>): List<Component> {
        val lines = if (messages.isList(key)) messages.getStringList(key) else defaults.getStringList(key)
        return lines.map { render(it, placeholders).decoration(TextDecoration.ITALIC, false) }
    }

    fun itemName(item: ItemChoice): Component {
        val name = Component.translatable(item.example.translationKey())
        return if (item.isGroup) text("item.any-kind", "item" to name) else name
    }

    fun send(to: CommandSender, key: String, vararg placeholders: Pair<String, Any>) {
        to.sendMessage(prefix.append(text(key, *placeholders)))
    }

    private fun render(template: String, placeholders: Array<out Pair<String, Any>>): Component {
        var source = template
        val resolvers = placeholders.map { (name, value) ->
            source = source.replace("{$name}", "<$name>")
            when (value) {
                is Component -> Placeholder.component(name, value)
                else -> Placeholder.unparsed(name, value.toString())
            }
        }
        return mm.deserialize(source, TagResolver.resolver(resolvers))
    }
}
