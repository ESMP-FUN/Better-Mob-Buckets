package com.esmpfun.mobbuckets.claims

import org.bukkit.Location
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.plugin.Plugin
import java.lang.reflect.Method

interface ClaimCheck {
    fun mayCatch(player: Player, entity: Entity): Boolean
    fun mayRelease(player: Player, location: Location): Boolean

    companion object {
        fun create(plugin: Plugin): ClaimCheck {
            val gp = plugin.server.pluginManager.getPlugin("GriefPrevention")?.takeIf { it.isEnabled } ?: return NoClaims
            return try {
                GriefPreventionClaims.create(plugin, gp).also {
                    plugin.logger.info("GriefPrevention found: claims are respected.")
                }
            } catch (e: ReflectiveOperationException) {
                plugin.logger.warning(
                    "GriefPrevention is installed, but Mob Buckets can't read this version of it ($e). " +
                        "Claims are not checked until Mob Buckets is updated."
                )
                NoClaims
            }
        }
    }
}

private object NoClaims : ClaimCheck {
    override fun mayCatch(player: Player, entity: Entity) = true
    override fun mayRelease(player: Player, location: Location) = true
}

/**
 * Read through reflection so the plugin builds and runs without GriefPrevention.
 * Catching needs container trust (what GriefPrevention asks for to hurt animals); releasing needs build trust.
 */
private class GriefPreventionClaims(
    private val plugin: Plugin,
    private val gp: Any,
    private val dataStore: Any,
    private val getClaimAt: Method,
    private val checkPermission: Method,
    private val containerPermission: Any,
    private val allowBuild: Method,
) : ClaimCheck {

    override fun mayCatch(player: Player, entity: Entity): Boolean = guarded {
        val claim = getClaimAt.invoke(dataStore, entity.location, false, null) ?: return@guarded true
        checkPermission.invoke(claim, player, containerPermission, null) == null
    }

    override fun mayRelease(player: Player, location: Location): Boolean = guarded {
        allowBuild.invoke(gp, player, location) == null
    }

    // A failed lookup refuses, so a GriefPrevention update can't silently open claims up.
    private inline fun guarded(block: () -> Boolean): Boolean = try {
        block()
    } catch (e: ReflectiveOperationException) {
        plugin.logger.warning("Couldn't check a GriefPrevention claim, so the action was refused: $e")
        false
    }

    companion object {
        fun create(plugin: Plugin, gp: Plugin): GriefPreventionClaims {
            val loader = gp.javaClass.classLoader
            val claimClass = Class.forName("me.ryanhamshire.GriefPrevention.Claim", false, loader)
            val permissionClass = Class.forName("me.ryanhamshire.GriefPrevention.ClaimPermission", false, loader)
            val dataStore = gp.javaClass.getField("dataStore").get(gp)
            val container = permissionClass.enumConstants.firstOrNull { (it as Enum<*>).name == "Container" }
                ?: throw NoSuchFieldException("ClaimPermission.Container")
            return GriefPreventionClaims(
                plugin,
                gp,
                dataStore,
                dataStore.javaClass.getMethod("getClaimAt", Location::class.java, Boolean::class.javaPrimitiveType, claimClass),
                claimClass.getMethod("checkPermission", Player::class.java, permissionClass, Event::class.java),
                container,
                gp.javaClass.getMethod("allowBuild", Player::class.java, Location::class.java),
            )
        }
    }
}
