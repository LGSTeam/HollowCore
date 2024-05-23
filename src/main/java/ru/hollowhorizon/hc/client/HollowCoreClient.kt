/*
 * MIT License
 *
 * Copyright (c) 2024 HollowHorizon
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package ru.hollowhorizon.hc.client

import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.screens.MenuScreens
import net.minecraft.server.packs.repository.Pack
import net.minecraft.server.packs.repository.PackSource
import net.minecraftforge.api.distmarker.Dist
import net.minecraftforge.api.distmarker.OnlyIn
import net.minecraftforge.client.event.EntityRenderersEvent
import net.minecraftforge.client.event.InputEvent
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent
import net.minecraftforge.event.AddPackFindersEvent
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent
import org.lwjgl.glfw.GLFW
import ru.hollowhorizon.hc.api.HudHideable
import ru.hollowhorizon.hc.client.handlers.TickHandler
import ru.hollowhorizon.hc.client.imgui.ImGuiMethods
import ru.hollowhorizon.hc.client.imgui.ImguiHandler
import ru.hollowhorizon.hc.client.models.gltf.manager.GltfManager
import ru.hollowhorizon.hc.client.render.RenderLoader
import ru.hollowhorizon.hc.client.render.block.GLTFBlockEntityRenderer
import ru.hollowhorizon.hc.client.render.effekseer.EffekseerNatives
import ru.hollowhorizon.hc.client.render.effekseer.loader.EffekAssets
import ru.hollowhorizon.hc.client.render.entity.GLTFEntityRenderer
import ru.hollowhorizon.hc.client.render.shaders.post.PostChain
import ru.hollowhorizon.hc.client.screens.HollowScreen
import ru.hollowhorizon.hc.client.utils.*
import ru.hollowhorizon.hc.common.registry.*
import ru.hollowhorizon.hc.common.ui.Anchor
import ru.hollowhorizon.hc.common.ui.HollowMenuScreen
import thedarkcolour.kotlinforforge.forge.FORGE_BUS
import thedarkcolour.kotlinforforge.forge.MOD_BUS

object HollowCoreClient {

    init {
        MOD_BUS.addListener(::onRegisterReloadListener)
        MOD_BUS.addListener(::onRegisterResourcePacks)
        MOD_BUS.addListener(::onCreatingRenderers)
        MOD_BUS.addListener(::onCreatingMenus)

        MOD_BUS.register(ModShaders)

        FORGE_BUS.register(TickHandler)

        EffekseerNatives.install()
        RenderSystem.recordRenderCall(RenderLoader::onInitialize)

        FORGE_BUS.addListener<InputEvent.Key> {
            if (it.key == GLFW.GLFW_KEY_INSERT) {
                Minecraft.getInstance().setScreen(object : HollowScreen(), HudHideable {
                    override fun render(pPoseStack: PoseStack, pMouseX: Int, pMouseY: Int, pPartialTick: Float) {
                        super.render(pPoseStack, pMouseX, pMouseY, pPartialTick)
                        val text = "Привет, я тестирую §6разные§r шрифты".mcText.font("hc:ubuntu".rl)

                        ImguiHandler.drawFrame {
                            ImGuiMethods.opengl(font.width(text)*Minecraft.getInstance().window.guiScale.toFloat(), 100f) {
                                font.drawScaled(pPoseStack, Anchor.START, text, 0, 100, 0xFFFFFF, 3f, false)
                            }
                        }
                    }
                })
            }
        }
    }

    private fun onRegisterReloadListener(event: RegisterClientReloadListenersEvent) {
        event.registerReloadListener(EffekAssets)
        event.registerReloadListener(GltfManager)
        event.registerReloadListener(PostChain)
    }

    private fun onRegisterResourcePacks(event: AddPackFindersEvent) {
        event.addRepositorySource { adder, creator ->
            adder.accept(
                creator.create(
                    HollowPack.name, HollowPack.name.mcText, true, { HollowPack },
                    HollowPack.section, Pack.Position.TOP, PackSource.BUILT_IN, HollowPack.isHidden
                )
            )
        }
    }

    @OnlyIn(Dist.CLIENT)
    private fun onCreatingRenderers(event: EntityRenderersEvent.RegisterRenderers) {
        event.registerEntityRenderer(ModEntities.TEST_ENTITY.get(), ::GLTFEntityRenderer)
    }

    private fun onCreatingMenus(event: FMLClientSetupEvent) {
        event.enqueueWork {
            MenuScreens.register(ModMenus.HOLLOW_MENU.get(), ::HollowMenuScreen)
        }
    }
}