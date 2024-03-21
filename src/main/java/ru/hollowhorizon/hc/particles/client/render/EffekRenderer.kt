package ru.hollowhorizon.hc.particles.client.render

import com.mojang.blaze3d.pipeline.RenderTarget
import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.math.Matrix4f
import ru.hollowhorizon.hc.particles.api.client.effekseer.DeviceType
import ru.hollowhorizon.hc.particles.api.client.effekseer.Effekseer
import ru.hollowhorizon.hc.particles.api.client.effekseer.Effekseer.Companion.deviceType
import ru.hollowhorizon.hc.particles.api.client.effekseer.ParticleEmitter
import ru.hollowhorizon.hc.particles.client.loader.EffekAssetLoader.Companion.get
import ru.hollowhorizon.hc.particles.client.registry.EffectDefinition
import net.minecraft.client.Camera
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.RenderType
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.InteractionHand
import org.lwjgl.BufferUtils
import java.nio.FloatBuffer
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean

object EffekRenderer {
    private val CAMERA_TRANSFORM_BUFFER: FloatBuffer = BufferUtils.createFloatBuffer(16)
    private val PROJECTION_BUFFER: FloatBuffer = BufferUtils.createFloatBuffer(16)
    private val INIT = AtomicBoolean()

    fun init() {
        if (INIT.compareAndExchange(false, true)) {
            return
        }
        if (deviceType != DeviceType.OPENGL) {
            if (!Effekseer.init()) throw ExceptionInInitializerError("Failed to initialize Effekseer")
            Runtime.getRuntime().addShutdownHook(Thread(Effekseer::terminate, "ShutdownHook Effekseer::terminate"))
        }
    }

    @JvmStatic
    fun onRenderWorldLast(partialTick: Float, pose: PoseStack, projection: Matrix4f, camera: Camera) {
        draw(ParticleEmitter.Type.WORLD, partialTick, pose, projection, camera)
    }

    @JvmStatic
    fun onRenderHand(
        partialTick: Float,
        hand: InteractionHand,
        pose: PoseStack,
        projection: Matrix4f,
        camera: Camera,
    ) {
        val type = when (hand) {
            InteractionHand.MAIN_HAND -> ParticleEmitter.Type.FIRST_PERSON_MAINHAND
            InteractionHand.OFF_HAND -> ParticleEmitter.Type.FIRST_PERSON_OFFHAND
        }
        draw(type, partialTick, pose, projection, camera)
    }

    private val CAMERA_TRANSFORM_DATA = FloatArray(16)
    private val PROJECTION_MATRIX_DATA = FloatArray(16)

    private fun draw(
        type: ParticleEmitter.Type,
        partialTick: Float,
        pose: PoseStack,
        projection: Matrix4f,
        camera: Camera,
    ) {
        val w = MinecraftHolder.MINECRAFT.window.width
        val h = MinecraftHolder.MINECRAFT.window.height

        projection.store(PROJECTION_BUFFER)
        transposeMatrix(PROJECTION_BUFFER)
        PROJECTION_BUFFER[PROJECTION_MATRIX_DATA]

        val position = camera.position

        pose.pushPose()

        if (type == ParticleEmitter.Type.WORLD) pose.translate(-position.x(), -position.y(), -position.z())

        pose.last().pose().store(CAMERA_TRANSFORM_BUFFER)
        transposeMatrix(CAMERA_TRANSFORM_BUFFER)
        CAMERA_TRANSFORM_BUFFER[CAMERA_TRANSFORM_DATA]

        pose.popPose()

        Optional.ofNullable(MinecraftHolder.MINECRAFT.levelRenderer.particlesTarget)
            .ifPresent { rt: RenderTarget -> rt.copyDepthFrom(MinecraftHolder.MINECRAFT.mainRenderTarget) }

        val deltaFrames = 60 * getDeltaTime(type)

        RenderType.PARTICLES_TARGET.setupRenderState()
        get().forEach { _: ResourceLocation, inst: EffectDefinition ->
            inst.draw(type, w, h, CAMERA_TRANSFORM_DATA, PROJECTION_MATRIX_DATA, deltaFrames, partialTick)
        }
        RenderType.PARTICLES_TARGET.clearRenderState()

        CAMERA_TRANSFORM_BUFFER.clear()
        PROJECTION_BUFFER.clear()
    }

    private fun transposeMatrix(m: FloatBuffer) {
        val m00 = m[0]
        val m01 = m[1]
        val m02 = m[2]
        val m03 = m[3]
        val m10 = m[4]
        val m11 = m[5]
        val m12 = m[6]
        val m13 = m[7]
        val m20 = m[8]
        val m21 = m[9]
        val m22 = m[0xA]
        val m23 = m[0xB]
        val m30 = m[0xC]
        val m31 = m[0xD]
        val m32 = m[0xE]
        val m33 = m[0xF]

        m.put(0, m00)
        m.put(1, m10)
        m.put(2, m20)
        m.put(3, m30)
        m.put(4, m01)
        m.put(5, m11)
        m.put(6, m21)
        m.put(7, m31)
        m.put(8, m02)
        m.put(9, m12)
        m.put(0xA, m22)
        m.put(0xB, m32)
        m.put(0xC, m03)
        m.put(0xD, m13)
        m.put(0xE, m23)
        m.put(0xF, m33)
    }

    private val lastDrawTimeByNanos = LongArray(256)

    private fun getDeltaTime(type: ParticleEmitter.Type): Float {
        val last = lastDrawTimeByNanos[type.ordinal]
        if (last == 0L) {
            lastDrawTimeByNanos[type.ordinal] = System.nanoTime()
            return 1f / 60
        }

        val now = System.nanoTime()
        lastDrawTimeByNanos[type.ordinal] = now
        return ((now - last) * 1e-9).toFloat()
    }

    object MinecraftHolder {
        val MINECRAFT: Minecraft = Minecraft.getInstance()
    }
}
