package com.esmpfun.mobbuckets.listener

import com.esmpfun.mobbuckets.MobBucketsPlugin
import com.esmpfun.mobbuckets.config.ItemNames
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.block.BlockFace
import org.bukkit.entity.LivingEntity
import org.bukkit.event.Event
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import kotlin.math.ceil

class ReleaseListener(private val plugin: MobBucketsPlugin) : Listener {

    @EventHandler(priority = EventPriority.HIGH)
    fun onInteract(event: PlayerInteractEvent) {
        val data = plugin.items.read(event.item) ?: return
        val hand = event.hand ?: return
        val refusedElsewhere = event.useItemInHand() == Event.Result.DENY
        // A mob bucket never scoops up water, lava or powder snow.
        event.setUseItemInHand(Event.Result.DENY)

        if (!data.isFilled || event.action != Action.RIGHT_CLICK_BLOCK || refusedElsewhere) return
        val player = event.player
        val block = event.clickedBlock ?: return
        if (hand == EquipmentSlot.OFF_HAND && plugin.items.read(player.inventory.itemInMainHand) != null) return

        // Like a normal bucket: clicking a chest or door uses it unless the player is sneaking.
        @Suppress("DEPRECATION")
        if (block.type.isInteractable && !player.isSneaking && event.useInteractedBlock() != Event.Result.DENY) return
        event.setUseInteractedBlock(Event.Result.DENY)

        val messages = plugin.messages
        val mob = ItemNames.mobName(data.mobId)
        val target = if (block.isPassable && !block.isLiquid) block else block.getRelative(event.blockFace)
        val spot = target.location.add(0.5, 0.0, 0.5).apply {
            yaw = player.location.yaw + 180f
            pitch = 0f
        }

        val settings = plugin.settings
        if (settings.respectClaims && !player.hasPermission("mobbuckets.bypass-claims") && !plugin.claims.mayRelease(player, spot)) {
            messages.send(player, "release.claim", "mob" to mob)
            return
        }

        val entity = try {
            @Suppress("DEPRECATION")
            Bukkit.getUnsafe().deserializeEntity(data.entityData ?: return, target.world, false)
        } catch (e: RuntimeException) {
            plugin.logger.warning("Couldn't read the ${data.mobId} in ${player.name}'s bucket: $e")
            messages.send(player, "release.failed", "mob" to mob)
            return
        }

        val blocksTall = ceil(entity.height).toInt().coerceAtLeast(1)
        if ((0 until blocksTall).any { !target.getRelative(BlockFace.UP, it).isPassable }) {
            messages.send(player, "release.no-room", "mob" to mob)
            return
        }
        if (settings.releasedNeverDespawn) (entity as? LivingEntity)?.removeWhenFarAway = false
        if (!entity.spawnAt(spot, SpawnReason.SPAWNER_EGG)) {
            messages.send(player, "release.blocked", "mob" to mob)
            return
        }

        plugin.useOne(player, hand, ItemStack(Material.BUCKET))
        player.swingHand(hand)
        player.world.playSound(spot, Sound.ITEM_BUCKET_EMPTY_FISH, 1f, 1f)
        messages.send(player, "release.success", "mob" to mob)
    }
}
