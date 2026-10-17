package com.esmpfun.mobbuckets

import org.bukkit.configuration.file.YamlConfiguration
import java.io.InputStreamReader
import kotlin.test.Test
import kotlin.test.assertTrue

class BundledFilesTest {

    private fun load(name: String): YamlConfiguration {
        val stream = javaClass.classLoader.getResourceAsStream(name) ?: error("$name is not bundled")
        return YamlConfiguration.loadConfiguration(InputStreamReader(stream, Charsets.UTF_8))
    }

    private val mobs = load("mobs.yml")

    private fun entries(): Map<String, String> = listOf("normal-mobs", "dangerous-mobs").flatMap { path ->
        val section = mobs.getConfigurationSection(path) ?: error("mobs.yml has no $path")
        section.getKeys(false).map { it to (section.getString(it) ?: "") }
    }.toMap()

    @Test
    fun `every mob has its own item`() {
        val duplicates = entries().entries.groupBy { it.value }.filterValues { it.size > 1 }
        assertTrue(duplicates.isEmpty(), "Items used by more than one mob: $duplicates")
    }

    @Test
    fun `every mob is listed once`() {
        val names = entries().keys.flatMap { key -> key.split(',').map { it.trim() } }
        val duplicates = names.groupBy { it }.filterValues { it.size > 1 }.keys
        assertTrue(duplicates.isEmpty(), "Mobs listed more than once: $duplicates")
    }

    @Test
    fun `no catchable mob is also blocked`() {
        val blocked = mobs.getStringList("cannot-be-caught").toSet()
        val both = entries().keys.flatMap { key -> key.split(',').map { it.trim() } }.filter { it in blocked }
        assertTrue(both.isEmpty(), "Listed as catchable and cannot-be-caught: $both")
    }

    @Test
    fun `default recipes fit a crafting grid`() {
        val config = load("config.yml")
        for (tier in listOf("normal-mobs", "dangerous-mobs")) {
            val section = config.getConfigurationSection("crafting.$tier") ?: error("config.yml has no crafting.$tier")
            val extras = section.getConfigurationSection("extra-items")?.let { s -> s.getKeys(false).sumOf { s.getInt(it) } } ?: 0
            val total = 1 + section.getInt("mob-item-amount") + extras
            assertTrue(total <= 9, "$tier recipe needs $total items")
        }
    }
}
