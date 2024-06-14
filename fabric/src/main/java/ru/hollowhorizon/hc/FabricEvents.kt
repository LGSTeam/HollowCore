package ru.hollowhorizon.hc

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityWorldChangeEvents
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents
import net.fabricmc.fabric.api.networking.v1.EntityTrackingEvents
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents
import net.fabricmc.fabric.api.`object`.builder.v1.entity.FabricDefaultAttributeRegistry
import net.fabricmc.fabric.api.resource.ResourceManagerHelper
import net.minecraft.server.packs.PackType
import net.minecraft.world.item.CreativeModeTabs
import ru.hollowhorizon.hc.common.events.EventBus
import ru.hollowhorizon.hc.common.events.entity.EntityTrackingEvent
import ru.hollowhorizon.hc.common.events.entity.player.PlayerEvent
import ru.hollowhorizon.hc.common.events.post
import ru.hollowhorizon.hc.common.events.registry.RegisterCommandsEvent
import ru.hollowhorizon.hc.common.events.registry.RegisterEntityAttributesEvent
import ru.hollowhorizon.hc.common.events.registry.RegisterReloadListenersEvent
import ru.hollowhorizon.hc.internal.DelegatedReloadListener

object FabricEvents {
    init {
        registerReloadListeners()
        registerAttributes()
        registerCommands()
        onEntityTracking()
        onPlayerEvents()
    }

    private fun registerReloadListeners() {
        val event = RegisterReloadListenersEvent.Server()
        EventBus.post(event)
        val helper = ResourceManagerHelper.get(PackType.SERVER_DATA)

        event.listeners.forEach {
            helper.registerReloadListener(DelegatedReloadListener(it))
        }
    }

    private fun registerAttributes() {
        val attributes = RegisterEntityAttributesEvent()
        EventBus.post(attributes)
        attributes.getAttributes().forEach(FabricDefaultAttributeRegistry::register)
    }

    private fun registerCommands() {
        CommandRegistrationCallback.EVENT.register(CommandRegistrationCallback { dispatcher, registryAccess, environment ->
            RegisterCommandsEvent(dispatcher, registryAccess, environment).post()
        })
    }

    private fun onEntityTracking() {
        EntityTrackingEvents.START_TRACKING.register { entity, player ->
            EntityTrackingEvent(player, entity).post()
        }
    }

    private fun onPlayerEvents() {
        ServerPlayConnectionEvents.JOIN.register { handler, sender, server ->
            PlayerEvent.Join(handler.player).post()
        }
        ServerEntityWorldChangeEvents.AFTER_PLAYER_CHANGE_WORLD.register { player, from, to ->
            PlayerEvent.ChangeDimension(player, from, to).post()
        }
    }
}