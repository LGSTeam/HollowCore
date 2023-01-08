package ru.hollowhorizon.hc.common.capabilities

import kotlinx.serialization.Serializable
import net.minecraft.entity.Entity
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.ResourceLocation
import net.minecraft.world.World
import net.minecraft.world.chunk.Chunk
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.event.AttachCapabilitiesEvent
import ru.hollowhorizon.hc.client.utils.toRL
import kotlin.random.Random

object HollowCapabilityStorageV2 {
    val capabilities = arrayListOf<Class<*>>()
    val storages = hashMapOf<String, Capability<*>>()
    val providers = arrayListOf<Pair<Class<*>, HollowCapabilitySerializer<*>>>()

    fun registerAll() {
        capabilities.forEach {
            register(it as Class<out HollowCapability<*>>)
        }
    }

    @JvmStatic
    fun registerProvidersEntity(event: AttachCapabilitiesEvent<Entity>) {
        providers.filter { it.first.isInstance(event.`object`) }.forEach {
            event.addCapability(it.second.cap.createName(), it.second)
        }
    }
    @JvmStatic
    fun registerProvidersWorld(event: AttachCapabilitiesEvent<World>) {
        providers.filter { it.first.isInstance(event.`object`) }.forEach {
            event.addCapability(it.second.cap.createName(), it.second)
        }
    }
    @JvmStatic
    fun registerProvidersTile(event: AttachCapabilitiesEvent<TileEntity>) {
        providers.filter { it.first.isInstance(event.`object`) }.forEach {
            event.addCapability(it.second.cap.createName(), it.second)
        }
    }
    @JvmStatic
    fun registerProvidersChunk(event: AttachCapabilitiesEvent<Chunk>) {
        providers.filter { it.first.isInstance(event.`object`) }.forEach {
            event.addCapability(it.second.cap.createName(), it.second)
        }
    }

    private fun Capability<*>.createName(): ResourceLocation {
        return ("hc_capabilities:"+
                this.name.lowercase()
            .replace(Regex("[^a-z0-9/._-]"), "")
        ).toRL()
    }
}