package com.esmpfun.mobbuckets.listener

import com.esmpfun.mobbuckets.MobBucketsPlugin
import com.esmpfun.mobbuckets.api.MobCatchEvent
import com.esmpfun.mobbuckets.api.MobCatchResultEvent
import com.esmpfun.mobbuckets.config.ItemNames
import org.bukkit.Bukkit
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.ChestedHorse
import org.bukkit.entity.Enemy
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.entity.Tameable
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason
import org.bukkit.event.player.PlayerInteractEntityEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.Merchant
import java.util.concurrent.ThreadLocalRandom

class CatchListener(private val plugin: MobBucketsPlugin) : Listener {

    // HIGH so claim and protection plugins have already had their say on this click.
    @EventHandler(priority = EventPriority.HIGH)
    fun onInteract(event: PlayerInteractEntityEvent) {
        val player = event.player
        val hand = event.hand
        val data = plugin.items.read(player.inventory.getItem(hand)) ?: return
        // Item frames and armour stands take the bucket like any other item.
        val target = event.rightClicked as? LivingEntity ?: return
        if (target is ArmorStand) return
        if (plugin.isRepeatClick(event)) {
            event.isCancelled = true
            return
        }

        // Whatever happens next, the mob must not react to a bucket (no milking, trading, sitting).
        val refusedElsewhere = event.isCancelled
        event.isCancelled = true
        if (refusedElsewhere) {
            plugin.debug("Mob Buckets: another plugin already blocked this click, so nothing happens.")
            return
        }
        if (!target.isValid) return
        if (hand == EquipmentSlot.OFF_HAND && plugin.items.read(player.inventory.itemInMainHand) != null) {
            plugin.debug("Mob Buckets: off hand ignored, the main hand holds a mob bucket too.")
            return
        }
        plugin.debug("Mob Buckets: trying to catch (bucket for \"${data.mobId}\", filled: ${data.isFilled}).")

        val messages = plugin.messages
        val catalog = plugin.catalog
        val targetName = ItemNames.mobName(target.type)

        if (data.isFilled) {
            messages.send(player, "catch.already-full", "mob" to ItemNames.mobName(data.mobId))
            return
        }
        val entry = catalog.entry(data.mobId)
        if (entry == null) {
            messages.send(player, "catch.cannot-be-caught", "mob" to ItemNames.mobName(data.mobId))
            return
        }
        if (catalog.entryFor(target.type) !== entry) {
            if (catalog.isBlocked(target.type)) {
                messages.send(player, "catch.cannot-be-caught", "mob" to targetName)
            } else {
                messages.send(player, "catch.wrong-mob", "mob" to entry.displayName)
            }
            return
        }
        refusal(player, target)?.let {
            messages.send(player, it, "mob" to targetName)
            return
        }

        val settings = plugin.settings
        val attempt = MobCatchEvent(
            player, target, player.inventory.getItem(hand).clone(), data.strengthened,
            if (data.strengthened) settings.strengthenedChance else settings.chance,
        )
        if (!attempt.callEvent()) {
            plugin.debug("Mob Buckets: another plugin cancelled the catch (MobCatchEvent).")
            return
        }
        plugin.debug("Mob Buckets: rolling the dice at ${attempt.chance}%.")
        if (ThreadLocalRandom.current().nextInt(100) >= attempt.chance) {
            plugin.useOne(player, hand, null)
            player.world.playSound(target.location, Sound.ENTITY_ITEM_BREAK, 1f, 1f)
            player.world.spawnParticle(Particle.POOF, target.location.add(0.0, target.height / 2, 0.0), 12, 0.3, 0.3, 0.3, 0.02)
            messages.send(player, "catch.missed", "mob" to targetName)
            MobCatchResultEvent(player, target.type, entry.id, target.location, false, null).callEvent()
            return
        }

        dropChestContents(target)
        val saved = try {
            @Suppress("DEPRECATION")
            Bukkit.getUnsafe().serializeEntity(target)
        } catch (e: RuntimeException) {
            plugin.logger.warning("Couldn't save a ${target.type.key} at ${target.location.toVector()} into a bucket: $e")
            messages.send(player, "catch.failed", "mob" to targetName)
            return
        }
        val filled = plugin.items.filled(target, saved)
        val effectAt = target.location.add(0.0, target.height / 2, 0.0)
        target.remove()
        plugin.useOne(player, hand, filled)
        player.world.playSound(effectAt, Sound.ITEM_BUCKET_FILL_FISH, 1f, 1f)
        player.world.spawnParticle(Particle.ITEM_SLIME, effectAt, 16, 0.3, 0.3, 0.3, 0.05)
        messages.send(player, "catch.success", "mob" to targetName)
        MobCatchResultEvent(player, target.type, entry.id, effectAt, true, filled.clone()).callEvent()
    }

    /** The message key explaining why this mob can't be caught right now, or null if it can. */
    private fun refusal(player: Player, target: LivingEntity): String? {
        val settings = plugin.settings
        return when {
            !player.hasPermission("mobbuckets.use") -> "catch.no-permission"
            settings.protectOtherPluginsMobs &&
                (target.entitySpawnReason == SpawnReason.CUSTOM || target.hasMetadata("NPC")) -> "catch.protected"
            !settings.spawnerMobs && target.entitySpawnReason in SPAWNER_REASONS -> "catch.spawner"
            target.passengers.isNotEmpty() || target.isInsideVehicle -> "catch.riding"
            target.isLeashed -> "catch.leashed"
            settings.onlyOwnerCatchesPets && target is Tameable && target.isTamed &&
                target.ownerUniqueId != null && target.ownerUniqueId != player.uniqueId -> "catch.not-your-pet"
            target is Merchant && target.isTrading -> "catch.trading"
            settings.respectClaims && target !is Enemy && !player.hasPermission("mobbuckets.bypass-claims") &&
                !plugin.claims.mayCatch(player, target) -> "catch.claim"
            else -> null
        }
    }

    /** Items in a donkey, mule or llama chest drop on the ground, so a bucket never carries items around. */
    private fun dropChestContents(target: LivingEntity) {
        val animal = target as? ChestedHorse ?: return
        if (!animal.isCarryingChest) return
        val inventory = animal.inventory
        // The first two slots are the saddle and the armour or carpet; the chest starts after them.
        for (slot in 2 until inventory.size) {
            val stack = inventory.getItem(slot) ?: continue
            if (stack.type.isAir) continue
            target.world.dropItemNaturally(target.location, stack)
            inventory.setItem(slot, null)
        }
    }

    private companion object {
        val SPAWNER_REASONS = setOf(SpawnReason.SPAWNER, SpawnReason.TRIAL_SPAWNER)
    }
}
